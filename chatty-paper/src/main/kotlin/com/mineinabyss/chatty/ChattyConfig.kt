package com.mineinabyss.chatty

import com.mineinabyss.chatty.components.ChannelType
import com.mineinabyss.chatty.helpers.ChattyTags
import com.mineinabyss.idofront.config.IdofrontConfig
import kotlinx.serialization.Serializable

object ChattyConfig : IdofrontConfig<ChattyConfig.Data>(chattyPlugin, Data.serializer()) {
    @Serializable
    data class Data(
        val useChattyCommandPrefix: Boolean = true,
        val playerHeadFont: String = "minecraft:chatty_heads",
        val nicknames: Nickname = Nickname(),
        val ping: Ping = Ping(),
        val join: Join = Join(),
        val leave: Leave = Leave(),
        val proxy: Proxy = Proxy(),
        val privateMessages: PrivateMessages = PrivateMessages(),
        val channels: Map<String, ChattyChannel> = mutableMapOf("global" to ChattyChannel(ChannelType.GLOBAL)),
    )

    @Serializable
    data class PrivateMessages(
        val enabled: Boolean = true,
        val proxy: Boolean = true,
        val messageSendFormat: String = "<gold>You -> %player_displayname%: ",
        val messageReceiveFormat: String = "<gold>%player_displayname% -> You: ",
        val messageSendSound: String = "",
        val messageReceivedSound: String = "",
    )

    @Serializable
    data class Nickname(
        val permission: String = "chatty.nickname",
        val nickOtherPermission: String = "chatty.nickname.other",
        val bypassFormatPermission: String = "chatty.nickname.bypassformat",
        val maxLength: Int = 32,
        val countColorsInLength: Boolean = false,
        val nickNameOtherPrefix: Char = '@',
        val allowedTags: List<ChattyTags> = emptyList()
    )

    @Serializable
    data class Join(
        val enabled: Boolean = true,
        val firstJoin: FirstJoin = FirstJoin(),
    )

    @Serializable
    data class FirstJoin(
        val enabled: Boolean = true,
    )

    @Serializable
    data class Leave(
        val enabled: Boolean = true,
    )

    @Serializable
    data class Proxy(
        val enableProxySwitchMessages: Boolean = true,
    )

    @Serializable
    data class ChattyChannel(
        val channelType: ChannelType,
        val permission: String = "",
        val proxy: Boolean = false,
        val discordsrv: Boolean = true,
        val isDefaultChannel: Boolean = false,
        val isStaffChannel: Boolean = false,
        val format: Format = Format(),
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
}
