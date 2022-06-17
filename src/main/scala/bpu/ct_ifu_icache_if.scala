package bpu

import chisel3._
import chisel3.util._
import chisel3.experimental._
//import c910.common._
import common._

class ct_ifu_icache_tag_array extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
    val forever_cpuclk              = Input(Bool())
    val cp0_ifu_icg_en              = Input(Bool())
    val ifu_icache_index            = Input(UInt(16.W))
    val ifu_icache_tag_cen_b        = Input(Bool())
    val ifu_icache_tag_clk_en       = Input(Bool())
    val ifu_icache_tag_din          = Input(UInt(59.W))
    val ifu_icache_tag_wen          = Input(UInt(3.W))
    val pad_yy_icg_scan_en          = Input(Bool())
    val icache_ifu_tag_dout         = Output(UInt(59.W))
  })
  addResource("/ct_ifu_icache_tag_array.v")
}

class ct_ifu_icache_predecd_array0 extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
      val cp0_ifu_icg_en                    = Input(Bool())
      val cp0_yy_clk_en                     = Input(Bool())
      val forever_cpuclk                    = Input(Bool())
      val ifu_icache_data_array0_wen_b      = Input(Bool())
      val ifu_icache_index                  = Input(UInt(16.W))
      val ifu_icache_predecd_array0_cen_b   = Input(Bool())
      val ifu_icache_predecd_array0_clk_en  = Input(Bool())
      val ifu_icache_predecd_array0_din     = Input(UInt(32.W))
      val ifu_icache_predecd_array0_wen_b   = Input(Bool())
      val pad_yy_icg_scan_en                = Input(Bool())
      val icache_ifu_predecd_array0_dout    = Output(UInt(32.W))
  })
  addResource("/ct_ifu_icache_predecd_array0.v")
}

class ct_ifu_icache_predecd_array1 extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
      val cp0_ifu_icg_en                    = Input(Bool())
      val cp0_yy_clk_en                     = Input(Bool())
      val forever_cpuclk                    = Input(Bool())
      val ifu_icache_data_array1_wen_b      = Input(Bool())
      val ifu_icache_index                  = Input(UInt(16.W))
      val ifu_icache_predecd_array1_cen_b   = Input(Bool())
      val ifu_icache_predecd_array1_clk_en  = Input(Bool())
      val ifu_icache_predecd_array1_din     = Input(UInt(32.W))
      val ifu_icache_predecd_array1_wen_b   = Input(Bool())
      val pad_yy_icg_scan_en                = Input(Bool())
      val icache_ifu_predecd_array1_dout    = Output(UInt(32.W))
  })
  addResource("/ct_ifu_icache_predecd_array1.v")
}

class ct_ifu_icache_data_array0 extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
      val cp0_ifu_icg_en                        = Input(Bool())
      val cp0_yy_clk_en                         = Input(Bool())
      val forever_cpuclk                        = Input(Bool())
      val ifu_icache_data_array0_bank_cen_b     = Input(Vec(4, Bool()))
      val ifu_icache_data_array0_bank_clk_en    = Input(Vec(4, Bool()))
      val ifu_icache_data_array0_din            = Input(UInt(128.W))
      val ifu_icache_data_array0_wen_b          = Input(Bool())
      val ifu_icache_index                      = Input(UInt(16.W))
      val pad_yy_icg_scan_en                    = Input(Bool())
      val icache_ifu_data_array0_dout           = Output(UInt(128.W))               
  })
  addResource("/ct_ifu_icache_data_array0.v")
}

class ct_ifu_icache_data_array1 extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
      val cp0_ifu_icg_en                        = Input(Bool())
      val cp0_yy_clk_en                         = Input(Bool())
      val forever_cpuclk                        = Input(Bool())
      val ifu_icache_data_array1_bank_cen_b     = Input(Vec(4, Bool()))
      val ifu_icache_data_array1_bank_clk_en    = Input(Vec(4, Bool()))
      val ifu_icache_data_array1_din            = Input(UInt(128.W))
      val ifu_icache_data_array1_wen_b          = Input(Bool())
      val ifu_icache_index                      = Input(UInt(16.W))
      val pad_yy_icg_scan_en                    = Input(Bool())
      val icache_ifu_data_array1_dout           = Output(UInt(128.W))               
  })
  addResource("/ct_ifu_icache_data_array1.v")
}

