package com.mineinabyss.chatty

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import com.charleskorn.kaml.encodeToStream
import com.google.inject.Inject
import com.mineinabyss.chatty.event.registerCoroutineContinuationAdapter
import com.velocitypowered.api.event.EventManager
import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.connection.PluginMessageEvent
import com.velocitypowered.api.event.player.ServerConnectedEvent
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyReloadEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
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
    @Subscribe(order = PostOrder.FIRST)
    fun onInit(e: ProxyInitializeEvent) {
        logger.info("The Kotlin Language Adapter is initialized!")
        server.channelRegistrar.register(chattyChannel)
        server.channelRegistrar.register(discordChannel)
        eventManager.register(this, ChattyProxyListener(server))

        val file = dataDirectory.resolve("already_joined.json").toFile().apply { parentFile.mkdirs() }
        if (!file.exists()) this::class.java.classLoader.getResourceAsStream("already_joined.json")?.use {
            file.writeBytes(it.readAllBytes())
        }
        alreadyJoined = runCatching {
            file.inputStream().use {
                Json.decodeFromStream<AlreadyJoined>(it).alreadyJoined
            }
        }.onFailure { it.printStackTrace() }.getOrDefault(mutableSetOf())


        val config = dataDirectory.resolve("config.yml").toFile().apply { parentFile.mkdirs() }
        if (!config.exists()) this::class.java.classLoader.getResourceAsStream("config.yml")?.use {
            config.writeBytes(it.readAllBytes())
        }
        chattyConfig = runCatching {
            config.inputStream().use {
                Yaml.default.decodeFromStream<ChattyConfig>(it)
            }
        }.onFailure { it.printStackTrace() }.getOrDefault(ChattyConfig())

        eventManager.register(this, ChattyJoinListener(server))
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Subscribe
    fun onShutdown(e: ProxyShutdownEvent) {
        dataDirectory.resolve("already_joined.json").toFile().apply { createNewFile() }.outputStream().use {
            runCatching {
                Json.encodeToStream(AlreadyJoined(alreadyJoined), it)
            }.onFailure {
                it.printStackTrace()
            }
        }
        dataDirectory.resolve("config.yml").toFile().apply { createNewFile() }.outputStream().use {
            runCatching {
                Yaml.default.encodeToStream(chattyConfig, it)
            }.onFailure {
                it.printStackTrace()
            }
        }
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

class ChattyJoinListener @Inject constructor(private val proxyServer: ProxyServer) {

    @Subscribe(order = PostOrder.NORMAL)
    fun ServerConnectedEvent.onPlayerJoin() {
        val prev = previousServer.getOrNull()
        when {
            chattyConfig.join.firstJoinMessage.isNotEmpty() && player.uniqueId.toString() !in alreadyJoined -> {
                server.sendMessage(chattyConfig.join.firstMessage(player, server))
                alreadyJoined.add(player.uniqueId.toString())
            }
            prev != null -> {
                if (chattyConfig.switch.toMessage.isNotEmpty()) server.sendMessage(chattyConfig.switch.toServerMessage(player, prev, server))
                if (chattyConfig.switch.fromMessage.isNotEmpty()) prev.sendMessage(chattyConfig.switch.fromServerMessage(player, prev, server))
            }
            else -> {
                if (chattyConfig.join.message.isNotEmpty()) server.sendMessage(chattyConfig.join.message(player, server))
                if (chattyConfig.join.otherServerMessage.isNotEmpty()) proxyServer.allServers.forEach {
                    if (it != server) it.sendMessage(chattyConfig.join.otherServerMessage(player, it))
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

    @Subscribe(order = PostOrder.NORMAL)
    fun PluginMessageEvent.onPluginMessage() {
        if (identifier != chattyChannel && identifier != discordChannel) return
        //Hacky way of knowing what the old server was, prob a better way lol
        server.allServers.filter { (it.serverInfo.name !in this.source.toString()) }.forEach {
            it.sendPluginMessage(identifier, this.data)
        }
    }
}
