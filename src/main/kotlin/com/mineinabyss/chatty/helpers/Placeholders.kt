package com.mineinabyss.chatty.helpers

import com.mineinabyss.chatty.components.playerData
import org.bukkit.entity.Player

fun chattyPlaceholders(player: Player) = mutableMapOf<String, Any>(
    "player_name" to { player.name },
    "player_display_name" to { player.displayName() },
    "player_uuid" to { player.uniqueId.toString() },
    "player_channel" to { player.playerData.channel },
    "player_channelname" to { player.playerData.channel.channelName },
    "player_channeltype" to { player.playerData.channel.channelType },
)
