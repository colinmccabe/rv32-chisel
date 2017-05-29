import Constants._
import TestConstants.SOME_WORD
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}

import scala.util.Random

case class TestVec(pcInitial: Long = Random.nextInt(INST_MEM_WIDTH.get),
                   inst: Long,
                   r1: Long,
                   r2: Long,
                   x: Long,
                   y: Long,
                   rf_wen: Boolean,
                   rf_rd: Int,
                   pcNext: Option[Long] = Option.empty)


class CPU_UnitTester(cpu: Cpu) extends PeekPokeTester(cpu) {
  def exec(vec: TestVec) {
    val pcNext = vec.pcNext.getOrElse((cpu.pcInit + 0x4L) & INST_ADDR_MASK)

    // fetch
    reset()
    expect(cpu.io.instMem.read, true)
    expect(cpu.io.instMem.addr, cpu.pcInit)
    expect(cpu.io.dataMem.read, false)
    expect(cpu.io.dataMem.write, false)
    expect(cpu.io.rf.write, false)
    // decode
    step(1)
    expect(cpu.io.instMem.read, false)
    poke(cpu.io.instMem.data, vec.inst)
    poke(cpu.io.instMem.err, false)
    expect(cpu.io.dataMem.read, false)
    expect(cpu.io.dataMem.write, false)
    expect(cpu.io.rf.write, false)
    poke(cpu.io.rf.r1, vec.r1)
    poke(cpu.io.rf.r2, vec.r2)
    // execute
    step(1)

    expect(cpu.io.instMem.read, false)
    expect(cpu.io.dataMem.read, false)
    expect(cpu.io.dataMem.write, false)
    expect(cpu.io.rf.write, false)
    expect(cpu.io.alu.x, vec.x)
    expect(cpu.io.alu.y, vec.y)
    poke(cpu.io.alu.o, SOME_WORD)
    poke(cpu.io.alu.isValid, true)
    // write
    step(1)
    expect(cpu.io.instMem.read, false)
    expect(cpu.io.dataMem.read, false)
    expect(cpu.io.dataMem.write, false)
    poke(cpu.io.dataMem.err, false)
    expect(cpu.io.rf.write, true)
    expect(cpu.io.rf.rd, vec.rf_rd)
    expect(cpu.io.rf.wdata, SOME_WORD)
    // fetch
    step(1)
    expect(cpu.io.instMem.read, true)
    expect(cpu.io.instMem.addr, pcNext)
    expect(cpu.io.dataMem.read, false)
    expect(cpu.io.dataMem.write, false)
    expect(cpu.io.rf.write, false)
  }
}


class LUI_UnitTester(cpu: Cpu) extends CPU_UnitTester(cpu) {
  val imm_u = rnd.nextInt(1 << 20).toLong << 12L
  val inst = imm_u | 0x0B7L
  exec(TestVec(
    inst = inst,
    r1 = 0,
    r2 = 0,
    x = imm_u,
    y = 0x0,
    rf_wen = true,
    rf_rd = 1))
}


class AUIPC_UnitTester(cpu: Cpu) extends CPU_UnitTester(cpu) {
  exec(TestVec(
    inst = 0xFFFECC17L,
    r1 = 0,
    r2 = 0,
    x = cpu.pcInit,
    y = 0xFFFEC000L,
    rf_wen = true,
    rf_rd = 24))
}


class JAL_UnitTester(cpu: Cpu) extends CPU_UnitTester(cpu) {
  val pcNext = (cpu.pcInit - 8) & INST_ADDR_MASK
  // fetch
  reset()
  expect(cpu.io.instMem.read, true)
  expect(cpu.io.instMem.addr, cpu.pcInit)
  expect(cpu.io.dataMem.read, false)
  expect(cpu.io.dataMem.write, false)
  expect(cpu.io.rf.write, false)
  // decode
  step(1)
  expect(cpu.io.instMem.read, false)
  poke(cpu.io.instMem.data, 0xFF9FF9EFL)
  poke(cpu.io.instMem.err, false)
  expect(cpu.io.dataMem.read, false)
  expect(cpu.io.dataMem.write, false)
  expect(cpu.io.rf.write, false)
  // execute
  step(1)
  expect(cpu.io.instMem.read, false)
  expect(cpu.io.dataMem.read, false)
  expect(cpu.io.dataMem.write, false)
  expect(cpu.io.rf.write, false)
  expect(cpu.io.alu.x, cpu.pcInit)
  expect(cpu.io.alu.y, 0xFFFFFFF8L)
  poke(cpu.io.alu.o, pcNext)
  poke(cpu.io.alu.isValid, true)
  // write
  step(1)
  expect(cpu.io.instMem.read, false)
  expect(cpu.io.dataMem.read, false)
  expect(cpu.io.dataMem.write, false)
  poke(cpu.io.dataMem.err, false)
  expect(cpu.io.rf.write, true)
  expect(cpu.io.rf.rd, 0x13)
  expect(cpu.io.rf.wdata, (cpu.pcInit + 4) & INST_ADDR_MASK)
  // fetch
  step(1)
  expect(cpu.io.instMem.read, true)
  expect(cpu.io.instMem.addr, pcNext)
  expect(cpu.io.dataMem.read, false)
  expect(cpu.io.dataMem.write, false)
  expect(cpu.io.rf.write, false)
}


