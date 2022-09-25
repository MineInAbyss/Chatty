package com.mineinabyss.chatty.helpers

import com.mineinabyss.chatty.ChattyConfig
import com.mineinabyss.chatty.ChattyMessages
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

//val bondrewd = BondrewdLikesHisEmotes.getPlugin(BondrewdLikesHisEmotes::class.java)
val chattyConfig = ChattyConfig.data
val chattyMessages = ChattyMessages.data
val emoteFixer = DiscordEmoteFixer.data
val legacy = LegacyComponentSerializer.builder().useUnusualXRepeatedCharacterHexFormat().build()
