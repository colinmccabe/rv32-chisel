import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}

class SevenSeg_UnitTester(ss: SevenSeg) extends PeekPokeTester(ss) {
  def testStep(an: Int, cath: Int) {
    expect(ss.io.anodes, an)
    expect(ss.io.cathodes, cath)
    step(1)
  }

  poke(ss.io.n, 0x1234)
  testStep(an = 0xE, cath = 0x99)
  testStep(an = 0xE, cath = 0x99)
  testStep(an = 0xD, cath = 0x0D)
  testStep(an = 0xD, cath = 0x0D)
  testStep(an = 0xB, cath = 0x25)
  testStep(an = 0xB, cath = 0x25)
  testStep(an = 0x7, cath = 0x9F)
  testStep(an = 0x7, cath = 0x9F)
  testStep(an = 0xE, cath = 0x99)
  testStep(an = 0xE, cath = 0x99)
}

class SevenSegTester extends ChiselFlatSpec {
  "SevenSeg" should s"work" in {
    Driver.execute(Array(), () => new SevenSeg(1)) {
      rf => new SevenSeg_UnitTester(rf)
    } should be(true)
  }
}
