module Wrapper(
    input clk,
    output[3:0] io_sevenseg_an,
    output[7:0] io_sevenseg_cath
  );

  wire reset;

  wire instMem_read;
  wire[11:0] instMem_addr;
  wire[31:0] instMem_data;

  wire dataMem_read;
  wire dataMem_write;
  wire[11:0] dataMem_addr;
  wire[31:0] dataMem_rdata;
  wire[31:0] dataMem_wdata;

  Resetter resetter (
    .clk(clk),
    .reset(reset)
  );

  instMem instMem (
    .clka(clk),
    .ena(instMem_read),
    .addra(instMem_addr[11:2]),
    .douta(instMem_data)
  );

  dataMem dataMem (
    .clka(clk),
    .ena(dataMem_read | dataMem_write),
    .wea(dataMem_write),
    .addra(dataMem_addr[11:2]),
    .dina(dataMem_wdata),
    .douta(dataMem_rdata)
  );

  Computer computer (
    .clock(clk),
    .reset(reset),
    .io_instMem_read(instMem_read),
    .io_instMem_addr(instMem_addr),
    .io_instMem_data(instMem_data),
    .io_dataMem_read(dataMem_read),
    .io_dataMem_write(dataMem_write),
    .io_dataMem_addr(dataMem_addr),
    .io_dataMem_rdata(dataMem_rdata),
    .io_dataMem_wdata(dataMem_wdata),
    .io_sevenseg_an(io_sevenseg_an),
    .io_sevenseg_cath(io_sevenseg_cath)
  );

endmodule