class ct_ifu_icache_if_io() extends Bundle {
val         cp0_ifu_icache_en                     = Input(Bool())                
val         cp0_ifu_icg_en                        = Input(Bool())                     
val         cp0_yy_clk_en                         = Input(Bool())                     
val         cpurst_b                              = Input(Bool())                          
val         forever_cpuclk                        = Input(Bool())                    
val         hpcp_ifu_cnt_en                       = Input(Bool())                   
val         ifctrl_icache_if_index                = Input(UInt(39.W))            
val         ifctrl_icache_if_inv_fifo             = Input(Bool())         
val         ifctrl_icache_if_inv_on               = Input(Bool())           
val         ifctrl_icache_if_read_req_data0       = Input(Bool())   
val         ifctrl_icache_if_read_req_data1       = Input(Bool())   
val         ifctrl_icache_if_read_req_index       = Input(UInt(39.W))   
val         ifctrl_icache_if_read_req_tag         = Input(Bool())     
val         ifctrl_icache_if_reset_req            = Input(Bool())        
val         ifctrl_icache_if_tag_req              = Input(Bool())          
val         ifctrl_icache_if_tag_wen              = Input(UInt(3.W))          
val         ifu_hpcp_icache_miss_pre              = Input(Bool())          
val         ipb_icache_if_index                   = Input(UInt(34.W))               
val         ipb_icache_if_req                     = Input(Bool())                 
val         ipb_icache_if_req_for_gateclk         = Input(Bool())     
val         l1_refill_icache_if_fifo              = Input(Bool())          
val         l1_refill_icache_if_first             = Input(Bool())         
val         l1_refill_icache_if_index             = Input(UInt(39.W))         
val         l1_refill_icache_if_inst_data         = Input(UInt(128.W))     
val         l1_refill_icache_if_last              = Input(Bool())          
val         l1_refill_icache_if_pre_code          = Input(UInt(32.W))      
val         l1_refill_icache_if_ptag              = Input(UInt(28.W))          
val         l1_refill_icache_if_wr                = Input(Bool())            
val         pad_yy_icg_scan_en                    = Input(Bool())                
val         pcgen_icache_if_chgflw                = Input(Bool())            
val         pcgen_icache_if_chgflw_bank           = Input(Vec(4, Bool()))      
val         pcgen_icache_if_chgflw_short          = Input(Bool())      
val         pcgen_icache_if_gateclk_en            = Input(Bool())        
val         pcgen_icache_if_index                 = Input(UInt(16.W))             
val         pcgen_icache_if_seq_data_req          = Input(Bool())      
val         pcgen_icache_if_seq_data_req_short    = Input(Bool())
val         pcgen_icache_if_seq_tag_req           = Input(Bool())       
val         pcgen_icache_if_way_pred              = Input(UInt(2.W))          
val icache_if_ifctrl_inst_data0   = Output(UInt(128.W))       
val icache_if_ifctrl_inst_data1   = Output(UInt(128.W))       
val icache_if_ifctrl_tag_data0    = Output(UInt(29.W))        
val icache_if_ifctrl_tag_data1    = Output(UInt(29.W))        
val icache_if_ifdp_fifo           = Output(Bool())               
val icache_if_ifdp_inst_data0     = Output(UInt(128.W))         
val icache_if_ifdp_inst_data1     = Output(UInt(128.W))         
val icache_if_ifdp_precode0       = Output(UInt(32.W))           
val icache_if_ifdp_precode1       = Output(UInt(32.W))           
val icache_if_ifdp_tag_data0      = Output(UInt(29.W))          
val icache_if_ifdp_tag_data1      = Output(UInt(29.W))          
val icache_if_ipb_tag_data0       = Output(UInt(29.W))           
val icache_if_ipb_tag_data1       = Output(UInt(29.W))           
val ifu_hpcp_icache_access        = Output(Bool())            
val ifu_hpcp_icache_miss          = Output(Bool())              

}

class ct_ifu_icache_if() extends RawModule{

  val io = IO(new ct_ifu_icache_if_io())

  private def genLocalGatedClk(local_en: Bool): Clock = {
    val _gated_clk_cell = Module(new gated_clk_cell)
    val clk_out = _gated_clk_cell.io.clk_out
    _gated_clk_cell.io.clk_in      := io.forever_cpuclk.asClock
    _gated_clk_cell.io.external_en := false.B
    _gated_clk_cell.io.global_en   := io.cp0_yy_clk_en
    _gated_clk_cell.io.local_en    := local_en
    _gated_clk_cell.io.module_en   := io.cp0_ifu_icg_en
    _gated_clk_cell.io.pad_yy_icg_scan_en := io.pad_yy_icg_scan_en
    clk_out
  }

//==========================================================
//         Chip Enable to Cache Tag Array
//==========================================================
  
