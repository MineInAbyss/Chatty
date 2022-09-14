package com.mineinabyss.chatty.helpers

import com.mineinabyss.chatty.ChattyConfig
import com.mineinabyss.chatty.ChattyMessages
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.entity.Player

//val bondrewd = BondrewdLikesHisEmotes.getPlugin(BondrewdLikesHisEmotes::class.java)
val chattyConfig = ChattyConfig.data
val chattyMessages = ChattyMessages.data
val emoteFixer = DiscordEmoteFixer.data
val mm = MiniMessage.miniMessage()
val plainText = PlainTextComponentSerializer.plainText()
val legacy = LegacyComponentSerializer.builder().useUnusualXRepeatedCharacterHexFormat().build()

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
