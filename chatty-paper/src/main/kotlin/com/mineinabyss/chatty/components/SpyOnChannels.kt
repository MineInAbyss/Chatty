package com.mineinabyss.chatty.components

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("chatty:spy_on_channels")
class SpyOnChannels(
    val channels: MutableList<String> = mutableListOf()
)
