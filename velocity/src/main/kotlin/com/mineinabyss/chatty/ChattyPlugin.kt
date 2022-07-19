package com.mineinabyss.chatty

import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.PlayerChatEvent
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.proxy.ProxyServer
import org.slf4j.Logger

@Plugin(id = "chatty", name = "chatty", version = "0.1")
class ChattyPlugin {

    private lateinit var server: ProxyServer
    private lateinit var logger: Logger

    @Inject
    fun ChattyPlugin(server: ProxyServer, logger: Logger) {
        this.server = server
        this.logger = logger
    }

    @Subscribe
    fun ProxyInitializeEvent.onProxyInitialization() {
        server.eventManager.register(this, ChattyProxyListener())
    }
}

class ChattyProxyListener {

    private lateinit var server: ProxyServer
    private lateinit var logger: Logger

    @Inject
    fun ChattyPlugin(server: ProxyServer, logger: Logger) {
        this.server = server
        this.logger = logger
    }

    @Subscribe
    fun PlayerChatEvent.onPlayerChat() {
        logger.info("Player ${player.username} said $message")
    }
}
