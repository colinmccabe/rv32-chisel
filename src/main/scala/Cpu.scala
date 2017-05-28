import Constants._
import chisel3._
import chisel3.util._

class Cpu(val pcInit: Int) extends Module {
  val io = IO(new Bundle {
    val instMem = Flipped(new InstMemIO)
    val dataMem = Flipped(new DataMemIO)
    val rf = Flipped(new RegFileIO)
    val alu = Flipped(new AluIO)
  })

  val fetch :: decode :: execute :: mem_access :: write :: exception :: Nil = Enum(6)

  val cycle = RegInit(fetch)
  val pc = RegInit(pcInit.U(INST_MEM_WIDTH))
  val exceptionNum = RegInit(0x0.U(5.W))

  val inst = Wire(UInt(width = WIDTH))
  val inst_save = RegInit(0x0.U(WIDTH))
  when(cycle === decode) {
    inst := io.instMem.data
    inst_save := io.instMem.data
  }.otherwise {
    inst := inst_save
  }

  val op = inst(6, 0)
  val funct3 = inst(14, 12)
  val funct7 = inst(31, 25)
  val rs1 = inst(19, 15)
  val rs2 = inst(24, 20)
  val rd = inst(11, 7)

  val imm_i = signExtend(inst(31, 20))
  val imm_s = signExtend(Cat(inst(31, 25), inst(11, 7)))
  val imm_u = Cat(inst(31, 12), 0.U(12.W))
  val imm_b = signExtend(Cat(
    inst(31),
    inst(7),
    inst(30, 25),
    inst(11, 8),
    0.U(1.W)))
  val imm_j = signExtend(Cat(
    inst(31),
    inst(19, 12),
    inst(20),
    inst(30, 21),
    0.U(1.W)))

  val x = RegInit(0x0.U(WIDTH))
  val y = RegInit(0x0.U(WIDTH))
  val aluResult = RegInit(0x0.U(WIDTH))

  io.rf.rs1 := rs1
  io.rf.rs2 := rs2
  io.rf.rd := rd
  io.rf.wdata := aluResult
  io.rf.wen := false.B

  io.alu.op := op
  io.alu.funct3 := funct3
  io.alu.funct7 := funct7
  io.alu.x := x
  io.alu.y := y

  io.instMem.read := false.B
  io.instMem.addr := pc

  io.dataMem.addr := aluResult
  io.dataMem.wdata := io.rf.r2
  io.dataMem.read := false.B
  io.dataMem.write := false.B

  switch(cycle) {
    is(fetch) {
      io.instMem.read := true.B
      cycle := decode
    }


    is(decode) {
      when(io.instMem.err) {
        cycle := exception
        exceptionNum := 1.U

      }.otherwise {
        when(op === OP_LUI) {
          x := imm_u
        }.elsewhen(op === OP_AUIPC) {
          x := pc
          y := imm_u
        }.elsewhen(op === OP_JAL) {
          x := pc
          y := imm_j
        }.elsewhen(op === OP_JALR || op === OP_IMM || op === OP_LOAD) {
          x := io.rf.r1
          y := imm_i
        }.elsewhen(op === OP_BRANCH || op === OP_RR) {
          x := io.rf.r1
          y := io.rf.r2
        }.elsewhen(op === OP_STORE) {
          x := io.rf.r1
          y := imm_s
        }

        cycle := execute
      }
    }


    is(execute) {
      when(io.alu.isValid) {
        aluResult := io.alu.o

        when(op === OP_LOAD || op === OP_STORE) {
          cycle := mem_access
        }.otherwise {
          cycle := write
        }

      }.otherwise {
        cycle := exception
        exceptionNum := 2.U
      }
    }


    is(mem_access) {
      when(aluResult(1, 0) =/= 0.U) { // Misaligned
        cycle := exception
        when(op === OP_LOAD) {
          exceptionNum := 8.U
        }.otherwise {
          exceptionNum := 9.U
        }

      }.otherwise { // Valid
        io.dataMem.read := (op === OP_LOAD)
        io.dataMem.write := (op === OP_STORE)
        cycle := write
      }
    }


    is(write) {
      when(io.dataMem.err) {  // Access error
        cycle := exception
        when(op === OP_LOAD) {
          exceptionNum := 10.U
        }.otherwise {
          exceptionNum := 11.U
        }

      }.otherwise {
        when(op === OP_BRANCH || op === OP_STORE) {
          // No RF write
        }.elsewhen(op === OP_LOAD) {
          io.rf.wen := true.B
          io.rf.wdata := io.dataMem.rdata
        }.elsewhen(op === OP_JAL || op === OP_JALR) {
          io.rf.wen := true.B
          io.rf.wdata := pc + 4.U
        }.otherwise {
          io.rf.wen := true.B
        }

        when(op === OP_JAL || op === OP_JALR) {
          pc := aluResult
        }.elsewhen(op === OP_BRANCH && aluResult =/= 0.U) {
          pc := pc + imm_b
        }.otherwise {
          pc := pc + 4.U
        }

        cycle := fetch
      }
    }
  }

  def signExtend(n: UInt): UInt =
    Wire(SInt(WIDTH), n.asSInt).asUInt
}
