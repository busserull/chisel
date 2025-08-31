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
      val signal = Output(Bool())
      val row = Output(UInt(32.W))
      val col = Output(UInt(32.W))
    }
  )

  // Matrices

  val mat_a = Module(new Matrix(rowDimsA, colDimsA))
  val mat_b = Module(new Matrix(rowDimsA, colDimsA))

  // Read matrix A and B

  val reading = Reg(Bool(), true.B)

  val row = RegInit(UInt(32.W), 0.U)
  val (col, next_row) = Counter(true.B, colDimsA)

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



  debug.row := row
  debug.col := col
  debug.signal := false.B

  io.dataOut := io.dataInA + io.dataInB
  io.outputValid := true.B

  /*
  val matrixA     = Module(new Matrix(rowDimsA, colDimsA)).io
  val matrixB     = Module(new Matrix(rowDimsA, colDimsA)).io
  val dotProdCalc = Module(new DotProd(colDimsA)).io

  matrixA.dataIn      := 0.U
  matrixA.rowIdx      := 0.U
  matrixA.colIdx      := 0.U
  matrixA.writeEnable := false.B

  matrixB.rowIdx      := 0.U
  matrixB.colIdx      := 0.U
  matrixB.dataIn      := 0.U
  matrixB.writeEnable := false.B

  dotProdCalc.dataInA := 0.U
  dotProdCalc.dataInB := 0.U

  io.dataOut := 0.U
  io.outputValid := false.B
  */


  // debug.myDebugSignal := false.B
}
