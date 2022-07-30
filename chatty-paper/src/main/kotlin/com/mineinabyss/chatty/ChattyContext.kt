package com.mineinabyss.chatty

import org.bukkit.Bukkit

val chatty: ChattyPlugin by lazy { Bukkit.getPluginManager().getPlugin("chatty") as ChattyPlugin }
const val chattyProxyChannel = "chatty:proxy"
object ChattyContext {
    val isBondrewdLikesHisEmotesLoaded: Boolean
        get() = chatty.server.pluginManager.isPluginEnabled("BondrewdLikesHisEmotes")
    val isPlaceholderApiLoaded: Boolean
        get() = chatty.server.pluginManager.isPluginEnabled("PlaceholderAPI")
    val isDiscordSRVLoaded: Boolean
        get() = chatty.server.pluginManager.isPluginEnabled("DiscordSRV")
    val isProtocolLibLoaded: Boolean
        get() = chatty.server.pluginManager.isPluginEnabled("ProtocolLib")

}
