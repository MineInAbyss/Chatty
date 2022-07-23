package com.mineinabyss.chatty.listeners

import com.mineinabyss.chatty.components.chattyData
import com.mineinabyss.chatty.helpers.emoteFixer
import com.mineinabyss.chatty.helpers.getChannelFromId
import com.mineinabyss.chatty.helpers.serializeLegacy
import com.mineinabyss.chatty.helpers.stripTags
import github.scarsz.discordsrv.api.ListenerPriority
import github.scarsz.discordsrv.api.Subscribe
import github.scarsz.discordsrv.api.events.*
import github.scarsz.discordsrv.dependencies.jda.api.MessageBuilder
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed
import github.scarsz.discordsrv.dependencies.kyori.adventure.text.Component
import github.scarsz.discordsrv.dependencies.kyori.adventure.text.TextReplacementConfig
import github.scarsz.discordsrv.dependencies.kyori.adventure.text.minimessage.MiniMessage


class DiscordListener {

    @Subscribe(priority = ListenerPriority.NORMAL)
    fun GameChatMessagePreProcessEvent.onChat() {
        val channel = getChannelFromId(player.chattyData.channelId) ?: return
        if (!channel.discordsrv) {
            isCancelled = true
            return
        }
        else messageComponent = messageComponent.translateEmoteIDsToComponent()
    }

    @Subscribe(priority = ListenerPriority.NORMAL)
    fun VentureChatMessagePreProcessEvent.onProxyChat() {
        val channelId = messageComponent.deserialize().substringBefore(" ")
        val channel = getChannelFromId(channelId) ?: return
        if (!channel.discordsrv) {
            isCancelled = true
            return
        }
        else messageComponent = messageComponent.removeAttachedChannel(channelId).translateEmoteIDsToComponent()
    }

    @Subscribe
    fun DeathMessagePreProcessEvent.onDeath() {
        val channel = getChannelFromId(player.chattyData.channelId) ?: return
        /*if (!channel.discordsrv) {
            isCancelled = true
            return
        }*/
        deathMessage = deathMessage.translateEmoteIDs()
    }

    @Subscribe
    fun AchievementMessagePreProcessEvent.onAchievement() {
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

private fun Component.removeAttachedChannel(id: String) =
    this.replaceText(TextReplacementConfig.builder().match(id).replacement("").build())

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

private fun String.translateEmoteIDsToComponent(): Component {
    var translated = this
    emoteFixer.emotes.entries.forEach { (emoteId, replacement) ->
        val id = ":$emoteId:"
        if (id in this) {
            translated = translated.replace(id, "<$replacement")
        }
    }
    return translated.miniMessage().cleanUpHackyFix()
}

private fun Component.translateEmoteIDsToComponent(): Component {
    var translated = this
    emoteFixer.emotes.entries.forEach { (emoteId, replacement) ->
        val id = ":$emoteId:"
        if (id in translated.deserialize()) {
            translated = translated.replaceText(
                TextReplacementConfig.builder().match(id)
                    .replacement("<$replacement".miniMessage()).build()
            )
        }
    }
    return translated.cleanUpHackyFix()
}

private fun Component.deserialize(): String {
    return MiniMessage.builder().build()
        .serialize(this)
}

private fun Component.stripTags(): String {
    return MiniMessage.builder().build().stripTokens(this.deserialize())
}

private fun String.miniMessage(): Component {
    return MiniMessage.builder().build()
        .deserialize(this)
}
