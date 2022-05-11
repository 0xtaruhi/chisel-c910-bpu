//package c910.common
package common
import chisel3._
import chisel3.util._

class ct_ifu_l0_btb_entry extends BlackBox {
  val io = IO(new Bundle {
    val cp0_ifu_btb_en      = Input(Bool())
    val cp0_ifu_icg_en      = Input(Bool())
    val cp0_ifu_l0btb_en    = Input(Bool())
    val cp0_yy_clk_en       = Input(Bool())
    val cpurst_b            = Input(Bool())
    val entry_inv           = Input(Bool())
    val entry_update        = Input(Bool())
    val entry_update_cnt    = Input(Bool())
    val entry_update_data   = Input(UInt((37).W))
    val entry_update_ras    = Input(Bool())
    val entry_update_vld    = Input(Bool())
    val entry_wen           = Input(UInt(4.W))
    val forever_cpuclk      = Input(Clock())
    val pad_yy_icg_scan_en  = Input(Bool())
    val entry_cnt           = Output(Bool())
    val entry_ras           = Output(Bool())
    val entry_tag           = Output(UInt((15).W))
    val entry_target        = Output(UInt(20.W))
    val entry_vld           = Output(Bool())
    val entry_way_pred      = Output(UInt(2.W))
  })
}