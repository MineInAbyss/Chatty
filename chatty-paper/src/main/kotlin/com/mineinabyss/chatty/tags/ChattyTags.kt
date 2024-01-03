package com.mineinabyss.chatty.tags

import com.mineinabyss.chatty.helpers.toPlayer
import com.mineinabyss.chatty.helpers.translateFullPlayerSkinComponent
import com.mineinabyss.chatty.helpers.translatePlayerHeadComponent
import com.mineinabyss.idofront.font.Space
import com.mineinabyss.idofront.textcomponents.miniMsg
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.Context
import net.kyori.adventure.text.minimessage.internal.serializer.SerializableResolver
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

object ChattyTags {

    private const val SHIFT = "shift"
    private const val HEAD = "head"
    private const val SKIN = "skin"

    val SHIFT_RESOLVER: TagResolver = SerializableResolver.claimingComponent(
        SHIFT, { args: ArgumentQueue, ctx: Context -> create(args, ctx, SHIFT) },
        { component: Component? -> emit(component) }
    )
    val HEAD_RESOLVER: TagResolver = SerializableResolver.claimingComponent(
        HEAD, { args: ArgumentQueue, ctx: Context -> create(args, ctx, HEAD) },
        { component: Component? -> emit(component) }
    )

    val SKIN_RESOLVER: TagResolver = SerializableResolver.claimingComponent(
        SKIN, { args: ArgumentQueue, ctx: Context -> create(args, ctx, SKIN) },
        { component: Component? -> emit(component) }
    )

    private fun create(args: ArgumentQueue, ctx: Context, tag: String): Tag {
        return Tag.selfClosingInserting(when {
            tag == SHIFT && args.hasNext() -> Space.of(args.popOr("A shift value is needed").value().toIntOrNull() ?: 0).miniMsg()
            tag == HEAD && args.hasNext() -> args.popOr("A player name is needed").value().toPlayer()?.translatePlayerHeadComponent() ?: Component.empty()
            tag == SKIN && args.hasNext() -> args.popOr("A player name is needed").value().toPlayer()?.translateFullPlayerSkinComponent() ?: Component.empty()
            else -> Component.empty()
        })
    }

    private fun emit(component: Component?) = null
}
