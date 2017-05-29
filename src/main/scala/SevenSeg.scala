import chisel3._
import chisel3.util._

class SevenSeg(div: Int) extends Module {
  val io = IO(new Bundle() {
    val n = Input(UInt(16.W))
    val anodes = Output(UInt(4.W))
    val cathodes = Output(UInt(8.W))
  })

  val COUNTER_WIDTH = div + 2

  val anode_counter = RegInit(0.U(COUNTER_WIDTH.W))
  anode_counter := anode_counter + 1.U

  val counter_top_bits = anode_counter(COUNTER_WIDTH - 1, COUNTER_WIDTH - 2)
  val nibble = Wire(UInt(4.W))

  // Anode mux
  nibble := 0.U
  io.anodes := 0.U(4.W)
  switch(counter_top_bits) {
    is(0.U) {
      nibble := io.n(3, 0)
      io.anodes := "b1110".U
    }
    is(1.U) {
      nibble := io.n(7, 4)
      io.anodes := "b1101".U
    }
    is(2.U) {
      nibble := io.n(11, 8)
      io.anodes := "b1011".U
    }
    is(3.U) {
      nibble := io.n(15, 12)
      io.anodes := "b0111".U
    }
  }

  val cathodeValues = Array(
    "b00000011".U,
    "b10011111".U,
    "b00100101".U,
    "b00001101".U,
    "b10011001".U,
    "b01001001".U,
    "b01000001".U,
    "b00011111".U,
    "b00000001".U,
    "b00001001".U,
    "b00010000".U,
    "b11000000".U,
    "b01100010".U,
    "b10000100".U,
    "b01100000".U,
    "b01110000".U)

  io.cathodes := 0.U(8.W)
  for (i <- 0 until 16) {
    when(nibble === i.U) {
      io.cathodes := cathodeValues(i)
    }
  }
}
