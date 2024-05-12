@file:Suppress("UnstableApiUsage")

package com.mineinabyss.chatty.helpers

import com.mineinabyss.idofront.textcomponents.serialize
import io.papermc.paper.chat.ChatRenderer
import io.papermc.paper.event.player.AsyncChatDecorateEvent
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.chat.SignedMessage
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.entity.Player

fun GenericChattyDecorateEvent(player: Player, msg: Component) =
    AsyncChatDecorateEvent(player, msg)
fun GenericChattyChatEvent(player: Player, msg: Component) =
    AsyncChatEvent(true, player, mutableSetOf(), ChatRenderer.defaultRenderer(), msg, msg, SignedMessage.system(msg.serialize(), null))
