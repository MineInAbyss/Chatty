package com.mineinabyss.chatty

import com.mineinabyss.idofront.config.IdofrontConfig
import kotlinx.serialization.Serializable

object ChattyMessages : IdofrontConfig<ChattyMessages.Data>(chatty, Data.serializer()) {
    @Serializable
    class Data(
        val nicknames: Nicknames = Nicknames(),
        val privateMessages: PrivateMessages = PrivateMessages(),
        val spying: Spying = Spying(),
        val ping: Pings = Pings(),
        val channels: Channels = Channels(),
        val proxies: Proxies = Proxies(),
        val joinLeave: JoinLeave = JoinLeave(),
        val other: Other = Other()
    ) {

        @Serializable
        data class PrivateMessages(
            val disabled: String = "<red>Private messages are disabled",
            val invalidPlayer: String = "<red>This player is not online or does not exist",
            val emptyReply: String = "<red>You have noone to reply to"
        )

        @Serializable
        data class Spying(
            val commandSpyOn: String = "<green>You are now spying on commands",
            val commandSpyOff: String = "<red>You are no longer spying on commands",
            val startSpyingOnChannel: String = "<gold>Started spying on <italic><chatty_player_spy_last>",
            val stopSpyingOnChannel: String = "<gold>Stopped spying on <italic><chatty_player_spy_last>",
            val cannotSpyOnChannel: String = "<red>You cannot spy on <italic><chatty_player_spy_last>",
            val noChannelWithName: String = "<red>No channel by this name exists."
        )

        @Serializable
        data class Nicknames(
            val selfSuccess: String = "<green>You changed your nickname to <i><chatty_nickname>!",
            val selfDenied: String = "<red>You can't change your nickname!",
            val selfEmpty: String = "<yellow>Removed nickname!",
            val otherSuccess: String = "<green>You changed %player_name%'s nickname to <i><chatty_nickname>!",
            val otherDenied: String = "<red>You can't change %player_name%'s nickname!",
            val otherEmpty: String = "<yellow>Removed nickname for %player_name%!",
            val invalidPlayer: String = "<red>That player doesn't exist!",
            val consoleNicknameSelf: String = "<red>Sadly console cannot have cool nickname :(",
            val tooLong: String = "<red>This nickname contains formatting that are not allowed!",
        )

        @Serializable
        data class Pings(
            val toggledPingSound: String = "Ping sound is now <i><chatty_player_ping_toggle></i>.",
            val changedPingSound: String = "Ping sound set to <i><chatty_player_ping_sound></i>",
            val invalidPingSound: String = "<red>You must specify a valid sound to play.",
        )

        @Serializable
        data class Channels(
            val availableChannels: String = "<gold>Available channels are:<newline><yellow><chatty_player_available_channels>",
            val channelChanged: String = "<red>You have changed to channel <yellow><chatty_player_channel><red>.",
            val noChannelWithName: String = "No channel by this name exists.",
            val missingChannelPermission: String = "You do not have permission to join this channel.",
            val emptyChannelMessage: String = "<red>There is no-one to read your message",
        )

        @Serializable
        data class Proxies(
            val proxyJoin: String = "<green>+ <white>| <aqua><chatty_nickname>%player_server%)",
            val proxyLeave: String = "<red>- <white>| <aqua><chatty_nickname>",
            val proxySwitchToMessage: String = "<gray>↑<white> ┃ <aqua><chatty_nickname> <gray>left to <aqua><chatty_player_proxy_new_server> server.",
            val proxySwitchFromMessage: String = "<gray>↑<white> ┃ <aqua><chatty_nickname> <gray>left to <aqua><chatty_player_proxy_new_server> server.",
        )

        @Serializable
        data class JoinLeave(
            val firstJoinMessage: String = "<gradient:#058330:#ff9200>Welcome %player_name% to %server_name%</gradient>",
            val joinMessage: String = "<green>%player_name%<white> has joined the server.",
            val leaveMessage: String = "<red>%player_name%<white> has left the server.",
        )

        @Serializable
        data class Other(
            val disallowedStyling: String = "<red>Your message contains formatting you cannot use!",
            val configReloaded: String = "<green>Chatty config reloaded.",
            val messagesReloaded: String = "<green>Chatty messages reloaded.",
            val nickNameChanged: String = "Nickname set to <white><i><chatty_nickname>.",
        )
    }
}
