import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}

class RegFileUnitTester(rf: RegFile) extends PeekPokeTester(rf) {
  reset()

  poke(rf.io.rs1, 0xF)
  expect(rf.io.r1, 0x0)

  poke(rf.io.rd, 0xF)
  poke(rf.io.wdata, 0x12345678)
  poke(rf.io.wen, true)
  step(1)
  poke(rf.io.rs1, 0xF)
  step(1)
  expect(rf.io.r1, 0x12345678)

  poke(rf.io.rd, 0)
  poke(rf.io.wdata, 0x12345678)
  poke(rf.io.wen, true)
  step(1)
  poke(rf.io.rs1, 0)
  step(1)
  expect(rf.io.r1, 0x0)
}


class RegFileTester extends ChiselFlatSpec {
  "RegFile" should s"work" in {
    Driver.execute(Array("--fint-write-vcd"), () => new RegFile()) {
      rf => new RegFileUnitTester(rf)
    } should be(true)
  }
}
