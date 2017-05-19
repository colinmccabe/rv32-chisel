import chisel3.{Bool, Input, Output, UInt}
import chisel3.core.Bundle

class InstMemIO extends Bundle {
  val read = Input(Bool())
  val addr = Input(UInt(Constants.INST_MEM_WIDTH))
  val data = Output(UInt(Constants.WIDTH))
  val err = Output(Bool())
}
