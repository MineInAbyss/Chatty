package com.mineinabyss.chatty.listeners

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.WrappedChatComponent
import com.mineinabyss.chatty.chatty
import com.mineinabyss.chatty.helpers.chattyConfig
import com.mineinabyss.chatty.helpers.getChannelFromPlayer
import com.mineinabyss.chatty.helpers.protocolManager
import com.mineinabyss.chatty.helpers.translatePlaceholders
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

class ChatPreviewPacketAdapter : PacketAdapter(
    chatty, PacketType.Play.Client.CHAT_PREVIEW
) {
    override fun onPacketReceiving(event: PacketEvent) {
        val msg =
            if (chattyConfig.chat.chatPreview.includeFormatInPreview) event.player.getChannelFromPlayer()?.format + event.packet.strings.read(0)
            else event.packet.strings.read(0)
        val result = translatePlaceholders(event.player, msg)
        val previewPacket = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.CHAT_PREVIEW)

        previewPacket.integers.write(0, event.packet.integers.read(0))
        previewPacket.chatComponents.write(0, WrappedChatComponent.fromLegacyText(result.serializeToLegacy()))

        protocolManager.sendServerPacket(event.player, previewPacket)
    }
}

private fun Component.serializeToLegacy() = LegacyComponentSerializer.builder().build().serialize(this).replace("\\<", "<")
