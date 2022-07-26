package com.mineinabyss.chatty

import com.mineinabyss.chatty.components.ChannelType
import com.mineinabyss.chatty.helpers.ChattyTags
import com.mineinabyss.idofront.config.IdofrontConfig
import com.mineinabyss.idofront.serialization.DurationSerializer
import kotlinx.serialization.Serializable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

object ChattyConfig : IdofrontConfig<ChattyConfig.Data>(chatty, Data.serializer()) {
    @Serializable
    data class Data(
        val playerHeadFont: String = "minecraft:chatty_heads",
        val nicknames: Nickname = Nickname(),
        val chat: Chat = Chat(),
        val book: Book = Book(),
        val sign: Sign = Sign(),
        val ping: Ping = Ping(),
        val join: Join = Join(),
        val leave: Leave = Leave(),
        val proxy: Proxy = Proxy(),
        val privateMessages: PrivateMessages = PrivateMessages(),
        // Mutable so other plugins can add channels on their end
        // Might be a safer way to do this but 3AM so first solution is best solution
        val channels: MutableMap<String, ChattyChannel> = mutableMapOf("global" to ChattyChannel(ChannelType.GLOBAL)),
    )

    @Serializable
    data class Chat(
        val bypassFormatPermission: String = "chatty.chat.bypassformat",
        val commandSpyFormat: String = "<gold>%chatty_player_displayname%: %chatty_command%",
        val allowedTags: List<ChattyTags> = emptyList(),
    )

    @Serializable
    data class PrivateMessages(
        val enabled: Boolean = true,
        val proxy: Boolean = true,
        val messageReplyTime: @Serializable(with = DurationSerializer::class) Duration = 5.minutes,
        val messageSendFormat: String = "<gold>You -> %%chatty_player_displayname%: ",
        val messageReceiveFormat: String = "<gold>%%chatty_player_displayname% -> You: ",
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
        val discordSrvChannelID: String = "Global"
    )

    @Serializable
    data class ChattyChannel(
        val channelType: ChannelType,
        val permission: String = "",
        val proxy: Boolean = false,
        val discordsrv: Boolean = true,
        val isDefaultChannel: Boolean = false,
        val isStaffChannel: Boolean = false,
        val format: String = "",
        val channelRadius: Int = 0,
        val channelAliases: List<String> = listOf(),
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
    data class Book(
        val useDisplayNameForAuthor: Boolean = false,
        val bypassFormatPermission: String = "chatty.book.bypassformat",
        val allowedTags: List<ChattyTags> = emptyList(),
    )

    @Serializable
    data class Sign(
        val bypassFormatPermission: String = "chatty.sign.bypassformat",
        val allowedTags: List<ChattyTags> = emptyList(),
    )
}
