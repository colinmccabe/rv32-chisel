import chisel3._

object Constants {
  val WIDTH = 32.W
  val INST_MEM_WIDTH = 12.W
  val DATA_MEM_WIDTH = 12.W
  val U_MAX = Math.pow(2, 32).toLong - 1
  val S_MAX = Math.pow(2, 31).toLong - 1
  val S_MIN = -Math.pow(2, 31).toLong
  val INST_ADDR_MASK = (1L << INST_MEM_WIDTH.get) - 1

  val OP_LUI = "b0110111".U
  val OP_AUIPC = "b0010111".U
  val OP_JAL = "b1101111".U
  val OP_JALR = "b1100111".U
  val OP_BRANCH = "b1100011".U
  val OP_LOAD = "b0000011".U
  val OP_STORE = "b0100011".U
  val OP_IMM = "b0010011".U
  val OP_RR = "b0110011".U

  val FUNCT3_BEQ = "b000".U
  val FUNCT3_BNE = "b001".U
  val FUNCT3_BLT = "b100".U
  val FUNCT3_BGE = "b101".U
  val FUNCT3_BLTU = "b110".U
  val FUNCT3_BGEU = "b111".U
  val FUNCT3_WORD = "b010".U
  val FUNCT3_ADD_SUB = "b000".U
  val FUNCT3_SLL = "b001".U
  val FUNCT3_SLT = "b010".U
  val FUNCT3_SLTU = "b011".U
  val FUNCT3_XOR = "b100".U
  val FUNCT3_SR = "b101".U
  val FUNCT3_OR = "b110".U
  val FUNCT3_AND = "b111".U

  val FUNCT7_0 = "b0000000".U
  val FUNCT7_1 = "b0100000".U

  val SOME_WORD = 12344321
}