class JALR_UnitTester(cpu: Cpu) extends CPU_UnitTester(cpu) {
  val pcNext = 0x333
  // fetch
  reset()
  expect(cpu.io.instMem.read, true)
  expect(cpu.io.instMem.addr, cpu.pcInit)
  expect(cpu.io.dataMem.read, false)
  expect(cpu.io.dataMem.write, false)
  expect(cpu.io.rf.write, false)
  // decode
  step(1)
  expect(cpu.io.instMem.read, false)
  poke(cpu.io.instMem.data, 0xFF8603E7L)
  poke(cpu.io.instMem.err, false)
  expect(cpu.io.dataMem.read, false)
  expect(cpu.io.dataMem.write, false)
  expect(cpu.io.rf.write, false)
  expect(cpu.io.rf.rs1, 0x0C)
  poke(cpu.io.rf.r1, 0x33B)
  // execute
  step(1)
  expect(cpu.io.instMem.read, false)
  expect(cpu.io.dataMem.read, false)
  expect(cpu.io.dataMem.write, false)
  expect(cpu.io.rf.write, false)
  expect(cpu.io.alu.x, 0x33B)
  expect(cpu.io.alu.y, 0xFFFFFFF8L)
  poke(cpu.io.alu.o, pcNext)
  poke(cpu.io.alu.isValid, true)
  // write
  step(1)
  expect(cpu.io.instMem.read, false)
  expect(cpu.io.dataMem.read, false)
  expect(cpu.io.dataMem.write, false)
  poke(cpu.io.dataMem.err, false)
  expect(cpu.io.rf.write, true)
  expect(cpu.io.rf.rd, 0x7)
  expect(cpu.io.rf.wdata, (cpu.pcInit + 4) & INST_ADDR_MASK)
  // fetch
  step(1)
  expect(cpu.io.instMem.read, true)
  expect(cpu.io.instMem.addr, pcNext)
  expect(cpu.io.dataMem.read, false)
  expect(cpu.io.dataMem.write, false)
  expect(cpu.io.rf.write, false)
}


class BRANCH_Taken_UnitTester(cpu: Cpu) extends CPU_UnitTester(cpu) {
  val pcNext = (cpu.pcInit - 8) & INST_ADDR_MASK
  // fetch
  reset()
  expect(cpu.io.instMem.read, true)
  expect(cpu.io.instMem.addr, cpu.pcInit)
  expect(cpu.io.dataMem.read, false)
  expect(cpu.io.dataMem.write, false)
  expect(cpu.io.rf.write, false)
  // decode
  step(1)
  expect(cpu.io.instMem.read, false)
  poke(cpu.io.instMem.data, 0xFFFF8CE3L)
  poke(cpu.io.instMem.err, false)
  expect(cpu.io.dataMem.read, false)
  expect(cpu.io.dataMem.write, false)
  expect(cpu.io.rf.write, false)
  expect(cpu.io.rf.rs1, 31)
  expect(cpu.io.rf.rs2, 31)
  poke(cpu.io.rf.r1, 123)
  poke(cpu.io.rf.r2, 456)
  // execute
  step(1)
  expect(cpu.io.instMem.read, false)
  expect(cpu.io.dataMem.read, false)
  expect(cpu.io.dataMem.write, false)
  expect(cpu.io.rf.write, false)
  expect(cpu.io.alu.x, 123)
  expect(cpu.io.alu.y, 456)
  poke(cpu.io.alu.o, 0x1)
  poke(cpu.io.alu.isValid, true)
  // write
  step(1)
  expect(cpu.io.instMem.read, false)
  expect(cpu.io.dataMem.read, false)
  expect(cpu.io.dataMem.write, false)
  poke(cpu.io.dataMem.err, false)
  expect(cpu.io.rf.write, false)
  // fetch
  step(1)
  expect(cpu.io.instMem.read, true)
  expect(cpu.io.instMem.addr, pcNext)
  expect(cpu.io.dataMem.read, false)
  expect(cpu.io.dataMem.write, false)
  expect(cpu.io.rf.write, false)
}

