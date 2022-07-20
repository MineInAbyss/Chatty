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
        val proxy: Proxy,
        val channels: Map<String, ChattyChannel>,
    )

    @Serializable
    data class Join(
        val enabled: Boolean = true,
        val sendAcrossProxy: Boolean = true,
        val firstJoin: FirstJoin,
    )

    @Serializable
    data class FirstJoin(
        val enabled: Boolean = true,
        val sendAcrossProxy: Boolean = true,
    )

    @Serializable
    data class Leave(
        val enabled: Boolean,
        val sendAcrossProxy: Boolean = true,
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
}
