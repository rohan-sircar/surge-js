package surge.js

import surge.scaladsl.command.AggregateCommandModel

import scala.util.{Failure, Success, Try}

import org.slf4j.{Logger, LoggerFactory}
import java.util.UUID
import play.api.libs.json.Json
import play.api.libs.json.JsValue
import org.graalvm.polyglot._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scala.concurrent.Await
import monix.execution.Scheduler
import monix.eval.Task
import scala.collection.mutable.ListBuffer

final case class State(aggregateId: String, value: String)
object State {
  implicit val format = Json.format[State]
}
final case class Command(aggregateId: String, value: String)
object Command {
  implicit val format = Json.format[Command]
}
final case class Event(aggregateId: String, value: String)
object Event {
  implicit val format = Json.format[Event]
}

object AppScheduler {
  def apply() = Scheduler.global
  // Scheduler.singleThread("graalvm-thread")
  // val io = Scheduler.io()
}

object JsTest {

  def future(f: Function1[Int, Int]) = {
    implicit val ec = ExecutionContext.global
    println("hello")
    println(s"Got ${f(2)}")
    val fut = Future {
      println("starting future")
      println(s"Got ${f(2)}")
    } onComplete (println)

    // Await.result(fut, 5.seconds)
  }

  // implicit val scheduler = Scheduler.singleThread("graalvm-thread")
  def task(f: Function1[Int, Int]) = {
    val task = for {
      _ <- Task(println("hello"))
      _ <- Task(println(s"Got ${f(2)}"))
      fut <- Task {
        println("starting task")
        println(s"Got ${f(2)}")
      }
    } yield ()
    // task.executeOn(scheduler).runToFuture onComplete (println)
    task
  }
}

object makeList {
  def apply[T](elems: T*) = {
    List(elems: _*)
  }
  List.apply()
}

object emptyListBuffer {
  def apply[A]() = ListBuffer.empty
}
