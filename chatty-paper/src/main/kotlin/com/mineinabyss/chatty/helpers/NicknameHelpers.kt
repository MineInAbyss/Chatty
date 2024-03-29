package com.mineinabyss.chatty.helpers

import com.mineinabyss.chatty.chatty
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Player
fun String.stripTags() = MiniMessage.miniMessage().stripTags(this)
fun String.verifyNickLength(): Boolean {
    return when (chatty.config.nicknames.countTagsInLength) {
        true -> this.length <= chatty.config.nicknames.maxLength
        false -> this.stripTags().length <= chatty.config.nicknames.maxLength
    }
}

fun List<String>.playerToNick(): Player? =
    this.first().replace(chatty.config.nicknames.nickNameOtherPrefix.toString(), "").toPlayer()

fun String.removePlayerToNickFromString(): String =
    this.split(" ").filter { it != this.split(" ").first() }.toSentence()
