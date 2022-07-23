package com.mineinabyss.chatty

import com.mineinabyss.idofront.plugin.getService
import org.bukkit.Bukkit

val chatty: ChattyPlugin by lazy { Bukkit.getPluginManager().getPlugin("chatty") as ChattyPlugin }
interface ChattyContext {
    companion object : ChattyContext by getService()
    val isPlaceholderApiLoaded: Boolean
        get() = chatty.server.pluginManager.isPluginEnabled("PlaceholderAPI")
    val isDiscordSRVLoaded: Boolean
        get() = chatty.server.pluginManager.isPluginEnabled("DiscordSRV")
}
