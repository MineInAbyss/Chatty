package com.mineinabyss.chatty.placeholders

import com.mineinabyss.chatty.chatty
import com.mineinabyss.chatty.components.ChannelData
import com.mineinabyss.chatty.components.SpyOnChannels
import com.mineinabyss.chatty.components.chattyNickname
import com.mineinabyss.chatty.helpers.getAllChannelNames
import com.mineinabyss.chatty.helpers.translateFullPlayerSkinComponent
import com.mineinabyss.chatty.helpers.translatePlayerHeadComponent
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.idofront.font.Space
import com.mineinabyss.idofront.textcomponents.miniMsg
import com.mineinabyss.idofront.textcomponents.serialize
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.entity.Player

fun chattyPlaceholders(player: Player?, string: String? = null): Map<String, String> {
    val channelData = player?.toGearyOrNull()?.get<ChannelData>()
    val channel = channelData?.channel
    val shift = string?.substringAfter("shift_", "0")?.toIntOrNull() ?: 0

    return mapOf(
        "all_channels" to getAllChannelNames().joinToString(", "),
        "player_available_channels" to chatty.config.channels.values.filter {
            player?.hasPermission(it.permission) ?: false
        }.joinToString(", "),
        "player_channel" to channelData?.channelId.toString(),
        "player_channel_permission" to channel?.permission.toString(),
        "player_channel_isdefault" to channel?.isDefaultChannel.toString(),
        "player_channel_type" to channel?.channelType.toString(),
        "player_channel_radius" to channel?.channelRadius.toString(),
        "player_channel_format" to channel?.format.toString(),
        "player_channel_aliases" to channel?.channelAliases.toString(),
        "player_channel_proxy_enabled" to channel?.proxy.toString(),
        "player_spy_last" to player?.toGeary()?.get<SpyOnChannels>()?.channels?.lastOrNull().toString(),

        "ping_defaultsound" to chatty.config.ping.defaultPingSound,
        "ping_volume" to chatty.config.ping.pingVolume.toString(),
        "ping_pitch" to chatty.config.ping.pingPitch.toString(),
        "ping_prefix" to chatty.config.ping.pingPrefix,
        "ping_clickreply" to chatty.config.ping.clickToReply.toString(),
        "ping_receiver_format" to chatty.config.ping.pingReceiveFormat,
        "ping_sender_format" to chatty.config.ping.pingSendFormat,
        "player_ping_sound" to channelData?.pingSound.toString(),
        "player_ping_toggle" to (channelData?.disablePingSound?.not() ?: "false").toString(),

        "nickname" to (player?.chattyNickname ?: player?.displayName()?.serialize() ?: player?.name.toString()),
        "player_head" to player?.translatePlayerHeadComponent()?.serialize().toString(),
        "player_full_skin" to player?.translateFullPlayerSkinComponent()?.serialize().toString(),
        "shift_$shift" to Space.of(shift)
    )
}

val Player?.chattyPlaceholderTags: TagResolver
    get() {
        val tagResolver = TagResolver.builder()
        chattyPlaceholders(this).map { p ->
            Placeholder.component("chatty_${p.key}", p.value.miniMsg())
        }.forEach { tagResolver.resolver(it) }

        return tagResolver.build()
    }
