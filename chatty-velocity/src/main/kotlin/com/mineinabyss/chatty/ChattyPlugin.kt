package com.mineinabyss.chatty

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import com.charleskorn.kaml.encodeToStream
import com.google.inject.Inject
import com.mineinabyss.chatty.event.registerCoroutineContinuationAdapter
import com.velocitypowered.api.event.EventManager
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.connection.PluginMessageEvent
import com.velocitypowered.api.event.player.ServerPostConnectEvent
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyReloadEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.ServerConnection
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier
import java.nio.file.Path
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import org.slf4j.Logger
import kotlin.jvm.optionals.getOrNull


val chattyChannel: MinecraftChannelIdentifier = MinecraftChannelIdentifier.create("chatty", "proxy")
val discordChannel: MinecraftChannelIdentifier = MinecraftChannelIdentifier.create("chatty", "discordsrv")
val privateMessageChannel: MinecraftChannelIdentifier = MinecraftChannelIdentifier.create("chatty", "privatemessage")

lateinit var alreadyJoined: MutableSet<String>
lateinit var chattyConfig: ChattyConfig

@Plugin(id = "chatty", name = "chatty", version = "0.9")
class ChattyPlugin @Inject constructor(
    private val server: ProxyServer,
    private val logger: Logger,
    private val eventManager: EventManager,
    @DataDirectory private val dataDirectory: Path,
) {

    init {
        eventManager.registerCoroutineContinuationAdapter(logger)
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Subscribe(priority = Short.MAX_VALUE)
    fun onInit(e: ProxyInitializeEvent) {
        logger.info("The Kotlin Language Adapter is initialized!")
        server.channelRegistrar.register(chattyChannel)
        server.channelRegistrar.register(discordChannel)
        server.channelRegistrar.register(privateMessageChannel)
        eventManager.register(this, ChattyProxyListener(server))
        eventManager.register(this, PrivateMessageProxyListener(server))

        alreadyJoined = AlreadyJoined.readConfig(dataDirectory)
        chattyConfig = ChattyConfig.readConfig(dataDirectory)

        eventManager.register(this, ChattyJoinListener(server, dataDirectory))
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Subscribe
    fun onShutdown(e: ProxyShutdownEvent) {
        // already_joined.json is written on each new player, saving the in-memory set here would
        // undo any edit made to the file while the proxy was running
        ChattyConfig.saveToFile(dataDirectory)
    }

    @Subscribe
    fun onReload(e: ProxyReloadEvent) {
        val config = dataDirectory.resolve("config.yml").toFile()
        chattyConfig = runCatching {
            config.inputStream().use {
                Yaml.default.decodeFromStream<ChattyConfig>(it)
            }
        }.onFailure { it.printStackTrace() }.getOrDefault(ChattyConfig())
    }
}

class ChattyJoinListener @Inject constructor(private val proxyServer: ProxyServer, private val dataDirectory: Path) {

    // ServerPostConnectEvent rather than ServerConnectedEvent, the joining player is only part of
    // the target server's audience once the connection completed
    @Subscribe
    fun ServerPostConnectEvent.onPlayerJoin() {
        val server = player.currentServer.getOrNull()?.server ?: return
        val prev = previousServer
        when {
            chattyConfig.join.firstJoinMessage.isNotEmpty() && player.uniqueId.toString() !in alreadyJoined -> {
                val message = chattyConfig.join.firstMessage(player, server)
                proxyServer.allServers.forEach { it.sendMessage(message) }
                AlreadyJoined.markJoined(player.uniqueId, dataDirectory)
            }
            prev != null -> {
                if (chattyConfig.switch.fromMessage.isNotEmpty()) server.sendMessage(chattyConfig.switch.fromServerMessage(player, prev, server))
                if (chattyConfig.switch.toMessage.isNotEmpty()) prev.sendMessage(chattyConfig.switch.toServerMessage(player, prev, server))
            }
            else -> {
                if (chattyConfig.join.message.isNotEmpty()) server.sendMessage(chattyConfig.join.message(player, server))
                if (chattyConfig.join.otherServerMessage.isNotEmpty()) proxyServer.allServers.forEach {
                    if (it.serverInfo.name != server.serverInfo.name) it.sendMessage(chattyConfig.join.otherServerMessage(player, server))
                }
            }
        }
    }

    @Subscribe
    fun DisconnectEvent.onPlayerQuit() {
        if (loginStatus != DisconnectEvent.LoginStatus.SUCCESSFUL_LOGIN) return

        val server = player.currentServer.getOrNull()?.server ?: return
        if (chattyConfig.leave.message.isNotEmpty()) server.sendMessage(chattyConfig.leave.message(player, server))
        if (chattyConfig.leave.otherServerMessage.isNotEmpty()) proxyServer.allServers.forEach {
            if (it.serverInfo.name != server.serverInfo.name) it.sendMessage(chattyConfig.leave.otherServerMessage(player, server))
        }
    }
}

class ChattyProxyListener @Inject constructor(private val server: ProxyServer) {

    @Subscribe
    fun PluginMessageEvent.onPluginMessage() {
        if (identifier != chattyChannel && identifier != discordChannel) return
        // Never forward to clients, and ignore payloads not sent by a backend server
        result = PluginMessageEvent.ForwardResult.handled()
        val sourceServer = source as? ServerConnection ?: return
        server.allServers.filter { it.serverInfo.name != sourceServer.serverInfo.name }.forEach {
            it.sendPluginMessage(identifier, this.data)
        }
    }
}
