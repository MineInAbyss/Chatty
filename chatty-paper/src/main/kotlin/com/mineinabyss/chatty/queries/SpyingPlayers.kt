package com.mineinabyss.chatty.queries

import com.mineinabyss.chatty.components.CommandSpy
import com.mineinabyss.chatty.components.SpyOnChannels
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.systems.accessors.Pointer
import com.mineinabyss.geary.systems.query.GearyQuery
import org.bukkit.entity.Player

class SpyingPlayers: GearyQuery() {
    val Pointer.player by get<Player>()
    val Pointer.spying by get<SpyOnChannels>()
}
