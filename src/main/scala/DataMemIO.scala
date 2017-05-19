import chisel3._

class DataMemIO() extends Bundle {
  val read = Input(Bool())
  val write = Input(Bool())
  val addr = Input(UInt(Constants.DATA_MEM_WIDTH))
  val rdata = Output(UInt(Constants.WIDTH))
  val wdata = Input(UInt(Constants.WIDTH))
  val err = Output(Bool())
}
