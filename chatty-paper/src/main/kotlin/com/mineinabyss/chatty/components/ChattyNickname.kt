package com.mineinabyss.chatty.components

import com.mineinabyss.geary.papermc.access.toGeary
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

@Serializable
@SerialName("chatty:nickname")
data class ChattyNickname(val nickname: Component)
val Player.chattyNickname
    get() = this.toGeary().get<ChattyNickname>()?.nickname
