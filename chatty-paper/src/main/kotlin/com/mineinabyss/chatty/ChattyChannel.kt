package com.mineinabyss.chatty

import com.mineinabyss.chatty.components.ChannelType
import com.mineinabyss.chatty.components.SpyOnChannels
import com.mineinabyss.chatty.queries.SpyingPlayers
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import org.bukkit.entity.Player

@Serializable
data class ChattyChannel(
    val channelType: ChannelType,
    val permission: String = "",
    val logToConsole: Boolean = true,
    val simpleConsoleMessages: Boolean = false,
    val proxy: Boolean = false,
    val discordsrv: Boolean = true,
    val isDefaultChannel: Boolean = false,
    val isStaffChannel: Boolean = false,
    val format: String = "",
    @SerialName("messageColor") val _messageColor: String = "white",
    val channelRadius: Int = 0,
    val channelAliases: List<String> = listOf(),
) {
    val key by lazy { chatty.config.channels.entries.first { it.value == this }.key }
    val messageColor: TextColor
        get() = TextColor.fromHexString(_messageColor) ?: NamedTextColor.NAMES.value(_messageColor)
        ?: NamedTextColor.WHITE


    fun getAudience(player: Player): Collection<Audience> {
        val onlinePlayers by lazy { Bukkit.getOnlinePlayers() }
        val audiences = mutableSetOf<Audience>()

        when (channelType) {
            ChannelType.GLOBAL -> audiences.addAll(onlinePlayers)
            ChannelType.RADIUS -> {
                if (channelRadius <= 0) audiences.addAll(onlinePlayers)
                else audiences.addAll(player.world.players.filter { p ->
                    player.location.distanceSquared(p.location) <= (channelRadius * channelRadius)
                })
            }

            ChannelType.PERMISSION -> audiences.addAll(onlinePlayers.filter { p -> p.hasPermission(permission) })
            // Intended for Guilds etc., want to consider finding a non-permission way for this
            ChannelType.PRIVATE -> audiences.add(player)
        }

        // Add spying players
        val spies = chatty.spyingPlayers.run {
            toList { query -> query.player.takeIf { query.spying.channels.contains(key) } }.filterNotNull()
        }
        audiences.addAll(spies)

        return audiences
    }
}