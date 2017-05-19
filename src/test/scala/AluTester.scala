import Constants._
import chisel3._
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}

class AluUnitTester(alu: Alu) extends PeekPokeTester(alu) {
  val ITERATIONS = 100

  test_IMM(FUNCT3_ADD_SUB, (x, y) => x + y)
  test_IMM(FUNCT3_SLT, (x, y) => if (x < y) 1 else 0)
  test_IMM(FUNCT3_SLTU, (x, y) => if (sToU_32(x) < sToU_32(y)) 1 else 0)
  test_IMM(FUNCT3_XOR, (x, y) => x ^ y)
  test_IMM(FUNCT3_OR, (x, y) => x | y)
  test_IMM(FUNCT3_AND, (x, y) => x & y)

  test_IMM_shift(FUNCT3_SLL, FUNCT7_0, (x, y) => x << y)
  test_IMM_shift(FUNCT3_SR, FUNCT7_0, (x, y) => x >>> y)
  test_IMM_shift(FUNCT3_SR, FUNCT7_1, (x, y) => x >> y)

  test_RR(FUNCT3_ADD_SUB, FUNCT7_0, (x, y) => x + y)
  test_RR(FUNCT3_SLL, FUNCT7_0, (x, y) => x << (y & 0x1F))
  test_RR(FUNCT3_SLT, FUNCT7_0, (x, y) => if (x < y) 1 else 0)
  test_RR(FUNCT3_SLTU, FUNCT7_0, (x, y) => if (sToU_32(x) < sToU_32(y)) 1 else 0)
  test_RR(FUNCT3_SR, FUNCT7_0, (x, y) => x >>> (y & 0x1F))
  test_RR(FUNCT3_XOR, FUNCT7_0, (x, y) => x ^ y)
  test_RR(FUNCT3_SR, FUNCT7_0, (x, y) => x >>> y)
  test_RR(FUNCT3_OR, FUNCT7_0, (x, y) => x | y)
  test_RR(FUNCT3_AND, FUNCT7_0, (x, y) => x & y)
  test_RR(FUNCT3_ADD_SUB, FUNCT7_1, (x, y) => x - y)
  test_RR(FUNCT3_SR, FUNCT7_1, (x, y) => x >> y)

  def test_IMM(funct3: UInt, f: (Int, Int) => Int) {
    for (_ <- 0 until ITERATIONS) {
      val x = rnd.nextInt()
      val imm = rnd.nextInt(4096) - 2048

      poke(alu.io.op, OP_IMM)
      poke(alu.io.funct3, funct3)
      poke(alu.io.x, sToU_32(x))
      poke(alu.io.y, sToU_32(imm))

      expect(alu.io.o, sToU_32(f(x, imm)))
    }
  }

  def test_IMM_shift(funct3: UInt, funct7: UInt, f: (Int, Int) => Int) {
    for (_ <- 0 until ITERATIONS) {
      val x = rnd.nextInt()
      val shamt = rnd.nextInt(32)

      poke(alu.io.op, OP_IMM)
      poke(alu.io.funct3, funct3)
      poke(alu.io.funct7, funct7)
      poke(alu.io.x, sToU_32(x))
      poke(alu.io.y, shamt)

      expect(alu.io.o, sToU_32(f(x, shamt)))
    }
  }

  def test_RR(funct3: UInt, funct7: UInt, f: (Int, Int) => Int) {
    for (_ <- 0 until ITERATIONS) {
      val x = rnd.nextInt()
      val y = rnd.nextInt()

      poke(alu.io.op, OP_RR)
      poke(alu.io.funct3, funct3)
      poke(alu.io.funct7, funct7)
      poke(alu.io.x, sToU_32(x))
      poke(alu.io.y, sToU_32(y))

      expect(alu.io.o, sToU_32(f(x, y)))
    }
  }

  def sToU_32(s: Long) = sToU(32, s)

  def sToU(bits: Int, s: Long) = {
    val u_max = Math.pow(2, bits).toLong - 1
    val s_max = Math.pow(2, bits - 1).toLong - 1
    val s_min = -Math.pow(2, bits - 1).toLong

    if (s < s_min || s > s_max)
      throw new IllegalArgumentException(s"Signed value out of range: ${s.toHexString}")

    if (s >= 0)
      s
    else
      (u_max + 1) + s
  }
}


class AluTester extends ChiselFlatSpec {
  "Alu" should s"work" in {
    Driver.execute(Array("--fint-write-vcd"), () => new Alu) {
      c => new AluUnitTester(c)
    } should be(true)
  }
}
