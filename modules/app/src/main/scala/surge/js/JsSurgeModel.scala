// Copyright © 2017-2021 UKG Inc. <https://www.ukg.com>

package surge.js

import play.api.libs.json.Json
import surge.core.{
  SerializedAggregate,
  SerializedMessage,
  SurgeAggregateReadFormatting,
  SurgeAggregateWriteFormatting,
  SurgeEventWriteFormatting
}
import surge.kafka.KafkaTopic
import surge.scaladsl.command.{AggregateCommandModel, SurgeCommandBusinessLogic}
import surge.js.Book

import java.util.UUID

object JsSurgeModel {

  def apply(
      _aggregateName: String,
      _stateTopic: String,
      _eventsTopic: String,
      _commandModel: AggregateCommandModel[State, Command, Event]
  ): SurgeCommandBusinessLogic[UUID, State, Command, Event] =
    new SurgeCommandBusinessLogic[UUID, State, Command, Event] {
      def commandModel: AggregateCommandModel[State, Command, Event] =
        _commandModel

      def aggregateName: String = _aggregateName

      def stateTopic: KafkaTopic = KafkaTopic(_stateTopic)

      def eventsTopic: KafkaTopic = KafkaTopic(_eventsTopic)

      def aggregateReadFormatting: SurgeAggregateReadFormatting[State] =
        (bytes: Array[Byte]) => Json.parse(bytes).asOpt[State]

      def aggregateWriteFormatting: SurgeAggregateWriteFormatting[State] =
        (agg: State) => {
          val aggBytes = Json.toJson(agg).toString().getBytes()
          val messageHeaders = Map("aggregate_id" -> agg.aggregateId.toString)
          SerializedAggregate(aggBytes, messageHeaders)
        }

      def eventWriteFormatting: SurgeEventWriteFormatting[Event] =
        (evt: Event) => {
          val evtKey = evt.aggregateId.toString
          val evtBytes = Json.toBytes(Json.toJson(evt))
          val messageHeaders = Map("aggregate_id" -> evt.aggregateId.toString)
          SerializedMessage(evtKey, evtBytes, messageHeaders)
        }
    }

}
