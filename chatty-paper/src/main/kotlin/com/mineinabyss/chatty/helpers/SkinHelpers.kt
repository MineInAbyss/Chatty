package com.mineinabyss.chatty.helpers

import com.combimagnetron.imageloader.Avatar
import com.combimagnetron.imageloader.Image
import com.combimagnetron.imageloader.ImageUtils
import com.mineinabyss.chatty.chatty
import com.mineinabyss.idofront.textcomponents.miniMsg
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.profile.PlayerTextures

val playerHeadMapCache = mutableMapOf<OfflinePlayer, Component>()
fun OfflinePlayer.translatePlayerHeadComponent(): Component {
    if (this !in playerHeadMapCache || playerHeadMapCache[this]!!.font() != Key.key(chatty.config.playerHeadFont)) {
        playerHeadMapCache[this] = runCatching { playerHeadTexture(ascent = -5) }.getOrDefault(Component.empty())
    }
    return playerHeadMapCache[this] ?: Component.empty()
}

val playerBodyMapCache = mutableMapOf<OfflinePlayer, Component>()
fun Player.refreshSkinInCaches() {
    playerBodyMapCache -= this
    playerHeadMapCache -= this
}
fun OfflinePlayer.translateFullPlayerSkinComponent(): Component {
    if (this !in playerBodyMapCache || playerBodyMapCache[this]!!.font() != Key.key(chatty.config.playerHeadFont)) {
        playerBodyMapCache[this] = runCatching { fullPlayerBodyTexture(ascent = -5) }.getOrDefault(Component.empty())
    }
    return playerBodyMapCache[this] ?: Component.empty()
}

fun OfflinePlayer.playerHeadTexture(
    scale: Int = 1,
    ascent: Int = 0,
    colorType: Image.ColorType = Image.ColorType.MINIMESSAGE,
    font: Key = Key.key(chatty.config.playerHeadFont)
): Component {
    val image = avatarBuilder(this, scale, ascent, colorType).getBodyBufferedImage(scale).getSubimage(4, 0, 8, 8)
    return "<font:$font>${ImageUtils.generateStringFromImage(image, colorType, ascent)}</font>".miniMsg()
}

fun OfflinePlayer.fullPlayerBodyTexture(
    scale: Int = 1,
    ascent: Int = 0,
    colorType: Image.ColorType = Image.ColorType.MINIMESSAGE,
    font: Key = Key.key(chatty.config.playerHeadFont)
): Component {
    val image = avatarBuilder(this, scale, ascent, colorType).getBodyBufferedImage(scale)
    return "<font:$font>${ImageUtils.generateStringFromImage(image, colorType, ascent)}</font>".miniMsg()
}

private fun avatarBuilder(
    player: OfflinePlayer,
    scale: Int = 1,
    ascent: Int = 0,
    colorType: Image.ColorType = Image.ColorType.MINIMESSAGE
): Avatar {
    return Avatar.builder().isSlim(player.playerProfile.apply { this.update() }.textures.skinModel == PlayerTextures.SkinModel.SLIM)
        .playerName(player.name)
        .ascent(ascent).colorType(colorType).scale(scale).build()
}