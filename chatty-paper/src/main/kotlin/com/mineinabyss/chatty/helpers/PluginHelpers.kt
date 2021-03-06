package com.mineinabyss.chatty.helpers

import com.mineinabyss.chatty.ChattyConfig
import com.mineinabyss.chatty.ChattyMessages
import org.bukkit.entity.Player


val chattyConfig = ChattyConfig.data
val chattyMessages = ChattyMessages.data
val emoteFixer = DiscordEmoteFixer.data

fun Player.checkPermission(perm: String): Boolean {
    return if (perm.isEmpty()) true
    else this.hasPermission(perm)
}

enum class ChattyTags {
    BOLD,
    ITALIC,
    UNDERLINE,
    STRIKETHROUGH,
    OBFUSCATED,
    TEXTCOLOR,
    HEXCOLOR,
    GRADIENT,
    RAINBOW,
    HOVER,
    CLICK,
    FONT,
    SPACES,
    INSERTION,
    RESET,
    TRANSITION,
    KEYBIND,
    TRANSLATABLE
}