  val ifu_icache_tag_cen_b = !(io.l1_refill_icache_if_wr && 
                                 (io.l1_refill_icache_if_first || 
                                  io.l1_refill_icache_if_last) && 
                                io.cp0_ifu_icache_en
                               ) &&
                              !(io.ifctrl_icache_if_tag_req
                               ) &&
                              !(io.pcgen_icache_if_chgflw &&
                                 (io.pcgen_icache_if_way_pred != 0.U(2.W)).asBool() && 
                                io.cp0_ifu_icache_en
                               ) &&
                              !(io.pcgen_icache_if_seq_tag_req && //Seq && !Stall
                                io.cp0_ifu_icache_en
                               ) &&
                              !(io.ipb_icache_if_req && 
                                io.cp0_ifu_icache_en
                               ) && 
                              !io.ifctrl_icache_if_read_req_tag

  val ifu_icache_tag_clk_en = io.ifctrl_icache_if_tag_req || 
                               io.ifctrl_icache_if_read_req_tag ||
                               io.cp0_ifu_icache_en && 
                               (
                                 io.l1_refill_icache_if_wr || 
                                 io.pcgen_icache_if_gateclk_en || 
                                 io.ipb_icache_if_req_for_gateclk
                               )

//==========================================================
//            Write Enable to Icache Tag Array
//==========================================================
  val fifo_bit = io.l1_refill_icache_if_fifo

  val ifu_icache_tag_wen = Wire(UInt(3.W))
  val ifu_icache_tag_wen_3 = Wire(UInt(1.W))
  val ifu_icache_tag_wen_2 = Wire(UInt(1.W))
  val ifu_icache_tag_wen_1 = Wire(UInt(1.W))
  ifu_icache_tag_wen := Cat(ifu_icache_tag_wen_3, ifu_icache_tag_wen_2, ifu_icache_tag_wen_1)
  ifu_icache_tag_wen_3 := MuxCase(1.U, Array(io.ifctrl_icache_if_inv_on -> io.ifctrl_icache_if_tag_wen(2),
                                            (io.l1_refill_icache_if_wr && io.l1_refill_icache_if_last) -> 0.U))

  ifu_icache_tag_wen_2 := MuxCase(1.U, Array(io.ifctrl_icache_if_inv_on -> io.ifctrl_icache_if_tag_wen(1),
                                            (io.l1_refill_icache_if_wr && (io.l1_refill_icache_if_first || io.l1_refill_icache_if_last)) -> (!fifo_bit).asUInt()))
                                          
  ifu_icache_tag_wen_1 := MuxCase(1.U, Array(io.ifctrl_icache_if_inv_on -> io.ifctrl_icache_if_tag_wen(0),
                                            (io.l1_refill_icache_if_wr && (io.l1_refill_icache_if_first || io.l1_refill_icache_if_last)) -> (fifo_bit).asUInt()))
  
//==========================================================
//            Write Data to Icache Tag Array
//==========================================================
  val tag_fifo_din = Wire(UInt(1.W))
  val tag_valid_din = Wire(UInt(1.W))
  val tag_pc_din = Wire(UInt(28.W))
  val ifu_icache_tag_din = Wire(UInt(59.W))

  tag_fifo_din := Mux(io.ifctrl_icache_if_inv_on, io.ifctrl_icache_if_inv_fifo, !fifo_bit)
  tag_valid_din := io.l1_refill_icache_if_last
  tag_pc_din := Mux((io.ifctrl_icache_if_inv_on || io.l1_refill_icache_if_first), 0.U(28.W), io.l1_refill_icache_if_ptag)
  ifu_icache_tag_din := Cat(tag_fifo_din, tag_valid_din, tag_pc_din, tag_valid_din, tag_pc_din)

//==========================================================
//            Chip Enable to Icache Data Array
//==========================================================
  val icache_reset_inv = io.ifctrl_icache_if_reset_req
  val icache_way_pred = Wire(UInt(2.W))
  
  val ifu_icache_data_array0_bank_cen_b = Wire(Vec(4, UInt(1.W)))
  val ifu_icache_data_array1_bank_cen_b = Wire(Vec(4, UInt(1.W)))

