package com.mineinabyss.chatty

import com.mineinabyss.idofront.config.IdofrontConfig
import kotlinx.serialization.Serializable
import kotlin.io.path.div

object ChattyMessages : IdofrontConfig<ChattyMessages.Messages>(chattyPlugin, Messages.serializer(), file = (chattyPlugin.dataFolder.toPath() / "config.yml")) {

    @Serializable
    data class Messages(
        val ping: Pings,
        val channels: Channels,
        val proxies: Proxies,
        val joinLeave: JoinLeave,
        val other: Other
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
