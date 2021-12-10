package surge.js

import surge.scaladsl.command.SurgeCommand
import java.util.UUID
import org.graalvm.polyglot._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import surge.scaladsl.command.AggregateCommandModel
import surge.scaladsl.command.SurgeCommandBusinessLogic
import scala.util.Success
import scala.util.Failure
import surge.scaladsl.common.CommandResult
import surge.scaladsl.common.ApplyEventResult
import monix.execution.Scheduler
import scala.concurrent.Await
import scala.concurrent.duration._

trait Thenable {
  def then(onResolve: Value, onReject: Value): Unit
}

object Thenable {

  def fromFuture[T](f: Future[T])(implicit ec: ExecutionContext): Thenable =
    (resolve, reject) =>
      f onComplete {
        case Success(value)     => resolve.executeVoid(value)
        case Failure(exception) => reject.executeVoid(exception)
      }
}

trait JsAggregateRef[Agg, Cmd, Event] {
  def getState(cb: Value): Unit

  def sendCommand(command: Cmd, cb: Value): Unit

  def applyEvent(event: Event, cb: Value): Unit
}

sealed trait SurgeCommandProto
object SurgeCommandProto {
  final case class State(value: Option[surge.js.State], cb: Value)
      extends SurgeCommandProto
  final case class Command(value: CommandResult[surge.js.State], cb: Value)
      extends SurgeCommandProto
  final case class Event(value: ApplyEventResult[surge.js.State], cb: Value)
      extends SurgeCommandProto
}

final class JsSurgeCommand private (
    inner: SurgeCommand[UUID, State, Command, Nothing, Event],
    executor: JsExecutor[SurgeCommandProto, Unit]
)(implicit val ec: ExecutionContext) {

  def start() = Await.result(inner.start(), 3.seconds)

  def stop() = Await.result(inner.stop(), 3.seconds)

  def aggregateFor(id: UUID) = {
    val agg = inner.aggregateFor(id)
    new JsAggregateRef[State, Command, Event] {

      def getState(cb: Value): Unit = for {
        s <- agg.getState
        _ <- executor.request(SurgeCommandProto.State(s, cb))
      } yield ()
      def sendCommand(command: Command, cb: Value): Unit =
        for {
          c <- agg.sendCommand(command)
          // _ <- Future.successful(cb.executeVoid(c))
          _ <- executor.request(SurgeCommandProto.Command(c, cb))
        } yield ()
      def applyEvent(event: Event, cb: Value): Unit = for {
        e <- agg.applyEvent(event)
        _ <- executor.request(SurgeCommandProto.Event(e, cb))
      } yield ()

    }
  }
}

object JsSurgeCommand {
  def apply(
      model: SurgeCommandBusinessLogic[UUID, State, Command, Event],
      executor: JsExecutor[SurgeCommandProto, Unit]
  )(implicit s: Scheduler) =
    new JsSurgeCommand(
      SurgeCommand(model),
      executor
    )
}
