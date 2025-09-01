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
      val iterate_complete = Output(Bool())
    }
  )

  // Matrices

  val mat_a = Module(new Matrix(rowDimsA, colDimsA))
  val mat_b = Module(new Matrix(rowDimsA, colDimsA))


  // Multiplier

  val dot = Module(new DotProd(colDimsA))


  // Reading controller
  
  val reading = RegInit(Bool(), true.B)

  val (col, next_row) = Counter(true.B, colDimsA)
  val (row, iterate_complete) = Counter(next_row, rowDimsA)

  val (out_row, _) = Counter(iterate_complete && dot.io.outputValid, rowDimsA)
  // val (out_col, )




  // Read matrix A and B

  mat_a.io.colIdx := col
  mat_a.io.rowIdx := row
  mat_a.io.dataIn := io.dataInA

  mat_b.io.colIdx := col
  mat_b.io.rowIdx := row
  mat_b.io.dataIn := io.dataInB

  mat_a.io.writeEnable := reading
  mat_b.io.writeEnable := reading

  when(iterate_complete){
    reading := false.B
  }

  // Calculate matrix multiplication

  when(!reading){
    mat_a.io.rowIdx := out_row
  }

  dot.io.dataInA := mat_a.io.dataOut
  dot.io.dataInB := mat_b.io.dataOut

  io.dataOut := dot.io.dataOut
  io.outputValid := dot.io.outputValid && !reading


  // when(mult.io.outputValid){
  //   io.dataOut := mult.io.dataOut
  // }


  debug.row := row
  debug.col := col
  debug.reading := reading
  debug.iterate_complete := iterate_complete
}
