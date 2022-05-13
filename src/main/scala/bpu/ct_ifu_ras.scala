package bpu

import chisel3._
import chisel3.util._
import chisel3.experimental._
//import c910.common._
import common._

class ct_ifu_ras_io extends Bundle{
  val cp0_ifu_icg_en      = Input(Bool())
  val cp0_ifu_ras_en      = Input(Bool())
  val cp0_yy_clk_en       = Input(Bool())
  val cp0_yy_priv_mode    = Input(UInt(2.W))
  val cpurst_b            = Input(Bool())
  val forever_cpuclk      = Input(Bool())
  val ibctrl_ras_inst_pcall       = Input(Bool())
  val ibctrl_ras_pcall_vld        = Input(Bool())
  val ibctrl_ras_pcall_vld_for_gateclk        = Input(Bool())
  val ibctrl_ras_preturn_vld                  = Input(Bool())
  val ibctrl_ras_preturn_vld_for_gateclk      = Input(Bool())
  val ibdp_ras_push_pc            = Input(UInt(39.W))
  val pad_yy_icg_scan_en          = Input(Bool())
  val rtu_ifu_flush               = Input(Bool())
  val rtu_ifu_retire0_inc_pc      = Input(UInt(39.W))
  val rtu_ifu_retire0_mispred     = Input(Bool())
  val rtu_ifu_retire0_pcall       = Input(Bool())
  val rtu_ifu_retire0_preturn     = Input(Bool())
  val ras_ipdp_data_vld           = Output(Bool())
  val ras_ipdp_pc                 = Output(UInt(39.W))
  val ras_l0_btb_pc               = Output(UInt(39.W))
  val ras_l0_btb_push_pc          = Output(UInt(39.W))
  val ras_l0_btb_ras_push         = Output(Bool())

}

class ct_ifu_ras extends RawModule{
  val io = IO(new ct_ifu_ras_io)
  val PC_WIDTH = 40

  //==========================================================
  //                    RTU RAS Pointer
  //==========================================================
  val rtu_ptr_pre = Wire(UInt(5.W))
  val rtu_ptr = Wire(UInt(5.W))
  val rtu_ras_empty = Wire(Bool())
  when(io.rtu_ifu_retire0_pcall && io.rtu_ifu_retire0_preturn){
    rtu_ptr_pre := rtu_ptr
  }.elsewhen(io.rtu_ifu_retire0_pcall){
    when(rtu_ptr(3,0) === "b1011".U ){
      rtu_ptr_pre := Cat((~rtu_ptr(4).asUInt),0.U(4.W))
    }.otherwise{
      rtu_ptr_pre := rtu_ptr + 1.U
    }

  }.elsewhen(io.rtu_ifu_retire0_preturn && !rtu_ras_empty){
    when(rtu_ptr(3,0) === "b0000".U){
      rtu_ptr_pre := Cat((~rtu_ptr(4).asUInt),11.U(4.W))
    }.otherwise{
      rtu_ptr_pre := rtu_ptr - 1.U
    }
  }.otherwise{rtu_ptr_pre := rtu_ptr}

  val m_rtu_ptr_upd_clk = Module(new gated_clk_cell())
  val rtu_ptr_upd_clk = m_rtu_ptr_upd_clk.io.clk_out
  val rtu_prt_upd_clk_en = io.cp0_ifu_ras_en && (io.rtu_ifu_retire0_pcall || io.rtu_ifu_retire0_preturn)
  m_rtu_ptr_upd_clk.io.clk_in      := io.forever_cpuclk.asClock
  m_rtu_ptr_upd_clk.io.external_en := false.B
  m_rtu_ptr_upd_clk.io.global_en   := io.cp0_yy_clk_en
  m_rtu_ptr_upd_clk.io.local_en    := rtu_prt_upd_clk_en
  m_rtu_ptr_upd_clk.io.module_en   := io.cp0_ifu_icg_en
  m_rtu_ptr_upd_clk.io.pad_yy_icg_scan_en := io.pad_yy_icg_scan_en


