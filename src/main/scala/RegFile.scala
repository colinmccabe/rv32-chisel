import Constants._
import chisel3.{Bundle, _}

class RegFileIO extends Bundle {
  val rs1 = Input(UInt(5.W))
  val rs2 = Input(UInt(5.W))
  val rd = Input(UInt(5.W))
  val wdata = Input(UInt(WIDTH))
  val write = Input(Bool())

  val r1 = Output(UInt(WIDTH))
  val r2 = Output(UInt(WIDTH))
  val x_peek = Output(UInt(WIDTH))
}

class RegFile extends Module {
  val io = IO(new RegFileIO)

  val x: Vec[UInt] = RegInit(Vec(Seq.fill(32)(0.U(WIDTH))))

  // Read r1
  io.r1 := 0.U
  for (i <- 1 until 32) {
    when(io.rs1 === i.U) {
      io.r1 := x(i)
    }
  }

  // Read r2
  io.r2 := 0.U
  for (i <- 1 until 32) {
    when(io.rs2 === i.U) {
      io.r2 := x(i)
    }
  }

  // Write rd
  for (i <- 1 until 32) {
    when(io.rd === i.U && io.write) {
      x(i) := io.wdata
    }
  }

  io.x_peek := x(31)
}
