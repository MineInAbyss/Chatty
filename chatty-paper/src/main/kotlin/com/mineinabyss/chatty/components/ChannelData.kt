package com.mineinabyss.chatty.components

import com.mineinabyss.chatty.ChattyChannel
import com.mineinabyss.chatty.chatty
import com.mineinabyss.chatty.helpers.getDefaultChat
import com.mineinabyss.idofront.serialization.UUIDSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
@SerialName("chatty:chatty_data")
data class ChannelData(
    val channelId: String = getDefaultChat().key,
    val lastChannelUsedId: String = channelId,
    val disablePingSound: Boolean = false,
    val pingSound: String? = null,
    val lastMessager: @Serializable(UUIDSerializer::class) UUID? = null,
) {
    val channel: ChattyChannel? get() = chatty.config.channels[channelId]
    val lastChannelUsed: ChattyChannel? get() = chatty.config.channels[lastChannelUsedId]

    fun withChannelVerified(): ChannelData {
        if (channelId !in chatty.config.channels)
            return copy(channelId = getDefaultChat().key)
        return this
    }
}

//val Player.chattyData get() =  toGeary().getOrSetPersisting { ChannelData() }

enum class ChannelType {
    GLOBAL,
    RADIUS,
    PERMISSION,
    PRIVATE
}
