import Constants._
import chisel3._

class Computer extends Module {
  val io = IO(new Bundle {
    val instMem_read = Output(Bool())
    val instMem_addr = Output(UInt(INST_MEM_WIDTH))
    val instMem_data = Input(UInt(WIDTH))

    val dataMem_read = Output(Bool())
    val dataMem_write = Output(Bool())
    val dataMem_addr = Output(UInt(DATA_MEM_WIDTH))
    val dataMem_rdata = Input(UInt(WIDTH))
    val dataMem_wdata = Output(UInt(WIDTH))

    val sevenseg_an = Output(UInt(4.W))
    val sevenseg_cath = Output(UInt(8.W))
  })

  val cpu = Module(new Cpu(pcInit = 0x0))
  val rf = Module(new RegFile)
  val alu = Module(new Alu)
  val sevenSeg = Module(new SevenSeg(div = 14))

  rf.io <> cpu.io.rf
  alu.io <> cpu.io.alu

  io.instMem_read := cpu.io.instMem.read
  io.instMem_addr := cpu.io.instMem.addr
  cpu.io.instMem.data := io.instMem_data
  cpu.io.instMem.err := false.B

  io.dataMem_read := cpu.io.dataMem.read
  io.dataMem_write := cpu.io.dataMem.write
  io.dataMem_addr := cpu.io.dataMem.addr
  cpu.io.dataMem.rdata := io.dataMem_rdata
  io.dataMem_wdata := cpu.io.dataMem.wdata
  cpu.io.dataMem.err := false.B

  sevenSeg.io.n := rf.io.x_peek
  io.sevenseg_an := sevenSeg.io.anodes
  io.sevenseg_cath := sevenSeg.io.cathodes
}
