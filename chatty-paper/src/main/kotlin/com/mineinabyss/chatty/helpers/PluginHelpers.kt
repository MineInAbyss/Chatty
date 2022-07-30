package com.mineinabyss.chatty.helpers

import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.mineinabyss.chatty.ChattyConfig
import com.mineinabyss.chatty.ChattyMessages
import io.github.bananapuncher714.bondrewd.likes.his.emotes.BondrewdLikesHisEmotes
import org.bukkit.Bukkit
import org.bukkit.entity.Player


val chattyConfig = ChattyConfig.data
val chattyMessages = ChattyMessages.data
val emoteFixer = DiscordEmoteFixer.data
val protocolManager: ProtocolManager = ProtocolLibrary.getProtocolManager()
val bondrewd: BondrewdLikesHisEmotes by lazy { Bukkit.getPluginManager().getPlugin("BondrewdLikesHisEmotes") as BondrewdLikesHisEmotes }

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
