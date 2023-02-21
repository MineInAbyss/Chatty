package com.mineinabyss.chatty

import com.google.inject.Inject
import com.velocitypowered.api.event.EventManager
import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.PluginMessageEvent
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier
import org.slf4j.Logger

@Plugin(id = "chatty", name = "chatty", version = "0.1")
class Chatty @Inject constructor(
    private val server: ProxyServer,
    private val logger: Logger,
    private val eventManager: EventManager,
) {

    @Subscribe(order = PostOrder.FIRST)
    fun onInit(e: ProxyInitializeEvent) {
        val chattyChannel = MinecraftChannelIdentifier.create("chatty", "proxy")
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
        //Hacky way of knowing what the old server was, prob a better way lol
        server.allServers.filter { (it.serverInfo.name !in this.source.toString()) }.forEach {
            it.sendPluginMessage(identifier, this.data)
        }
    }
}
