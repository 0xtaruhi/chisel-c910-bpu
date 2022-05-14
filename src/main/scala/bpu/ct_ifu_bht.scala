package c910.bpu

import chisel3._
import chisel3.util._
import chisel3.experimental._
import c910.common._
import c910.common.Constants._
import c910.common.Config._

class IpCtrlBhtBundle extends Bundle with HasGatedClkEn with ConBr with HasConBrVld {
  val more_br = Bool()
  val vld     = Bool()
  val con_br_gateclk_en = Bool()
}

class IfCtrlBhtBundle extends Bundle {
  val inv      = Bool()
  val pipedown = Bool()
  val stall    = Bool() 
}

class LbufBhtBundle extends Bundle with ConBr with HasConBrVld {
  val active_state = Bool()
}

class IuIfuBhtBundle extends Bundle {
  val bht_check_vld    = Bool()
  val bht_condbr_taken = Bool()
  val bht_pred         = Bool()
  val chgflw_vld       = Bool()
  val chk_idx          = UInt(25.W)
  val cur_pc           = UInt(39.W)
}

class PcgenBhtBundle extends Bundle {
  val chgflw = Bool()
  val chgflw_short = Bool()
  val ifpc = UInt(7.W)
  val pcindex = UInt(10.W)
  val seq_read = Bool()
}

class DualCondBr extends Bundle {
  val condbr = Bool()
  val condbr_taken = Bool()
}

class RtuIfuBundle extends Bundle {
  val flush = Bool()
  val retire = Vec(3, new DualCondBr)
}

class BhtIfCtrlBundle extends Bundle {
  val inv_done = Bool()
  val inv_on   = Bool()
}

class BhtIndBtbBundle extends Bundle {
  val rtu_ghr = UInt(8.W)
  val vghr    = UInt(8.W)
}

class BhtIpdpBundle extends Bundle {
  val pre_array_data_ntake = UInt(WORD_WIDTH.W)
  val pre_array_data_taken = UInt(HWORD_WIDTH.W)
  val pre_offset_onehot    = UInt(16.W)
  val sel_array_result     = UInt(2.W)
  val vghr                 = UInt(22.W)
}

class BhtLbufBundle extends Bundle {
  val pre_ntaken_result = Bool()
  val pre_taken_resutl  = Bool()
}

class ct_ifu_bht(val entryNum: Int) extends Module {
  require(entryNum > 0)
  val io = IO(new Bundle {
    // Input
    val cp0                = Input(new Cp0IfuCtrlBundle)
    val cpurst_b           = Input(Bool())
    val forever_cpuclk     = Input(Clock())
    val ifctrl_bht         = Input(new IfCtrlBhtBundle)
    val ipctrl_bht         = Input(new IpCtrlBhtBundle)
    val ipdp_bht_h0_con_br = Input(Bool())
    val ipdp_bht_vpc       = Input(Bool())
    val iu_ifu             = Input(new IuIfuBundle)
    val lbuf_bht           = Input(new LbufBhtBundle)
    val pad_yy_icg_scan_en = Input(Bool())
    val pc_gen_chgflw      = Input(Bool())
    val pcgen_bht          = Input(new PcgenBhtBundle)
    val rtu_ifu            = Input(new RtuIfuBundle)
    // Output
    val bht_ifctrl         = Output(new BhtIfCtrlBundle)
    val bht_ind_btb        = Output(new BhtIndBtbBundle)
    val bht_ipdp           = Output(new BhtIpdpBundle)
    val bht_lbuf           = Output(new BhtLbufBundle)
  })
  
  private def genLocalGatedClk(local_en: Bool): Clock = {
    val _gated_clk_cell = Module(new gated_clk_cell)
    val clk_out = _gated_clk_cell.io.clk_out
    _gated_clk_cell.io.clk_in      := io.forever_cpuclk
    _gated_clk_cell.io.external_en := false.B
    _gated_clk_cell.io.global_en   := io.cp0.yy_clk_en
    _gated_clk_cell.io.local_en    := local_en
    _gated_clk_cell.io.module_en   := io.cp0.ifu_icg_en
    _gated_clk_cell.io.pad_yy_icg_scan_en := io.pad_yy_icg_scan_en
    clk_out
  }

