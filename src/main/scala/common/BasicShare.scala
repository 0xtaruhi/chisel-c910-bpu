package common

import chisel3._
import chisel3.util._
import chisel3.experimental._
//import c910.common._
import bpu._


trait cp0_arry_io extends Bundle{
  val          cp0_ifu_btb_en                = Input(Bool())
  val          cp0_ifu_icg_en                = Input(Bool())
  val          cp0_ifu_l0btb_en              = Input(Bool())
  val          cp0_yy_clk_en                 = Input(Bool())
  val          cpurst_b                      = Input(Bool())
  val          forever_cpuclk                = Input(Bool())
}

trait addrgen_arry_io extends Bundle{
  //val          addrgen_l0_btb_update_entry   = Input(UInt(16.W))
  val          addrgen_l0_btb_update_vld     = Input(Bool())
  val          addrgen_l0_btb_update_vld_bit = Input(Bool())
  //val          addrgen_l0_btb_wen            = Input(UInt(4.W))
}

trait ibdp_arry_io extends Bundle{
  val          ibdp_l0_btb_fifo_update_vld   = Input(Bool())
  val          ibdp_l0_btb_update_cnt_bit    = Input(Bool())
  //val          ibdp_l0_btb_update_data       = Input(UInt(37.W))
  //val          ibdp_l0_btb_update_entry      = Input(UInt(16.W))
  val          ibdp_l0_btb_update_ras_bit    = Input(Bool())
  val          ibdp_l0_btb_update_vld        = Input(Bool())
  val          ibdp_l0_btb_update_vld_bit    = Input(Bool())
  //val          ibdp_l0_btb_wen               = Input(UInt(4.W))
}

trait ctrl_arry_io extends Bundle{
  val          ifctrl_l0_btb_inv             = Input(Bool())
  val          ifctrl_l0_btb_stall           = Input(Bool())
  val          ipctrl_l0_btb_chgflw_vld      = Input(Bool())
  val          ipctrl_l0_btb_ip_vld          = Input(Bool())
  val          ipctrl_l0_btb_wait_next       = Input(Bool())
}

trait pcgen_arry_io extends Bundle{
  val          pcgen_l0_btb_chgflw_mask      = Input(Bool())
  //val          pcgen_l0_btb_chgflw_pc        = Input(UInt(15.W))
  val          pcgen_l0_btb_chgflw_vld       = Input(Bool())
  //val          pcgen_l0_btb_if_pc            = Input(UInt(39.W))
}

trait ras_arry_io extends Bundle{
  //val          ras_l0_btb_pc                 = Input(UInt(39.W))
  //val          ras_l0_btb_push_pc            = Input(UInt(39.W))
  val          ras_l0_btb_ras_push           = Input(Bool())
}

trait if_out_io extends Bundle{
  //val l0_btb_ifctrl_chgflw_pc      = Output(UInt(39.W))
  //val l0_btb_ifctrl_chgflw_way_pred= Output(UInt(2.W))
  val l0_btb_ifctrl_chglfw_vld     = Output(Bool())
  //val l0_btb_ifdp_chgflw_pc        = Output(UInt(39.W))
  //val l0_btb_ifdp_chgflw_way_pred  = Output(UInt(2.W))
  val l0_btb_ifdp_counter          = Output(Bool())
  //val l0_btb_ifdp_entry_hit        = Output(UInt(16.W))
  val l0_btb_ifdp_hit              = Output(Bool())
  val l0_btb_ifdp_ras              = Output(Bool())
}