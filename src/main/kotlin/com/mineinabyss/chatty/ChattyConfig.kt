package com.mineinabyss.chatty

import com.mineinabyss.chatty.helpers.chattyPlugin
import com.mineinabyss.idofront.config.IdofrontConfig
import kotlinx.serialization.Serializable

object ChattyConfig : IdofrontConfig<ChattyConfig.Data>(chattyPlugin, Data.serializer()) {
    @Serializable
    data class Data(
        val join: Join,
        val leave: Leave,
    )

    @Serializable
    data class Join(
        val enabled: Boolean,
        val message: String,
        val firstJoin: FirstJoin,
    )

    @Serializable
    data class FirstJoin(
        val enabled: Boolean,
        val message: String,
    )

    @Serializable
    data class Leave(
        val enabled: Boolean,
        val message: String,
    )
}
