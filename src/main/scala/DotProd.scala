package Ex0

import chisel3._
import chisel3.util.Counter

class DotProd(val elements: Int) extends Module {

  val io = IO(
    new Bundle {
      val dataInA     = Input(UInt(32.W))
      val dataInB     = Input(UInt(32.W))

      val dataOut     = Output(UInt(32.W))
      val outputValid = Output(Bool())
    }
  )

  val (counter, wrap) = Counter(true.B, elements)

  val acc = RegInit(UInt(32.W))

  acc := acc + io.dataInA * io.dataInB

  io.dataOut := acc
  io.outputValid := wrap

  when(wrap){
    acc := 0.U
  }
}
