const express = require('express');
const app = express();
const server = require('http').Server(app);
const io = require('socket.io')(server);
const bodyParser = require('body-parser');
const mongoose = require('mongoose');
const URL_CB = process.env.URL_CB || "http://orion:1026/ngsi-ld/v1/entities/urn:ngsi-ld:ReqMalagaParkingPrediction1/attrs";
const PORT = process.env.PORT ? (process.env.PORT) : 3000;
const MONGO_URI = process.env.MONGO_URI || "mongodb://mongodb-svc:27017/sth_test";
const fetch = require('cross-fetch')
console.log("Orion URL: " + URL_CB);

const connectWithRetry = () => {
    mongoose.connect(MONGO_URI).then(() => {
        console.log('MongoDB is connected to the web');
    }).catch(err => {
        console.log('MongoDB connection with web unsuccessful, retry after 5 seconds.');
        setTimeout(connectWithRetry, 5000);
    })
}

connectWithRetry()

const createAttr = (attr) => {
    return { "value": attr, "type": "Property" };
}



const updateEntity = (data) => {
    console.log("updateEntity")
    fetch(URL_CB, {
            body: JSON.stringify(data),
            headers: { "Content-Type": "application/json" },
            method: "PATCH"
        })
        .then(res => {
            console.log("Reply from Orion", res.ok)
            if (res.ok) {
                io.to(data.socketId.value).emit("messages", { type: "CONFIRMATION", payload: { msg: "Your request is being processed" } });
                return;
            }
            throw new Error("Error")
        })
        .catch(e => {
            io.to(data.socketId.value).emit("messages", { type: "ERROR", payload: { msg: "There has been a problem with your request" } });
            console.error(e);
        });
}

server.listen(PORT, function() {
    console.log("Listening on port " + PORT);
});


io.on('connection', function(socket) {
    console.log('New socket connection');
    socket.on('predict', (msg) => {
        console.log("new predict")
        console.log(msg)
        const { name, year, month, day, weekday, time, predictionId } = msg;
        updateEntity({
            "name": createAttr(name),
            "year": createAttr(year),
            "month": createAttr(month),
            "day": createAttr(day),
            "weekday": createAttr(weekday),
            "time": createAttr(time),
            "predictionId": createAttr(predictionId),
            "socketId": createAttr(socket.id)
        });
    })
});

app.use(express.static(__dirname + "/public/"));
app.use(bodyParser.text());
app.use(bodyParser.json());

app.post("/notify", function(req, res) {
    if (req.body && req.body.data) {
        console.log("req.body.data")
        console.log(req.body.data)
        req.body.data.map(({ socketId, predictionId, predictionValue, name, weekday, time }) => {
            io.to(socketId.value).emit('messages', {
                type: "PREDICTION",
                payload: {
                    socketId: socketId.value,
                    // name: name.value,
                    // weekday: weekday.value,
                    time: time.value,
                    predictionId: predictionId.value,
                    predictionValue: predictionValue.value
                }
            });
        });
    }
    res.sendStatus(200);
});