  withClockAndReset(rtu_ptr_upd_clk,(!io.cpurst_b).asAsyncReset)
  {
    val rtu_ptr_t = RegInit(0.U(5.W))
    rtu_ptr := rtu_ptr_t
    when(io.cp0_ifu_ras_en){
      rtu_ptr_t := rtu_ptr_pre
    }otherwise{
      rtu_ptr_t := rtu_ptr_t
    }
  }

  //==========================================================
  //                    TOP RAS Pointer
  //==========================================================
  
  val top_ptr_pre = Wire(UInt(5.W))
  val top_ptr = Wire(UInt(5.W))
  val top_entry_rtu_updt = io.rtu_ifu_retire0_mispred || io.rtu_ifu_flush
  val ras_push = io.ibctrl_ras_pcall_vld
  val ras_pop = io.ibctrl_ras_preturn_vld
  val ras_empty = Wire(Bool())
  when(top_entry_rtu_updt){
    top_ptr_pre := rtu_ptr_pre
  }.elsewhen(ras_push && ras_pop){
    top_ptr_pre := top_ptr
  }.elsewhen(ras_push){
    when(top_ptr(3,0) === "b1011".U){
      top_ptr_pre := Cat((~top_ptr(4).asUInt),0.U(4.W))
    }.otherwise{
      top_ptr_pre := top_ptr + 1.U
    }
  }.elsewhen(ras_pop && !ras_empty){
    when(top_ptr(3,0) === "b0000".U){
      top_ptr_pre := Cat((~top_ptr(4).asUInt),11.U(4.W))
    }.otherwise{
      top_ptr_pre := top_ptr - 1.U
    }
  }.otherwise{top_ptr_pre := top_ptr}

  val m_top_ptr_upd_clk = Module(new gated_clk_cell())
  val top_ptr_upd_clk = m_top_ptr_upd_clk.io.clk_out
  val top_prt_upd_clk_en = io.cp0_ifu_ras_en && (top_entry_rtu_updt || io.ibctrl_ras_pcall_vld_for_gateclk || io.ibctrl_ras_preturn_vld_for_gateclk)
  m_top_ptr_upd_clk.io.clk_in      := io.forever_cpuclk.asClock
  m_top_ptr_upd_clk.io.external_en := false.B
  m_top_ptr_upd_clk.io.global_en   := io.cp0_yy_clk_en
  m_top_ptr_upd_clk.io.local_en    := top_prt_upd_clk_en
  m_top_ptr_upd_clk.io.module_en   := io.cp0_ifu_icg_en
  m_top_ptr_upd_clk.io.pad_yy_icg_scan_en := io.pad_yy_icg_scan_en


  withClockAndReset(top_ptr_upd_clk,(!io.cpurst_b).asAsyncReset)
  {
    val top_ptr_t = RegInit(0.U(5.W))
    top_ptr := top_ptr_t
    when(io.cp0_ifu_ras_en){
      top_ptr_t := top_ptr_pre
    }.otherwise{
      top_ptr_t := top_ptr_t
    }
  }

  //==========================================================
  //                   Status Pointer
  //==========================================================
  val status_ptr = Wire(UInt(5.W))
  val status_ptr_pre = Mux((status_ptr(3,0) === "b1011".U ),Cat((~status_ptr(4).asUInt),0.U(4.W)),status_ptr+1.U)
   ras_empty := top_ptr === status_ptr
   rtu_ras_empty := rtu_ptr === status_ptr
  val ras_full = top_ptr === Cat((~status_ptr(4)).asUInt,status_ptr(3,0))


