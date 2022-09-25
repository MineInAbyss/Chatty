package com.mineinabyss.chatty.helpers

import com.mineinabyss.idofront.messaging.stripTags
import org.bukkit.Bukkit
import org.bukkit.entity.Player

fun String.verifyNickLength(): Boolean {
    return when (chattyConfig.nicknames.countTagsInLength) {
        true -> this.length <= chattyConfig.nicknames.maxLength
        false -> this.stripTags().length <= chattyConfig.nicknames.maxLength
    }
}

fun List<String>.getPlayerToNick(): Player? = Bukkit.getPlayer(
    this.first().replace(chattyConfig.nicknames.nickNameOtherPrefix.toString(), "")
)

fun String.removePlayerToNickFromString(): String =
    this.split(" ").filter { it != this.split(" ").first() }.toSentence()
