package com.mineinabyss.chatty.tags

import com.mineinabyss.chatty.helpers.toPlayer
import com.mineinabyss.chatty.helpers.translatePlayerHeadComponent
import com.mineinabyss.idofront.font.Space
import com.mineinabyss.idofront.messaging.miniMsg
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.Context
import net.kyori.adventure.text.minimessage.internal.serializer.SerializableResolver
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

object ChattyTags {

    private val SHIFT = "shift"
    private val HEAD = "head"

    val SHIFT_RESOLVER: TagResolver = SerializableResolver.claimingComponent(
        SHIFT, { args: ArgumentQueue, ctx: Context -> create(args, ctx, SHIFT) },
        { component: Component? -> emit(component) }
    )
    val HEAD_RESOLVER: TagResolver = SerializableResolver.claimingComponent(
        HEAD, { args: ArgumentQueue, ctx: Context -> create(args, ctx, HEAD) },
        { component: Component? -> emit(component) }
    )

    private fun create(args: ArgumentQueue, ctx: Context, tag: String): Tag {
        when (tag) {
            SHIFT -> return if (args.hasNext()) Tag.selfClosingInserting(
                Space.of(args.popOr("A shift value is needed").value().toIntOrNull() ?: 0).miniMsg()
            ) else Tag.selfClosingInserting(Component.empty())

            HEAD -> return if (args.hasNext()) Tag.selfClosingInserting(
                args.popOr("A player name is needed").value().toPlayer()?.translatePlayerHeadComponent()
                    ?: Component.empty()
            ) else Tag.selfClosingInserting(Component.empty())
        }

        return Tag.inserting(Component.empty())
    }

    private fun emit(component: Component?) = null
}
