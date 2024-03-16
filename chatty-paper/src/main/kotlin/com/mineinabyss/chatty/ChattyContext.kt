package com.mineinabyss.chatty

import com.mineinabyss.chatty.helpers.DiscordEmoteFixer
import com.mineinabyss.chatty.queries.SpyingPlayersQuery
import com.mineinabyss.geary.systems.query.CachedQueryRunner
import com.mineinabyss.idofront.di.DI

const val chattyProxyChannel = "chatty:proxy"
const val discordSrvChannel = "chatty:discordsrv"
val chatty by DI.observe<ChattyContext>()
interface ChattyContext {
    val plugin: ChattyPlugin
    val config: ChattyConfig
    val messages: ChattyMessages
    val emotefixer: DiscordEmoteFixer
    val isPlaceholderApiLoaded: Boolean
    val isDiscordSRVLoaded: Boolean
    val spyingPlayers: CachedQueryRunner<SpyingPlayersQuery>
}
