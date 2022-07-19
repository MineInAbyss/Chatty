package com.mineinabyss.chatty

import com.google.inject.Inject
import com.velocitypowered.api.event.EventManager
import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.PluginMessageEvent
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.kt.event.registerCoroutineContinuationAdapter
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.slf4j.Logger

@Plugin(id = "chatty", name = "chatty", version = "0.1")
class ChattyPlugin @Inject constructor(
    private val server: ProxyServer,
    private val logger: Logger,
    private val eventManager: EventManager,
) {

    init {
        eventManager.registerCoroutineContinuationAdapter(logger)
    }

    @Subscribe(order = PostOrder.FIRST)
    fun onInit(e: ProxyInitializeEvent) {
        val chattyChannel = MinecraftChannelIdentifier.create("chatty", "proxy")
        logger.info("The Kotlin Language Adapter is initialized!")
        server.channelRegistrar.register(chattyChannel)
        eventManager.register(this, ChattyProxyListener(server, logger))
    }
}

class ChattyProxyListener @Inject constructor(
    private var server: ProxyServer,
    private var logger: Logger,
) {

    @Inject
    fun ChattyProxyListener(server: ProxyServer, logger: Logger) {
        this.server = server
        this.logger = logger
    }

    @Subscribe(order = PostOrder.NORMAL)
    fun PluginMessageEvent.onPluginMessage() {
        if (identifier != MinecraftChannelIdentifier.create("chatty", "proxy")) return
        logger.info("PluginMessageEvent")
        server.sendMessage(Component.text("Plugin Message received!"))
        //Hacky way of knowing what the old server was, prob a better way lol
        server.allServers.filter { (it.serverInfo.name !in this.source.toString()) }.forEach {
            server.sendMessage(Component.text("Sending message to ${it.serverInfo.name} on $identifier").color(TextColor.color(0xFF0000)))
            it.sendPluginMessage(identifier, this.data)
        }
    }
}
