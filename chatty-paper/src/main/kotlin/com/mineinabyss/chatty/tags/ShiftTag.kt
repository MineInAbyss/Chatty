package com.mineinabyss.chatty.tags

import com.mineinabyss.chatty.helpers.translatePlayerHeadComponent
import com.mineinabyss.idofront.font.Space
import com.mineinabyss.idofront.messaging.miniMsg
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.Context
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue
import org.bukkit.Bukkit

class ChattyTags {

    private val CHATTY = "chatty"
    private val SHIFT = "shift"
    private val HEAD = "head"

    //val RESOLVER get() = SerializableResolver.claimingComponent(CHATTY, ChattyTags::create, ChattyTags::emit)
    
    private fun create(args: ArgumentQueue, ctx: Context): Tag {
        if (args.hasNext()) when (args.pop().value()) {
            SHIFT -> return Tag.inserting(Space.of(args.popOr("A shift value is needed").value().toIntOrNull() ?: 0).miniMsg())
            HEAD -> return Tag.inserting(Bukkit.getPlayer(args.popOr("A player name is needed").value())?.translatePlayerHeadComponent() ?: Component.empty())
        }

        return Tag.inserting(Component.empty())
    }

    private fun emit(component: Component?) = null
}
