package com.mineinabyss.chatty.helpers

import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.mineinabyss.chatty.ChattyConfig
import com.mineinabyss.chatty.ChattyMessages
import org.bukkit.entity.Player


val chattyConfig = ChattyConfig.data
val chattyMessages = ChattyMessages.data
val emoteFixer = DiscordEmoteFixer.data
val protocolManager: ProtocolManager = ProtocolLibrary.getProtocolManager()

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
