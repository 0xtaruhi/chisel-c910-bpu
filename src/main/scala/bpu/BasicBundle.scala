package c910.bpu

import chisel3._
import chisel3.util._

class Cp0IfuCtrlBundle extends Bundle {
  val ifu_icg_en = Bool()
  val ifu_bht_en = Bool()
  val yy_clk_en  = Bool()
}

trait HasL0BhtEn {
  val l0_bht_en = Bool()
}

trait HasBhtEn {
  val bht_en = Bool()
}

trait HasGatedClkEn {
  val gatedclk_en = Bool()
}

trait HasConBrVld {
  val con_br_vld = Bool()
}

trait ConBr extends Bundle {
  val con_br_taken = Bool()
}