  val ifu_icache_data_array0_bank_clk_en = Wire(Vec(4, UInt(1.W)))
  val ifu_icache_data_array1_bank_clk_en = Wire(Vec(4, UInt(1.W)))

  icache_way_pred := Mux(io.l1_refill_icache_if_wr, 1.U(2.W), io.pcgen_icache_if_way_pred)

  (ifu_icache_data_array0_bank_cen_b).zip(io.pcgen_icache_if_chgflw_bank).foreach(x => 
                                {x._1 := (
                                             !(io.l1_refill_icache_if_wr && !fifo_bit
                                              ) &&
                                             !(x._2
                                              ) &&
                                             !(io.pcgen_icache_if_seq_data_req
                                              )
                                              || !(io.cp0_ifu_icache_en && icache_way_pred(0))
                                            ) && 
                                            !io.ifctrl_icache_if_read_req_data0 &&
                                            !icache_reset_inv})

  (ifu_icache_data_array1_bank_cen_b).zip(io.pcgen_icache_if_chgflw_bank).foreach(x => 
                                {x._1 := (
                                             !(io.l1_refill_icache_if_wr && fifo_bit
                                              ) &&
                                             !(x._2
                                              ) &&
                                             !(io.pcgen_icache_if_seq_data_req
                                              )
                                              || !(io.cp0_ifu_icache_en && icache_way_pred(1))
                                            ) && 
                                            !io.ifctrl_icache_if_read_req_data1 &&
                                            !icache_reset_inv})

  ifu_icache_data_array0_bank_clk_en.foreach(x => 
                                {x :=     (
                                              io.l1_refill_icache_if_wr && !fifo_bit || 
                                              io.pcgen_icache_if_chgflw_short || 
                                              io.pcgen_icache_if_seq_data_req_short 
                                            ) && 
                                            io.cp0_ifu_icache_en || 
                                            io.ifctrl_icache_if_read_req_data0 ||
                                            icache_reset_inv})
  ifu_icache_data_array1_bank_clk_en.foreach(x => 
                                {x :=     (
                                              io.l1_refill_icache_if_wr && fifo_bit || 
                                              io.pcgen_icache_if_chgflw_short || 
                                              io.pcgen_icache_if_seq_data_req_short 
                                            ) && 
                                            io.cp0_ifu_icache_en || 
                                            io.ifctrl_icache_if_read_req_data1 ||
                                            icache_reset_inv})

//==========================================================
//            Write Enable to Icache Data Array
//==========================================================
  val ifu_icache_data_array0_wen_b = !(io.l1_refill_icache_if_wr && !fifo_bit
                                       ) &&
                                      !icache_reset_inv
  val ifu_icache_data_array1_wen_b = !(io.l1_refill_icache_if_wr && fifo_bit
                                       ) &&
                                      !icache_reset_inv

//==========================================================
//            Write Data to Icache Data Array
//==========================================================
  val ifu_icache_data_array0_din = Wire(UInt(128.W))
  val ifu_icache_data_array1_din = Wire(UInt(128.W))

  ifu_icache_data_array0_din := Mux(icache_reset_inv, 0.U(128.W), io.l1_refill_icache_if_inst_data)
  ifu_icache_data_array1_din := Mux(icache_reset_inv, 0.U(128.W), io.l1_refill_icache_if_inst_data)

//==========================================================
//          Chip Enable to Icache Predecode Array
//==========================================================
  val ifu_icache_predecd_array0_cen_b = ( !(io.l1_refill_icache_if_wr && !fifo_bit
                                           ) &&
                                           !(io.pcgen_icache_if_chgflw
                                           ) &&
                                           !(io.pcgen_icache_if_seq_data_req
                                           )
                                           || !(io.cp0_ifu_icache_en && icache_way_pred(0))
                                         ) &&
                                          !icache_reset_inv
  val ifu_icache_predecd_array1_cen_b = ( !(io.l1_refill_icache_if_wr && fifo_bit
                                           ) &&
                                           !(io.pcgen_icache_if_chgflw
                                           ) &&
                                           !(io.pcgen_icache_if_seq_data_req
                                           )
                                           || !(io.cp0_ifu_icache_en && icache_way_pred(1))
                                         ) &&
                                          !icache_reset_inv
  
