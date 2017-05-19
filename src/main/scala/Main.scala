object Main {
  def main(cliArgs: Array[String]): Unit = {
    val args = Array(
      "--backend", "c",
      "--compile",
      "--test",
      "--genHarness",
      "--minimumCompatibility", "3.0.0"
    ) ++ cliArgs

//    chiselMainTest(args, () => Module(new RegFile())) {
//      rf => new rf.Tests(rf)
//    }
//
//    chiselMainTest(args, () => Module(new Alu())) {
//      alu => new alu.Tests(alu)
//    }
//
//    chiselMainTest(args, () => Module(new Cpu(32))) {
//      cpu => new cpu.Tests(cpu)
//    }
  }

}
