package com.mineinabyss.chatty.components

import com.mineinabyss.chatty.helpers.getDefaultChat
import com.mineinabyss.chatty.helpers.toPlayer
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.entity.Player

@Serializable
@SerialName("chatty:chatty_data")
class ChannelData(
    var channelId: String = getDefaultChat().key,
    var lastChannelUsed: String = channelId,
    var disablePingSound: Boolean = false,
    var pingSound: String? = null,
    @SerialName("lastMessager")
    var _lastMessager: String? = null,
) {
    var lastMessager
        get() = _lastMessager?.toPlayer()
        set(value) = run { _lastMessager = value?.name }
}

val Player.chattyData get() =  toGeary().getOrSetPersisting { ChannelData() }
enum class ChannelType {
    GLOBAL,
    RADIUS,
    PERMISSION,
    PRIVATE
}
