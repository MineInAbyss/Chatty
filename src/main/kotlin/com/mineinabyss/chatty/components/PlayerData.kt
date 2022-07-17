package com.mineinabyss.chatty.components

import com.mineinabyss.geary.papermc.access.toGeary
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import org.bukkit.entity.Player

@Serializable
class PlayerData(
    var firstJoin: Boolean = true,
)

val Player.playerData get() = toGeary().getOrSetPersisting { PlayerData() }