  def asyncReg[T <: Data](clk: Clock, init: T): T = {
    val _reg = withClockAndReset(clk, (~io.cp0.reset).asAsyncReset) { RegInit(init) }
    _reg
  }

  val after_bju_mispred = Wire(Bool())
  val after_inv_reg = Wire(Bool())
  val after_rtu_ifu_flush = Wire(Bool())
  val bht_flop_clk = Wire(Clock())
  val bht_flop_clk_en = Wire(Bool())
  val bht_ghr_updt_clk = Wire(Clock())
  val bht_ghr_updt_clk_en = Wire(Bool())
  val bht_inv_cnt_clk = Wire(Clock())
  val bht_inv_cnt_clk_en = Wire(Bool())
  val bht_inv_on_reg    = asyncReg(bht_inv_cnt_clk, false.B)
  val bht_inv_on_reg_ff = Wire(Bool())
  val bht_inval_cnt = Wire(UInt(10.W))
  val bht_inval_cnt_pre = asyncReg(bht_inv_cnt_clk, 0.U(10.W))
  val bht_pipe_clk = Wire(Clock())
  val bht_pipe_clk_en = Wire(Bool())
  val bht_pre = new Bundle {
    val ntaken_data = UInt(32.W)
    val taken_data  = UInt(32.W)
    val data_out    = UInt(64.W)
  }
  val bht_pred = new Bundle {
    val array_cen_b = Wire(Bool())
    val array_din   = Wire(UInt(64.W))
    val array_gwen  = Wire(Bool())
    val array_rd = Wire(Bool())
    val array_wen_b = Wire(UInt(32.W))
    val array_wr = Wire(Bool())
    val bwen     = Wire(UInt(64.W))
    val array_index = Wire(UInt(10.W))
    val array_index_flop = asyncReg(io.forever_cpuclk, 0.U(10.W))
    val array_rd_index = Wire(UInt(10.W))
  }
  val bht_sel = new Bundle {
    val array_cen_b = Wire(Bool())
    val array_clk_en = Wire(Bool())
    val array_din = Wire(UInt(16.W))
    val array_gwen = Wire(Bool())
    val array_rd = Wire(Bool())
    val array_index       = Wire(UInt(10.W))
    val array_index_flop = asyncReg(io.forever_cpuclk, 0.U(10.W))
    val array_wen_b = Wire(UInt(8.W))
    val array_wr = Wire(Bool())
    val bwen = Wire(UInt(16.W))
    val data = Wire(UInt(16.W))
    val data_out = Wire(UInt(16.W))
    val data_reg = asyncReg(sel_reg_clk, 0.U(16.W))
  }

