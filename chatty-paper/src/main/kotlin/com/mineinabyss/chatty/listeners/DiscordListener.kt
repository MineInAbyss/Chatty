package com.mineinabyss.chatty.listeners

import com.mineinabyss.chatty.components.chattyData
import com.mineinabyss.chatty.helpers.*
import com.mineinabyss.idofront.messaging.miniMsg
import github.scarsz.discordsrv.api.ListenerPriority
import github.scarsz.discordsrv.api.Subscribe
import github.scarsz.discordsrv.api.events.AchievementMessagePostProcessEvent
import github.scarsz.discordsrv.api.events.AchievementMessagePreProcessEvent
import github.scarsz.discordsrv.api.events.DeathMessagePreProcessEvent
import github.scarsz.discordsrv.api.events.GameChatMessagePreProcessEvent
import github.scarsz.discordsrv.dependencies.jda.api.MessageBuilder
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed
import github.scarsz.discordsrv.dependencies.kyori.adventure.text.Component
import github.scarsz.discordsrv.dependencies.kyori.adventure.text.TextReplacementConfig
import github.scarsz.discordsrv.dependencies.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer


class DiscordListener {

    @Subscribe(priority = ListenerPriority.NORMAL)
    fun GameChatMessagePreProcessEvent.onChat() {
        val channel = getChannelFromId(player.chattyData.channelId) ?: return
        if (isCancelled) return
        else if (!channel.discordsrv) isCancelled = true
        else {
            val plain = PlainTextComponentSerializer.builder().build()
            val format = plain.serialize(translatePlaceholders(player, player.getChannelFromPlayer()?.format.toString()))
            val msg = plain.serialize(messageComponent.serialize().miniMsg()).replace(format, "")
            messageComponent = msg.miniMessage().translateEmoteIDsToComponent()
        }
    }

    @Subscribe
    fun DeathMessagePreProcessEvent.onDeath() {
        getChannelFromId(player.chattyData.channelId) ?: return
        if (isCancelled) return
        deathMessage = deathMessage.translateEmoteIDs()
    }

    @Subscribe
    fun AchievementMessagePreProcessEvent.onAchievement() {
        if (isCancelled) return
        achievementName = achievementName.translateEmoteIDs()
        val format = messageFormat
        if (format.content != null)
            format.content = format.content.translateEmoteIDs()
        if (format.description != null)
            format.description = format.description.translateEmoteIDs()
        if (format.title != null)
            format.title = format.title.translateEmoteIDs()
        if (format.fields != null) {
            val fields: MutableList<MessageEmbed.Field> = ArrayList()
            format.fields.forEach { f ->
                fields.add(MessageEmbed.Field(f.name?.translateEmoteIDs(), f.value?.translateEmoteIDs(), f.isInline))
            }
            format.fields = fields
        }
        messageFormat = format
    }

    @Subscribe
    fun AchievementMessagePostProcessEvent.onAchievement() {
        if (isCancelled) return
        val message = discordMessage
        val embeds = mutableListOf<MessageEmbed>()

        message.embeds.forEach { embed ->
            val fields = mutableListOf<MessageEmbed.Field>()
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
        val builder = MessageBuilder(message)
        if (embeds.isNotEmpty()) builder.setEmbeds(embeds)
        discordMessage = builder.build()
    }
}

private fun Component.cleanUpHackyFix() =
    this.replaceText(TextReplacementConfig.builder().match("<<").replacement("<").build())


private fun String.cleanUpHackyFix() =
    this.replace("<<", "<").serializeLegacy().stripTags()

private fun String.translateEmoteIDs(): String {
    var translated = this
    emoteFixer.emotes.entries.forEach { (emoteId, replacement) ->
        val id = ":$emoteId:"
        if (id in this) {
            translated = translated.replace(id, replacement)
        }
    }
    return translated.cleanUpHackyFix()
}

private fun Component.translateEmoteIDsToComponent(): Component {
    var translated = this
    emoteFixer.emotes.entries.forEach { (emoteId, replacement) ->
        val id = ":$emoteId:"
        if (id in translated.serialize()) {
            translated = translated.replaceText(
                TextReplacementConfig.builder().match(id)
                    .replacement("<$replacement".miniMessage()).build()
            )
        }
    }
    return translated.cleanUpHackyFix()
}

private fun Component.serialize(): String {
    return MiniMessage.builder().build()
        .serialize(this)
}

private fun String.miniMessage(): Component {
    return MiniMessage.builder().build()
        .deserialize(this)
}
