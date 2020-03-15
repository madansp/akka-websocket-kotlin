package com.madan

import akka.actor.AbstractActor
import akka.actor.ActorRef
import akka.http.javadsl.model.ws.BinaryMessage
import akka.http.javadsl.model.ws.TextMessage
import akka.japi.pf.ReceiveBuilder
import akka.util.ByteString

internal class CommandHandler : AbstractActor() {

    private val clients = mutableMapOf<String, ActorRef>()

    override fun createReceive(): Receive = ReceiveBuilder()
        .match(IncomingMessage::class.java) {
            println("incoming message  ${it}")

            val bytes = it.message.strictData.toArray()
            clients[it.clientId]!!.tell(BinaryMessage.create(ByteString.fromString("Hey  ${String(bytes)}")), ActorRef.noSender())

            println("message ${String(bytes)}")
        }
        .match(UserConnected::class.java) {
            println("user connected $it")
            clients[it.clientId] = it.actorRef
            it.actorRef.tell(TextMessage.create("Your client id is : ${it.clientId}"), self)
        }.match(UserDisconnected::class.java) {
            println("user disconnected $it")
            clients.remove(it.clientId)
        }
        .build()


    data class UserDisconnected(val clientId: String)

    data class UserConnected(val clientId: String, val actorRef: ActorRef)

    data class IncomingMessage(val clientId: String, val message: BinaryMessage)
}