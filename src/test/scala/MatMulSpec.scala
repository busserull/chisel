package Ex0

import chisel3._
import chisel3.iotesters.PeekPokeTester
import org.scalatest.{Matchers, FlatSpec}
import TestUtils._

class MatMulSpec extends FlatSpec with Matchers {
  import MatMulTests._

  val rowDims = 3
  val colDims = 7


  behavior of "MatMul"

  it should "Multiply two matrices" in {
    wrapTester(
      chisel3.iotesters.Driver(() => new MatMul(rowDims, colDims)) { c =>
        new FullMatMul(c)
      } should be(true)
    )
  }

  /*
  it should "Multiply two predefined matrices" in {
    wrapTester(
      chisel3.iotesters.Driver(() => new MatMul(2, 3)) { c =>
        new TestExample(c)
      } should be(true)
    )
  }

  it should "Iterate over rows and columns" in {
    wrapTester(
      chisel3.iotesters.Driver(() => new MatMul(rowDims, colDims)) { c =>
        new IterateOverRowsAndColumns(c)
      } should be(true)
    )
  }

  it should "Stop reading after matrix A and B are input" in {
    wrapTester(
      chisel3.iotesters.Driver(() => new MatMul(rowDims, colDims)) { c =>
        new StopReadingAfterInput(c)
      } should be(true)
    )
  }
  */

  /*
  it should "Start computational iteration once input is done" in {
    wrapTester(
      chisel3.iotesters.Driver(() => new MatMul(rowDims, colDims)) { c =>
        new StartIteratingAfterInput(c)
      } should be(true)
    )
  }
  */
}

object MatMulTests {

  val rand = new scala.util.Random(100)

  class TestExample(c: MatMul) extends PeekPokeTester(c) {
    /*
        Multiply A and B

        A = [1, 2, 3;
             4, 5, 6]

        B = [7, 10;     B' = [ 7,  8,  9;
             8, 11;           10, 11, 12]
             9, 12]

        Should be C

        C = [ 50,  68;
             122, 167]
    */

    val input = List((1.U, 7.U), (2.U, 8.U), (3.U, 9.U), (4.U, 10.U), (5.U, 11.U), (6.U, 12.U))

    val wanted = List(50.U, 68.U, 122.U, 167.U)

    for((a, b) <- input){
      poke(c.io.dataInA, a)
      poke(c.io.dataInB, b)
      expect(c.io.outputValid, false, "Incorrectly flagged as valid during input")

      println(s"[${peek(c.debug.cycle)}] ---> R/Or/C - a/b/Acc ${peek(c.debug.row)} ${peek(c.debug.out_row)} ${peek(c.debug.col)} - ${peek(c.debug.a)} ${peek(c.debug.b)} ${peek(c.io.dataOut)}")

      step(1)
    }

    for(element <- 0 until (2 * 2)){
      for(_ <- 0 until 3 - 1){
        expect(c.io.outputValid, false, "Valid output mistimed")

      println(s"[${peek(c.debug.cycle)}] ---> R/Or/C - a/b/Acc ${peek(c.debug.row)} ${peek(c.debug.out_row)} ${peek(c.debug.col)} - ${peek(c.debug.a)} ${peek(c.debug.b)} ${peek(c.io.dataOut)}")
        step(1)
      }

      println(s"[${peek(c.debug.cycle)}] ---> R/Or/C - a/b/Acc ${peek(c.debug.row)} ${peek(c.debug.out_row)} ${peek(c.debug.col)} - ${peek(c.debug.a)} ${peek(c.debug.b)} ${peek(c.io.dataOut)}")
      expect(c.io.outputValid, true, "Valid output timing is wrong")
      expect(c.io.dataOut, wanted(element), "Wrong value calculated")

      println("")

      step(1)
    }
  }

  /*
  class StartIteratingAfterInput(c: MatMul) extends PeekPokeTester(c) {
    val matrix = genMatrix(c.rowDimsA, c.colDimsA)

    for(row <- 0 until c.rowDimsA){
      for(col <- 0 until c.colDimsA){
        poke(c.io.dataInA, matrix(row)(col))
        poke(c.io.dataInB, matrix(row)(col))

        expect(c.debug.comp_row, 0.U, "Computational row changed before input complete")
        expect(c.debug.comp_col, 0.U, "Computational column changed before input complete")

        step(1)
      }
    }

    for(i <- 0 until 12){
      expect(c.debug.comp_row, i / c.rowDimsA, "Computational row not correctly incremented")
      expect(c.debug.comp_col, i % c.rowDimsA, "Computational column not correctly incremented")

      step(1)
    }
  }
  */

  class StopReadingAfterInput(c: MatMul) extends PeekPokeTester(c) {
    val matrix = genMatrix(c.rowDimsA, c.colDimsA)

    for(row <- 0 until c.rowDimsA){
      for(col <- 0 until c.colDimsA){
        poke(c.io.dataInA, matrix(row)(col))
        poke(c.io.dataInB, matrix(row)(col))

        expect(c.debug.reading, true, "Not reading input matrices")
        step(1)
      }
    }

    expect(c.debug.reading, false, "Still reading after matrices input")
  }

  class IterateOverRowsAndColumns(c: MatMul) extends PeekPokeTester(c) {
    val matrix = genMatrix(c.rowDimsA, c.colDimsA)

    for(row <- 0 until c.rowDimsA){
      for(col <- 0 until c.colDimsA){
        poke(c.io.dataInA, matrix(row)(col))
        poke(c.io.dataInB, matrix(row)(col))

        expect(c.debug.row, row, "Not at the correct row")
        expect(c.debug.col, col, "Not at the correct column")

        step(1)
      }
    }
  }

  class FullMatMul(c: MatMul) extends PeekPokeTester(c) {

    val mA = genMatrix(c.rowDimsA, c.colDimsA)
    val mB = genMatrix(c.rowDimsA, c.colDimsA)
    val mC = matrixMultiply(mA, mB.transpose)

    println("Multiplying")
    println(printMatrix(mA))
    println("With")
    println(printMatrix(mB.transpose))
    println("Expecting")
    println(printMatrix(mC))

    // Input data
    for(ii <- 0 until c.colDimsA * c.rowDimsA){

      val rowInputIdx = ii / c.colDimsA
      val colInputIdx = ii % c.colDimsA

      poke(c.io.dataInA, mA(rowInputIdx)(colInputIdx))
      poke(c.io.dataInB, mB(rowInputIdx)(colInputIdx))
      expect(c.io.outputValid, false, "Valid output during initialization")

      step(1)
    }

    // Perform calculation
    for(ii <- 0 until (c.rowDimsA * c.rowDimsA)){
      for(kk <- 0 until c.colDimsA - 1){
        expect(c.io.outputValid, false, "Valid output mistimed")
        step(1)
      }
      expect(c.io.outputValid, true, "Valid output timing is wrong")
      expect(c.io.dataOut, mC(ii / c.rowDimsA)(ii % c.rowDimsA), "Wrong value calculated")
      step(1)
    }
  }
}
