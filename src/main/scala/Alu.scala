import Constants._
import chisel3._
import chisel3.util._

class AluIO extends Bundle {
  val op = Input(UInt(7.W))
  val funct3 = Input(UInt(3.W))
  val funct7 = Input(UInt(7.W))
  val x = Input(UInt(WIDTH))
  val y = Input(UInt(WIDTH))

  val isValid = Output(Bool())
  val o = Output(UInt(WIDTH))
}


class Alu extends Module {
  val io = IO(new AluIO)

  val x_s = io.x.asSInt
  val y_s = io.y.asSInt

  // Default
  io.isValid := false.B
  io.o := 0.U

  when(
    io.op === OP_LUI
  ) {
    validResult(io.x)


  }.elsewhen(io.op === OP_AUIPC
    || io.op === OP_JAL
    || io.op === OP_JALR
    || io.op === OP_LOAD && io.funct3 === FUNCT3_WORD
    || io.op === OP_STORE && io.funct3 === FUNCT3_WORD
    // TODO: non-word load/store
    || isCompFunct7_0(FUNCT3_ADD_SUB)
  ) {
    validResult((x_s + y_s).asUInt)


  }.elsewhen((io.op === OP_IMM || io.op === OP_RR)
    && io.funct3 === FUNCT3_SLL
    && io.funct7 === FUNCT7_0
  ) {
    validResult((io.x << io.y(4, 0)).asUInt)


  }.elsewhen((io.op === OP_IMM || io.op === OP_RR)
    && io.funct3 === FUNCT3_SR
    && io.funct7 === FUNCT7_0
  ) {
    validResult((io.x >> io.y(4, 0)).asUInt)


  }.elsewhen((io.op === OP_IMM || io.op === OP_RR)
    && io.funct3 === FUNCT3_SR
    && io.funct7 === FUNCT7_1
  ) {
    validResult((x_s >> io.y(4, 0)).asUInt)


  }.elsewhen(io.op === OP_RR && io.funct3 === FUNCT3_ADD_SUB
    && io.funct7 === FUNCT7_1
  ) {
    validResult((x_s - y_s).asUInt)


  }.elsewhen(isBranch(FUNCT3_BEQ)) {
    validResult(Cat(0.U(31.W), x_s === y_s))


  }.elsewhen(isBranch(FUNCT3_BNE)) {
    validResult(Cat(0.U(31.W), x_s =/= y_s))


  }.elsewhen(isCompFunct7_0(FUNCT3_SLT)
    || isBranch(FUNCT3_BLT)) {
    validResult(Cat(0.U(31.W), x_s < y_s))


  }.elsewhen(isBranch(FUNCT3_BGE)) {
    validResult(Cat(0.U(31.W), x_s >= y_s))


  }.elsewhen(isCompFunct7_0(FUNCT3_SLTU) || isBranch(FUNCT3_BLTU)
  ) {
    validResult(Cat(0.U(31.W), io.x < io.y))


  }.elsewhen(isBranch(FUNCT3_BGEU)) {
    validResult(Cat(0.U(31.W), io.x >= io.x))


  }.elsewhen(isCompFunct7_0(FUNCT3_XOR)) {
    validResult(io.x ^ io.y)


  }.elsewhen(isCompFunct7_0(FUNCT3_OR)) {
    validResult(io.x | io.y)


  }.elsewhen(isCompFunct7_0(FUNCT3_AND)) {
    validResult(io.x & io.y)
  }


  def isCompFunct7_0(funct3: UInt): Bool = {
    val isOpImm = io.op === OP_IMM
    val isOpRR = io.op === OP_RR && io.funct7 === FUNCT7_0
    io.funct3 === funct3 && (isOpImm || isOpRR)
  }


  def isBranch(funct3: UInt): Bool =
    io.op === OP_BRANCH && io.funct3 === funct3


  def validResult(result: UInt): Unit = {
    io.isValid := true.B
    io.o := result
  }

}
