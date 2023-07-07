package com.nat3z.qoluxe.utils

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.minecraft.client.MinecraftClient
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.io.File
import java.net.URI


class LithiumWebSocket(val url: URI, uuid: Long) : WebSocketClient(url) {
    val authenticationSignature = File("${MinecraftClient.getInstance().runDirectory}/signatures/${uuid}-player.txt")

    companion object {
        @JvmStatic
        var networkConnection: LithiumWebSocket? = null
    }

    var authorizationHandshake = "UNAUTHORIZED";

    var heldKeys = mutableListOf<UsernameKeyOwnership>()

    init {
        val signaturesDir = File("${MinecraftClient.getInstance().runDirectory}/signatures")
        if (!signaturesDir.exists()) {
            signaturesDir.mkdir()
        }

        if (authenticationSignature.exists()) {
            authorizationHandshake = authenticationSignature.readLines().first()
        }
    }
    fun getEncryptionKey(username: String): String {
        return heldKeys.firstOrNull { it.username == username }?.key ?: ""
    }
    override fun onOpen(handshakedata: ServerHandshake?) {
        if (authorizationHandshake == "") {
            sendMessage(SocketMessageTypes.KEY_EXCHANGE, "test")
        }
        println("Connected to server")

        JsonObject().apply {
            addProperty("type", SocketMessageTypes.AUTHENTICATE.name)
            addProperty("message", JsonObject().apply {
                addProperty("username", MinecraftClient.getInstance().session.username)
                addProperty("authorization", authorizationHandshake)
            }.toString())
        }.let {
            connection.send(it.toString())
        }
    }

    fun authenticate(username: String) {
        JsonObject().apply {
            addProperty("type", SocketMessageTypes.AUTHENTICATE.name)
            addProperty("message", username)
        }.let {
            connection.send(it.toString())
        }
    }

    override fun onMessage(message: String?) {
        try {
            // parse message with json object
            val json = message?.let { JsonParser.parseString(it) }?.asJsonObject
            if (json == null) {
                println("Received invalid message from server: $message")
                return
            }

            if (json.get("type").asString == SocketMessageTypes.KEY_EXCHANGE.string) {
                // send key exchange response
                sendMessage(SocketMessageTypes.KEY_EXCHANGE_RESPONSE, "test")
            } else if (json.get("type").asString == SocketMessageTypes.AUTHENTICATE_RESPONSE.string) {

            } else if (json.get("type").asString == SocketMessageTypes.KEY_EXCHANGE_RESPONSE.string) {
                heldKeys.clear()
                json.get("message").asJsonArray.forEach() {
                    heldKeys.add(UsernameKeyOwnership(it.asJsonObject.get("username").asString, it.asJsonObject.get("key").asString))
                }
                System.out.println("Received key exchange response")
            } else {
                println("Received invalid message from server: $message")
            }
        } catch (e: Exception) {
            println("Received invalid message from server: $message")
        }


    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
    }

    override fun onError(ex: java.lang.Exception?) {

    }

    // sendMessage
    fun sendMessage(type: SocketMessageTypes, message: String) {
        JsonObject().apply {
            addProperty("type", type.name)
            addProperty("message", message)
        }.let {
            connection.send(it.toString())
        }
    }

    enum class SocketMessageTypes(val string: String) {
        KEY_EXCHANGE("KEY_EXCHANGE"),
        CONNECTED_HANDSHAKE("CONNECTED_HANDSHAKE"),
        KEY_EXCHANGE_RESPONSE("KEY_EXCHANGE_RESPONSE"),
        AUTHENTICATE("AUTHENTICATE"),
        AUTHENTICATE_RESPONSE("send_private_message"),
    }

    class UsernameKeyOwnership(val username: String, val key: String);
}