  val bht_wr_buf = new Bundle {
    val create_vld = Wire(Bool())
    val not_empty = Wire(Bool())
    val pred_updt = new Bundle {
      val index = Wire(UInt(10.W))
      val sel_b = Wire(UInt(32.W))
      val value = Wire(UInt(64.W))
    }
    val retire_vld = Wire(Bool())
    val sel_updt = new Bundle {
      val index = Wire(UInt(7.W))
      val sel_b = Wire(UInt(8.W))
      val value = Wire(Uint(16.W))
    }
    val updt_vld = Wire(Bool())
    val updt_vld_for_gateclk = Wire(Bool())
    val pred_updt_sel_b = Wire(UInt(32.W))
    val sel_updt_sel_b   = Wire(UInt(8.W))
  }
  val bju = new Bundle {
    val check_updt_vld = Wire(Bool())
    val ghr = Wire(UInt(22.W))
    val mispred = Wire(Bool())
    val pred_rst = Wire(UInt(2.W))
    val sel_rst = Wire(UInt(2.W))
  }
  val buf = new Bundle {
    val condbr_taken = Wire(Bool())
    val cur_pc = Wire(UInt(10.W))
    val full = Wire(Bool())
    val ghr = Wire(UInt(22.W))
    val pred_rst = Wire(UInt(2.W))
    val sel_rst = Wire(UInt(2.W))
  }
  val create_ptr = asyncReg(wr_buf_clk, "b0001".U(4.W))
  val cur = new Bundle {
    val condbr_taken = Wire(Bool())
    val cur_pc       = Wire(UInt(10.W))
    val cur_ghr      = Wire(UInt(22.W))
    val pred_rst     = Wire(UInt(2.W))
    val sel_rst      = Wire(UInt(2.W))
  }
  class entryInfo extends Bundle {
    val data = Wire(UInt(37.W))
    val sel_updt_data = Wire(UInt(2.W))
    val vld = asyncReg(wr_buf_clk, false.B)
  }
  val entries = Vec(4, new entryInfo)
  val entry = new entryInfo
  val entry_create = Wire(UInt(4.W))
  val entry_retire = Wire(UInt(4.W))
  val entry_updt_data = Wire(UInt(37.W))
  val ghr_updt_vld = Wire(Bool())
  val if_pc_onehot = Wire(UInt(8.W))
  val ip_vld = Wire(Bool())
  val lbuf_pre = new Bundle {
    val ntaken_reg = Wire(UInt(32.W))
    val taken_reg  = Wire(UInt(32.W))
  }
  val memory_sel_array_result = Wire(UInt(2.W))
  val pre_array_pipe = new Bundle {
    val ntaken_data = Wire(UInt(32.W))
    val taken_data  = Wire(UInt(32.W))
  }
  val pre_ntaken_reg = Wire(UInt(32.W))
  val pre_offset = new Bundle {
    val ip = Vec(2, UInt(4.W))
    val if = Vec(2, UInt(4.W))
    val onehot_ip = UInt(16.W)
    val onehot_if = UInt(16.W)
  }
  val pre_rd_flop = asyncReg(io.forever_cpuclk, false.B)
  val pre_reg_clk = Wire(Clock())
  val pre_reg_clk_en = Wire(Bool())
  val pre_taken_reg = asyncReg(pre_reg_clk, 0.U(32.W))
  val pre_vghr_offset = Vec(2, UInt(4.W))
  val pred_array_updt_data = Wire(UInt(2.W))
  val pred_array_check_updt_vld = Wire(Bool())
  val retire_ptr = asyncReg(wr_buf_clk, "b0001".U(4.W))
  val rtu_con_br_vld = Wire(Bool())
  val rtughr = new Bundle {
    val pre = Wire(UInt(22.W))
    val reg = asyncReg(bht_ghr_updt_clk, 0.U(22.W))
    val updt_vld = Wire(Bool())
  }
  val sel_reg_clk = Wire(Clock())
  val sel_reg_clk_en = Wire(Bool())
  val sel = new Bundle {
    val array_check_updt_vld = Wire(Bool())
    val array_val = Wire(UInt(2.W))
    val array_val_cur = Wire(UInt(2.W))
    val array_updt_data = Wire(UInt(2.W))
    val array_val_flop = asyncReg(io.forever_cpuclk, 0.U(2.W))
    val rd_flop = asyncReg(io.forever_cpuclk, false.B)
  }
  val vghr = new Bundle {
    val reg = asyncReg(bht_ghr_updt_clk, 0.U(22.W))
    val value = Wire(UInt(22.W))
    val up_updt_vld = Wire(Bool())
    val lbuf_updt_vld = Wire(Bool())
  }
  val wr_buf_clk = Wire(Clock())
  val wr_buf_clk_en = Wire(Bool())
  val wr_buf = new Bundle {
    val hits = Wire(Vec(4, Bool()))
    val pre_hits = Wire(Vec(4, Bool()))
    val sel_hits = Wire(Vec(4, Bool()))
    val sel_array_result = Wire(UInt(2.W))
    val hit = Wire(Bool())
    val rd = Wire(Bool())
  }

