package com.mineinabyss.chatty

import com.mineinabyss.idofront.config.IdofrontConfig
import kotlinx.serialization.Serializable
import kotlin.io.path.div

object ChattyMessages : IdofrontConfig<ChattyMessages.Messages>(chattyPlugin, Messages.serializer(), file = (chattyPlugin.dataFolder.toPath() / "messages.yml")) {

    @Serializable
    data class Messages(
        val nicknames: Nicknames,
        val privateMessages: PrivateMessages,
        val ping: Pings,
        val channels: Channels,
        val proxies: Proxies,
        val joinLeave: JoinLeave,
        val other: Other
    )

    @Serializable
    data class PrivateMessages(
        val disabled: String = "<red>Private messages are disabled",
        val invalidPlayer: String = "<red>Invalid player",
    )

    @Serializable
    data class Nicknames(
        val selfSuccess: String = "<green>You changed your nickname to <i>%player_displayname%</i>!",
        val selfDenied: String = "<red>You can't change your nickname!",
        val selfEmpty: String = "<yellow>Removed nickname!",
        val otherSuccess: String = "<green>You changed %player_name%'s nickname to <i>%player_displayname%</i>!",
        val otherDenied: String = "<red>You can't change %player_name%'s nickname!",
        val otherEmpty: String = "<yellow>Removed nickname for %player_name%!",
        val invalidPlayer: String = "<red>That player doesn't exist!",
        val consoleNicknameSelf: String = "<red>Sadly console cannot have cool nickname :(",
        val disallowedStyling: String = "<red>This nickname contains formatting that are not allowed!",
        val tooLong: String = "<red>The nickname was too long!",
    )

    @Serializable
    data class Pings(
        val toggledPingSound: String = "Ping sound is now <i>%chatty_player_ping_toggle%</i>.",
        val changedPingSound: String = "Ping sound set to <i>%chatty_ping_sound%</i>",
        val invalidPingSound: String = "<red>You must specify a valid sound to play.",
    )

    @Serializable
    data class Channels(
        val availableChannels: String = "<gold>Available channels are:<newline><yellow>%chatty_available_channels%",
        val channelChanged: String = "<red>You have changed to channel <yellow>%chatty_channel%<red>.",
        val noChannelWithName: String = "No channel by this name exists.",
        val missingChannelPermission: String = "You do not have permission to join this channel.",
        val emptyChannelMessage: String = "<red>There is no-one to read your message",
    )

    @Serializable
    data class Proxies(
        val proxyJoin: String = "<green>+ <white>| <aqua>%player_displayname%",
        val proxyLeave: String = "<red>- <white>| <aqua>%player_displayname%",
        val proxySwitchToMessage: String = "<gray>↑<white> ┃ <aqua>%player_displayname% <gray>left to <aqua>%player_proxy_new_server% server.",
        val proxySwitchFromMessage: String = "<gray>↑<white> ┃ <aqua>%player_displayname% <gray>left to <aqua>%player_proxy_new_server% server.",
    )

    @Serializable
    data class JoinLeave(
        val firstJoinMessage: String = "<gradient:#058330:#ff9200>Welcome %player_name% to %server_name%</gradient>",
        val joinMessage: String = "<green>%player_name%<white> has joined the server.",
        val leaveMessage: String = "<red>%player_name%<white> has left the server.",
    )

    @Serializable
    data class Other(
        val configReloaded: String = "<green>Chatty config reloaded.",
        val messagesReloaded: String = "<green>Chatty messages reloaded.",
        val nickNameChanged: String = "Nickname set to <white><i>&player_displayname%</i></white>.",
    )
}
