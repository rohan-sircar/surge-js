module.exports = {
    java: {
        Product: Java.type('scala.Product'),
        UUID: Java.type('java.util.UUID'),
    },
    scala: {
        CommandModel: Java.type('surge.js.JsCommandModel'),
        SurgeCommand: Java.type('surge.js.JsSurgeCommand'),
        SurgeModel: Java.type('surge.js.JsSurgeModel'),
        Try: Java.type('scala.util.Try'),
        Option: Java.type('scala.Option'),
        List: Java.type('scala.collection.immutable.List'),
        ListBuffer: Java.type('scala.collection.mutable.ListBuffer'),
        Future: Java.type('scala.concurrent.Future'),
        Json: Java.type('play.api.libs.json.Json'),
        Function1: Java.type('scala.Function1'),
        surge: {
            Command: Java.type('surge.js.Command'),
            State: Java.type('surge.js.State'),
            Event: Java.type('surge.js.Event')
        },
        // AppScheduler: Java.type("surge.js.AppScheduler"),
        JsExecutor: Java.type('surge.js.JsExecutor'),
        Scheduler: Java.type('monix.execution.Scheduler'),
        makeList: Java.type('surge.js.makeList'),
        emptyListBuffer: Java.type('surge.js.emptyListBuffer')
    }
}
// Java.type('')