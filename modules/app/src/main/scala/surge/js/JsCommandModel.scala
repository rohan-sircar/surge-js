package surge.js

import surge.scaladsl.command.AggregateCommandModel
import scala.util.Try
import surge.scaladsl.command.AsyncAggregateCommandModel
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import cats.syntax.all._
import monix.eval.Task
import monix.execution.Scheduler

final class JsCommandModel(
    commandExecutor: JsExecutor[(Option[State], Command), List[Event]],
    eventExecutor: JsExecutor[(Option[State], Event), Option[State]]
)(implicit s: Scheduler)
    extends AsyncAggregateCommandModel[State, Command, Event] {

  def executionContext: ExecutionContext = s

  def handleEvents(
      aggregate: Option[State],
      events: Seq[Event]
  ): Future[Option[State]] = {
    val r =
      events.foldLeftM[Task, Option[State]](aggregate) { (agg, event) =>
        Task.deferFuture(eventExecutor.request(agg -> event))
      }
    // Future
    // .traverse(events)(event => eventExecutor.request(aggregate -> event))
    // .map(_.sequence.flatMap(_.lastOption))
    // Future.successful(None)
    r.runToFuture
  }

  def processCommand(
      aggregate: Option[State],
      command: Command
  ): Future[Seq[Event]] = commandExecutor.request((aggregate, command))

}

object JsCommandModel {
  // def apply(
  //     _processCommand: (Option[State], Command) => Try[List[Event]],
  //     _handleEvent: (Option[State], Event) => Option[State]
  // ) =
  //   new AggregateCommandModel[State, Command, Event] {
  //     def processCommand(
  //         aggregate: Option[State],
  //         command: Command
  //     ): Try[List[Event]] = _processCommand(aggregate, command)

  //     def handleEvent(
  //         aggregate: Option[State],
  //         event: Event
  //     ): Option[State] = _handleEvent(aggregate, event)

  //   }
}
