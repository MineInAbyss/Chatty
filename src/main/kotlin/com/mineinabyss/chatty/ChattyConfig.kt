package com.mineinabyss.chatty

import com.mineinabyss.chatty.components.ChannelType
import com.mineinabyss.idofront.config.IdofrontConfig
import kotlinx.serialization.Serializable

object ChattyConfig : IdofrontConfig<ChattyConfig.Data>(chattyPlugin, Data.serializer()) {
    @Serializable
    data class Data(
        val useChattyCommandPrefix: Boolean = true,
        val playerHeadFont: String = "minecraft:chatty_heads",
        val ping: Ping,
        val join: Join,
        val leave: Leave,
        val channels: Set<ChattyChannel>,
        val messages: Messages,
    )

    @Serializable
    data class Join(
        val enabled: Boolean = true,
        val firstJoin: FirstJoin,
    )

    @Serializable
    data class FirstJoin(
        val enabled: Boolean = true,
    )

    @Serializable
    data class Leave(
        val enabled: Boolean,
    )

    @Serializable
    data class ChattyChannel(
        val channelName: String,
        val channelType: ChannelType,
        val permission: String = "",
        val isDefaultChannel: Boolean = false,
        val format: Format,
        val channelRadius: Int = 0,
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
        val pingReceiveFormat: String = "<yellow><b>",
        val pingSendFormat: String = "<i>"
    )

    @Serializable
    data class Messages(
        val changedPingSound: String = "Ping sound set to <i>%chatty_ping_sound%</i>",
        val noSoundError: String = "<red>You must specify a valid sound to play.",
        val channelChangedMessage: String = "<red>You have changed to channel <yellow>%chatty_channel%<red>.",
        val emptyChannelMessage: String = "<red>There is no-one to read your message",
        val firstJoinMessage: String = "<gradient:#058330:#ff9200>Welcome %player_name% to %server_name%</gradient>",
        val joinMessage: String = "<green>%player_name%<white> has joined the server.",
        val leaveMessage: String = "<red>%player_name%<white> has left the server.",
    )
}