  val m_status_ptr_upd_clk = Module(new gated_clk_cell())
  val status_ptr_upd_clk = m_status_ptr_upd_clk.io.clk_out
  val status_prt_upd_clk_en = io.cp0_ifu_ras_en && ras_full && io.ibctrl_ras_pcall_vld_for_gateclk ||
    io.cp0_ifu_ras_en && ras_full && io.ibctrl_ras_preturn_vld_for_gateclk ||
    io.cp0_ifu_ras_en && top_entry_rtu_updt
  m_status_ptr_upd_clk.io.clk_in      := io.forever_cpuclk.asClock
  m_status_ptr_upd_clk.io.external_en := false.B
  m_status_ptr_upd_clk.io.global_en   := io.cp0_yy_clk_en
  m_status_ptr_upd_clk.io.local_en    := status_prt_upd_clk_en
  m_status_ptr_upd_clk.io.module_en   := io.cp0_ifu_icg_en
  m_status_ptr_upd_clk.io.pad_yy_icg_scan_en := io.pad_yy_icg_scan_en

 
  withClockAndReset(status_ptr_upd_clk,(!io.cpurst_b).asAsyncReset)
  {
    val status_ptr_t = RegInit(0.U(5.W))
    status_ptr := status_ptr_t
    when(io.cp0_ifu_ras_en && top_entry_rtu_updt){
      when(rtu_ptr_pre(4) ^ top_ptr(4)){
        status_ptr_t := 0.U
      }.otherwise{
        status_ptr_t := Cat(status_ptr_t(4),rtu_ptr_pre(3,0))
      }
    }.elsewhen(io.cp0_ifu_ras_en && ras_full && ras_push && ras_pop){
      status_ptr_t := status_ptr_t      
    }.elsewhen(io.cp0_ifu_ras_en && ras_full && ras_push){
      status_ptr_t := status_ptr_pre
    }.elsewhen(io.cp0_ifu_ras_en && ras_full && ras_pop){
      when(status_ptr_t(3,0) === 0.U){
        status_ptr_t := 0.U
      }.otherwise{
        status_ptr_t := status_ptr_t - 1.U
      }
    }.otherwise{status_ptr_t := status_ptr_t}
  }


  //==========================================================
  //                    RTU RAS FIFO
  //==========================================================
  val rtu_entry_push = Wire(Vec(6,Bool()))
  val rtu_entry_pc = Wire(Vec(6,UInt(39.W)))
  val rtu_entry_filled = Wire(Vec(6,Bool()))
  val rtu_entry_priv_mode = Wire(Vec(6,UInt(2.W)))
  val rtu_entry_upd_clk_en = Wire(Vec(6,Bool()))
  val m_rtu_entry_upd_clk = Array.fill(6)(Module(new gated_clk_cell))
  val rtu_entry_upd_clk = Wire(Vec(6,Clock()))

  var i = 0.U
  for(i<-0 to 5){

    rtu_entry_push(i) := (rtu_ptr(3,0) === i.U || rtu_ptr(3,0) === (i+6).U)
    rtu_entry_upd_clk(i) := m_rtu_entry_upd_clk(i).io.clk_out
    rtu_entry_upd_clk_en(i) := io.cp0_ifu_ras_en && io.rtu_ifu_retire0_pcall && rtu_entry_push(i)
    m_rtu_entry_upd_clk(i).io.clk_in      := io.forever_cpuclk.asClock
    m_rtu_entry_upd_clk(i).io.external_en := false.B
    m_rtu_entry_upd_clk(i).io.global_en   := io.cp0_yy_clk_en
    m_rtu_entry_upd_clk(i).io.local_en    := rtu_entry_upd_clk_en(i)
    m_rtu_entry_upd_clk(i).io.module_en   := io.cp0_ifu_icg_en
    m_rtu_entry_upd_clk(i).io.pad_yy_icg_scan_en := io.pad_yy_icg_scan_en
    

    withClockAndReset(rtu_entry_upd_clk(i),(!io.cpurst_b).asAsyncReset){
      val rtu_entry_pc_t = RegInit(0.U(39.W))
      val rtu_entry_filled_t = RegInit(false.B)
      val rtu_entry_priv_mode_t = RegInit(0.U(2.W))
      rtu_entry_pc(i) := rtu_entry_pc_t
      rtu_entry_filled(i) := rtu_entry_filled_t
      rtu_entry_priv_mode(i) := rtu_entry_priv_mode_t
      when(io.cp0_ifu_ras_en && io.rtu_ifu_retire0_pcall && rtu_entry_push(i)){
        rtu_entry_pc_t := io.rtu_ifu_retire0_inc_pc
        rtu_entry_filled_t := true.B
        rtu_entry_priv_mode_t := io.cp0_yy_priv_mode
      }.otherwise{
        rtu_entry_pc_t := rtu_entry_pc_t
        rtu_entry_filled_t := rtu_entry_filled_t
        rtu_entry_priv_mode_t := rtu_entry_priv_mode_t
      }
    }


  }


