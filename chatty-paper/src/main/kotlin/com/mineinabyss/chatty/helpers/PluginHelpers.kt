package com.mineinabyss.chatty.helpers

import com.mineinabyss.chatty.ChattyConfig
import com.mineinabyss.chatty.ChattyMessages
import github.scarsz.discordsrv.DiscordSRV
import io.github.bananapuncher714.bondrewd.likes.his.emotes.BondrewdLikesHisEmotes
import me.clip.placeholderapi.PlaceholderAPIPlugin


val chattyConfig = ChattyConfig.data
val chattyMessages = ChattyMessages.data
val emoteFixer = DiscordEmoteFixer.data

val bondrewd = BondrewdLikesHisEmotes.getPlugin(BondrewdLikesHisEmotes::class.java)
val discordSrv = DiscordSRV.getPlugin()
val papi = PlaceholderAPIPlugin.getPlugin(PlaceholderAPIPlugin::class.java)
