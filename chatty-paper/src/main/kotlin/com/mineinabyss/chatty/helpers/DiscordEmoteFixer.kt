package com.mineinabyss.chatty.helpers

import com.mineinabyss.chatty.chattyPlugin
import com.mineinabyss.idofront.config.IdofrontConfig
import kotlinx.serialization.Serializable
import kotlin.io.path.div

object DiscordEmoteFixer : IdofrontConfig<DiscordEmoteFixer.Emotes>(
    chattyPlugin,
    Emotes.serializer(),
    file = (chattyPlugin.dataFolder.toPath() / "emotefixer.yml")
) {

    @Serializable
    data class Emotes(val emotes: Map<String, String>)
}
