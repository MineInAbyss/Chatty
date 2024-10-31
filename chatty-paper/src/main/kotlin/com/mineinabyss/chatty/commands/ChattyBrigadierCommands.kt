package com.mineinabyss.chatty.commands

import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.mineinabyss.chatty.ChattyChannel
import com.mineinabyss.chatty.chatty
import com.mineinabyss.chatty.commands.ChattyBrigadierCommands.handleSendingPrivateMessage
import com.mineinabyss.chatty.commands.ChattyBrigadierCommands.sendFormattedMessage
import com.mineinabyss.chatty.commands.ChattyBrigadierCommands.shortcutCommand
import com.mineinabyss.chatty.components.*
import com.mineinabyss.chatty.helpers.*
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.geary.serialization.getOrSetPersisting
import com.mineinabyss.geary.serialization.setPersisting
import com.mineinabyss.idofront.commands.brigadier.IdoRootCommand
import com.mineinabyss.idofront.commands.brigadier.commands
import com.mineinabyss.idofront.commands.brigadier.playerExecutes
import com.mineinabyss.idofront.entities.toPlayer
import com.mineinabyss.idofront.events.call
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.textcomponents.miniMsg
import com.mineinabyss.idofront.textcomponents.serialize
import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.tree.ArgumentCommandNode
import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.SignedMessageResolver
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import io.papermc.paper.event.player.AsyncChatDecorateEvent
import io.papermc.paper.event.player.AsyncChatEvent
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import net.kyori.adventure.chat.ChatType
import net.kyori.adventure.chat.SignedMessage
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@Suppress("UnstableApiUsage", "NAME_SHADOWING")
object ChattyBrigadierCommands {

    fun registerSignedCommands() {}

    fun registerCommands() {
        chatty.plugin.commands {
            "chatty" {
                "reload" {
                    executes {
                        chatty.plugin.createChattyContext()
                        sender.sendMessage(Component.text("Chatty has been reloaded!", NamedTextColor.GREEN))
                    }
                }
                "ping" {
                    "toggle" {
                        playerExecutes {
                            val gearyPlayer = player.toGeary()
                            val oldData = gearyPlayer.get<ChannelData>() ?: return@playerExecutes
                            gearyPlayer.setPersisting(oldData.copy(disablePingSound = !oldData.disablePingSound))
                            player.sendFormattedMessage(chatty.messages.ping.toggledPingSound)
                        }
                    }
                    "sound" {
                        playerExecutes(StringArgumentType.word().suggests {
                            suggest(
                                chatty.config.ping.alternativePingSounds
                                    .takeUnless { "all" in it } ?: Sound.entries.map { it.key().asString() }
                            )
                        }.named("sound")) { sound ->
                            val gearyPlayer = player.toGeary()
                            val oldData = gearyPlayer.get<ChannelData>() ?: return@playerExecutes
                            if (sound in alternativePingSounds) {
                                gearyPlayer.setPersisting(oldData.copy(pingSound = sound))
                                player.sendFormattedMessage(chatty.messages.ping.changedPingSound)
                            } else player.sendFormattedMessage(chatty.messages.ping.invalidPingSound)
                        }
                    }
                }
                "channels" {
                    requiresPermission("")
                    executes {
                        (sender as? Player)?.sendFormattedMessage(chatty.messages.channels.availableChannels)
                            ?: sender.sendRichMessage(chatty.messages.channels.availableChannels)
                    }
                }
                "channel" {
                    requiresPermission("")
                    playerExecutes(ChattyChannelArgument().suggests {
                        suggest(chatty.config.channels.entries.asSequence()
                            .filter { it.value.channelType != ChannelType.CUSTOM }
                            .filter { it.value.permission.isEmpty() || context.source.sender.hasPermission(it.value.permission) }
                            .sortedBy {
                                it.key in setOf(defaultChannel().key, radiusChannel()?.key, adminChannel()?.key).filterNotNull()
                            }.map { it.key }.toList()
                        )
                    }.named("channel")) { channel ->
                        if (channel.channelType != ChannelType.CUSTOM) swapChannel(player, channel)
                    }
                }
                "commandspy" {
                    playerExecutes {
                        val gearyPlayer = player.toGeary()
                        if (gearyPlayer.has<CommandSpy>()) {
                            gearyPlayer.remove<CommandSpy>()
                            player.sendFormattedMessage(chatty.messages.spying.commandSpyOff)
                        } else {
                            gearyPlayer.getOrSetPersisting<CommandSpy> { CommandSpy() }
                            player.sendFormattedMessage(chatty.messages.spying.commandSpyOn)
                        }
                    }
                }
                "spy" {
                    playerExecutes(ChattyChannelArgument().suggests {
                        suggest(chatty.config.channels.entries.asSequence()
                            .filter { it.value.channelType != ChannelType.CUSTOM }
                            .filter { it.value.permission.isEmpty() || context.source.sender.hasPermission(it.value.permission) }
                            .sortedBy {
                                it.key in setOf(defaultChannel().key, radiusChannel()?.key, adminChannel()?.key).filterNotNull()
                            }.map { it.key }.toList()
                        )
                    }.named("channel")) { channel ->
                        val spy = player.toGeary().getOrSetPersisting<SpyOnChannels> { SpyOnChannels() }

                        when {
                            channel.channelType == ChannelType.GLOBAL ->
                                player.sendFormattedMessage(chatty.messages.spying.cannotSpyOnChannel)

                            !player.hasPermission(channel.permission) ->
                                player.sendFormattedMessage(chatty.messages.spying.cannotSpyOnChannel)

                            channel.key in spy.channels -> {
                                player.sendFormattedMessage(chatty.messages.spying.stopSpyingOnChannel)
                                spy.channels.remove(channel.key)
                            }

                            else -> {
                                spy.channels.add(channel.key)
                                player.sendFormattedMessage(chatty.messages.spying.startSpyingOnChannel)
                            }
                        }
                    }
                }
                "nickname" {
                    val nickMessage = chatty.messages.nicknames

                    fun String.nicknameTooLong(): Boolean {
                        return when (chatty.config.nicknames.countTagsInLength) {
                            true -> this.length > chatty.config.nicknames.maxLength
                            false -> this.stripTags().length >= chatty.config.nicknames.maxLength
                        }
                    }

                    playerExecutes(
                        StringArgumentType.string().suggests {
                            (context.source.executor as? Player)?.chattyNickname?.let {
                                suggestFiltering(it)
                            }
                        }.named("nickname").default { "" },
                        ArgumentTypes.player().resolve().map { it.single() }.named("player").default { executor as Player },
                        ) { nickname, applyTo ->
                        when {
                            player == applyTo && nickname.isEmpty() -> {
                                applyTo.chattyNickname = null
                                applyTo.sendFormattedMessage(nickMessage.selfEmpty)
                            }
                            player.uniqueId != applyTo.uniqueId && !player.hasPermission(ChattyPermissions.NICKNAME_OTHERS) ->
                                player.sendFormattedMessage(nickMessage.otherDenied, applyTo)

                            player.uniqueId != applyTo.uniqueId && nickname.isEmpty() -> {
                                applyTo.chattyNickname = null
                                applyTo.sendFormattedMessage(nickMessage.selfEmpty)
                                player.sendFormattedMessage(nickMessage.otherEmpty, applyTo)
                            }

                            !player.hasPermission(ChattyPermissions.BYPASS_TAG_PERM) && nickname.nicknameTooLong() ->
                                player.sendFormattedMessage(nickMessage.tooLong)

                            nickname.isNotEmpty() -> {
                                applyTo.chattyNickname = nickname
                                if (player.uniqueId != applyTo.uniqueId) player.sendFormattedMessage(nickMessage.otherSuccess, player)
                                else player.sendFormattedMessage(nickMessage.selfSuccess)
                            }
                        }
                    }
                }
            }

            ("global" / "g") {
                requiresPermission("")
                handleShortCutChannel(globalChannel())
            }
            ("local" / "l") {
                requiresPermission("")
                handleShortCutChannel(radiusChannel())
            }
            ("admin" / "a") {
                requiresPermission("chatty.channel.admin")
                handleShortCutChannel(adminChannel())
            }
            ("message" / "msg") {
                requiresPermission("")
                handleMessage()
            }
            ("reply" / "r") {
                requiresPermission("")
                handleReply()
            }
        }
    }

