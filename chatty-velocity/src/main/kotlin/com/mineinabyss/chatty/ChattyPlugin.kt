package com.mineinabyss.chatty

import com.google.inject.Inject
import com.velocitypowered.api.event.EventManager
import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.PlayerChatEvent
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.kt.event.registerCoroutineContinuationAdapter
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.proxy.ProxyServer
import net.kyori.adventure.text.Component
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
    fun onInit(event: ProxyInitializeEvent) {
        logger.info("The Kotlin Language Adapter is initialized!")
        eventManager.register(this, ChattyProxyListener(server, logger,))
    }
}

class ChattyProxyListener(private var server: ProxyServer, private var logger: Logger) {

    private val servers = server.allServers.filter { it != server }

    @Inject
    fun ChattyPlugin(server: ProxyServer, logger: Logger) {
        this.server = server
        this.logger = logger
    }

    @Subscribe
    fun PlayerChatEvent.onPlayerChat() {
        servers.forEach { regServer ->
            regServer.playersConnected.forEach { player ->
                player.sendMessage(Component.text(message))
            }

        }
    }
}
