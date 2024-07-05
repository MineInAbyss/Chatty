package com.mineinabyss.chatty.tags

import com.mineinabyss.chatty.helpers.*
import com.mineinabyss.idofront.font.Space
import com.mineinabyss.idofront.textcomponents.miniMsg
import com.mineinabyss.idofront.textcomponents.serialize
import com.mineinabyss.idofront.textcomponents.toPlainText
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.Context
import net.kyori.adventure.text.minimessage.internal.serializer.SerializableResolver
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.translation.GlobalTranslator
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object ChattyTags {

    private const val SHIFT = "shift"

    val SHIFT_RESOLVER: TagResolver = SerializableResolver.claimingComponent(
        SHIFT, { args: ArgumentQueue, ctx: Context -> create(args, ctx, SHIFT) },
        { component: Component? -> emit(component) }
    )

    fun HELD_ITEM(player: Player) = (player.inventory.itemInMainHand.takeUnless { it == ItemStack.empty() } ?: player.inventory.itemInOffHand.takeUnless { it == ItemStack.empty() })?.let { item ->
        val component = item.itemMeta?.displayName()?.let { Component.textOfChildren("[".miniMsg(), it, "]".miniMsg()) } ?: item.displayName()
        Placeholder.component("held_item", component.colorIfAbsent(NamedTextColor.AQUA).hoverEvent(item))
    }

    private fun create(args: ArgumentQueue, ctx: Context, tag: String): Tag {
        return Tag.selfClosingInserting(when {
            tag == SHIFT && args.hasNext() -> Space.of(args.popOr("A shift value is needed").value().toIntOrNull() ?: 0).miniMsg()
            else -> Component.empty()
        })
    }

    private fun emit(component: Component?) = null
}
