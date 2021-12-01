package surge.js

import surge.scaladsl.command.AggregateCommandModel

import scala.util.{Failure, Success, Try}
import surge.js.Book
import org.slf4j.{Logger, LoggerFactory}
import java.util.UUID
import play.api.libs.json.Json
import play.api.libs.json.JsValue

final case class State(aggregateId: String, value: JsValue)
object State {
  implicit val format = Json.format[State]
}
final case class Command(aggregateId: String, value: JsValue)
object Command {
  implicit val format = Json.format[Command]
}
final case class Event(aggregateId: String, value: JsValue)
object Event {
  implicit val format = Json.format[Event]
}
