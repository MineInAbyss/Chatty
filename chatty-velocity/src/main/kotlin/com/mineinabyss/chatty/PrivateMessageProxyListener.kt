package com.mineinabyss.chatty

import com.google.common.io.ByteStreams
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.connection.PluginMessageEvent
import com.velocitypowered.api.event.player.ServerPostConnectEvent
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.ServerConnection
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

// Payloads must stay in sync with ProxyPrivateMessages in chatty-paper
class PrivateMessageProxyListener(private val server: ProxyServer) {

    @Subscribe
    fun PluginMessageEvent.onPrivateMessage() {
        if (identifier != privateMessageChannel) return
        result = PluginMessageEvent.ForwardResult.handled()
        val source = source as? ServerConnection ?: return
        val input = ByteStreams.newDataInput(data)
        when (input.readUTF()) {
            SEND -> {
                val senderUuid = input.readUTF()
                val target = input.readUTF().let { target ->
                    runCatching { UUID.fromString(target) }.getOrNull()?.let(server::getPlayer)?.getOrNull()
                        ?: server.getPlayer(target).getOrNull()
                }
                val targetServer = target?.currentServer?.getOrNull()
                if (target == null || targetServer == null) {
                    source.sendPluginMessage(privateMessageChannel, ByteStreams.newDataOutput().apply {
                        writeUTF(NOT_FOUND)
                        writeUTF(senderUuid)
                    }.toByteArray())
                    return
                }
                targetServer.sendPluginMessage(privateMessageChannel, ByteStreams.newDataOutput().apply {
                    writeUTF(DELIVER)
                    writeUTF(senderUuid)
                    writeUTF(target.uniqueId.toString())
                    writeUTF(input.readUTF())
                    writeUTF(input.readUTF())
                }.toByteArray())
            }

            ECHO -> {
                val senderUuid = input.readUTF()
                server.getPlayer(UUID.fromString(senderUuid)).getOrNull()?.currentServer?.getOrNull()
                    ?.sendPluginMessage(privateMessageChannel, data)
            }
        }
    }

    @Subscribe
    fun ServerPostConnectEvent.onConnect() = broadcastPlayers()

    @Subscribe
    fun DisconnectEvent.onDisconnect() = broadcastPlayers(player)

    // A server without players cannot receive plugin messages, it gets the list once someone connects to it
    private fun broadcastPlayers(leaving: Player? = null) {
        val names = server.allPlayers.filter { it != leaving }.map { it.username }
        val data = ByteStreams.newDataOutput().apply {
            writeUTF(PLAYERS)
            writeInt(names.size)
            names.forEach(::writeUTF)
        }.toByteArray()
        server.allServers.forEach { it.sendPluginMessage(privateMessageChannel, data) }
    }

    private companion object {
        const val SEND = "send"
        const val DELIVER = "deliver"
        const val ECHO = "echo"
        const val NOT_FOUND = "notfound"
        const val PLAYERS = "players"
    }
}