  val ifu_icache_predecd_array0_clk_en = (
                                           io.l1_refill_icache_if_wr && !fifo_bit || 
                                           io.pcgen_icache_if_chgflw_short        || 
                                           io.pcgen_icache_if_seq_data_req_short 
                                          ) && 
                                          io.cp0_ifu_icache_en ||
                                          icache_reset_inv
  val ifu_icache_predecd_array1_clk_en = (
                                           io.l1_refill_icache_if_wr && fifo_bit || 
                                           io.pcgen_icache_if_chgflw_short       || 
                                           io.pcgen_icache_if_seq_data_req_short 
                                          ) && 
                                          io.cp0_ifu_icache_en ||
                                          icache_reset_inv

//==========================================================
//         Write Enable to Icache Predecode Array
//==========================================================
  val ifu_icache_predecd_array0_wen_b = !(io.l1_refill_icache_if_wr && !fifo_bit) &&
                                         !icache_reset_inv
  val ifu_icache_predecd_array1_wen_b = !(io.l1_refill_icache_if_wr && fifo_bit) &&
                                         !icache_reset_inv

//==========================================================
//          Write Data to Icache Predecode Array
//==========================================================
  val ifu_icache_predecd_array0_din = Wire(UInt(32.W))
  val ifu_icache_predecd_array1_din = Wire(UInt(32.W))
  
  ifu_icache_predecd_array0_din := Mux(icache_reset_inv, 0.U(32.W), io.l1_refill_icache_if_pre_code)
  ifu_icache_predecd_array1_din := Mux(icache_reset_inv, 0.U(32.W), io.l1_refill_icache_if_pre_code)

//==========================================================
//                   Index to Icache
//==========================================================
  val icache_req_higher      = io.ifctrl_icache_if_tag_req || 
                                io.ifctrl_icache_if_reset_req ||
                                io.l1_refill_icache_if_wr || 
                                io.ipb_icache_if_req ||
                                io.ifctrl_icache_if_read_req_data0 || 
                                io.ifctrl_icache_if_read_req_data1 || 
                                io.ifctrl_icache_if_read_req_tag
  
  val ifu_icache_index = Wire(UInt(16.W))
  val icache_index_higher = Wire(UInt(16.W))
  val icache_read_req = io.ifctrl_icache_if_read_req_data0 ||
                        io.ifctrl_icache_if_read_req_data1 ||
                        io.ifctrl_icache_if_read_req_tag

  icache_index_higher := MuxCase(0.U(16.W), Array((io.ifctrl_icache_if_tag_req || io.ifctrl_icache_if_reset_req) -> io.ifctrl_icache_if_index,
                                        io.l1_refill_icache_if_wr -> io.l1_refill_icache_if_index,
                                        io.ipb_icache_if_req -> Cat(io.ipb_icache_if_index(10,0), 0.U(5.W)),
                                        icache_read_req -> io.ifctrl_icache_if_read_req_index))
  ifu_icache_index := Mux(icache_req_higher, icache_index_higher, io.pcgen_icache_if_index)
  
//==========================================================
//              Icache Tag Array Output Data
//==========================================================
  val icache_ifu_tag_dout = Wire(UInt(59.W))
  val icache_ifu_data_array0_dout = Wire(UInt(128.W))
  val icache_ifu_data_array1_dout = Wire(UInt(128.W))
  
  io.icache_if_ifdp_tag_data0    := icache_ifu_tag_dout(28,0)
  io.icache_if_ifdp_tag_data1    := icache_ifu_tag_dout(57,29)
  io.icache_if_ifdp_fifo         := icache_ifu_tag_dout(58)
  io.icache_if_ifctrl_tag_data0  := icache_ifu_tag_dout(28,0)
  io.icache_if_ifctrl_tag_data1  := icache_ifu_tag_dout(57,29)
  io.icache_if_ifctrl_inst_data0 := icache_ifu_data_array0_dout
  io.icache_if_ifctrl_inst_data1 := icache_ifu_data_array1_dout

//==========================================================
//              Icache Data Array Output Data
//==========================================================
  val icache_ifu_predecd_array0_dout = Wire(UInt(32.W))
  val icache_ifu_predecd_array1_dout = Wire(UInt(32.W))

  io.icache_if_ifdp_precode0   := icache_ifu_predecd_array0_dout
  io.icache_if_ifdp_inst_data0 := icache_ifu_data_array0_dout

