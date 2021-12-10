const { java, scala } = require('./utils/Bindings')

const Function1Ext = Java.extend(scala.Function1)
const Function1Impl = new Function1Ext({
    apply: function (x) { return x + 1 }
})

const jsTest = Java.type("surge.js.JsTest")
// jsTest.apply(x => x + 1)
const appScheduler = scala.AppScheduler.apply()
jsTest.task(Function1Impl).runToFuture(appScheduler)

setTimeout(() => { console.log("DoNE") }, 5000)