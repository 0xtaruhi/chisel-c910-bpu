//package c910.common
package common
import chisel3._
import chisel3.util._

class gated_clk_cell extends BlackBox {
  val io = IO(new Bundle {
    val clk_in                = Input(Clock())
    val global_en             = Input(Bool())
    val module_en             = Input(Bool())
    val local_en              = Input(Bool())
    val external_en           = Input(Bool())
    val pad_yy_icg_scan_en    = Input(Bool())
    val clk_out               = Output(Clock())
  })
}