  io.icache_if_ifdp_precode1   := icache_ifu_predecd_array1_dout
  io.icache_if_ifdp_inst_data1 := icache_ifu_data_array1_dout

//==========================================================
//               Interactive with Ipb
//==========================================================
  io.icache_if_ipb_tag_data0 := icache_ifu_tag_dout(28, 0)
  io.icache_if_ipb_tag_data1 := icache_ifu_tag_dout(57,29)

//==========================================================
//               Interactive with PMU
//==========================================================
  val ifu_hpcp_icache_access_pre = (io.pcgen_icache_if_seq_data_req || io.pcgen_icache_if_chgflw)&& io.cp0_ifu_icache_en
  val hpcp_clk_en =  io.cp0_ifu_icache_en && io.hpcp_ifu_cnt_en

  val hpcp_clk = genLocalGatedClk(hpcp_clk_en)

  withClockAndReset(hpcp_clk, (!io.cpurst_b).asAsyncReset){
    val ifu_hpcp_icache_access_reg = RegInit(0.U(1.W))
    val ifu_hpcp_icache_miss_reg = RegInit(0.U(1.W))
    
    when(io.cp0_ifu_icache_en && io.hpcp_ifu_cnt_en){
      ifu_hpcp_icache_access_reg := ifu_hpcp_icache_access_pre
      ifu_hpcp_icache_miss_reg   := io.ifu_hpcp_icache_miss_pre
    }.otherwise{
      ifu_hpcp_icache_access_reg <= ifu_hpcp_icache_access_reg
      ifu_hpcp_icache_miss_reg   <= ifu_hpcp_icache_miss_reg
    }

    io.ifu_hpcp_icache_access := ifu_hpcp_icache_access_reg
    io.ifu_hpcp_icache_miss   := ifu_hpcp_icache_miss_reg
  }

//==========================================================
//            Memory Connect -- Tag Array
//==========================================================
  val x_ct_ifu_icache_tag_array = Module(new ct_ifu_icache_tag_array()).io
    x_ct_ifu_icache_tag_array.cp0_ifu_icg_en        := io.cp0_ifu_icg_en
    x_ct_ifu_icache_tag_array.forever_cpuclk        := io.forever_cpuclk       
    x_ct_ifu_icache_tag_array.ifu_icache_index      := ifu_icache_index     
    x_ct_ifu_icache_tag_array.ifu_icache_tag_cen_b  := ifu_icache_tag_cen_b 
    x_ct_ifu_icache_tag_array.ifu_icache_tag_clk_en := ifu_icache_tag_clk_en
    x_ct_ifu_icache_tag_array.ifu_icache_tag_din    := ifu_icache_tag_din   
    x_ct_ifu_icache_tag_array.ifu_icache_tag_wen    := ifu_icache_tag_wen   
    x_ct_ifu_icache_tag_array.pad_yy_icg_scan_en    := io.pad_yy_icg_scan_en  
    
    icache_ifu_tag_dout     := x_ct_ifu_icache_tag_array.icache_ifu_tag_dout

  val x_ct_ifu_icache_data_array0 = Module(new ct_ifu_icache_data_array0()).io
    x_ct_ifu_icache_data_array0.cp0_ifu_icg_en                      := io.cp0_ifu_icg_en                     
    x_ct_ifu_icache_data_array0.cp0_yy_clk_en                       := io.cp0_yy_clk_en                      
    x_ct_ifu_icache_data_array0.forever_cpuclk                      := io.forever_cpuclk                     

    x_ct_ifu_icache_data_array0.ifu_icache_data_array0_bank_cen_b  := ifu_icache_data_array0_bank_cen_b 
    x_ct_ifu_icache_data_array0.ifu_icache_data_array0_bank_clk_en := ifu_icache_data_array0_bank_clk_en
    x_ct_ifu_icache_data_array0.ifu_icache_data_array0_din          := ifu_icache_data_array0_din         
    x_ct_ifu_icache_data_array0.ifu_icache_data_array0_wen_b        := ifu_icache_data_array0_wen_b       
    x_ct_ifu_icache_data_array0.ifu_icache_index                    := ifu_icache_index                   
    x_ct_ifu_icache_data_array0.pad_yy_icg_scan_en                  := io.pad_yy_icg_scan_en                 
    
    icache_ifu_data_array0_dout     := x_ct_ifu_icache_data_array0.icache_ifu_data_array0_dout