  //==========================================================
  //                    TOP RAS FIFO
  //==========================================================

  val ras_push_pc = io.ibdp_ras_push_pc
  val rtu_fifo_ptr = Wire(UInt(4.W))
  val rtu_fifo_ptr_pre = Mux(io.rtu_ifu_retire0_pcall,rtu_ptr,rtu_fifo_ptr)



  
  val m_rtu_fifo_ptr_upd_clk = Module(new gated_clk_cell())
  val rtu_fifo_ptr_upd_clk = m_rtu_fifo_ptr_upd_clk.io.clk_out
  val rtu_fifo_ptr_upd_clk_en = io.cp0_ifu_ras_en && io.rtu_ifu_retire0_pcall
  m_rtu_fifo_ptr_upd_clk.io.clk_in      := io.forever_cpuclk.asClock
  m_rtu_fifo_ptr_upd_clk.io.external_en := false.B
  m_rtu_fifo_ptr_upd_clk.io.global_en   := io.cp0_yy_clk_en
  m_rtu_fifo_ptr_upd_clk.io.local_en    := rtu_fifo_ptr_upd_clk_en
  m_rtu_fifo_ptr_upd_clk.io.module_en   := io.cp0_ifu_icg_en
  m_rtu_fifo_ptr_upd_clk.io.pad_yy_icg_scan_en := io.pad_yy_icg_scan_en

 
  withClockAndReset(rtu_fifo_ptr_upd_clk,(!io.cpurst_b).asAsyncReset){
    val rtu_fifo_ptr_t = RegInit(0.U(4.W))
    rtu_fifo_ptr := rtu_fifo_ptr_t
    when(io.cp0_ifu_ras_en && io.rtu_ifu_retire0_pcall){
      rtu_fifo_ptr_t := rtu_fifo_ptr_pre(3,0)
    }.otherwise{
      rtu_fifo_ptr_t := rtu_fifo_ptr_t
    }
  }

  val ras_entry_pc = Wire(Vec(12,UInt(39.W)))
  val rtu_entry_pre = Wire(Vec(6,UInt(39.W)))
  val rtu_entry_copy = Wire(Vec(12,Bool()))
  val ras_entry_priv_mode = Wire(Vec(12,UInt(2.W)))
  val ras_entry_filled = Wire(Vec(12,Bool()))
  val m_ras_entry_upd_clk = Array.fill(12)(Module(new gated_clk_cell))
  val ras_entry_upd_clk = Wire(Vec(12,Clock())) 
  val ras_entry_upd_clk_en = Wire(Vec(12,Bool()))
  val entry_push = Wire(Vec(12,Bool()))


