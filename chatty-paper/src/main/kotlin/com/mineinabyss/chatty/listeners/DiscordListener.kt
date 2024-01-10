package com.mineinabyss.chatty.listeners

import com.mineinabyss.chatty.ChattyChannel
import com.mineinabyss.chatty.chatty
import com.mineinabyss.chatty.chattyProxyChannel
import com.mineinabyss.chatty.components.ChannelData
import com.mineinabyss.chatty.helpers.parseTags
import com.mineinabyss.chatty.helpers.translatePlaceholders
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.idofront.textcomponents.serialize
import github.scarsz.discordsrv.api.ListenerPriority
import github.scarsz.discordsrv.api.Subscribe
import github.scarsz.discordsrv.api.events.*
import github.scarsz.discordsrv.dependencies.jda.api.MessageBuilder
import github.scarsz.discordsrv.dependencies.jda.api.entities.Message
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed
import github.scarsz.discordsrv.dependencies.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import github.scarsz.discordsrv.objects.MessageFormat
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.entity.Player
import github.scarsz.discordsrv.dependencies.kyori.adventure.text.Component as ComponentDSV


class DiscordListener {
    private val mm = github.scarsz.discordsrv.dependencies.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
    private val plainText = github.scarsz.discordsrv.dependencies.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
    private val legacy = LegacyComponentSerializer.builder().useUnusualXRepeatedCharacterHexFormat().build()
    private val gson = GsonComponentSerializer.gson()

    @Subscribe(priority = ListenerPriority.HIGHEST)
    fun DiscordGuildMessagePostProcessEvent.sendDiscordToProxy() {
        chatty.plugin.server.sendPluginMessage(chatty.plugin, chattyProxyChannel, gson.serialize(ComponentDSV.textOfChildren(minecraftMessage)).toByteArray())
    }

    @Subscribe(priority = ListenerPriority.NORMAL)
    fun GameChatMessagePreProcessEvent.onChat() {
        val data = player.toGeary().get<ChannelData>() ?: return
        val channel = data.channel ?: return
        val lastUsedChannel = data.lastChannelUsed ?: return
    }

    // Parse the DSRV Component through the Chatty normal MM instance to format <chatty> tags, then serialize/deserialize it back to DSRV Component
    private fun ComponentDSV.stripFormat(player: Player, channel: ChattyChannel): ComponentDSV =
        mm.deserialize(plainText.serialize(this).replace(
            plainText.serialize(
                mm.deserialize(translatePlaceholders(player, channel.format).parseTags(player, true).serialize())
            ), ""
        ))

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
        chatty.emotefixer.emotes.entries.forEach { (emoteId, replacement) ->
            val id = ":$emoteId:"
            if (id in this) {
                translated = translated.replace(id, replacement)
            }
        }
        return translated.cleanUpHackyFix()
    }
}
