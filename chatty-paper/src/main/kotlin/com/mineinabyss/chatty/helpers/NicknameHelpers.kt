package com.mineinabyss.chatty.helpers

import com.mineinabyss.idofront.messaging.miniMsg
import org.bukkit.Bukkit
import org.bukkit.entity.Player

fun String.verifyNickLength(): Boolean {
    return when (chattyConfig.nicknames.countColorsInLength) {
        true -> this.length <= chattyConfig.nicknames.maxLength
        false -> this.miniMsg().toPlainText().length <= chattyConfig.nicknames.maxLength
    }
}

// Splits <color> and <gradient:...> tags and checks if they're allowed
fun String.verifyNickStyling(): Boolean {
    return this.getTags().all { tag -> tag in chattyConfig.nicknames.allowedTags }
}

fun List<String>.getPlayerToNick(): Player? = Bukkit.getPlayer(
    this.first().replace(chattyConfig.nicknames.nickNameOtherPrefix.toString(), "")
)

fun String.removePlayerToNickFromString(): String =
    this.split(" ").filter { it != this.split(" ").first() }.joinToString(" ")