  var j = 0.U
  for(j<-0 to 5){
    entry_push(j) := (top_ptr(3,0) === j.U)
    rtu_entry_copy(j) := rtu_fifo_ptr_pre(3,0) >= j.U  && rtu_fifo_ptr_pre(3,0) <= (j+5).U 

    
    rtu_entry_pre(j) := Mux(io.rtu_ifu_retire0_pcall && rtu_entry_push(j) , io.rtu_ifu_retire0_inc_pc , rtu_entry_pc(j)) 
    

    
    ras_entry_upd_clk(j) := m_ras_entry_upd_clk(j).io.clk_out
    ras_entry_upd_clk_en(j) := io.cp0_ifu_ras_en && (top_entry_rtu_updt && rtu_entry_copy(j) || io.ibctrl_ras_pcall_vld_for_gateclk && entry_push(j)) 
    m_ras_entry_upd_clk(j).io.clk_in      := io.forever_cpuclk.asClock
    m_ras_entry_upd_clk(j).io.external_en := false.B
    m_ras_entry_upd_clk(j).io.global_en   := io.cp0_yy_clk_en
    m_ras_entry_upd_clk(j).io.local_en    := ras_entry_upd_clk_en(j)
    m_ras_entry_upd_clk(j).io.module_en   := io.cp0_ifu_icg_en
    m_ras_entry_upd_clk(j).io.pad_yy_icg_scan_en := io.pad_yy_icg_scan_en
    

    withClockAndReset(ras_entry_upd_clk(j),(!io.cpurst_b).asAsyncReset){
      val ras_entry_pc_t = RegInit(0.U(39.W))
      ras_entry_pc(j) := ras_entry_pc_t
      when(top_entry_rtu_updt){
        when(rtu_entry_copy(j)){
          
          ras_entry_pc_t := rtu_entry_pre(j)
          
          // ras_entry_pc(j) := Mux(j.U>=6.U, rtu_entry_pre(j-6),rtu_entry_pre(j))
        }.otherwise{
          ras_entry_pc_t := ras_entry_pc_t
        }
      }.elsewhen(ras_push){
        when(entry_push(j)){
          ras_entry_pc_t := ras_push_pc
        }.otherwise{
          ras_entry_pc_t := ras_entry_pc_t
        }
      }.otherwise{
        ras_entry_pc_t := ras_entry_pc_t
      }
    }
    withClockAndReset(ras_entry_upd_clk(j),(!io.cpurst_b).asAsyncReset){
      val ras_entry_priv_mode_t = RegInit(0.U(2.W))
      val ras_entry_filled_t = RegInit(false.B)
      ras_entry_priv_mode(j) := ras_entry_priv_mode_t
      ras_entry_filled(j) := ras_entry_filled_t
      when(top_entry_rtu_updt && rtu_entry_copy(j)){
        ras_entry_filled_t := rtu_entry_filled(j)
        ras_entry_priv_mode_t := rtu_entry_priv_mode(j)
      }.elsewhen(ras_push && entry_push(j)){
        ras_entry_filled_t := true.B
        ras_entry_priv_mode_t := io.cp0_yy_priv_mode
      }.otherwise{
        ras_entry_filled_t := ras_entry_filled_t
        ras_entry_priv_mode_t := ras_entry_priv_mode_t
      }
    }

  }
  
