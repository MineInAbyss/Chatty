package com.mineinabyss.chatty.helpers

import com.mineinabyss.chatty.ChattyConfig
import com.mineinabyss.chatty.ChattyMessages
import io.papermc.paper.chat.ChatRenderer
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.entity.Player

val chattyConfig get() = ChattyConfig()
val chattyMessages get() = ChattyMessages()
val emoteFixer = DiscordEmoteFixer()
val legacy = LegacyComponentSerializer.builder().useUnusualXRepeatedCharacterHexFormat().build()
fun GenericChattyChatEvent(player: Player, msg: Component) =
    AsyncChatEvent(true, player, mutableSetOf(), ChatRenderer.defaultRenderer(), msg, msg)
