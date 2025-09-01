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
      val out_row = Output(UInt(32.W))
      val col = Output(UInt(32.W))
      val iterate_complete = Output(Bool())
      val cycle = Output(UInt(32.W))
      val a = Output(UInt(32.W))
      val b = Output(UInt(32.W))
    }
  )

  val (cycle, _) = Counter(true.B, 255)

  // Matrices

  val mat_a = Module(new Matrix(rowDimsA, colDimsA))
  val mat_b = Module(new Matrix(rowDimsA, colDimsA))


  // Multiplier

  val acc = RegInit(0.U(32.W))


  // Reading controller
  
  val reading = RegInit(Bool(), true.B)

  val (col, next_row) = Counter(true.B, colDimsA)
  val (row, iterate_complete) = Counter(next_row, rowDimsA)

  val out_row = RegInit(0.U(32.W))
  val (_, next_out_row) = Counter(!reading, rowDimsA * colDimsA)


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

  when(next_out_row){
    out_row := out_row + 1.U
  }

  when(!reading){
    mat_a.io.rowIdx := out_row
  }
  
  acc := acc + mat_a.io.dataOut * mat_b.io.dataOut

  io.dataOut := acc + mat_a.io.dataOut * mat_b.io.dataOut
  io.outputValid := !reading && next_row

  when(next_row){
    acc := 0.U
  }

  debug.a := mat_a.io.dataOut
  debug.b := mat_b.io.dataOut
  debug.row := row
  debug.out_row := out_row
  debug.col := col
  debug.reading := reading
  debug.iterate_complete := iterate_complete
  debug.cycle := cycle
}
