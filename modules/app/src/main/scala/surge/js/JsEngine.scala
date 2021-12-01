// Copyright © 2017-2021 UKG Inc. <https://www.ukg.com>

package surge.js

import surge.js.Book
import surge.scaladsl.command.SurgeCommand

import java.util.UUID
import surge.js.State
import surge.js.Command
import surge.js.Event
import surge.js.JsSurgeModel
import surge.scaladsl.command.AggregateCommandModel

// final class LibraryEngine {
//   lazy val surgeEngine
//       : SurgeCommand[UUID, Book, LibraryCommand, Nothing, LibraryEvent] = {
//     val engine = SurgeCommand(new LibrarySurgeModel)
//     engine.start()
//     engine
//   }
// }

// final class JsEngine {
//   lazy val surgeEngine: SurgeCommand[UUID, State, Command, Nothing, Event] = {
//     val engine = SurgeCommand(new JsSurgeModel {})
//     engine.start()
//     engine
//   }
// }

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
