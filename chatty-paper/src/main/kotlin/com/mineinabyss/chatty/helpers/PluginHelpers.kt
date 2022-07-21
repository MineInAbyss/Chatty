package com.mineinabyss.chatty.helpers

import com.mineinabyss.chatty.ChattyConfig
import com.mineinabyss.chatty.ChattyMessages
import github.scarsz.discordsrv.DiscordSRV
import io.github.bananapuncher714.bondrewd.likes.his.emotes.BondrewdLikesHisEmotes
import me.clip.placeholderapi.PlaceholderAPIPlugin
import org.bukkit.Bukkit


val chattyConfig = ChattyConfig.data
val chattyMessages = ChattyMessages.data
val emoteFixer = DiscordEmoteFixer.data

val bondrewd: BondrewdLikesHisEmotes by lazy { Bukkit.getPluginManager().getPlugin("BondrewdLikesHisEmotes") as BondrewdLikesHisEmotes }
val discordSrv: DiscordSRV by lazy { Bukkit.getPluginManager().getPlugin("DiscordSRV") as DiscordSRV }
val papi: PlaceholderAPIPlugin by lazy { Bukkit.getPluginManager().getPlugin("PlaceholderAPI") as PlaceholderAPIPlugin }

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
