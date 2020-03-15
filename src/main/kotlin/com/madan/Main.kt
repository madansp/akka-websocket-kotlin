package com.madan

import akka.NotUsed
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.http.javadsl.ConnectHttp
import akka.http.javadsl.Http
import akka.http.javadsl.model.ws.BinaryMessage
import akka.http.javadsl.model.ws.Message
import akka.http.javadsl.server.Directives.handleWebSocketMessages
import akka.http.javadsl.server.Directives.path
import akka.stream.Materializer
import akka.stream.OverflowStrategy
import akka.stream.javadsl.*
import akka.util.ByteString
import java.time.Duration
import java.util.*


fun main() {

    val actorSystem = ActorSystem.create("Web-socket-demo")

    val materializer = Materializer.createMaterializer(actorSystem)

    val http = Http.get(actorSystem)

    val actor = actorSystem.actorOf(Props.create(CommandHandler::class.java), "CommandHandler")

    val routeFlow = path("events") {
        handleWebSocketMessages(mainFlow(actor))
    }.flow(actorSystem, materializer)


    http.bindAndHandle(routeFlow, ConnectHttp.toHost("0.0.0.0", 8080), materializer).thenRun {
        println("Web socket server is running at : localhost:8080")
    }


}

fun mainFlow(actor: ActorRef): Flow<Message, Message, NotUsed> {
    val clientId = UUID.randomUUID().toString()

    val source = Source.actorRef<Message>(
        { Optional.empty() },
        { Optional.empty() },
        100,
        OverflowStrategy.dropHead()
    )
        .mapMaterializedValue {
            actor.tell(CommandHandler.UserConnected(clientId, it), ActorRef.noSender())
            NotUsed.getInstance()
        }
        .keepAlive(Duration.ofSeconds(10)){
            BinaryMessage.create(ByteString.fromString("Pong Message"))
        }



    val sink: Sink<Message, NotUsed> = Flow.create<Message>()
        .map {
            actor.tell(CommandHandler.IncomingMessage(clientId, it.asBinaryMessage()), ActorRef.noSender())
        }
        .to(Sink.onComplete {
            actor.tell(CommandHandler.UserDisconnected(clientId), ActorRef.noSender())
        })


    return Flow.fromSinkAndSource(sink, source)
}




