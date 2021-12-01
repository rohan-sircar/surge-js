package surge.js

import surge.scaladsl.command.SurgeCommand

import java.util.UUID
import surge.js.State
import surge.js.Command
import surge.js.Event
import surge.js.JsSurgeModel
import surge.scaladsl.command.AggregateCommandModel

object JsEngine {
  def apply(
      _aggregateName: String,
      _stateTopic: String,
      _eventsTopic: String,
      _commandModel: AggregateCommandModel[State, Command, Event]
  ) = SurgeCommand(
    JsSurgeModel(_aggregateName, _stateTopic, _eventsTopic, _commandModel)
  )
}
