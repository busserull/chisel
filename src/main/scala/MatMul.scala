package Ex0

import chisel3._
import chisel3.util.Counter
import chisel3.experimental.MultiIOModule

class MatMul(val rowDimsA: Int, val colDimsA: Int) extends MultiIOModule {
  val io = IO(
    new Bundle {
      val dataInA     = Input(UInt(32.W))
      val dataInB     = Input(UInt(32.W))

      val dataOut     = Output(UInt(32.W))
      val outputValid = Output(Bool())
    }
  )

  val debug = IO(
    new Bundle {
      val reading = Output(Bool())
      val row = Output(UInt(32.W))
      val col = Output(UInt(32.W))
      val comp_row = Output(UInt(32.W))
      val comp_col = Output(UInt(32.W))
    }
  )

  // Matrices

  val mat_a = Module(new Matrix(rowDimsA, colDimsA))
  val mat_b = Module(new Matrix(rowDimsA, colDimsA))


  // Reading controller
  
  val reading = RegInit(Bool(), true.B)

  val row = RegInit(UInt(32.W), 0.U)
  val (col, next_row) = Counter(true.B, colDimsA)


  // Multiplier and output

  val dot = Module(new DotProd(colDimsA))

  val mat_out = Module(new Matrix(rowDimsA, rowDimsA))

  val comp_row = RegInit(UInt(32.W), 0.U)
  val (comp_col, next_comp_row) = Counter(!reading && dot.io.outputValid, rowDimsA)


  // Read matrix A and B

  when(next_row){
    row := row + 1.U
  }

  mat_a.io.colIdx := col
  mat_a.io.rowIdx := row
  mat_a.io.dataIn := io.dataInA

  mat_b.io.colIdx := col
  mat_b.io.rowIdx := row
  mat_b.io.dataIn := io.dataInB

  mat_a.io.writeEnable := reading
  mat_b.io.writeEnable := reading

  when(row === rowDimsA.U - 1.U && next_row){
    reading := false.B
    row := 0.U

    // comp_col := 0.U
    // comp_row := 0.U
  }

  // Calculate matrix multiplication
  
  when(next_comp_row){
    comp_row := comp_row + 1.U
  }

  dot.io.dataInA := mat_a.io.dataOut
  dot.io.dataInB := mat_b.io.dataOut

  mat_out.io.colIdx := comp_col
  mat_out.io.rowIdx := comp_row
  mat_out.io.dataIn := dot.io.dataOut
  mat_out.io.writeEnable := dot.io.outputValid

  io.dataOut := dot.io.dataOut
  io.outputValid := dot.io.outputValid && !reading


  // when(mult.io.outputValid){
  //   io.dataOut := mult.io.dataOut
  // }


  debug.row := row
  debug.col := col
  debug.comp_row := comp_row
  debug.comp_col := comp_col
  debug.reading := reading
}
