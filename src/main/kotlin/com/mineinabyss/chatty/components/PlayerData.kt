package com.mineinabyss.chatty.components

import com.mineinabyss.chatty.ChattyConfig
import com.mineinabyss.chatty.helpers.getDefaultChat
import com.mineinabyss.geary.papermc.access.toGeary
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.entity.Player

@Serializable
@SerialName("chatty:player_data")
class PlayerData(
    var channel: ChattyConfig.ChattyChannel = getDefaultChat(),
    var disablePingSound: Boolean = false,
    var pingSound: String? = null,
)

val Player.playerData get() = toGeary().getOrSetPersisting { PlayerData() }
enum class ChannelType {
    GLOBAL,
    RADIUS,
    PERMISSION,
    PRIVATE
}
