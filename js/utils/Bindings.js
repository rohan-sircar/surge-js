module.exports = {
    java: {
        Product: Java.type('scala.Product'),
        UUID: Java.type('java.util.UUID'),
    },
    scala: {
        CommandModel: Java.type('surge.js.JsCommandModel'),
        SurgeCommand: Java.type('surge.scaladsl.command.SurgeCommand'),
        SurgeModel: Java.type('surge.js.JsSurgeModel'),
        Try: Java.type('scala.util.Try'),
        Option: Java.type('scala.Option'),
        List: Java.type('scala.collection.immutable.List'),
        Future: Java.type('scala.concurrent.Future'),
        Json: Java.type('play.api.libs.json.Json'),
        surge: {
            Command: Java.type('surge.js.Command'),
            State: Java.type('surge.js.State'),
            Event: Java.type('surge.js.Event')
        }
    }
}
// Java.type('')