package com.mineinabyss.chatty.helpers

import com.mineinabyss.chatty.components.chattyData
import com.mineinabyss.idofront.font.Space
import org.bukkit.entity.Player

fun chattyPlaceholders(player: Player, string: String) : Map<String, String> {
    val data = player.chattyData
    val ping = chattyConfig.ping
    val channel = player.getChannelFromPlayer()
    val shift = Integer.parseInt(string.substringAfter("shift_", "0"))

    return mapOf(
        "available_channels" to getAllChannelNames().joinToString(),
        "player_channel" to data.channelId,
        "player_channel_permission" to channel?.permission.toString(),
        "player_channel_isdefault" to channel?.isDefaultChannel.toString(),
        "player_channel_type" to channel?.channelType.toString(),
        "player_channel_radius" to channel?.channelRadius.toString(),
        "player_channel_format" to channel?.format.toString(),
        "player_channel_aliases" to channel?.channelAliases.toString(),
        "player_channel_proxy_enabled" to channel?.proxy.toString(),

        "ping_defaultsound" to ping.defaultPingSound,
        "ping_volume" to ping.pingVolume.toString(),
        "ping_pitch" to ping.pingPitch.toString(),
        "ping_prefix" to ping.pingPrefix,
        "ping_clickreply" to ping.clickToReply.toString(),
        "ping_receiver_format" to ping.pingReceiveFormat,
        "ping_sender_format" to ping.pingSendFormat,
        "player_ping_sound" to data.pingSound.toString(),
        "player_ping_toggle" to (!data.disablePingSound).toString(),

        "player_displayname" to (player.chattyData.displayName ?: player.name)+"<reset>",
        "player_head" to player.translatePlayerHeadComponent().serialize(),
        "shift_$shift" to Space.of(shift)
    )
}
