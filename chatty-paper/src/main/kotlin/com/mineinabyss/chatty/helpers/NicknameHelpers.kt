package com.mineinabyss.chatty.helpers

import com.mineinabyss.chatty.chatty
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Player
fun String.stripTags() = MiniMessage.miniMessage().stripTags(this)


fun List<String>.playerToNick(): Player? =
    this.first().replace(chatty.config.nicknames.nickNameOtherPrefix.toString(), "").toPlayer()

fun String.removePlayerToNickFromString(): String =
    this.split(" ").filter { it != this.split(" ").first() }.toSentence()
