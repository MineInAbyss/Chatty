package com.mineinabyss.chatty.helpers

import kotlinx.serialization.Serializable

@Serializable
data class DiscordEmoteFixer(val emotes: Map<String, String> = emptyMap())
