package general

import collection.mutable.ArrayBuffer
import org.junit.Test
import scala.math._
import collection.mutable.HashSet
import collection.mutable

class GeneralTest {

  @Test
  def traitTest {

    var logger: Logger = new LoggerImpl()

    logger.log("abc")
    logger log "abc"

  }

  @Test
  def anotherFormToTraitTest {

    val s: ShowLog = new ShowLog with FileLogger

    s.show

  }

  @Test
  def applyAndUpdateTest {

    val x = Fraction(2, 3) * Fraction(3, 4)

    println(x)

  }

  @Test
  def unapply {
    val x = Fraction(2, 3)

    val Fraction(y) = x

    println(y)
  }

  @Test
  def function {

    val fun = ceil _

    println(fun(10.5))

    println(Array(1.2, 2.8).map(fun).array(0))

  }

  @Test
  def sets() {

    var array = new ArrayBuffer[String]

    for (i <- 0 to 100000)
      array += "" + i

    var start = System.currentTimeMillis()

    println(array.contains("99999"))

    println(System.currentTimeMillis() - start)


    var set = new mutable.HashSet[String]

    for (i <- 0 to 100000)
      set += "" + i

    start = System.currentTimeMillis()

    println(set.contains("99999"))

    println(System.currentTimeMillis() - start)

  }

  @Test
  def stream {

    val x = Stream[Int](1, 2)

    val y = x ++ Stream[Int](3)

    val w = y.force

    println(x.tail.head)
    println(y.print(", "))
    println(w.print(", "))

    val a = w.map(_ * 2)

    val v = a apply 2

    a.force

    println(a)
  }

  @Test
  def parallel {

    val col = ArrayBuffer[Int]()

    for (i <- 0 until 10000)
      col += i

    val s = col.par.sum

    println(s)

    //    col.par.foreach(println(_))

    for (v <- col.par)
      println(v)

  }

  @Test
  def another {

    val s = new Another("abc").aa("123", "456")

    val s1 = "123"

    println(s)

  }

  @Test
  def singleton {

    val v: Every = new Every

    v setId "123"

    v setId {"123"}

    val p: Person = new Person

    p setId "123" setName "abc"

  }

  @Test
  def generalTest {

    import general.ClassOne._

    abc {
      println("outer abc")

      def lop { println("OK") }

      val x = 123

      lop
    }

    println("end")

  }

  @Test def stringManipulation() {

    val s =
      """abcdef
        |123456
      """.stripMargin

    println(s)

  }
}
