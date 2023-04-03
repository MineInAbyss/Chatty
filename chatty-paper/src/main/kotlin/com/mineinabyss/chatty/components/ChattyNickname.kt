package com.mineinabyss.chatty.components

import com.mineinabyss.chatty.chattyConfig
import com.mineinabyss.chatty.helpers.parseTags
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.entity.Player

@Serializable
@SerialName("chatty:nickname")
data class ChattyNickname(val nickname: String)
var Player.chattyNickname
    get() = this.toGeary().get<ChattyNickname>()?.nickname
    set(value) = this.toGeary().run {
        if (chattyConfig.nicknames.useDisplayName) this@chattyNickname.displayName(value?.parseTags(this@chattyNickname) ?: name())
        value?.let { setPersisting(ChattyNickname(it)) } ?: remove<ChattyNickname>()
    }
