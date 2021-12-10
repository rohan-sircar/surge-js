const { java, scala } = require('./utils/Bindings')
const surge = scala.surge
const express = require('express');
const app = express();
app.use(express.json());

const scheduler = scala.Scheduler.global()
// scala.List.apply(1, 2, 3)
// console.log(scala.List)

const commandHandler = (aggregate, command) => {
    // console.log(aggregate)
    // console.log(command)
    const cmd = JSON.parse(command.value())
    const lb = scala.emptyListBuffer.apply()
    if (cmd.action == 'CreateBook') {
        lb.addOne(surge.Event.apply(command.aggregateId(), JSON.stringify({ kind: "BookCreated", title: cmd.data.title, author: cmd.data.author })))
    }
    else if (cmd.action == 'UpdateBook') {
        const a = aggregate.get()
        lb.addOne(surge.Event.apply(a.aggregateId(), JSON.stringify({ kind: "BookUpdated", title: cmd.data.title })))
    }
    else if (cmd.action == 'GetBook') {
        const a = aggregate.get()
        lb.addOne(surge.Event.apply(a.aggregateId(), JSON.stringify({ kind: "RetrieveBook" })))
    }
    // scala.List.apply()

    // scala.makeList.apply(surge.Event.apply(command.aggregateId(), JSON.stringify({ action: "Retrieve Book" })))
    return lb.toList()
}
const eventHandler = (aggregate, event) => {
    console.log("Running event handler")
    // console.log(aggregate)
    // console.log(event)
    const evt = JSON.parse(event.value())
    if (evt.kind == "BookCreated") {
        return scala.Option.apply(scala.surge.State.apply(event.aggregateId(), JSON.stringify({ title: evt.title, author: evt.author })))
    }
    else if (evt.kind == "BookUpdated") {
        const a = aggregate.get
        const oldData = JSON.parse(a.value())
        return scala.Option.apply(scala.surge.State.apply(a.aggregateId(), JSON.stringify({ title: evt.title || oldData.title, author: evt.author || oldData.author })))
    } else if (evt.kind == "RetrieveBook") {
        return aggregate
    }
}
const eventExecutor = scala.JsExecutor.apply(scheduler)
const commandExecutor = scala.JsExecutor.apply(scheduler)
const surgeCommandProtoExecutor = scala.JsExecutor.apply(scheduler)
const commandModel = new scala.CommandModel(commandExecutor, eventExecutor, scheduler)
const surgeModel = scala.SurgeModel.apply("library", "library-state", "library-events", commandModel)
// console.log(surgeModel)
const surgeCommand = scala.SurgeCommand.apply(surgeModel, surgeCommandProtoExecutor, scheduler)

surgeCommand.start()


function pollEvery(n) {
    setInterval(() => {
        const request = eventExecutor.pollTask();
        if (request != null) {
            console.log("Event handler: Found item")
            console.log("Event handler: request: ", request)
            const payloadValue = request.value()
            console.log("Event handler: payload: ", payloadValue)
            const state = payloadValue._1()
            const event = payloadValue._2()
            console.log("Event handler: Fetched task with state: " + state + " and event: " + event)
            const newState = eventHandler(state, event)
            eventExecutor.pushResult(request, newState)
            console.log("Event handler: Pushed result!")
        } else {
            console.log("Event handler: no task available!")
        }
    }, n)
    setInterval(() => {
        const request = commandExecutor.pollTask();
        if (request != null) {
            console.log("Found item")
            console.log("request: ", request)
            const payloadValue = request.value()
            console.log("payload: ", payloadValue)
            const state = payloadValue._1()
            const command = payloadValue._2()
            console.log("Fetched task with state: " + state + " and command: " + command)
            const events = commandHandler(state, command)
            console.log(events)
            commandExecutor.pushResult(request, events)
            console.log("Pushed result!")
        } else {
            console.log("no task available!")
        }
    }, n)
    setInterval(() => {
        const request = surgeCommandProtoExecutor.pollTask();
        if (request != null) {
            console.log("Command proto: Found item")
            console.log("request: ", request)
            const payloadValue = request.value()
            console.log("payload: ", payloadValue)
            const command = payloadValue.value()
            const cb = payloadValue.cb()
            console.log("executing", cb(command))
        } else {
            console.log("Command proto: no task available!")
        }
    }, n + 100)
}

pollEvery(500)

const id = java.UUID.randomUUID()

function sendSurgeCmd(cmd) {
    return new Promise((resolve, reject) => {
        try {
            surgeCommand.aggregateFor(id).sendCommand(cmd, r => resolve(r))
        } catch (error) {
            reject(error)
        }
    })
}

// setTimeout(async () => {
//     console.log("Sending command 1")
//     const r = await sendSurgeCmd(surge.Command.apply(id.toString(), JSON.stringify({ action: "CreateBook", data: { title: "Book1", author: "Author1" } })))
//     console.log("Command 1", r)
// }, 100)

// setTimeout(async () => {

//     console.log("Sending command 2")
//     surgeCommand.aggregateFor(id)
//         .sendCommand(surge.Command.apply(id.toString(), JSON.stringify({ action: "GetBook" })), cmd => {
//             console.log("Command2: From js:", cmd.aggregateState().get().value())
//         })
// }, 500)

app.get('/api/library/books/:id', async (req, res) => {
    console.log(req.params)
    const id = req.params.id
    const r = await sendSurgeCmd(surge.Command.apply(id, JSON.stringify({ action: "GetBook" })))
    console.log("Surge Command Result", r)
    res.send(r.aggregateState().get().value())
})
app.post('/api/library/books/:id', async (req, res) => {
    console.log(req.params)
    const id = req.params.id
    console.log("reqbody", req)
    const r = await sendSurgeCmd(surge.Command.apply(id, JSON.stringify(req.body)))
    console.log("Surge Command Result", r)
    res.send(r.aggregateState().get().value())
})
app.patch('/api/library/books/:id', async (req, res) => {
    console.log(req.params)
    const id = req.params.id
    const r = await sendSurgeCmd(surge.Command.apply(id, JSON.stringify(req.body)))
    console.log("Surge Command Result", r)
    res.send(r.aggregateState().get().value())
})

const server = app.listen(8082, () => {
    var host = server.address().address
    var port = server.address().port
    console.log("Example app listening at http://%s:%s", host, port)
})

server.on('close', () => surgeCommand.stop())