  var k = 0.U
  for(k<-0 to 5){
    entry_push(k+6) := (top_ptr(3,0) === (k+6).U)
    rtu_entry_copy(k+6) := rtu_fifo_ptr_pre(3,0) >= (k+6).U  && rtu_fifo_ptr_pre(3,0) <= (k+11).U || rtu_fifo_ptr_pre(3,0) < k.U
    

    
    ras_entry_upd_clk(k+6) := m_ras_entry_upd_clk(k+6).io.clk_out
    ras_entry_upd_clk_en(k+6) := io.cp0_ifu_ras_en && (top_entry_rtu_updt && rtu_entry_copy(k+6) || io.ibctrl_ras_pcall_vld_for_gateclk && entry_push(k+6)) 
    m_ras_entry_upd_clk(k+6).io.clk_in      := io.forever_cpuclk.asClock
    m_ras_entry_upd_clk(k+6).io.external_en := false.B
    m_ras_entry_upd_clk(k+6).io.global_en   := io.cp0_yy_clk_en
    m_ras_entry_upd_clk(k+6).io.local_en    := ras_entry_upd_clk_en(k+6)
    m_ras_entry_upd_clk(k+6).io.module_en   := io.cp0_ifu_icg_en
    m_ras_entry_upd_clk(k+6).io.pad_yy_icg_scan_en := io.pad_yy_icg_scan_en
    

    withClockAndReset(ras_entry_upd_clk(k+6),(!io.cpurst_b).asAsyncReset){
      val ras_entry_pc_tt = RegInit(0.U(39.W))
      ras_entry_pc(k+6) := ras_entry_pc_tt
      when(top_entry_rtu_updt){
        when(rtu_entry_copy(k+6)){
          
          ras_entry_pc_tt := rtu_entry_pre(k)
          
          // ras_entry_pc(j) := Mux(j.U>=6.U, rtu_entry_pre(j-6),rtu_entry_pre(j))
        }.otherwise{
          ras_entry_pc_tt := ras_entry_pc_tt
        }
      }.elsewhen(ras_push){
        when(entry_push(k+6)){
          ras_entry_pc_tt := ras_push_pc
        }.otherwise{
          ras_entry_pc_tt := ras_entry_pc_tt
        }
      }.otherwise{
        ras_entry_pc_tt := ras_entry_pc_tt
      }
    }
    withClockAndReset(ras_entry_upd_clk(j),(!io.cpurst_b).asAsyncReset){
      val ras_entry_priv_mode_tt = RegInit(0.U(2.W))
      val ras_entry_filled_tt = RegInit(false.B)
      ras_entry_priv_mode(k+6) := ras_entry_priv_mode_tt
      ras_entry_filled(k+6) := ras_entry_filled_tt
      when(top_entry_rtu_updt && rtu_entry_copy(k+6)){
        ras_entry_filled_tt := rtu_entry_filled(k)
        ras_entry_priv_mode_tt := rtu_entry_priv_mode(k)
      }.elsewhen(ras_push && entry_push(k+6)){
        ras_entry_filled_tt := true.B
        ras_entry_priv_mode_tt := io.cp0_yy_priv_mode
      }.otherwise{
        ras_entry_filled_tt := ras_entry_filled_tt
        ras_entry_priv_mode_tt := ras_entry_priv_mode_tt
      }
    }

  }
  


  //==========================================================
  //                    POP PC from RAS
  //==========================================================

  val ras_pc_out = Wire(UInt(39.W))
  val ras_filled = Wire(Bool())
  val ras_priv_mode = Wire(UInt(2.W))

  when(top_ptr(3,0) === 0.U){
    ras_pc_out := ras_entry_pc(11)
    ras_filled := ras_entry_filled(11)
    ras_priv_mode := ras_entry_priv_mode(11)
  }.elsewhen(top_ptr(3,0) <=11.U){
    ras_pc_out := ras_entry_pc(top_ptr(3,0)-1.U)
    ras_filled := ras_entry_filled(top_ptr(3,0)-1.U)
    ras_priv_mode := ras_entry_priv_mode(top_ptr(3,0)-1.U)
  }.otherwise{
    ras_pc_out := ras_entry_pc(0)
    ras_filled := ras_entry_filled(0)
    ras_priv_mode := ras_entry_priv_mode(0)
  }


  io.ras_ipdp_data_vld := ((!ras_empty && ras_filled && (io.cp0_yy_priv_mode === ras_priv_mode)) || ras_push) && io.cp0_ifu_ras_en
  io.ras_ipdp_pc := Mux(io.ibctrl_ras_inst_pcall , io.ibdp_ras_push_pc , ras_pc_out)
  io.ras_l0_btb_ras_push := ras_push && io.cp0_ifu_ras_en
  io.ras_l0_btb_push_pc := io.ibdp_ras_push_pc
  io.ras_l0_btb_pc := ras_pc_out





}
