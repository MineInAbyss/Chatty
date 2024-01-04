package com.mineinabyss.chatty.listeners

import com.mineinabyss.chatty.ChattyChannel
import com.mineinabyss.chatty.chatty
import com.mineinabyss.chatty.chattyProxyChannel
import com.mineinabyss.chatty.components.ChannelData
import com.mineinabyss.chatty.components.ChannelType
import com.mineinabyss.chatty.helpers.gson
import com.mineinabyss.chatty.helpers.toPlayer
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.idofront.textcomponents.serialize
import github.scarsz.discordsrv.Debug
import github.scarsz.discordsrv.DiscordSRV
import github.scarsz.discordsrv.dependencies.jda.api.Permission
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed
import github.scarsz.discordsrv.util.DiscordUtil
import github.scarsz.discordsrv.util.MessageUtil
import github.scarsz.discordsrv.util.PlaceholderUtil
import github.scarsz.discordsrv.util.WebhookUtil
import net.kyori.adventure.text.TextComponent
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.messaging.PluginMessageListener


class ChattyProxyListener : PluginMessageListener {
    override fun onPluginMessageReceived(ch: String, player: Player, byteArray: ByteArray) {
        if (ch != chattyProxyChannel) return
        val decoded = gson.deserialize(byteArray.decodeToString())
        val senderName = (decoded.children()[0] as? TextComponent)?.content() ?: return
        val channelId = (decoded.children()[1] as? TextComponent)?.content() ?: return
        val message = decoded.children()[2] ?: return
        val simpleMessage = decoded.children()[3] ?: return
        val channel = chatty.config.channels[channelId]
        val onlinePlayers = Bukkit.getOnlinePlayers().filter { it.server == Bukkit.getServer() }

        val canSpy = chatty.spyingPlayers.run {
            toList { query -> query.player.takeIf { query.spying.channels.contains(channelId) } }
                .filterNotNull()
        }

        // If the channel is not found, it is discord
        if (channel != null) {
            if (channel.logToConsole)
                Bukkit.getConsoleSender().sendMessage(simpleMessage)

            when (channel.channelType) {
                ChannelType.GLOBAL -> onlinePlayers
                ChannelType.RADIUS -> canSpy
                ChannelType.PERMISSION -> onlinePlayers.filter { it.hasPermission(channel.permission) || it in canSpy }
                ChannelType.PRIVATE -> onlinePlayers.filter {
                    it.toGeary().get<ChannelData>()?.withChannelVerified()?.channel == channel || it in canSpy
                }
            }.forEach { it.sendMessage(message) }
        }

        if (chatty.config.proxy.sendProxyMessagesToDiscord
            && channel?.discordsrv == true
            && chatty.isDiscordSRVLoaded
        ) {
            sendToDiscord(message.serialize(), senderName, channel)
        }

    }

    fun sendToDiscord(message: String, senderName: String, channel: ChattyChannel) {
        val reserializer = DiscordSRV.config().getBoolean("Experiment_MCDiscordReserializer_ToDiscord")
        val discordChannel = DiscordSRV.getPlugin()
            .getDestinationTextChannelForGameChannelName(chatty.config.proxy.discordSrvChannelID)

        when {
            discordChannel == null -> {
                DiscordSRV.debug(
                    Debug.MINECRAFT_TO_DISCORD,
                    "Failed to find Discord channel to forward message from game channel $channel"
                )
            }

            !DiscordUtil.checkPermission(discordChannel.guild, Permission.MANAGE_WEBHOOKS) ->
                DiscordSRV.error("Couldn't deliver chat message as webhook because the bot lacks the \"Manage Webhooks\" permission.")

            else -> {
                val discordMessage = message
                    .run { PlaceholderUtil.replacePlaceholdersToDiscord(this) }
                    .run { if (!reserializer) MessageUtil.strip(this) else this }
                    .run {
                        if (translateMentions)
                            DiscordUtil.convertMentionsFromNames(this,DiscordSRV.getPlugin().mainGuild)
                        else this
                    }

                val whUsername = DiscordSRV.config().getString("Experiment_WebhookChatMessageUsernameFormat")
                    .replace("(%displayname%)|(%username%)".toRegex(), senderName)
                    .let { MessageUtil.strip(PlaceholderUtil.replacePlaceholders(it)) }

                WebhookUtil.deliverMessage(
                    discordChannel, whUsername,
                    DiscordSRV.getAvatarUrl(senderName, senderName.toPlayer()?.uniqueId),
                    discordMessage.translateEmoteIDsToComponent(),
                    MessageEmbed(null, null, null, null, null, 10, null, null, null, null, null, null, null)
                )
            }
        }

    }

    private val translateMentions =
        if (!chatty.isDiscordSRVLoaded) false else DiscordSRV.config().getBoolean("DiscordChatChannelTranslateMentions")

    private fun String.translateEmoteIDsToComponent(): String {
        var translated = this
        chatty.emotefixer.emotes.entries.forEach { (emoteId, replacement) ->
            val id = ":$emoteId:"
            if (id in translated)
                translated = translated.replace(id, "<$replacement")
        }
        return translated.replace("<<", "<")
    }
}
