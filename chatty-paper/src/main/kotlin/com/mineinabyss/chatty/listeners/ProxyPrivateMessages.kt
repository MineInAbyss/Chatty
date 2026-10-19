package com.mineinabyss.chatty.listeners

import com.google.common.io.ByteStreams
import com.mineinabyss.chatty.chatty
import com.mineinabyss.chatty.commands.ChattyCommands
import com.mineinabyss.chatty.commands.ChattyCommands.playPrivateMessageSendSound
import com.mineinabyss.chatty.commands.ChattyCommands.receivedPrivateMessage
import com.mineinabyss.chatty.helpers.gson
import com.mineinabyss.chatty.helpers.sendFormattedMessage
import com.mineinabyss.chatty.privateMessageChannel
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.messaging.PluginMessageListener
import java.util.*

// Payloads must stay in sync with PrivateMessageProxyListener in chatty-velocity
object ProxyPrivateMessages : PluginMessageListener {
    private const val SEND = "send"
    private const val DELIVER = "deliver"
    private const val ECHO = "echo"
    private const val NOT_FOUND = "notfound"
    private const val PLAYERS = "players"

    var networkPlayers: List<String> = emptyList()
        private set

    /** @param target player name or UUID of a player on another server */
    fun send(sender: Player, target: String, message: String) {
        if (message.isEmpty()) return
        val receiveMessage = ChattyCommands.privateMessageComponent(chatty.config.privateMessages.messageReceiveFormat, message, sender)
        sender.sendPluginMessage(chatty, privateMessageChannel, ByteStreams.newDataOutput().apply {
            writeUTF(SEND)
            writeUTF(sender.uniqueId.toString())
            writeUTF(target)
            writeUTF(gson.serialize(receiveMessage))
            writeUTF(message)
        }.toByteArray())
    }

    override fun onPluginMessageReceived(channel: String, connection: Player, bytes: ByteArray) {
        if (channel != privateMessageChannel) return
        val input = ByteStreams.newDataInput(bytes)
        when (input.readUTF()) {
            DELIVER -> {
                val senderUuid = input.readUTF()
                val receiver = Bukkit.getPlayer(UUID.fromString(input.readUTF())) ?: return
                val receiveMessage = gson.deserialize(input.readUTF())
                val message = input.readUTF()

                receiver.sendMessage(receiveMessage)
                receiver.receivedPrivateMessage(UUID.fromString(senderUuid))

                val sendMessage = ChattyCommands.privateMessageComponent(chatty.config.privateMessages.messageSendFormat, message, receiver)
                receiver.sendPluginMessage(chatty, privateMessageChannel, ByteStreams.newDataOutput().apply {
                    writeUTF(ECHO)
                    writeUTF(senderUuid)
                    writeUTF(gson.serialize(sendMessage))
                }.toByteArray())
            }

            ECHO -> {
                val sender = Bukkit.getPlayer(UUID.fromString(input.readUTF())) ?: return
                sender.sendMessage(gson.deserialize(input.readUTF()))
                sender.playPrivateMessageSendSound()
            }

            NOT_FOUND -> Bukkit.getPlayer(UUID.fromString(input.readUTF()))
                ?.sendFormattedMessage(chatty.messages.privateMessages.invalidPlayer)

            PLAYERS -> networkPlayers = List(input.readInt()) { input.readUTF() }
        }
    }
}
