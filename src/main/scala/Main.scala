object Main {
  def main(args: Array[String]): Unit = {
    val options = Array("--target-dir", "verilog")
    chisel3.Driver.execute(options, () => new Computer)
  }

}
