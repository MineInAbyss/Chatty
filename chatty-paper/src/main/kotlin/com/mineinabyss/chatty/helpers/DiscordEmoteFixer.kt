package com.mineinabyss.chatty.helpers

import com.mineinabyss.chatty.chatty
import com.mineinabyss.idofront.config.IdofrontConfig
import kotlinx.serialization.Serializable
import kotlin.io.path.div

object DiscordEmoteFixer : IdofrontConfig<DiscordEmoteFixer.Emotes>(
    chatty,
    Emotes.serializer(),
    file = (chatty.dataFolder.toPath() / "emotefixer.yml")
) {

    @Serializable
    data class Emotes(val emotes: Map<String, String>)
}
