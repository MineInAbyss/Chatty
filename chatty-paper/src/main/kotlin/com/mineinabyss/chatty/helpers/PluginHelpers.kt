@file:Suppress("UnstableApiUsage")

package com.mineinabyss.chatty.helpers

import com.mineinabyss.chatty.chatty
import io.papermc.paper.chat.ChatRenderer
import io.papermc.paper.event.player.AsyncChatDecorateEvent
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.entity.Player

val chattyConfig get() = chatty.config.data
val chattyMessages get() = chatty.messages.data
val emoteFixer get() = chatty.emoteFixer.data
val legacy = LegacyComponentSerializer.builder().useUnusualXRepeatedCharacterHexFormat().build()
fun GenericChattyDecorateEvent(player: Player, msg: Component) =
    AsyncChatDecorateEvent(true, player, msg, true, formattedResult(player, msg))
fun GenericChattyChatEvent(player: Player, msg: Component) =
    AsyncChatEvent(true, player, mutableSetOf(), ChatRenderer.defaultRenderer(), msg, msg)