    private fun IdoRootCommand.handleShortCutChannel(channel: MutableMap.MutableEntry<String, ChattyChannel>?) {
        playerExecutes {
            swapChannel(player, channel?.value)
        }
        if (chatty.config.chat.disableChatSigning) {
            val message by StringArgumentType.greedyString()
            playerExecutes { player.shortcutCommand(channel, message(), null) }
        } else {
            val message by ArgumentTypes.signedMessage()
            playerExecutes {
                chatty.plugin.launch {
                    val signedMessage = message().resolveSignedMessage("message", context).await()
                    player.shortcutCommand(channel, null, signedMessage)
                }
            }
        }
    }

    private fun IdoRootCommand.handleMessage() {
        playerExecutes(
            ArgumentTypes.player().resolve().map { it.single() }.named("sendTo"),
            StringArgumentType.greedyString().named("message")
        ) { sendTo, message ->
            player.handleSendingPrivateMessage(sendTo, null, message, false)
        }
    }

    private fun IdoRootCommand.handleReply() {
        playerExecutes(StringArgumentType.greedyString().named("message")) { message ->
            player.toGeary().get<ChannelData>()?.lastMessager?.toPlayer()
                ?.let { player.handleSendingPrivateMessage(it, null, message, true) }
                ?: player.sendFormattedMessage(chatty.messages.privateMessages.emptyReply)
        }
    }

