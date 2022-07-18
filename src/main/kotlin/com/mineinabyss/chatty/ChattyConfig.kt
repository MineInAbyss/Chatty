package com.mineinabyss.chatty

import com.mineinabyss.chatty.components.ChannelType
import com.mineinabyss.idofront.config.IdofrontConfig
import kotlinx.serialization.Serializable

object ChattyConfig : IdofrontConfig<ChattyConfig.Data>(chattyPlugin, Data.serializer()) {
    @Serializable
    data class Data(
        val useChattyCommandPrefix: Boolean = true,
        val channelChangedMessage: String = "You have changed channels to %channel%",
        val join: Join,
        val leave: Leave,
        val channels: Set<ChattyChannel>,
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

    @Serializable
    data class ChattyChannel(
        val channelName: String,
        val channelType: ChannelType,
        val isDefaultChannel: Boolean,
        val format: Format,
        val channelRadius: Int = 0,
        val emptyChannelMessage: String = "<red>There is no-one to read your message",
        val channelCommand: String = channelName,
        val channelCommandAliases: List<String> = listOf(),
    )

    @Serializable
    data class Format(
        val useDisplayName: Boolean = true,
        val prefix: String = "",
        val suffix: String = "",
    )
}
