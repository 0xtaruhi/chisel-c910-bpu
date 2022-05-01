package c910.common

import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}
import firrtl.options.TargetDirAnnotation
import c910._

object l0btb extends App {
  (new chisel3.stage.ChiselStage).execute(
     Array("-X", "verilog"),
      Seq(
          ChiselGeneratorAnnotation(() => new bpu.ct_ifu_l0_btb),
          TargetDirAnnotation("./gen_rtl/")
      )
  )
}
