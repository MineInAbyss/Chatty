package com.mineinabyss.chatty.queries

import com.mineinabyss.chatty.components.SpyOnChannels
import com.mineinabyss.geary.systems.query.GearyQuery
import org.bukkit.entity.Player

class SpyingPlayersQuery : GearyQuery() {
    val player by get<Player>()
    val spying by get<SpyOnChannels>()
}
