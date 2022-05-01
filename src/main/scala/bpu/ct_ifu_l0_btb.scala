package c910.bpu

import chisel3._
import chisel3.util._
import chisel3.experimental._
import c910.common._
 // import common._

class Cp0BtbCtrlBundle extends Bundle {
  val ifu_btb_en = Bool()
  val ifu_icg_en = Bool()
  val ifu_l0btb_en = Bool()
  val yy_clk_en = Bool()
}

class ct_ifu_l0_btb_io extends Bundle {
  val addrgen_l0_btb_update_entry   = Input(UInt(16.W))
  val addrgen_l0_btb_update_vld     = Input(Bool())
  val addrgen_l0_btb_update_vld_bit = Input(Bool())
  val addrgen_l0_btb_wen            = Input(UInt(4.W))
  // val cp0.ifu_btb_en                = Input(Bool())
  // val cp0.ifu_icg_en                = Input(Bool())
  // val cp0.ifu_l0btb_en              = Input(Bool())
  // val cp0.yy_clk_en                 = Input(Bool())
  val cp0                           = Input(new Cp0BtbCtrlBundle)
  val cpurst_b                      = Input(Bool())
  val forever_cpuclk                = Input(Bool())
  val ibdp_l0_btb_fifo_update_vld   = Input(Bool())
  val ibdp_l0_btb_update_cnt_bit    = Input(Bool())
  val ibdp_l0_btb_update_data       = Input(UInt(37.W))
  val ibdp_l0_btb_update_entry      = Input(UInt(16.W))
  val ibdp_l0_btb_update_ras_bit    = Input(Bool())
  val ibdp_l0_btb_update_vld        = Input(Bool())
  val ibdp_l0_btb_update_vld_bit    = Input(Bool())
  val ibdp_l0_btb_wen               = Input(UInt(4.W))
  val ifctrl_l0_btb_inv             = Input(Bool())
  val ifctrl_l0_btb_stall           = Input(Bool())
  val ipctrl_l0_btb_chgflw_vld      = Input(Bool())
  val ipctrl_l0_btb_ip_vld          = Input(Bool())
  val ipctrl_l0_btb_wait_next       = Input(Bool())
  val ipdp_l0_btb_ras_pc            = Input(UInt(39.W))
  val ipdp_l0_btb_ras_push          = Input(Bool())
  val l0_btb_update_vld_for_gateclk = Input(Bool())
  val pad_yy_icg_scan_en            = Input(Bool())
  val pcgen_l0_btb_chgflw_mask      = Input(Bool())
  val pcgen_l0_btb_chgflw_pc        = Input(UInt(15.W))
  val pcgen_l0_btb_chgflw_vld       = Input(Bool())
  val pcgen_l0_btb_if_pc            = Input(UInt(39.W))
  val ras_l0_btb_pc                 = Input(UInt(39.W))
  val ras_l0_btb_push_pc            = Input(UInt(39.W))
  val ras_l0_btb_ras_push           = Input(Bool())
  val l0_btb_debug_cur_state        = Output(UInt(2.W))
  val l0_btb_ibdp_entry_fifo        = Output(UInt(16.W))
  val l0_btb_ifctrl_chgflw_pc       = Output(UInt(39.W))
  val l0_btb_ifctrl_chgflw_way_pred = Output(UInt(2.W))
  val l0_btb_ifctrl_chglfw_vld      = Output(Bool())
  val l0_btb_ifdp_chgflw_pc         = Output(UInt(39.W))
  val l0_btb_ifdp_chgflw_way_pred   = Output(UInt(2.W))
  val l0_btb_ifdp_counter           = Output(Bool())
  val l0_btb_ifdp_entry_hit         = Output(UInt(16.W))
  val l0_btb_ifdp_hit               = Output(Bool())
  val l0_btb_ifdp_ras               = Output(Bool())
  val l0_btb_ipctrl_st_wait         = Output(Bool())
}



class ct_ifu_l0_btb extends Module {
  val io = IO(new ct_ifu_l0_btb_io)
  val PC_WIDTH = 40
  val IDLE = 1.U(2.W)
  val WAIT = 2.U(2.W)
  
