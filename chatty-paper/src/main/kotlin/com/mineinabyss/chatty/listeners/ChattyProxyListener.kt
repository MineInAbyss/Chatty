package com.mineinabyss.chatty.listeners

import com.mineinabyss.chatty.ChattyContext
import com.mineinabyss.chatty.chattyProxyChannel
import com.mineinabyss.chatty.components.ChannelType
import com.mineinabyss.chatty.components.SpyOnChannels
import com.mineinabyss.chatty.components.chattyData
import com.mineinabyss.chatty.helpers.*
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.idofront.messaging.miniMsg
import github.scarsz.discordsrv.Debug
import github.scarsz.discordsrv.DiscordSRV
import github.scarsz.discordsrv.dependencies.jda.api.Permission
import github.scarsz.discordsrv.util.DiscordUtil
import github.scarsz.discordsrv.util.MessageUtil
import github.scarsz.discordsrv.util.PlaceholderUtil
import github.scarsz.discordsrv.util.WebhookUtil
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.messaging.PluginMessageListener


class ChattyProxyListener : PluginMessageListener {
    override fun onPluginMessageReceived(ch: String, player: Player, message: ByteArray) {
        if (ch != chattyProxyChannel) return
        // Get channel from last part of string as it is sent via the proxy message
        // playername0playerchannel0channelformat(minimsg formatted)0 full message
        val decoded = message.decodeToString()
        val senderName = decoded.substringBefore(ZERO_WIDTH)
        val channelId = decoded.substringAfter(ZERO_WIDTH).split(ZERO_WIDTH).first()
        val channelFormat = decoded.substringAfter(channelId + ZERO_WIDTH).split("$ZERO_WIDTH ").first()
        val channel = getChannelFromId(channelId)
        val proxyMessage = decoded.substringAfter(channelFormat).replaceFirst("$ZERO_WIDTH ", "")
        val onlinePlayers = Bukkit.getOnlinePlayers()
        val canSpy = onlinePlayers.filter { it.toGeary().get<SpyOnChannels>()?.channels?.contains(player.chattyData.channelId) == true }

        when (channel?.channelType) {
            ChannelType.GLOBAL -> onlinePlayers
            ChannelType.RADIUS -> canSpy
            ChannelType.PERMISSION -> onlinePlayers.filter { it.hasPermission(channel.permission) || it in canSpy }
            ChannelType.PRIVATE -> onlinePlayers.filter { it.getChannelFromPlayer() == channel || it in canSpy }
            else -> onlinePlayers
        }.forEach {
            // Sent from discord
            if (channel == null)
                it.sendMessage(decoded.miniMsg())
            else it.sendMessage(proxyMessage.miniMsg())
        }

        if (!ChattyContext.isDiscordSRVLoaded || channel?.discordsrv != true) return
        val dsrv = DiscordSRV.getPlugin()
        var discordMessage = proxyMessage.replaceFirst(channelFormat, "")
        val reserializer = DiscordSRV.config().getBoolean("Experiment_MCDiscordReserializer_ToDiscord")
        val discordChannel =
            dsrv.getDestinationTextChannelForGameChannelName(chattyConfig.proxy.discordSrvChannelID)

        if (discordChannel == null) {
            DiscordSRV.debug(
                Debug.MINECRAFT_TO_DISCORD,
                "Failed to find Discord channel to forward message from game channel $channel"
            )
        } else if (!DiscordUtil.checkPermission(discordChannel.guild, Permission.MANAGE_WEBHOOKS)) {
            DiscordSRV.error("Couldn't deliver chat message as webhook because the bot lacks the \"Manage Webhooks\" permission.")
        } else {
            discordMessage = PlaceholderUtil.replacePlaceholdersToDiscord(discordMessage)
            if (!reserializer) discordMessage = MessageUtil.strip(discordMessage)

            if (translateMentions)
                discordMessage = DiscordUtil.convertMentionsFromNames(discordMessage, dsrv.mainGuild)
            var whUsername: String =
                DiscordSRV.config().getString("Experiment_WebhookChatMessageUsernameFormat")
                    .replace("(%displayname%)|(%username%)".toRegex(), senderName)
            whUsername = PlaceholderUtil.replacePlaceholders(whUsername)
            whUsername = MessageUtil.strip(whUsername)

            WebhookUtil.deliverMessage(
                discordChannel,
                whUsername,
                DiscordSRV.getAvatarUrl(senderName, senderName.toPlayer()?.uniqueId),
                discordMessage.translateEmoteIDsToComponent(),
                null
            )
        }
    }
}

private val translateMentions = DiscordSRV.config().getBoolean("DiscordChatChannelTranslateMentions")
private fun String.translateEmoteIDsToComponent(): String {
    var translated = this
    emoteFixer.emotes.entries.forEach { (emoteId, replacement) ->
        val id = ":$emoteId:"
        if (id in translated)
            translated = translated.replace(id, "<$replacement")
    }
    return translated.cleanUpHackyFix()
}
private fun String.cleanUpHackyFix() =
    this.replace("<<", "<")
