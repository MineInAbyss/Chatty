package com.mineinabyss.chatty

import com.mineinabyss.chatty.components.ChannelType
import com.mineinabyss.idofront.config.IdofrontConfig
import kotlinx.serialization.Serializable

object ChattyConfig : IdofrontConfig<ChattyConfig.Data>(chattyPlugin, Data.serializer()) {
    @Serializable
    data class Data(
        val useChattyCommandPrefix: Boolean = true,
        val channelChangedMessage: String = "You have changed channels to %channel%",
        val playerHeadFont: String = "minecraft:chatty_heads",
        val ping: Ping,
        val join: Join,
        val leave: Leave,
        val channels: Set<ChattyChannel>,
    )

    @Serializable
    data class Join(
        val enabled: Boolean = true,
        val message: String = "<green>%player_name%<white> has joined the server.",
        val firstJoin: FirstJoin,
    )

    @Serializable
    data class FirstJoin(
        val enabled: Boolean = true,
        val message: String = "<gradient:#058330:#ff9200>Welcome %player_name% to %server_name%</gradient>",
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
        val permission: String = "",
        val isDefaultChannel: Boolean = false,
        val format: Format,
        val channelRadius: Int = 0,
        val emptyChannelMessage: String? = null,
        val channelAliases: List<String> = listOf(),
    )

    @Serializable
    data class Format(
        val useDisplayName: Boolean = true,
        val prefix: String = "",
        val suffix: String = "",
        val messageFormat: String = "",
    )

    @Serializable
    data class Ping(
        val enabledChannels: List<String> = listOf(),
        val defaultPingSound: String = "block.amethyst_block.place",
        val alternativePingSounds: List<String> = emptyList(),
        val pingVolume: Float = 1.0f,
        val pingPitch: Float = 1.0f,
        val pingPrefix: String = "@",
        val clickToReply: Boolean = true,
        val pingFormat: String = "<yellow><b>"
    )
}
