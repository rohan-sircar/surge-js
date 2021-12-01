// import { Bindings } from "./utils/Bindings"
const { java, scala } = require('./utils/Bindings')
const surge = scala.surge
const express = require('express');
const app = express();

console.log(java)
console.log(scala)
const commandModel = scala.CommandModel.apply((aggregate, command) => {
    console.log(aggregate)
    console.log(command)
    scala.Try.apply(scala.List.empty())
},
    (aggregate, event) => {
        console.log(aggregate)
        console.log(event)
        aggregate
    })
console.log(commandModel)
const surgeModel = scala.SurgeModel.apply("library", "library-state", "library-events", commandModel)
console.log(surgeModel)
const surgeCommand = scala.SurgeCommand.apply(surgeModel)
console.log(surgeCommand)

// const fn = async () => {
surgeCommand.start()

app.get('/api/library/books/:id', async (req, res) => {
    console.log(req.params)
    const r = await surgeCommand.aggregateFor(java.UUID.fromString(req.params.id))
        .sendCommand(surge.Command.apply(req.params.id, JSON.stringify({ action: "GetBook" })))
    // .map((result) => {
    //     res.send(result.toString())
    //     return ""
    // }, Java.type('scala.concurrent.Promise').apply())
    console.log(r)
    res.send("ok")
})
app.post('/api/library/books/:id', (req, res) => {
    console.log(req.params)
    surgeCommand.aggregateFor(java.UUID.fromString(req.params.id))
        .sendCommand(surge.Command.apply(req.params.id, JSON.stringify({ action: "CreateBook" })))
        .flatMap(result => scala.Future.successful(""))
})
app.patch('/api/library/books/:id', (req, res) => {
    console.log(req.params)
    surgeCommand.aggregateFor(java.UUID.fromString(req.params.id))
        .sendCommand(surge.Command.apply(req.params.id, JSON.stringify({ action: "UpdateBook" })))
        .flatMap(result => scala.Future.successful(""))
})

const server = app.listen(8082, () => {
    var host = server.address().address
    var port = server.address().port
    console.log("Example app listening at http://%s:%s", host, port)
})

server.on('close', async () => await surgeCommand.stop())
// }
// fn()