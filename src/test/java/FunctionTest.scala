package general

import collection.mutable.ArrayBuffer
import org.junit.Test
import org.junit.Assert._

class FunctionTest {

  @Test
  def anonymousTest {

    // x is a function
    val x = (a: Double) => a * 3
    // Another way to write a funcion using parameter inference
    val y = 3 * (_: Double)
    val w: (Double) => (Double) = 3 * _

    assertEquals(Double.box(9.0), x(3))
    assertEquals(Double.box(9.0), y(3))
    assertEquals(Double.box(9.0), w(3))
  }

  @Test
  def a2Test() {

    // This function yields another funcion
    def mult(factor: Double) = (a: Double) => factor * a

    val x = mult(5)

    println(x(10))

    assertEquals(Double.box(50), x(10))

  }

  @Test
  def a3Test() {

    def mult(func: (Double) => Double) = func(5)

    // The function
    val a = (b: Double) => b
    // The function used as parameter
    val x = mult(a)

    println(x)

    assertEquals(Double.box(5), x)

  }

  @Test
  def a4Test() {

    val x = (a: Int) => a * 3
    val y = (a: Int) => a % 2 == 0
    val w = (a: Int, b: Int) => a * b

    println((1 to 9).map(x))
    println((1 to 9).filter(y))

    // reduceLeft is 1 * 2 * 3 * 4 * 5 * 6 * 7 * 8 * 9
    println((1 to 9).reduceLeft(w))

    val s = (a: String, b: String) => a.length < b.length

    println(Array("1234", "123", "12", "1").sortWith(s).toVector)
    println(ArrayBuffer("d", "c", "b", "a").sortWith( _.compareTo(_) < 0 ).toVector)

  }

  @Test
  def funcTest {

    val e: Every = new Every

    e.setId({"abc"})

    val r = e.func (
      (x: String) => x + "123"
    )("abc")

    assertEquals("onetwothree", r)

  }

  def sum(a: Int, b: Int): Int = {
    a + b
  }

  def esum(a: Int, b: Int)(f: (Int, Int) => Int) = {
    f(a, b)
  }

  @Test def a5Test {

    println(esum(10, 20)(sum))

    println(esum(10, 20)( (a, b) => a * b ))

    println(esum(10, 20) { (a, b) =>
      a * b
    })

  }

}



