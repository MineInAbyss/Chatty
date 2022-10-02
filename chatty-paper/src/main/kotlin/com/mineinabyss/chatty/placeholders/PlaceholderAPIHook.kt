package com.mineinabyss.chatty.placeholders

import com.mineinabyss.chatty.chatty
import com.mineinabyss.chatty.helpers.toSentence
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.entity.Player

class PlaceholderAPIHook : PlaceholderExpansion() {
    override fun getIdentifier() = "chatty"
    override fun getAuthor() = chatty.description.authors.toSentence()
    override fun getVersion() = chatty.description.version
    override fun persist() = true

    override fun onPlaceholderRequest(player: Player, string: String): String {
        chattyPlaceholders(player, string).forEach { placeholder ->
            if (string == placeholder.key) {
                return placeholder.value
            }
        }
        return string
    }
}