  val x_l0_btb_pipe_clk = Module(new gated_clk_cell())
  val l0_btb_pipe_clk = x_l0_btb_pipe_clk.io.clk_out
  val l0_btb_pipe_en = io.cp0.ifu_btb_en && io.cp0.ifu_l0btb_en
  x_l0_btb_pipe_clk.io.clk_in      := io.forever_cpuclk.asClock
  x_l0_btb_pipe_clk.io.external_en := false.B
  x_l0_btb_pipe_clk.io.global_en   := io.cp0.yy_clk_en
  x_l0_btb_pipe_clk.io.local_en    := l0_btb_pipe_en
  x_l0_btb_pipe_clk.io.module_en   := io.cp0.ifu_icg_en
  x_l0_btb_pipe_clk.io.pad_yy_icg_scan_en := io.pad_yy_icg_scan_en
  // val IDLE :: WAIT :: Nil = Enum(UInt(), 2)
//==========================================================
//                 Read Enable Signal
//==========================================================
//1. Read enable
  withClockAndReset(l0_btb_pipe_clk, (~io.cpurst_b).asAsyncReset) {
    val l0_btb_rd = io.cp0.ifu_btb_en &&
                    io.cp0.ifu_l0btb_en &&
                    !io.pcgen_l0_btb_chgflw_mask &&
                    (io.pcgen_l0_btb_chgflw_vld || !io.ifctrl_l0_btb_stall)

    val l0_btb_rd_tag = io.pcgen_l0_btb_chgflw_pc
    val l0_btb_rd_flop = RegNext(l0_btb_rd,0.U)

    val entry_rd_hit = Wire(Vec(16,UInt(1.W)))
    val entry_tag   = Wire(Vec(16,UInt(15.W)))
    val entry_vld   = Wire(Vec(16,UInt(1.W)))
    entry_rd_hit.zip(entry_tag).zip(entry_vld).foreach(x => {x._1._1 := (l0_btb_rd_tag === x._1._2) &
                                                    x._2 & !io.pcgen_l0_btb_chgflw_mask})
    //why use & not &&?
    //* It's a style choice.

    val l0_btb_update_data = Wire(UInt(37.W))
    val l0_btb_update_vld_bit = Wire(UInt(1.W))
    val bypass_rd_hit = (l0_btb_rd_tag === l0_btb_update_data(36,22)) &&
      l0_btb_update_vld_bit.asBool && !io.pcgen_l0_btb_chgflw_mask


    //only ib ras miss will cause bypass hit
    val entry_bypass_hit = Wire(Vec(16,UInt(1.W)))
    entry_bypass_hit.zipWithIndex.foreach(x => {x._1 := bypass_rd_hit && io.ibdp_l0_btb_update_entry(x._2) })
    //val entry_hit = entry_rd_hit |  entry_bypass_hit
    // val entry_hit = Seq.fill(16)(UInt(1.W))
    val entry_hit = Wire(Vec(16,UInt(1.W)))
    entry_bypass_hit.zip(entry_rd_hit).zip(entry_hit).foreach(x=>{x._2 := x._1._1 | x._1._2})
    //* Try to use "case ()" to replace annoymous variable to make it more readable.
    //how can i do it?

    // val entry_hit_flop = Reg(UInt(16.W))
    /* DO NOT declare a register outside the withClockAndReset block 
    * if you want to use your own async reset signal.
    * It's better to use ONE withClockAndReset block.
    **/

      val entry_hit_flop = RegInit(0.U)
      when(l0_btb_rd){
          entry_hit_flop := entry_hit.asUInt //ok?
      }.elsewhen(l0_btb_rd_flop.asBool & !io.ifctrl_l0_btb_stall){
          entry_hit_flop := entry_hit.asUInt //why? forwhat?
      }.otherwise{
          entry_hit_flop := entry_hit_flop // if need?
      }


    val l0_btb_ras_pc = Wire(UInt((40-1).W))

    l0_btb_ras_pc := MuxCase(io.ras_l0_btb_pc,
                      Array(io.ras_l0_btb_ras_push -> io.ras_l0_btb_push_pc,
                            io.ipdp_l0_btb_ras_push -> io.ipdp_l0_btb_ras_pc))

    // val ras_pc = Reg(UInt((PC_WIDTH-1).W))
      val ras_pc = RegInit(0.U)
      when(l0_btb_rd || l0_btb_rd_flop.asBool){
        ras_pc := l0_btb_ras_pc
      }
      //* There's no need to write ras_ps := ras_pc in chisel which is nessary in verilog.

    //==========================================================
    //                 Entry Hit Logic
    //==========================================================
    //Only when Counter == 1,L0 BTB can be hit

    val entry_hit_counter = Wire(Bool())
    val entry_cnt = Wire(Vec(16,UInt(1.W)))
    // entry_hit_counter :=(entry_hit_flop & entry_cnt.asUInt).asBools.reduce(_||_)
    entry_hit_counter := (entry_hit_flop & entry_cnt.asUInt).orR
    //* Try to use (entry_hit_flop & entry_cnt.asUInt).orR
    val entry_hit_ras = Wire(Bool())
    val entry_ras = Wire(Vec(16,UInt(1.W)))
    entry_hit_ras := (entry_hit_flop & entry_ras.asUInt).orR
    //* Same advice as above.
    val entry_hit_way_pred_1 = Wire(Bool())
    val entry_hit_way_pred_2 = Wire(Bool())
    val entry_way_pred_1 = Wire(Vec(16,UInt(1.W)))
    val entry_way_pred_2 = Wire(Vec(16,UInt(1.W)))
    entry_hit_way_pred_1 := (entry_hit_flop & entry_way_pred_1.asUInt).orR
    entry_hit_way_pred_2 := (entry_hit_flop & entry_way_pred_2.asUInt).orR
    //* pack them in a Vector

    val entry_hit_pcs = Wire(Vec(20,Bool()))
    val entry_targets = Wire(Vec(20,Vec(16,UInt(1.W))))
    entry_hit_pcs.zip(entry_targets).foreach(x =>x._1 := (entry_hit_flop & x._2.asUInt).orR)

    val entry_if_hit = entry_hit_flop.orR
    val entry_chgflw_vld = entry_if_hit && entry_hit_counter
    val entry_hit_target = Wire(UInt((PC_WIDTH-1).W))
    val entry_hit_target_temp = Cat(entry_hit_pcs)
    entry_hit_target :=   Mux(entry_hit_ras,ras_pc,Cat(io.pcgen_l0_btb_if_pc(38,20),entry_hit_target_temp))

    //==========================================================
    //                 L0 BTB Write State
    //==========================================================
    // &Instance("gated_clk_cell","x_l0_btb_clk"); @210
    val x_l0_btb_clk = Module(new gated_clk_cell())
    val l0_btb_clk = x_l0_btb_clk.io.clk_out
    val l0_btb_clk_en = io.cp0.ifu_btb_en && io.cp0.ifu_l0btb_en
    x_l0_btb_clk.io.clk_in      := io.forever_cpuclk.asClock
    x_l0_btb_clk.io.external_en := false.B
    x_l0_btb_clk.io.global_en   := io.cp0.yy_clk_en
    x_l0_btb_clk.io.local_en    := l0_btb_clk_en
    x_l0_btb_clk.io.module_en   := io.cp0.ifu_icg_en
    x_l0_btb_clk.io.pad_yy_icg_scan_en := io.pad_yy_icg_scan_en

    val l0_btb_cur_state = Reg(UInt(2.W))
    val l0_btb_next_state = Reg(UInt(2.W))
    l0_btb_cur_state := RegNext(l0_btb_next_state,IDLE)
    when(l0_btb_cur_state === IDLE){
      l0_btb_next_state := Mux(io.pcgen_l0_btb_chgflw_vld,WAIT,IDLE)
    }.elsewhen(l0_btb_cur_state === WAIT){
      l0_btb_next_state := MuxCase(IDLE,Array(io.pcgen_l0_btb_chgflw_mask -> IDLE,io.ipctrl_l0_btb_chgflw_vld -> WAIT,
      io.ipctrl_l0_btb_wait_next -> WAIT,!io.ipctrl_l0_btb_ip_vld ->WAIT))
    }

    io.l0_btb_ipctrl_st_wait := (l0_btb_cur_state === WAIT)

    //==========================================================
    //                 L0 BTB FIFO bit
    //==========================================================
    // &Instance("gated_clk_cell", "x_l0_btb_create_clk"); @254
    val x_l0_btb_create_clk = Module(new gated_clk_cell())
    val l0_btb_create_clk = x_l0_btb_create_clk.io.clk_out
    val l0_btb_create_en = io.cp0.ifu_btb_en && io.cp0.ifu_l0btb_en && io.ibdp_l0_btb_fifo_update_vld
    x_l0_btb_create_clk.io.clk_in      := io.forever_cpuclk.asClock
    x_l0_btb_create_clk.io.external_en := false.B
    x_l0_btb_create_clk.io.global_en   := io.cp0.yy_clk_en
    x_l0_btb_create_clk.io.local_en    := l0_btb_create_en
    x_l0_btb_create_clk.io.module_en   := io.cp0.ifu_icg_en
    x_l0_btb_create_clk.io.pad_yy_icg_scan_en := io.pad_yy_icg_scan_en

      val entry_fifo = RegInit(1.U)
      when(l0_btb_create_en){
        entry_fifo := Cat(entry_fifo(14,0),entry_fifo(15))
      }.otherwise{
        entry_fifo := entry_fifo
      }

    io.l0_btb_ibdp_entry_fifo := entry_fifo


    //==========================================================
    //                 L0 BTB Write Preparation
    //==========================================================

    val l0_btb_update_entry = Wire(Vec(16,UInt(1.W)))
    l0_btb_update_entry.zip(io.addrgen_l0_btb_update_entry.asBools).zip(
      io.ibdp_l0_btb_update_entry.asBools).foreach(x => x._1._1 := (io.addrgen_l0_btb_update_vld && x._1._2) ||
      (io.l0_btb_update_vld_for_gateclk && x._2)
    )

    val l0_btb_wen = Wire(UInt(4.W))
    //val l0_btb_update_vld_bit     = Reg(UInt(1.W))
    val l0_btb_update_cnt_bit     = Wire(UInt(1.W))
    val l0_btb_update_ras_bit     = Wire(UInt(1.W))
    //val l0_btb_update_data        = Reg(UInt(37.W))

    when(io.addrgen_l0_btb_update_vld === 1.U){

      l0_btb_wen := io.addrgen_l0_btb_wen
      l0_btb_update_vld_bit := io.addrgen_l0_btb_update_vld_bit
      l0_btb_update_cnt_bit := 0.U
      l0_btb_update_ras_bit := 0.U
      l0_btb_update_data    := 0.U
    }.elsewhen(io.ibdp_l0_btb_update_vld === 1.U){
      l0_btb_wen := io.ibdp_l0_btb_wen
      l0_btb_update_vld_bit := io.ibdp_l0_btb_update_vld_bit
      l0_btb_update_cnt_bit := io.ibdp_l0_btb_update_cnt_bit
      l0_btb_update_ras_bit := io.ibdp_l0_btb_update_ras_bit
      l0_btb_update_data    := io.ibdp_l0_btb_update_data
    }.otherwise{
      l0_btb_wen := 0.U
      l0_btb_update_vld_bit := 0.U
      l0_btb_update_cnt_bit := 0.U
      l0_btb_update_ras_bit := 0.U
      l0_btb_update_data    := 0.U
    }


    //==========================================================
    //             Invalidating Status Register
    //==========================================================
    val x_l0_btb_inv_reg_upd_clk = Module(new gated_clk_cell())
    val l0_btb_inv_reg_upd_clk = x_l0_btb_inv_reg_upd_clk.io.clk_out

    val l0_btb_entry_inv = Reg(UInt(1.W))
    val l0_btb_inv_reg_upd_clk_en =l0_btb_entry_inv | io.ifctrl_l0_btb_inv
    x_l0_btb_inv_reg_upd_clk.io.clk_in      := io.forever_cpuclk.asClock
    x_l0_btb_inv_reg_upd_clk.io.external_en := false.B
    x_l0_btb_inv_reg_upd_clk.io.global_en   := io.cp0.yy_clk_en
    x_l0_btb_inv_reg_upd_clk.io.local_en    := l0_btb_inv_reg_upd_clk_en
    x_l0_btb_inv_reg_upd_clk.io.module_en   := io.cp0.ifu_icg_en
    x_l0_btb_inv_reg_upd_clk.io.pad_yy_icg_scan_en := io.pad_yy_icg_scan_en

      l0_btb_entry_inv := RegInit(0.U)
      when(l0_btb_entry_inv.asBool){
        l0_btb_entry_inv := 0.U
      }.elsewhen(io.ifctrl_l0_btb_inv){
        l0_btb_entry_inv := 1.U
      }.otherwise{
        l0_btb_entry_inv := l0_btb_entry_inv
      }

    //==========================================================
    //                 Instance 16 Entry
    //==========================================================
    val l0_btb_entry = Array.fill(16)(Module(new ct_ifu_l0_btb_entry()).io)
    for(i <- 0 until 16){
      // l0_btb_entry(i).cp0.ifu_btb_en := io.cp0.ifu_btb_en
      // l0_btb_entry(i).cp0.ifu_icg_en := io.cp0.ifu_icg_en
      // l0_btb_entry(i).cp0.ifu_l0btb_en := io.cp0.ifu_l0btb_en
      // l0_btb_entry(i).cp0.yy_clk_en := io.cp0.yy_clk_en
      l0_btb_entry(i).cpurst_b  := io.cpurst_b
      l0_btb_entry(i).cp0 <> io.cp0

      entry_cnt(i) := l0_btb_entry(i).entry_cnt.asUInt
      l0_btb_entry(i).entry_inv := l0_btb_entry_inv
        entry_ras(i) := l0_btb_entry(i).entry_ras.asUInt
      entry_tag(i) := l0_btb_entry(i).entry_tag
      //l0_btb_entry(i).entry_target := entry_targets.flatten.filter()
  //    val entry_targets = Seq.fill(20)(UInt(16.W))
  //    entry_hit_pcs.zip(entry_targets).foreach(x =>x._1 := (entry_hit_flop & x._2).orR
      for(j <- 0 until 20)
        {
        entry_targets(j)(i) := l0_btb_entry(i).entry_target(j)
        }
      l0_btb_entry(i).entry_update := l0_btb_update_entry(i)
      l0_btb_entry(i).entry_update_cnt := l0_btb_update_cnt_bit
      l0_btb_entry(i).entry_update_data := l0_btb_update_data
      l0_btb_entry(i).entry_update_ras := l0_btb_update_ras_bit
      l0_btb_entry(i).entry_update_vld := l0_btb_update_vld_bit
      entry_vld(i) :=l0_btb_entry(i).entry_vld
      entry_way_pred_1(i):=l0_btb_entry(i).entry_way_pred(0)
      entry_way_pred_2(i):=l0_btb_entry(i).entry_way_pred(1)
        l0_btb_entry(i).entry_wen := l0_btb_wen
      l0_btb_entry(i).forever_cpuclk := io.forever_cpuclk.asClock
      l0_btb_entry(i).pad_yy_icg_scan_en := io.pad_yy_icg_scan_en

    }
    io.l0_btb_ifctrl_chglfw_vld := entry_chgflw_vld
    io.l0_btb_ifctrl_chgflw_pc  := entry_hit_target
    io.l0_btb_ifctrl_chgflw_way_pred := Cat(entry_hit_way_pred_2,entry_hit_way_pred_1)
    io.l0_btb_ifdp_chgflw_pc := entry_hit_target
    io.l0_btb_ifdp_chgflw_way_pred := Cat(entry_hit_way_pred_2,entry_hit_way_pred_1)
    io.l0_btb_ifdp_entry_hit := entry_hit_flop
    io.l0_btb_ifdp_hit := entry_if_hit
    io.l0_btb_ifdp_counter := entry_hit_counter
    io.l0_btb_ifdp_ras := entry_hit_ras
    io.l0_btb_debug_cur_state := l0_btb_cur_state
  }
}






