//package c910.bpu
//package  bpu
package common
import chisel3._
import chisel3.util._
import chisel3.experimental._
//import c910.common._
import  common._
class ct_ifu_l0_btb_entry_io(val PC_WIDTH :Int=40,val ENTRY_SIZE :Int =16,val ENTRY_TARGET :Int =20)  extends
  Bundle with cp0_arry_io {
//  val cp0_ifu_btb_en      = Input(Bool())
//  val cp0_ifu_icg_en      = Input(Bool())
//  val cp0_ifu_l0btb_en    = Input(Bool())
//  val cp0_yy_clk_en       = Input(Bool())
//  val cpurst_b            = Input(Bool())
  val entry_inv           = Input(Bool())
  val entry_update        = Input(Bool())
  val entry_update_cnt    = Input(Bool())
  val entry_update_data   = Input(UInt((PC_WIDTH-3).W))
  val entry_update_ras    = Input(Bool())
  val entry_update_vld    = Input(Bool())
  val entry_wen           = Input(UInt(4.W))
  //val forever_cpuclk      = Input(Clock())
  val pad_yy_icg_scan_en  = Input(Bool())
  val entry_cnt           = Output(Bool())
  val entry_ras           = Output(Bool())
  val entry_tag           = Output(UInt((ENTRY_SIZE-1).W))
  val entry_target        = Output(UInt(ENTRY_TARGET.W))
  val entry_vld           = Output(Bool())
  val entry_way_pred      = Output(UInt(2.W))
}

class my_ct_ifu_l0_btb_entry(val PC_WIDTH :Int=40,val ENTRY_SIZE :Int =16,val ENTRY_TARGET :Int =20) extends RawModule {
  val io = IO(new ct_ifu_l0_btb_entry_io(PC_WIDTH=PC_WIDTH,ENTRY_SIZE=ENTRY_SIZE,ENTRY_TARGET = ENTRY_TARGET))

  // Gated Clock
  val gatedclk = Module(new gated_clk_cell())
  val entry_clk = gatedclk.io.clk_out
  val entry_update_en = io.entry_update & io.cp0_ifu_btb_en & io.cp0_ifu_l0btb_en
  val entry_clk_en = entry_update_en
  gatedclk.io.clk_in      := io.forever_cpuclk.asClock
  gatedclk.io.external_en := false.B
  gatedclk.io.global_en   := io.cp0_yy_clk_en
  gatedclk.io.local_en    := entry_clk_en
  gatedclk.io.module_en   := io.cp0_ifu_icg_en
  gatedclk.io.pad_yy_icg_scan_en := io.pad_yy_icg_scan_en

  withClockAndReset(entry_clk, (!io.cpurst_b).asAsyncReset) {
    // pack the tag, way_pred, target into a single register
    val entry_data     = Wire(UInt((PC_WIDTH-3).W))
    io.entry_tag      := entry_data(36, 22)
    io.entry_way_pred := entry_data(21, 20)
    io.entry_target   := entry_data(19, 0)

    // pack four entries
    val entry_signals   = Seq(entry_data, io.entry_ras, io.entry_cnt, io.entry_vld)
    val update_signals_map = Map(
      entry_data      -> io.entry_update_data,
      io.entry_ras    -> io.entry_update_ras,
      io.entry_cnt    -> io.entry_update_cnt,
      io.entry_vld    -> io.entry_update_vld
    )
    // Reset
    val rst_signals_map = Map(
      entry_data      -> 0.U,
      io.entry_ras    -> false.B,
      io.entry_cnt    -> false.B,
      io.entry_vld    -> false.B
    )
    entry_signals.zipWithIndex.foreach { case (s, i) =>
      val rst_signal = rst_signals_map(s)
      val update_signal = update_signals_map(s)
      val entry_signal = RegInit(rst_signal)
      when (entry_update_en & io.entry_wen(i)) {
        entry_signal := update_signal
      }
      s := entry_signal
    }
  }
}
