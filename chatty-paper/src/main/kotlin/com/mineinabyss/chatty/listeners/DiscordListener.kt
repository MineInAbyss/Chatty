package com.mineinabyss.chatty.listeners

import com.mineinabyss.chatty.chatty
import com.mineinabyss.chatty.chattyProxyChannel
import com.mineinabyss.chatty.components.chattyData
import com.mineinabyss.chatty.helpers.*
import com.mineinabyss.idofront.messaging.miniMsg
import github.scarsz.discordsrv.api.ListenerPriority
import github.scarsz.discordsrv.api.Subscribe
import github.scarsz.discordsrv.api.events.*
import github.scarsz.discordsrv.dependencies.jda.api.MessageBuilder
import github.scarsz.discordsrv.dependencies.jda.api.entities.Message
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed.Field
import github.scarsz.discordsrv.dependencies.kyori.adventure.text.Component
import github.scarsz.discordsrv.dependencies.kyori.adventure.text.TextReplacementConfig
import github.scarsz.discordsrv.dependencies.kyori.adventure.text.minimessage.MiniMessage
import github.scarsz.discordsrv.objects.MessageFormat
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit


class DiscordListener {

    @Subscribe(priority = ListenerPriority.HIGHEST)
    fun DiscordGuildMessagePostProcessEvent.sendDiscordToProxy() {
        Bukkit.getServer().sendPluginMessage(chatty, chattyProxyChannel, minecraftMessage.serialize().toByteArray())
    }

    @Subscribe(priority = ListenerPriority.NORMAL)
    fun GameChatMessagePreProcessEvent.onChat() {
        val channel = getChannelFromId(player.chattyData.channelId) ?: return
        val lastUsedChannel = getChannelFromId(player.chattyData.lastChannelUsed) ?: return

        if (isCancelled) return
        else if (!channel.discordsrv || (channel != lastUsedChannel && !lastUsedChannel.discordsrv))
            isCancelled = true
        else {
            val plain = PlainTextComponentSerializer.builder().build()
            val format = plain.serialize(translatePlaceholders(player, player.getChannelFromPlayer()?.format.toString()))
            val msg = plain.serialize(messageComponent.serialize().miniMsg()).replace(format, "")
            messageComponent = msg.miniMessage().translateEmoteIDsToComponent()
        }
    }

    @Subscribe
    fun DeathMessagePreProcessEvent.onDeath() {
        if (isCancelled) return
        messageFormat = messageFormat.translatePreFormat()
        deathMessage = deathMessage.translateEmoteIDs()
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
}

private fun Message.translatePostFormat(): Message {
    val message = this
    val embeds = mutableListOf<MessageEmbed>()
    val fields = mutableListOf<Field>()

    message.embeds.forEach { embed ->
        if (embed.fields.isNotEmpty())
            embed.fields.forEach { field ->
                fields.add(Field(field.name?.translateEmoteIDs(), field.value?.translateEmoteIDs(), field.isInline))
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
    val fields = mutableListOf<Field>()
    val format = this
    if (content != null)
        format.content = content.translateEmoteIDs()
    if (title != null)
        format.title = title.translateEmoteIDs()
    if (description != null)
        format.description = description.translateEmoteIDs()
    if (format.fields != null)
        format.fields.forEach { f ->
            fields.add(Field(f.name?.translateEmoteIDs(), f.value?.translateEmoteIDs(), f.isInline))
        }
    format.fields = fields
    return format
}

private fun Component.cleanUpHackyFix() =
    this.replaceText(TextReplacementConfig.builder().match("<<").replacement("<").build())


private fun String.cleanUpHackyFix() =
    this.deSerializeLegacy().stripTags().replace("\\<", "<").replace("<<", "<")

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
