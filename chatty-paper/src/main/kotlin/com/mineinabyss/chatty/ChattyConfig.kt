package com.mineinabyss.chatty

import com.charleskorn.kaml.YamlComment
import com.mineinabyss.chatty.components.ChannelType
import com.mineinabyss.idofront.serialization.DurationSerializer
import kotlinx.serialization.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

@Serializable
data class ChattyConfig(
    val playerHeadFont: String = "minecraft:chatty_heads",
    val nicknames: Nickname = Nickname(),
    val chat: Chat = Chat(),
    val ping: Ping = Ping(),
    val join: Join = Join(),
    val leave: Leave = Leave(),
    val proxy: Proxy = Proxy(),
    val privateMessages: PrivateMessages = PrivateMessages(),
    // Mutable so other plugins can add channels on their end
    // Might be a safer way to do this but 3AM so first solution is the best solution
    val channels: MutableMap<String, ChattyChannel> = mutableMapOf("global" to ChattyChannel(ChannelType.GLOBAL, messageDeletion = ChattyChannel.MessageDeletion(true))),
) {

    @Serializable
    data class Chat(
        val disableChatSigning: Boolean = true,
        val commandSpyFormat: String = "<gold><chatty_nickname>: ",
        @YamlComment("Valid formats: STRIKETHROUGH, CENSOR, DELETE, BLOCK", "STRIKETHROUGH: Replaces filtered words with a strikethrough", "CENSOR: Replaces filtered words with a censor", "DELETE: Deletes filtered words", "BLOCK: Blocks filtered words from being sent")
        val filterFormat: FilterFormat = FilterFormat.CENSOR,
        @SerialName("filters") val _filters: List<String> = listOf(),
    ) {
        enum class FilterFormat {
            STRIKETHROUGH, CENSOR, DELETE, BLOCK
        }
        @Transient
        val filters: List<@Contextual Regex> = _filters.map { it.toRegex() }
    }

    @Serializable
    data class PrivateMessages(
        val enabled: Boolean = true,
        val proxy: Boolean = true,
        val messageReplyTime: @Serializable(with = DurationSerializer::class) Duration = 5.minutes,
        val messageSendFormat: String = "<gold>You -> <chatty_nickname>: ",
        val messageReceiveFormat: String = "<gold><chatty_nickname> -> You: ",
        val messageSendSound: String = "",
        val messageReceivedSound: String = "",
    )

    @Serializable
    data class Nickname(
        val useDisplayName: Boolean = true,
        val maxLength: Int = 32,
        val countTagsInLength: Boolean = false,
        val nickNameOtherPrefix: Char = '@',
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
        val discordSrvChannelID: String = "Global",
        val sendProxyMessagesToDiscord: Boolean = true,
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