    private fun Player.shortcutCommand(
        channel: Map.Entry<String, ChattyChannel>?,
        message: String?,
        signedMessage: SignedMessage?
    ) {
        val chattyData = toGeary().get<ChannelData>() ?: return
        val currentChannel = chattyData.channelId
        when {
            channel == null -> sendFormattedMessage(chatty.messages.channels.noChannelWithName)
            channel.value.permission.isNotBlank() && !hasPermission(channel.value.permission) ->
                sendFormattedMessage(chatty.messages.channels.missingChannelPermission)

            message.isNullOrEmpty() && signedMessage == null -> swapChannel(this, channel.value)
            else -> {
                if (chatty.config.chat.disableChatSigning && !message.isNullOrEmpty()) {
                    toGeary().setPersisting(chattyData.copy(channelId = channel.key, lastChannelUsedId = channel.key))
                    chatty.plugin.launch(chatty.plugin.asyncDispatcher) {
                        AsyncChatDecorateEvent(this@shortcutCommand, message.miniMsg()).call<AsyncChatDecorateEvent> {
                            GenericChattyChatEvent(this@shortcutCommand, result()).callEvent()
                        }
                        withContext(chatty.plugin.minecraftDispatcher) {
                            // chance that player logged out by now
                            toGearyOrNull()?.setPersisting(chattyData.copy(channelId = currentChannel))
                        }
                    }
                } else if (!chatty.config.chat.disableChatSigning && signedMessage != null) {
                    val audience = channel.value.getAudience(this)
                    audience.forEach { it.sendMessage(signedMessage, ChatType.CHAT.bind(appendChannelFormat(Component.empty(), this, channel.value))) }
                }
            }
        }
    }

    fun swapChannel(player: Player, newChannel: ChattyChannel?) {
        when {
            newChannel == null ->
                player.sendFormattedMessage(chatty.messages.channels.noChannelWithName)

            newChannel.channelType != ChannelType.CUSTOM && newChannel.permission.isNotBlank() && !player.hasPermission(newChannel.permission) ->
                player.sendFormattedMessage(chatty.messages.channels.missingChannelPermission)

            else -> {
                val gearyPlayer = player.toGeary()
                val chattyData = gearyPlayer.get<ChannelData>() ?: return
                gearyPlayer.setPersisting(
                    chattyData.copy(
                        channelId = newChannel.key,
                        lastChannelUsedId = newChannel.key
                    )
                )
                player.sendFormattedMessage(chatty.messages.channels.channelChanged)
            }
        }
    }

    private fun CommandSender.sendFormattedMessage(message: String, optionalPlayer: Player? = null) =
        (optionalPlayer ?: this as? Player)?.let { player ->
            this.sendMessage(translatePlaceholders(player, message).miniMsg(player.buildTagResolver(true)))
        }

    private fun Player.sendFormattedPrivateMessage(messageFormat: String, signedMessage: SignedMessage?, message: String?, receiver: Player) {
        if (signedMessage != null) {
            this.sendMessage(signedMessage, ChatType.CHAT.bind(translatePlaceholders(receiver, messageFormat).miniMsg(receiver.buildTagResolver(true))))
        } else if (message != null) {
            this.sendMessage(
                Component.textOfChildren(
                    translatePlaceholders(receiver, messageFormat).miniMsg(receiver.buildTagResolver(true)),
                    message.miniMsg(receiver.buildTagResolver(true))
                )
            )
        }
    }

    private val replyMap = mutableMapOf<Player, Job>()
    private fun handleReplyTimer(player: Player, chattyData: ChannelData): Job {
        replyMap[player]?.let { return it }
        replyMap[player]?.cancel()
        return chatty.plugin.launch {
            delay(chatty.config.privateMessages.messageReplyTime)
            replyMap[player]?.cancel()
            replyMap.remove(player)
            player.toGeary().setPersisting(chattyData.copy(lastMessager = null))
        }
    }

    private fun Player.handleSendingPrivateMessage(other: Player, signedMessage: SignedMessage? = null, message: String? = null, isReply: Boolean = false) {
        val chattyData = toGeary().get<ChannelData>() ?: return
        when {
            !chatty.config.privateMessages.enabled ->
                sendFormattedMessage(chatty.messages.privateMessages.disabled)

            isReply && chattyData.lastMessager == null ->
                sendFormattedMessage(chatty.messages.privateMessages.emptyReply)

            else -> {
                if ((message.isNullOrEmpty() && signedMessage == null) || this == other) return

                replyMap[other] = handleReplyTimer(other, chattyData)

                this.sendFormattedPrivateMessage(chatty.config.privateMessages.messageSendFormat, signedMessage, message, other)
                other.sendFormattedPrivateMessage(chatty.config.privateMessages.messageReceiveFormat, signedMessage, message, this)
                val gearyOther = other.toGeary()
                val otherChannelData = gearyOther.get<ChannelData>()
                if (otherChannelData != null) {
                    gearyOther.setPersisting(otherChannelData.copy(lastMessager = uniqueId))
                }
                if (chatty.config.privateMessages.messageSendSound.isNotEmpty())
                    this.playSound(other.location, chatty.config.privateMessages.messageSendSound, 1f, 1f)
                if (chatty.config.privateMessages.messageReceivedSound.isNotEmpty())
                    other.playSound(other.location, chatty.config.privateMessages.messageReceivedSound, 1f, 1f)
            }
        }
    }
}