class BRANCH_NotTaken_UnitTester(cpu: Cpu) extends CPU_UnitTester(cpu) {
  val pcNext = (cpu.pcInit + 4) & INST_ADDR_MASK
  // fetch
  reset()
  expect(cpu.io.instMem.read, true)
  expect(cpu.io.instMem.addr, cpu.pcInit)
  expect(cpu.io.dataMem.read, false)
  expect(cpu.io.dataMem.write, false)
  expect(cpu.io.rf.write, false)
  // decode
  step(1)
  expect(cpu.io.instMem.read, false)
  poke(cpu.io.instMem.data, 0xFFFF8CE3L)
  poke(cpu.io.instMem.err, false)
  expect(cpu.io.dataMem.read, false)
  expect(cpu.io.dataMem.write, false)
  expect(cpu.io.rf.write, false)
  expect(cpu.io.rf.rs1, 31)
  expect(cpu.io.rf.rs2, 31)
  poke(cpu.io.rf.r1, 123)
  poke(cpu.io.rf.r2, 456)
  // execute
  step(1)
  expect(cpu.io.instMem.read, false)
  expect(cpu.io.dataMem.read, false)
  expect(cpu.io.dataMem.write, false)
  expect(cpu.io.rf.write, false)
  expect(cpu.io.alu.x, 123)
  expect(cpu.io.alu.y, 456)
  poke(cpu.io.alu.o, 0x0)
  poke(cpu.io.alu.isValid, true)
  // write
  step(1)
  expect(cpu.io.instMem.read, false)
  expect(cpu.io.dataMem.read, false)
  expect(cpu.io.dataMem.write, false)
  poke(cpu.io.dataMem.err, false)
  expect(cpu.io.rf.write, false)
  // fetch
  step(1)
  expect(cpu.io.instMem.read, true)
  expect(cpu.io.instMem.addr, pcNext)
  expect(cpu.io.dataMem.read, false)
  expect(cpu.io.dataMem.write, false)
  expect(cpu.io.rf.write, false)
}

class IMM_UnitTester(cpu: Cpu) extends CPU_UnitTester(cpu) {
  exec(TestVec(
    inst = 0x9ABF8613L,
    r1 = 0x5454,
    r2 = 0,
    x = 0x5454,
    y = 0xFFFFF9ABL,
    rf_wen = true,
    rf_rd = 12))
}


class RR_UnitTester(cpu: Cpu) extends CPU_UnitTester(cpu) {
  exec(TestVec(
    inst = 0x01FF00B3L,
    r1 = 1,
    r2 = 2,
    x = 1,
    y = 2,
    rf_wen = true,
    rf_rd = 1))
}


class LW_UnitTester(cpu: Cpu) extends PeekPokeTester(cpu) {
  val pcNext = (cpu.pcInit + 0x4L) & INST_ADDR_MASK
  // fetch
  reset()
  expect(cpu.io.instMem.read, true)
  expect(cpu.io.instMem.addr, cpu.pcInit)
  expect(cpu.io.dataMem.read, false)
  expect(cpu.io.dataMem.write, false)
  expect(cpu.io.rf.write, false)
  // decode
  step(1)
  expect(cpu.io.instMem.read, false)
  poke(cpu.io.instMem.data, 0xFECE2403L)
  poke(cpu.io.instMem.err, false)
  expect(cpu.io.dataMem.read, false)
  expect(cpu.io.dataMem.write, false)
  expect(cpu.io.rf.write, false)
  expect(cpu.io.rf.rs1, 28)
  poke(cpu.io.rf.r1, 0x35C)
  poke(cpu.io.rf.r2, 0x0)
  // execute
  step(1)
  expect(cpu.io.instMem.read, false)
  expect(cpu.io.dataMem.read, false)
  expect(cpu.io.dataMem.write, false)
  expect(cpu.io.rf.write, false)
  expect(cpu.io.alu.x, 0x35C)
  expect(cpu.io.alu.y, 0xFFFFFFECL)
  poke(cpu.io.alu.o, 0x354)
  poke(cpu.io.alu.isValid, true)
  // mem
  step(1)
  expect(cpu.io.instMem.read, false)
  expect(cpu.io.dataMem.read, true)
  expect(cpu.io.dataMem.write, false)
  expect(cpu.io.dataMem.addr, 0x354)
  poke(cpu.io.dataMem.rdata, SOME_WORD)
  expect(cpu.io.rf.write, false)
  // write
  step(1)
  expect(cpu.io.instMem.read, false)
  expect(cpu.io.dataMem.read, false)
  expect(cpu.io.dataMem.write, false)
  poke(cpu.io.dataMem.err, false)
  expect(cpu.io.rf.write, true)
  expect(cpu.io.rf.rd, 8)
  expect(cpu.io.rf.wdata, SOME_WORD)
  // fetch
  step(1)
  expect(cpu.io.instMem.read, true)
  expect(cpu.io.instMem.addr, pcNext)
  expect(cpu.io.dataMem.read, false)
  expect(cpu.io.dataMem.write, false)
  expect(cpu.io.rf.write, false)
}


