package com.mineinabyss.chatty

import org.bukkit.Bukkit

val chatty: ChattyPlugin by lazy { Bukkit.getPluginManager().getPlugin("chatty") as ChattyPlugin }
const val chattyProxyChannel = "chatty:proxy"
object ChattyContext {
    val isPlaceholderApiLoaded: Boolean
        get() = chatty.server.pluginManager.isPluginEnabled("PlaceholderAPI")
    val isDiscordSRVLoaded: Boolean
        get() = chatty.server.pluginManager.isPluginEnabled("DiscordSRV")
}