  bht_pred.array_wr := bht_inv_on_reg | bht_wr_buf.updt_vld
  bht_pred.array_rd := after_inv_reg | io.ipctrl_bht.con_br_vld & (~io.lbuf_bht.active_state)
                      | io.lbuf_bht.conbr_vld & io.lbuf_bht.active_state
                      | bju.mispred
                      | after_bju_mispred
                      | rtu_ifu.flush
                      | after_rtu_ifu_flush
  bht_pred.array_cen_b := ~(
                          bht_inv_on_reg | after_inv_reg
                          | (bht_wr_buf.updt_vld & ~lbuf_bht.active_state
                            | lbuf_bht.con_br_vld & lbuf_bht.active_state
                            | bju.mispred
                            | after_bju_mispred
                            | rtu_ifu.flush
                            | after_rtu_ifu_flush)
                          & io.cp0.ifu_bht_en)
  bht_pre.array_clk_en := bht_inv_on_reg | bht_wr_buf.updt_vld_for_gateclk | bht_pred.array_rd

  bht_pre.array_clk_en:= bht_inv_on_reg | bht_wr_buf.updt_vld_for_gateclk | bht_pred.array_rd
  // Write Enable
  bht_pred.array_gwen := ~bht_pred.array_wr
  // Write Bit Enable
  bht_pred.array_wen_b := Mux(bht_inv_on_reg, 0.U(32.W),
                              bht_wr_buf.pred_updt_sel_b | Fill(32, ~bht_wr_buf.updt_vld))
  bht_pred.bwen := doubleWidth(bht_pred.array_wen_b)
  // Predict Array Data Input
  bht_pred.array_din := Mux(bht_inv_on_reg, Fill(16, "b0011".U), bht_wr_buf.pred_updt.value)
  // Predict Array Index
  when (bht_inv_on_reg | after_inv_reg) {
    bht_pred.array_index := bht_inval_cnt
  } .elsewhen (bht_pred.array_rd) {
    bht_pred.array_index := bht_pred.array_rd_index
  } .otherwise {
    bht_pred.array_index := bht_wr_buf.pred_updt.index
  }

  bht_pred.array_rd_index := MuxCase(
    Cat(vghr.reg(11, 8), vghr.reg(7, 2) ^ vghr.reg(19, 14)),
    Array(
      io.rtu_ifu.flush  ->
        Cat(rtughr.reg(13, 10), rtughr.reg(9, 4) ^ rtughr.reg(21, 16)),
      bju.mispred && !io.iu_ifu.bht_check_vld ->
        Cat(bju.ghr(13, 10), bju.ghr(9, 4) ^ bju.ghr(21, 16)),
      bju.mispred && io.iu_ifu.bht_check_vld ->
        Cat(bju.ghr(12, 9), bju.ghr(8, 3) ^ bju.ghr(20, 15)),
      after_bju_mispred || after_rtu_ifu_flush ->
        Cat(vghr.reg(12, 9), vghr.reg(8, 3) ^ vghr.reg(20, 15)),
    )
  )

  // Signal Input to BHT Select Array
  bht_sel.array_wr := bht_inv_on_reg | bht_wr_buf.updt_vld
  bht_sel.array_rd := after_inv_reg | io.pcgen_bht.chgflw | io.pcgen_bht.seq_read
  bht_sel.array_cen_b := ~(bht_inv_on_reg
                          | after_inv_reg
                          |(bht_wr_buf.updt_vld
                            | io.pcgen_bht.chgflw
                            | io.pcgen_bht.seq_read)
                          & io.cp0.ifu_bht_en)
  bht_sel.array_clk_en := bht_inv_on_reg | bht_wr_buf.updt_vld_for_gateclk
                          | after_inv_reg
                          | io.pcgen_bht.seq_read | io.pcgen_bht.chgflw_short
  // Write Enable
  bht_sel.array_gwen := ~bht_sel.array_wr
  bht_sel.array_wen_b := Mux(bht_inv_on_reg, 0.U(8.W),
                             bht_wr_buf.sel_updt_sel_b | Fill(8, ~bht_wr_buf.updt_vld))
  bht_sel.bwen := doubleWidth(bht_sel.array_wen_b)
  bht_sel.array_din := Mux(bht_inv_on_reg, 0.U(16.W), bht_wr_buf.updt_vld)