class SW_UnitTester(cpu: Cpu) extends PeekPokeTester(cpu) {
  val pcNext = (cpu.pcInit + 0x4L) & INST_ADDR_MASK
  // fetch
  reset()
  expect(cpu.io.instMem.read, true)
  expect(cpu.io.instMem.addr, cpu.pcInit)
  expect(cpu.io.dataMem.read, false)
  expect(cpu.io.dataMem.write, false)
  expect(cpu.io.rf.write, false)
  // decode
  step(1)
  expect(cpu.io.instMem.read, false)
  poke(cpu.io.instMem.data, 0xFF95A623L)
  poke(cpu.io.instMem.err, false)
  expect(cpu.io.dataMem.read, false)
  expect(cpu.io.dataMem.write, false)
  expect(cpu.io.rf.write, false)
  poke(cpu.io.rf.r1, 0x234)
  poke(cpu.io.rf.r2, SOME_WORD)
  // execute
  step(1)
  expect(cpu.io.instMem.read, false)
  expect(cpu.io.dataMem.read, false)
  expect(cpu.io.dataMem.write, false)
  expect(cpu.io.rf.write, false)
  expect(cpu.io.alu.x, 0x234)
  expect(cpu.io.alu.y, 0xFFFFFFECL)
  poke(cpu.io.alu.o, 0x220)
  poke(cpu.io.alu.isValid, true)
  // mem
  step(1)
  expect(cpu.io.instMem.read, false)
  expect(cpu.io.dataMem.read, false)
  expect(cpu.io.dataMem.write, true)
  expect(cpu.io.dataMem.addr, 0x220)
  expect(cpu.io.dataMem.wdata, SOME_WORD)
  expect(cpu.io.rf.write, false)
  // write
  step(1)
  expect(cpu.io.instMem.read, false)
  expect(cpu.io.dataMem.read, false)
  expect(cpu.io.dataMem.write, false)
  poke(cpu.io.dataMem.err, false)
  expect(cpu.io.rf.write, false)
  // fetch
  step(1)
  expect(cpu.io.instMem.read, true)
  expect(cpu.io.instMem.addr, pcNext)
  expect(cpu.io.dataMem.read, false)
  expect(cpu.io.dataMem.write, false)
  expect(cpu.io.rf.write, false)
}


class CpuTester extends ChiselFlatSpec {
  val pcInit = 0xEC

  "Cpu" should s"execute AUIPC" in {
    Driver.execute(Array(), () => new Cpu(pcInit))({
      c => new AUIPC_UnitTester(c)
    }) should be(true)
  }

  "Cpu" should s"execute LUI" in {
    Driver.execute(Array(), () => new Cpu(pcInit))({
      c => new LUI_UnitTester(c)
    }) should be(true)
  }

  "Cpu" should s"execute JAL" in {
    Driver.execute(Array(), () => new Cpu(pcInit))({
      c => new JAL_UnitTester(c)
    }) should be(true)
  }

  "Cpu" should s"execute JALR" in {
    Driver.execute(Array(), () => new Cpu(pcInit))({
      c => new JALR_UnitTester(c)
    }) should be(true)
  }

  "Cpu" should s"execute BRANCH taken" in {
    Driver.execute(Array(), () => new Cpu(pcInit))({
      c => new BRANCH_Taken_UnitTester(c)
    }) should be(true)
  }

  "Cpu" should s"execute BRANCH not taken" in {
    Driver.execute(Array(), () => new Cpu(pcInit))({
      c => new BRANCH_NotTaken_UnitTester(c)
    }) should be(true)
  }

  "Cpu" should s"execute OP_IMM" in {
    Driver.execute(Array(), () => new Cpu(pcInit))({
      c => new IMM_UnitTester(c)
    }) should be(true)
  }

  "Cpu" should s"execute OP_RR" in {
    Driver.execute(Array(), () => new Cpu(pcInit))({
      c => new RR_UnitTester(c)
    }) should be(true)
  }

  "Cpu" should s"execute LW" in {
    Driver.execute(Array(), () => new Cpu(pcInit))({
      c => new LW_UnitTester(c)
    }) should be(true)
  }

  "Cpu" should s"execute SW" in {
    Driver.execute(Array(), () => new Cpu(pcInit))({
      c => new SW_UnitTester(c)
    }) should be(true)
  }
}
