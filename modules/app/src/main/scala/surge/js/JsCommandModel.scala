package surge.js

import surge.scaladsl.command.AggregateCommandModel
import scala.util.Try

object JsCommandModel {
  def apply(
      _processCommand: (Option[State], Command) => Try[List[Event]],
      _handleEvent: (Option[State], Event) => Option[State]
  ) =
    new AggregateCommandModel[State, Command, Event] {
      def processCommand(
          aggregate: Option[State],
          command: Command
      ): Try[List[Event]] = _processCommand(aggregate, command)

      def handleEvent(
          aggregate: Option[State],
          event: Event
      ): Option[State] = _handleEvent(aggregate, event)

    }
}