  val x_ct_ifu_icache_data_array1 = Module(new ct_ifu_icache_data_array1()).io
    x_ct_ifu_icache_data_array1.cp0_ifu_icg_en                      := io.cp0_ifu_icg_en                     
    x_ct_ifu_icache_data_array1.cp0_yy_clk_en                       := io.cp0_yy_clk_en                      
    x_ct_ifu_icache_data_array1.forever_cpuclk                      := io.forever_cpuclk                     

    x_ct_ifu_icache_data_array1.ifu_icache_data_array1_bank_cen_b   := ifu_icache_data_array1_bank_cen_b 
    x_ct_ifu_icache_data_array1.ifu_icache_data_array1_bank_clk_en  := ifu_icache_data_array1_bank_clk_en
    x_ct_ifu_icache_data_array1.ifu_icache_data_array1_din          := ifu_icache_data_array1_din         
    x_ct_ifu_icache_data_array1.ifu_icache_data_array1_wen_b        := ifu_icache_data_array1_wen_b       
    x_ct_ifu_icache_data_array1.ifu_icache_index                    := ifu_icache_index                   
    x_ct_ifu_icache_data_array1.pad_yy_icg_scan_en                  := io.pad_yy_icg_scan_en                 
    
    icache_ifu_data_array1_dout         := x_ct_ifu_icache_data_array1.icache_ifu_data_array1_dout

  val x_ct_ifu_icache_predecd_array0 = Module(new ct_ifu_icache_predecd_array0()).io
    x_ct_ifu_icache_predecd_array0.cp0_ifu_icg_en                   := io.cp0_ifu_icg_en                  
    x_ct_ifu_icache_predecd_array0.cp0_yy_clk_en                    := io.cp0_yy_clk_en                   
    x_ct_ifu_icache_predecd_array0.forever_cpuclk                   := io.forever_cpuclk                  
    x_ct_ifu_icache_predecd_array0.ifu_icache_data_array0_wen_b     := ifu_icache_data_array0_wen_b    
    x_ct_ifu_icache_predecd_array0.ifu_icache_index                 := ifu_icache_index                
    x_ct_ifu_icache_predecd_array0.ifu_icache_predecd_array0_cen_b  := ifu_icache_predecd_array0_cen_b 
    x_ct_ifu_icache_predecd_array0.ifu_icache_predecd_array0_clk_en := ifu_icache_predecd_array0_clk_en
    x_ct_ifu_icache_predecd_array0.ifu_icache_predecd_array0_din    := ifu_icache_predecd_array0_din   
    x_ct_ifu_icache_predecd_array0.ifu_icache_predecd_array0_wen_b  := ifu_icache_predecd_array0_wen_b 
    x_ct_ifu_icache_predecd_array0.pad_yy_icg_scan_en               := io.pad_yy_icg_scan_en              

    icache_ifu_predecd_array0_dout      := x_ct_ifu_icache_predecd_array0.icache_ifu_predecd_array0_dout

  val x_ct_ifu_icache_predecd_array1 = Module(new ct_ifu_icache_predecd_array1()).io
    x_ct_ifu_icache_predecd_array1.cp0_ifu_icg_en                   := io.cp0_ifu_icg_en                  
    x_ct_ifu_icache_predecd_array1.cp0_yy_clk_en                    := io.cp0_yy_clk_en                   
    x_ct_ifu_icache_predecd_array1.forever_cpuclk                   := io.forever_cpuclk                  
    x_ct_ifu_icache_predecd_array1.ifu_icache_data_array1_wen_b     := ifu_icache_data_array1_wen_b    
    x_ct_ifu_icache_predecd_array1.ifu_icache_index                 := ifu_icache_index                
    x_ct_ifu_icache_predecd_array1.ifu_icache_predecd_array1_cen_b  := ifu_icache_predecd_array1_cen_b 
    x_ct_ifu_icache_predecd_array1.ifu_icache_predecd_array1_clk_en := ifu_icache_predecd_array1_clk_en
    x_ct_ifu_icache_predecd_array1.ifu_icache_predecd_array1_din    := ifu_icache_predecd_array1_din   
    x_ct_ifu_icache_predecd_array1.ifu_icache_predecd_array1_wen_b  := ifu_icache_predecd_array1_wen_b 
    x_ct_ifu_icache_predecd_array1.pad_yy_icg_scan_en               := io.pad_yy_icg_scan_en              

    icache_ifu_predecd_array1_dout      := x_ct_ifu_icache_predecd_array1.icache_ifu_predecd_array1_dout
}