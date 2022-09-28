package com.mineinabyss.chatty.placeholders

import com.mineinabyss.chatty.components.SpyOnChannels
import com.mineinabyss.chatty.components.chattyData
import com.mineinabyss.chatty.components.chattyNickname
import com.mineinabyss.chatty.helpers.*
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.idofront.font.Space
import com.mineinabyss.idofront.messaging.miniMsg
import com.mineinabyss.idofront.messaging.serialize
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.entity.Player

fun chattyPlaceholders(player: Player?, string: String? = null) : Map<String, String> {
    val channel = player?.getChannelFromPlayer()
    val shift = if (string == null) 0 else try {
        Integer.parseInt(string.substringAfter("shift_", "0"))
    } catch (e: NumberFormatException) { 0 }

    return mapOf(
        "available_channels" to getAllChannelNames().joinToString(", "),
        "player_channel" to player?.chattyData?.channelId.toString(),
        "player_channel_permission" to channel?.permission.toString(),
        "player_channel_isdefault" to channel?.isDefaultChannel.toString(),
        "player_channel_type" to channel?.channelType.toString(),
        "player_channel_radius" to channel?.channelRadius.toString(),
        "player_channel_format" to channel?.format.toString(),
        "player_channel_aliases" to channel?.channelAliases.toString(),
        "player_channel_proxy_enabled" to channel?.proxy.toString(),
        "player_spy_last" to player?.toGeary()?.get<SpyOnChannels>()?.channels?.last().toString(),

        "ping_defaultsound" to chattyConfig.ping.defaultPingSound,
        "ping_volume" to chattyConfig.ping.pingVolume.toString(),
        "ping_pitch" to chattyConfig.ping.pingPitch.toString(),
        "ping_prefix" to chattyConfig.ping.pingPrefix,
        "ping_clickreply" to chattyConfig.ping.clickToReply.toString(),
        "ping_receiver_format" to chattyConfig.ping.pingReceiveFormat,
        "ping_sender_format" to chattyConfig.ping.pingSendFormat,
        "player_ping_sound" to player?.chattyData?.pingSound.toString(),
        "player_ping_toggle" to (player?.chattyData?.disablePingSound?.not() ?: "false").toString(),

        "nickname" to (player?.chattyNickname ?: player?.displayName())?.serialize().toString(),
        "player_head" to player?.translatePlayerHeadComponent()?.serialize().toString(),
        "player_full_skin" to player?.translateFullPlayerSkinComponent()?.serialize().toString(),
        "shift_$shift" to Space.of(shift)
    )
}

val Player?.chattyPlaceholderTags: TagResolver get() {
    val tagResolver = TagResolver.builder()
    chattyPlaceholders(this).map { p ->
        Placeholder.component("chatty_${p.key}", p.value.miniMsg())
    }.forEach { tagResolver.resolver(it) }

    return tagResolver.build()

    /*return TagResolver.resolver(
        Placeholder.component("chatty:player_head", translatePlayerHeadComponent()),
        Placeholder.component("chatty:player_skin", translateFullPlayerSkinComponent()),
        Placeholder.component("chatty:nickname", chattyNickname ?: displayName()),

        Placeholder.component("chatty:available_channels", getAllChannelNames().joinToString(", ").miniMsg()),
        Placeholder.component("chatty:channel", chattyData.channelId.miniMsg()),
        Placeholder.component("chatty:channel:permission", channel?.permission.toString().miniMsg()),
        Placeholder.component("chatty:channel:isdefault", channel?.isDefaultChannel.toString().miniMsg()),
        Placeholder.component("chatty:channel:type", channel?.channelType.toString().miniMsg()),
        Placeholder.component("chatty:channel:format", channel?.channelRadius.toString().miniMsg()),
        Placeholder.component("chatty:channel:aliases", channel?.format.toString().miniMsg()),
        Placeholder.component("chatty:channel:proxy_enabled", channel?.proxy.toString().miniMsg()),
        Placeholder.component("chatty:player_spy_last", toGeary().get<SpyOnChannels>()?.channels?.last().toString().miniMsg()),

        Placeholder.component("chatty:ping_defaultsound", chattyConfig.ping.defaultPingSound.miniMsg()),
        Placeholder.component("chatty:ping_volume", chattyConfig.ping.pingVolume.toString().miniMsg()),
        Placeholder.component("chatty:ping_pitch", chattyConfig.ping.pingPitch.toString().miniMsg()),
        Placeholder.component("chatty:ping_prefix", chattyConfig.ping.pingPrefix.miniMsg()),
        Placeholder.component("chatty:ping_clickreply", chattyConfig.ping.clickToReply.toString().miniMsg()),
        Placeholder.component("chatty:ping_receiver_format", chattyConfig.ping.pingReceiveFormat.miniMsg()),
        Placeholder.component("chatty:ping_sender_format", chattyConfig.ping.pingSendFormat.miniMsg()),
    )*/
}
