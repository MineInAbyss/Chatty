package com.mineinabyss.chatty.listeners

import com.mineinabyss.chatty.components.playerData
import com.mineinabyss.chatty.helpers.getChannelFromId
import com.mineinabyss.idofront.messaging.broadcast
import github.scarsz.discordsrv.api.Subscribe
import github.scarsz.discordsrv.api.events.GameChatMessagePostProcessEvent
import github.scarsz.discordsrv.api.events.VentureChatMessagePostProcessEvent

class DiscordListener {

    @Subscribe
    fun GameChatMessagePostProcessEvent.onChat() {
        val channel = getChannelFromId(player.playerData.channelId) ?: return
        broadcast("Discordsrv: " + channel.discordsrv)
        if (!channel.discordsrv) isCancelled = true
    }

    @Subscribe
    fun VentureChatMessagePostProcessEvent.onProxyChat() {
        val channelId = processedMessage.substringBefore(" ")
        val channel = getChannelFromId(channelId) ?: return
        broadcast("Discordsrv: " + channel.discordsrv)
        if (!channel.discordsrv) isCancelled = true
        else processedMessage.replaceFirst("$channelId ", "")
    }
}
