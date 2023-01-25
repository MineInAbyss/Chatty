package com.mineinabyss.chatty.helpers

import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags
import org.bukkit.permissions.Permission

object ChattyPermissions {
    const val NICKNAME = "chatty.nickname"
    const val NICKNAME_OTHERS = "chatty.nickname.others"
    const val BYPASS_TAG_PERM = "chatty.tags.bypass"
    val chatFormattingPerms = mapOf(
        Permission("chatty.tags.color") to StandardTags.color(),
        Permission("chatty.tags.rainbow") to StandardTags.rainbow(),
        Permission("chatty.tags.gradient") to StandardTags.gradient(),
        Permission("chatty.tags.bold") to StandardTags.decorations(TextDecoration.BOLD),
        Permission("chatty.tags.strikethrough") to StandardTags.decorations(TextDecoration.STRIKETHROUGH),
        Permission("chatty.tags.underline") to StandardTags.decorations(TextDecoration.UNDERLINED),
        Permission("chatty.tags.italic") to StandardTags.decorations(TextDecoration.ITALIC),
        Permission("chatty.tags.obfuscated") to StandardTags.decorations(TextDecoration.OBFUSCATED),
        Permission("chatty.tags.font") to StandardTags.font(),
        Permission("chatty.tags.insertion") to StandardTags.insertion(),
        Permission("chatty.tags.click") to StandardTags.clickEvent(),
        Permission("chatty.tags.hover") to StandardTags.hoverEvent(),
        Permission("chatty.tags.reset") to StandardTags.reset(),
    )
}