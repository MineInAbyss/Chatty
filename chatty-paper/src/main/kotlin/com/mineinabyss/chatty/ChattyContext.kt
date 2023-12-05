package com.mineinabyss.chatty

import com.mineinabyss.chatty.helpers.DiscordEmoteFixer
import com.mineinabyss.chatty.queries.SpyingPlayers
import com.mineinabyss.idofront.config.IdofrontConfig
import com.mineinabyss.idofront.di.DI
import org.bukkit.Bukkit

const val chattyProxyChannel = "chatty:proxy"
val chatty by DI.observe<ChattyContext>()
interface ChattyContext {
    val plugin: ChattyPlugin
    val config: ChattyConfig
    val messages: ChattyMessages
    val emotefixer: DiscordEmoteFixer
    val isPlaceholderApiLoaded: Boolean
    val isDiscordSRVLoaded: Boolean
    val spyingPlayers: SpyingPlayers
}
