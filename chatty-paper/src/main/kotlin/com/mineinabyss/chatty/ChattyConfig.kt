package com.mineinabyss.chatty

import com.charleskorn.kaml.YamlComment
import com.deepl.api.Language
import com.deepl.api.LanguageCode
import com.mineinabyss.chatty.components.ChannelType
import com.mineinabyss.chatty.helpers.TranslationLanguage
import com.mineinabyss.idofront.serialization.DurationSerializer
import com.mineinabyss.idofront.textcomponents.miniMsg
import kotlinx.serialization.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

@Serializable
data class ChattyConfig(
    val translation: Translation = Translation(),
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
    data class Translation(
        @YamlComment("Which type of translation should be done for the target language.",
            "FORCE - Forces all messages to be translated to the target language",
            "SKIP_SAME_LANGUAGE - Avoids translating a message if the senders language is same as ones own",
            "ALL_SAME_LANGUAGE - Translates all messages to the receivers language"
        )
        val type: TargetLanguageType = TargetLanguageType.FORCE,
        val targetLanguage: TranslationLanguage = TranslationLanguage.English_US,
        val rateLimitPerPlayer: Boolean = true,
        internal val authKey: String? = null
    ) {
        enum class TargetLanguageType {
            FORCE, SKIP_SAME_LANGUAGE, ALL_SAME_LANGUAGE
        }
    }

    @Serializable
    data class Chat(
        val disableChatSigning: Boolean = true,
        val commandSpyFormat: String = "<gold><chatty_nickname>: ",
        @YamlComment("Valid formats: STRIKETHROUGH, CENSOR, DELETE, BLOCK", "STRIKETHROUGH: Replaces filtered words with a strikethrough", "CENSOR: Replaces filtered words with a censor", "DELETE: Deletes filtered words", "BLOCK: Blocks filtered words from being sent")
        val filterFormat: FilterFormat = FilterFormat.CENSOR,
        @SerialName("filters") val _filters: List<String> = listOf(),
        val formatURLs: Boolean = true,
        val urlReplacements: Set<UrlReplacements> = setOf(
            UrlReplacements("^https:\\/\\/cdn\\.discordapp\\.com\\/attachments\\/[^\\/]+\\/[^\\/]+\\.png\\?.*\$", "[Discord Image]"),
            UrlReplacements("^https:\\/\\/cdn\\.discordapp\\.com\\/attachments\\/[^\\/]+\\/[^\\/]+\\.mp4\\?.*\$", "[Discord Video]"),
            UrlReplacements("youtube.com", "[YouTube]"),
        )
    ) {

        @Serializable
        data class UrlReplacements(private val pattern: String, @SerialName("replacement") private val _replacement: String) {
            @Transient val regex = pattern.toRegex()
            @Transient val replacement = _replacement.miniMsg()
        }

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
