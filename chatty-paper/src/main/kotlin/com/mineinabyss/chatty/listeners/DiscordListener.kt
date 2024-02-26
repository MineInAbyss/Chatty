package com.mineinabyss.chatty.listeners

import com.mineinabyss.chatty.chatty
import com.mineinabyss.chatty.components.ChannelData
import com.mineinabyss.chatty.discordSrvChannel
import com.mineinabyss.chatty.helpers.defaultChannel
import com.mineinabyss.chatty.helpers.globalChannel
import com.mineinabyss.chatty.helpers.handleChatFilters
import com.mineinabyss.chatty.helpers.handleUrlReplacements
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.idofront.textcomponents.miniMsg
import com.mineinabyss.idofront.textcomponents.serialize
import github.scarsz.discordsrv.api.ListenerPriority
import github.scarsz.discordsrv.api.Subscribe
import github.scarsz.discordsrv.api.events.*
import github.scarsz.discordsrv.dependencies.jda.api.MessageBuilder
import github.scarsz.discordsrv.dependencies.jda.api.entities.Message
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed
import github.scarsz.discordsrv.dependencies.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import github.scarsz.discordsrv.objects.MessageFormat
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import github.scarsz.discordsrv.dependencies.kyori.adventure.text.Component as ComponentDSV


class DiscordListener {
    private val mm = github.scarsz.discordsrv.dependencies.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
    private val plainText = github.scarsz.discordsrv.dependencies.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
    private val legacy = LegacyComponentSerializer.builder().useUnusualXRepeatedCharacterHexFormat().build()
    private val gson = GsonComponentSerializer.gson()

    @Subscribe(priority = ListenerPriority.HIGHEST)
    fun DiscordGuildMessagePostProcessEvent.sendDiscordToProxy() {
        val senderName = mm.deserialize(message.author.displayName ?: message.author.name)
        val channel = defaultChannel().takeIf { it.value.discordsrv } ?: globalChannel()?.takeIf { it.value.discordsrv } ?: chatty.config.channels.entries.firstOrNull { it.value.discordsrv } ?: return
        val channelId = ComponentDSV.text(channel.key)
        val simpleMessage = mm.deserialize(message.author.name + ": " + message.contentRaw)
        val message = if (chatty.config.chat.formatURLs) handleUrlReplacements(minecraftMessage.toComponent(), null).toComponentDSV() else minecraftMessage
        val minecraftMessage = ComponentDSV.textOfChildren(senderName, channelId, message, simpleMessage)
        chatty.plugin.server.sendPluginMessage(chatty.plugin, discordSrvChannel, gson.serialize(minecraftMessage).toByteArray())
        setMinecraftMessage(message)
    }

    private fun ComponentDSV.toComponent() = mm.serialize(this).miniMsg()
    private fun Component.toComponentDSV() = mm.deserialize(this.serialize())

    @Subscribe(priority = ListenerPriority.NORMAL)
    fun GameChatMessagePreProcessEvent.onChat() {
        val channel = player.toGeary().get<ChannelData>()?.channel ?: return
        val baseMessage = messageComponent.children().last().toComponent()
        val filteredMessage = handleChatFilters(baseMessage, player, null)?.toComponentDSV()

        if (!channel.discordsrv || filteredMessage == null) isCancelled = true
        else messageComponent = filteredMessage
    }

    @Subscribe
    fun DeathMessagePreProcessEvent.onDeath() {
        if (isCancelled) return
        messageFormat = messageFormat.translatePreFormat()
        deathMessage = legacy.deserialize(deathMessage).serialize().translateEmoteIDs()
    }

    @Subscribe
    fun AchievementMessagePreProcessEvent.onAchievement() {
        if (isCancelled) return
        messageFormat = messageFormat.translatePreFormat()
        achievementName = achievementName.translateEmoteIDs()
    }

    @Subscribe
    fun AchievementMessagePostProcessEvent.onAchievement() {
        if (isCancelled) return
        discordMessage = discordMessage.translatePostFormat()
    }

    private fun Message.translatePostFormat(): Message {
        val message = this
        val embeds = mutableListOf<MessageEmbed>()
        val fields = mutableListOf<MessageEmbed.Field>()

        message.embeds.forEach { embed ->
            if (embed.fields.isNotEmpty())
                embed.fields.forEach { field ->
                    fields.add(
                        MessageEmbed.Field(
                            field.name?.translateEmoteIDs(),
                            field.value?.translateEmoteIDs(),
                            field.isInline
                        )
                    )
                }
            embeds.add(
                MessageEmbed(
                    embed.url, embed.title?.translateEmoteIDs(), embed.description?.translateEmoteIDs(),
                    embed.type, embed.timestamp, embed.colorRaw, embed.thumbnail, embed.siteProvider,
                    embed.author, embed.videoInfo, embed.footer, embed.image, fields
                )
            )
        }
        var builder = MessageBuilder(message)
        if (embeds.isNotEmpty()) builder = builder.setEmbeds(embeds)
        return builder.build()
    }

    private fun MessageFormat.translatePreFormat(): MessageFormat {
        val fields = mutableListOf<MessageEmbed.Field>()
        val format = this
        if (content != null)
            format.content = content.translateEmoteIDs()
        if (title != null)
            format.title = title.translateEmoteIDs()
        if (description != null)
            format.description = description.translateEmoteIDs()
        if (format.fields != null)
            format.fields.forEach { f ->
                fields.add(MessageEmbed.Field(f.name?.translateEmoteIDs(), f.value?.translateEmoteIDs(), f.isInline))
            }
        format.fields = fields
        return format
    }

    private fun String.cleanUpHackyFix() =
        plainText.serialize(mm.deserialize(this)).replace("\\<", "<").replace("<<", "<")

    private fun String.translateEmoteIDs(): String {
        var translated = this
        chatty.emotefixer.emotes.entries.map { ":${it.key}:" to it.value }.forEach { (emoteId, replacement) ->
            translated = translated.replace(emoteId, replacement)
        }

        return translated.cleanUpHackyFix()
    }
}
