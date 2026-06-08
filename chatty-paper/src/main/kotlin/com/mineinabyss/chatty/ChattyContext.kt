package com.mineinabyss.chatty

import com.mineinabyss.chatty.helpers.DiscordEmoteFixer
import com.mineinabyss.chatty.queries.SpyingPlayersQuery
import com.mineinabyss.geary.systems.query.CachedQuery
import org.bukkit.plugin.Plugin

const val chattyProxyChannel = "chatty:proxy"
const val discordSrvChannel = "chatty:discordsrv"

interface ChattyContext : Plugin {
    val config: ChattyConfig
    val messages: ChattyMessages
    val emotefixer: DiscordEmoteFixer
    val isPlaceholderApiLoaded: Boolean
    val isDiscordSRVLoaded: Boolean
    val spyingPlayers: CachedQuery<SpyingPlayersQuery>

    companion object {
        var instance: ChattyContext? = null
    }
}

val chatty get() = ChattyContext.instance ?: error("Chatty not loaded!")