  when (bht_inv_on_reg | after_inv_reg) {
    bht_sel.array_index := bht_inval_cnt
  } .elsewhen (bht_sel.array_rd) {
    bht_sel.array_index := io.pcgen_bht.pcindex
  } .otherwise {
    bht_sel.array_index := bht_wr_buf.sel_updt.index
  }
  bht_ghr_updt_clk := genLocalGatedClk(bht_ghr_updt_clk_en)
  bht_ghr_updt_clk_en := bht_inv_on_reg 
                        | io.cp0.ifu_bht_en & (
                          io.rtu_ifu.flush
                          | rtu_con_br_vld
                          | io.iu_ifu.chgflw_vld
                          | io.ipctrl_bht.con_br_gateclk_en
                          | lbuf_bht.con_br_vld
                        )
  rtughr.updt_vld := io.cp0.ifu_bht_en & rtu_con_br_vld
  rtu_con_br_vld := io.rtu_ifu.retire.map(_ => _.condbr).reduce(_||_)
  when (bht_inv_on_reg) {
    rtughr.reg := 0.U(22.W)
  } .elsewhen (rtughr.updt_vld) {
    rtughr.reg := rtughr.pre
  }
  val retireSize = io.rtu_ifu.retire.length
  val retire2RtughrPre = io.rtu_ifu.retire.filter(_.condbr).map(_.condbr_taken)
  val retire2RtughrPreNum = retire2RtughrPre.length
  rtughr.pre := Cat(rtughr.reg(rtughr.reg.getWidth-retire2RtughrPreNum, 0), Cat(retire2RtughrPre.reverse))
  vghr.reg := MuxCase(vghr.reg, 
    Array(
      bht_inv_on_reg -> 0.U(22.W),
      io.rtu_ifu.flush & io.cp0.ifu_bht_en -> rtughr.reg
      ghr.updt_vld & io.iu_ifu.bht_check_vld -> Cat(bjt.ghr(20, 0), io.iu_ifu.condbr_taken)
      ghr.updt_vld & !io.iu_ifu.bht_check_vld -> bju.ghr
      vghr.lbuf_updt_vld -> Cat(vghr.reg(20, 0), io.lbuf_bht.con_br_taken)
      vghr.ip.updt_vld & !io.lbuf_bht.active_state -> Cat(vghr.reg(20, 0), io.ipctrl_bht.con_br_taken)
    )
  )

  sel_reg_clk_en := sel.rd_flop | bht_inv_on_reg
  when (bht_inv_on_reg) {
    bht_sel.data_reg := 0.U(16.W)
  } .elsewhen (sel.rd_flop) {
    bht_sel.data_reg := bht_sel,data_out
  } .otherwise {
    bht_sel.data_reg := bht_sel.data_reg
  }
  sel.rd_flop := MuxCase(false.B, Array(
    bht_inv_on_reg -> false.B,
    bht_sel.array_rd & ~io.ifctrl_bht.stall -> true.B
  ))

  bht_sel.data := Mux(sel.rd_flop, bht_sel.data_out, bht_sel.data_reg)
  if_pc_onehot := UIntToOH(io,pcgen_bht.ifpc)
  sel.array_val_cur := (doubleWidth(if_pc_onehot) & bht_sel.data).asBools.grouped(2).map(Cat(_)).reduce(_|_)
  memory_sel_array_result := Mux(io.ipctrl_bht.more_br | io.ipdp_bht_h0_con_br, 
                                sel.array_val_flop, sel.array_val_cur)
  sel.array_val := Mux(wr_buf.hit, wr_buf.sel_array_result, memory_sel_array_result)
  sel.array_val_flop := sel.array_val
  bht_pre.taken_data := {
    val _vec = Vec(32, Bool())
    for (i <- 0 until 32 by 2) {
      _vec(i) := bht_pre.data_out(2*i)
      _vec(i+1) := bht_pre.data_out(2*i+1)
    }
    _vec.asUInt
  }
  bht_pre.ntaken_data := {
    val _vec = Vec(32, Bool())
    for (i <- 0 until 32 by 2) {
      _vec(i) := bht_pre.data_out(2*i+2)
      _vec(i+1) := bht_pre.data_out(2*i+3)
    }
    _vec.asUInt
  }
  pre_reg_clk := genLocalGatedClk(pre_reg_clk_en)
  pre_reg_clk_en := pre_rd_flop | bht_inv_on_reg
  when (bht_inv_on_reg) {
    pre_taken_reg := 0.U(32.W)
    pre_ntaken_reg := 0.U(32.W)
  } .elsewhen (pre.rd_flop) {
    pre_taken_reg := bht_pre.taken_data
    pre_ntaken_reg := bht_pre.ntaken_data
  }
 
