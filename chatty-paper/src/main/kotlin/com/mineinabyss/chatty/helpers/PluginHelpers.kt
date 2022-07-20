package com.mineinabyss.chatty.helpers

import com.mineinabyss.chatty.ChattyConfig
import com.mineinabyss.chatty.ChattyMessages
import github.scarsz.discordsrv.DiscordSRV
import me.clip.placeholderapi.PlaceholderAPIPlugin


val chattyConfig = ChattyConfig.data
val messages = ChattyMessages.data
val discordSrv = DiscordSRV.getPlugin()
val papi = PlaceholderAPIPlugin.getPlugin(PlaceholderAPIPlugin::class.java)
