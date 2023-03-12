package com.mineinabyss.chatty.helpers

import com.mineinabyss.chatty.chatty
import com.mineinabyss.idofront.config.IdofrontConfig
import kotlinx.serialization.Serializable

object DiscordEmoteFixer : IdofrontConfig<DiscordEmoteFixer.Data>(chatty, Data.serializer()) {
    @Serializable
    data class Data(val emotes: Map<String, String> = emptyMap())
}