  // Invalidation of BHT
  when (!(bht_inval_cnt.orR) & bht_inv_on_reg) {
    bht_inval_cnt_pre := bht_inval_cnt_pre - 1.U
    bht_inv_on_reg := false.B
  } .elsewhen (ifctrl_bht.inv) {
    bht_inval_cnt_pre := "b1111111111".U
    bht_inv_on_reg := true.B
  }

  when (bht_inv_on_reg) {
    pre_rd_flop := false.B
  } .elsewhen (bht_pred.array_rd) {
    pre_rd_flop true.B
  } .otherwise { false.B }

  when (pre_rd_flop & ip_vld & ~io.lbuf_bht.active_state) { // TODO: WHY
    pre_array_pipe.taken_data := bht_pre.taken_data
    pre_array_pipe.ntaken_data := bht_pre.ntaken_data
  } .elsewhen (pre_rd_flop & io.lbuf_bht.con_br_vld & io.lbuf_bht.active_state) {
    pre_array_pipe.taken_data := lbuf_bht.taken_data
    pre_array_pipe.ntaken_data := lbuf_bht.ntaken_data 
  } .otherwise {
    pre_array_pipe.taken_data := pre.taken_reg
    pre_array_pipe.ntaken_data := pre.ntaken_reg
  }
  bht_wr_buf.updt_vld_for_gateclk := bju.check_updt_vld | bht_wr_buf.not_empty
  bht_wr_buf.updt_vld = (bju.check_updt_vld | bht_wr_buf.not_empty) 
                        & !(after_inv_reg | ipctrl_bht.con_br_vld | after_bju_mispred
                            | io.rtu_ifu.flush | after_rtu_ifu_flush
                            | io.pcgen_bht.chgflw & ~io.lbuf_bht.active_state
                            | io.pcgen_bht.seq_read)
  bju.check_updt_vld := (pred_array_chcek_updt_vld | sel.array_check_updt_vld) & io.iu_ifu.bht_check_vld
  pred_array_check_updt_vld := !(((bju.pred_rst === 0.U(2.W)) && !io.iu_ifu.bht_condbr_taken)
                                || (bju.pred_rst === 3.U(2.W)) && io.iu_ifu.bht_condbr_taken)
  sel.array_check_updt_vld := !(((bju.sel_rst === 0.U(2.W)) && !io.iu_ifu.bht_condbr_taken)
                                || (bju.sel_rst === 3.U(2.W)) && io.iu_ifu.bht_condbr_taken
                                || (bju.sel_rst(1) === 0.U(1.W) && io.iu_ifu.bht_condbr_taken && !io.iu_ifu.chgflw_vld)
                                || (bju.sel_rst(1) === 1.U(1.W) && !io.iu_ifu.bht_condbr_taken && !io.iu_ifu.chgflw_vld))
  bht_wr_buf.create_vld := bju.check_updt_vld && (bht_pred,array_rd || bht_sel.array_rd || bht_wr_buf.not_empty)  && io.cp0.ifu_bht_en
  bht_wr_buf.retire_vld := bht_wr_buf.not_empty && !(bht_pred.array_rd || bht_sel.array_rd) && io.cp0.ifu_bht_en
  bht_wr_buf.not_empty  := entry.vld
  wr_buf_clk := genLocalGatedClk(wr_buf_clk_en)
  wr_buf_clk_en := bju.check_updt_vld | bht_wr_buf.not_empty | bht_inv_on_reg | io.ifctrl_bht.inv
  when (bht_inv_on_reg) {
    create_ptr := "b0001".U
  } .elsewhen (bht_wr_buf.create_vld && !buf.full) {
    create_ptr := Cat(create_ptr(2, 0), create_ptr(3))
  }
  when (bht_inv_on_reg) {
    retire_ptr := "b0001".U
  } .elsewhen (bht_wr_buf.retire_vld) {
    retire_ptr := Cat(retire_ptr(2, 0), retire_ptr(3))
  }
  entry_create := (Fill(4, bht_wr_buf.create_vld) & ~buf.full) & create_ptr
  entry_retire := (Fill(4, bht_wr_buf.retire_vld) & retire_ptr
  entry_updt_data := Cat(io.iu_ifu.bht_condbr_taken, bju.sel_rst, bju.pred_rst, bju.ghr, io.iu_ifu.cur_pc)
  for (i <- 0 until entries.length) {
    when (bht_inv_on_reg) {
      entries(i).vld := false.B
    } .elsewhen (entry_create(i)) {
      entries(i).vld := true.B
    } .elsewhen (entry_retire(i)) {
      entries(i).vld := false.B
    }
    when (entry_create(i)) {
      entries(i).data := entry_updt_data
    }
  }
  
  when (retire_ptr.orR) {
    entry.vld := Mux1H(retire_ptr, entries.map(_.vld))
    entry.data := Mux1H(retire_ptr, entries.map(_.data))
  } .otherwise {
    entry.vld := false.B
    entry.data := 0.U
  }
  buf.condbr_taken := entry.data(36)
  buf.sel_rst := entry.data(35, 34)
  buf.pred_rst := entry.data(33, 32)
  buf.ghr := entry.data(31, 10)
  buf.cur_pc := entry.data(9, 0)
  buf.full := Mux(create_ptr.orR, Mux1H(create_ptr, entries.map(_.vld)), false.B)
  when (entry.vld) {
    cur.condbr_taken := buf.condbr_taken
    cur.sel_rst := buf.sel_rst
    cur.pred_rst := buf.pred_rst
    cur.ghr := buf.ghr
    cur.cur_pc := buf.cur_pc
  } .otherwise {
    cur.condbr_taken := io.iu_ifu.bht_condbr_taken
    cur.sel_rst := bju.sel_rst
    cur.pred_rst := bju.pred_rst
    cur.ghr := bju.ghr
    cur.cur_pc := io.iu_ifu.cur_pc
  }

  switch (Cat(cur.pred_rst, cur.condbr_taken)) {
    is (0.U) { pred.array_updt_data := "b00".U }
    is (1.U) { pred.array_updt_data := "b01".U }
    is (2.U) { pred.array_updt_data := "b00".U }
    is (3.U) { pred.array_updt_data := "b10".U }
    is (4.U) { pred.array_updt_data := "b01".U }
    is (5.U) { pred.array_updt_data := "b11".U }
    is (6.U) { pred.array_updt_data := "b10".U }
    is (7.U) { pred.array_updt_data := "b11".U }
  }

  
  // BHT bypass way
  bht_pred.array_index_flop := Mux(bht_pred.array_rd, 
                                  bht_pred_array_index, bht_pred.array_index_flop)
  bht_sel.array_index_flop  := Mux(bht_sel.array_rd & (~ifctrl_bht.stall), 
                                  io.pcgen_bht.pcindex, bht_sel.array_index_flop)

  // bht_pred.array_wr := bht_inv_on_reg & 

  bht_ifctrl.inv_done := ~bht_inv_on_reg
  bht_ifctrl.inv_on   := bht_inv_on_reg

  when (ipctrl_bht.con_br_vld) {
    pre_offset.onehot_if := UIntToOH(pre_offset.ip(0))
    pre_offset.onehot_ip := UIntToOH(pre_offset.if(0))
  } .otherwise {
    pre_offset.onehot_if := UIntToOH(pre_offset.ip(1))
    pre_offset.onehot_ip := UIntToOH(pre_offset.if(1))
  } 
}

object ct_ifu_bht {
  def doubleWidth(x: UInt): UInt = {
    Cat(x.asBools.map(Fill(2, _)))
  }
}