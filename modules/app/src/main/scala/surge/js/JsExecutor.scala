package surge.js

import java.util.UUID
import java.util.concurrent.{ConcurrentHashMap, ConcurrentLinkedQueue}
import scala.concurrent._
import scala.util.Success
import scala.util.Failure
import monix.execution.CancelablePromise
import scala.collection.immutable.Queue
import monix.execution.atomic.Atomic
import monix.execution.CancelableFuture
import monix.execution.Scheduler
import scala.collection.concurrent.TrieMap

final case class Request[A](id: UUID, value: A)

final class Queuee[IN, OUT] {

  private val underlyingQueue =
    new ConcurrentLinkedQueue[Request[IN]]()
  private val taskToPromise =
    new ConcurrentHashMap[Request[IN], Promise[OUT]]()

  // alias for pushTask
  def invoke(input: IN): Future[OUT] = pushTask(input)

  // called by the Scala side of the application
  private def pushTask(input: IN): Future[OUT] = {
    val taskWithCorrelationId = Request(UUID.randomUUID(), input)
    underlyingQueue.offer(taskWithCorrelationId)
    val promise = CancelablePromise[OUT]()
    taskToPromise.put(taskWithCorrelationId, promise)
    promise.future
  }

  // called by the JavaScript side
  // can return null
  def pollTask(): Request[IN] = {
    underlyingQueue.poll()
  }

  // called by the JavaScript side
  def pushResult(
      taskWithCorrelationId: Request[IN],
      result: OUT
  ): Unit = {
    val promise = taskToPromise.get(taskWithCorrelationId)
    promise.complete(Success(result))
    taskToPromise.remove(taskWithCorrelationId)
  }

  // called by the JavaScript side
  def pushFailure(
      taskWithCorrelationId: Request[IN],
      exception: Exception
  ): Unit = {
    val promise = taskToPromise.get(taskWithCorrelationId)
    promise.complete(Failure(exception))
    taskToPromise.remove(taskWithCorrelationId)
  }

}

object JsExecutor {
  def apply[IN, OUT]()(implicit s: Scheduler) = new JsExecutor[IN, OUT](
    new ConcurrentLinkedQueue[Request[IN]],
    TrieMap[Request[IN], CancelablePromise[OUT]]()
  )
}

final class JsExecutor[IN, OUT](
    queue: ConcurrentLinkedQueue[Request[IN]],
    promises: TrieMap[Request[IN], CancelablePromise[OUT]]
)(implicit s: Scheduler) {
  // alias for pushTask
  def request(input: IN): Future[OUT] = pushTask(input)

//   def requestAll(inputs: Seq[IN]) =
//     Future.traverse(inputs)(input => pushTask(input))

//   import scala.jdk.CollectionConverters._
//   promises.asScala

  // called by the Scala side of the application
  private def pushTask(input: IN): Future[OUT] = {
    val payload = Request(UUID.randomUUID(), input)
    queue.offer(payload)
    val promise = CancelablePromise[OUT]()
    promises.put(payload, promise)
    promise.future
  }

  // called by the JavaScript side
  // can return null
  def pollTask(): Request[IN] = {
    queue.poll()
  }

  // called by the JavaScript side
  def pushResult(
      payload: Request[IN],
      result: OUT
  ): Unit = {
    val promise = promises(payload)
    promise.complete(Success(result))
    promises.remove(payload)
  }

  // called by the JavaScript side
  def pushFailure(
      payload: Request[IN],
      exception: Exception
  ): Unit = {
    val promise = promises(payload)
    promise.complete(Failure(exception))
    promises.remove(payload)
  }
}

// final class JsSurgeExecutorDs[IN, OUT] private (
//     queue: Queue[Payload[IN]],
//     promises: Map[Payload[IN], CancelablePromise[OUT]]
// ) {

//   // alias for pushTask
//   def invoke(input: IN) = pushTask(input)

//   // called by the Scala side of the application
//   private def pushTask(input: IN) = {
//     val promise = CancelablePromise[OUT]()
//     val payload = Payload(UUID.randomUUID(), input)
//     (
//       new JsSurgeExecutorDs(
//         queue :+ payload,
//         promises + (payload -> promise)
//       ),
//       promise.future
//     )
//   }

//   def poll = queue.dequeueOption

//   // called by the JavaScript side
//   def pushResult(
//       payload: Payload[IN],
//       result: OUT
//   ) = {
//     val promise = promises(payload)
//     promise.success(result)
//     promises - payload
//     new JsSurgeExecutorDs(queue, promises)
//   }

//   // called by the JavaScript side
//   def pushFailure(
//       payload: Payload[IN],
//       exception: Exception
//   ) = {
//     val promise = promises(payload)
//     promise.failure(exception)
//     promises - payload
//     new JsSurgeExecutorDs(queue, promises)
//   }

// }

// final case class Execution[IN, OUT](
//     executor: JsSurgeExecutorDs[IN, OUT],
//     future: CancelableFuture[OUT]
// )

// final class JsSurgeExecutor[IN, OUT] private (
//     inner: Atomic[Execution[IN, OUT]]
// ) {
//   def invoke(input: IN) = {
//     inner.transformAndGet { e =>
//       val res = e.executor.invoke(input)
//       e.copy(res._1, res._2)
//     }
//   }
// }
