package surge.js

import surge.scaladsl.command.SurgeCommand
import java.util.UUID
import org.graalvm.polyglot._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import surge.scaladsl.command.AggregateCommandModel
import surge.scaladsl.command.SurgeCommandBusinessLogic

trait Thenable {
  def then(onResolve: Value, onReject: Value): Unit
}

object Thenable {
//   implicit val ec: scala.concurrent.ExecutionContext =
//     scala.concurrent.ExecutionContext.global
//   val t: Thenable = (resolve, reject) => resolve.executeVoid(42)

//   val t2: Thenable = (resolve, reject) =>
//     Future(1).map { i => resolve.executeVoid(i) }

  def fromFuture[T](f: Future[T])(implicit ec: ExecutionContext): Thenable =
    (resolve, reject) => f.map { t => resolve.executeVoid(t) }
}

trait JsAggregateRef[Agg, Cmd, Event] {
  def getState: Thenable

  def sendCommand(command: Cmd): Thenable

  def applyEvent(event: Event): Thenable
}

final class JsSurgeCommand private (
    inner: SurgeCommand[UUID, State, Command, Nothing, Event]
)(implicit val ec: ExecutionContext) {

  def start() = Thenable.fromFuture(inner.start())

  def stop() = Thenable.fromFuture(inner.stop())

  def aggregateFor(id: UUID) = {
    val agg = inner.aggregateFor(id)
    new JsAggregateRef[State, Command, Event] {

      def getState: Thenable = Thenable.fromFuture(agg.getState)

      def sendCommand(command: Command): Thenable =
        Thenable.fromFuture(agg.sendCommand(command))

      def applyEvent(event: Event): Thenable = ???

    }
  }
}

object JsSurgeCommand {
  def apply(model: SurgeCommandBusinessLogic[UUID, State, Command, Event]) =
    new JsSurgeCommand(SurgeCommand(model))(ExecutionContext.global)
}
