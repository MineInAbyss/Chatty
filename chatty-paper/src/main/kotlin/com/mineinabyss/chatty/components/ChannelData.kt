package com.mineinabyss.chatty.components

import com.mineinabyss.chatty.helpers.getDefaultChat
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.idofront.serialization.UUIDSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.entity.Player
import java.util.*

@Serializable
@SerialName("chatty:chatty_data")
data class ChannelData(
    val channelId: String = getDefaultChat().key,
    val lastChannelUsed: String = channelId,
    val disablePingSound: Boolean = false,
    val pingSound: String? = null,
    val lastMessager: @Serializable(UUIDSerializer::class) UUID? = null,
)

val Player.chattyData get() =  toGeary().getOrSetPersisting { ChannelData() }
enum class ChannelType {
    GLOBAL,
    RADIUS,
    PERMISSION,
    PRIVATE
}
