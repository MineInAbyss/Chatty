package com.mineinabyss.chatty.commands

import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.mineinabyss.chatty.ChattyChannel
import com.mineinabyss.chatty.chatty
import com.mineinabyss.chatty.components.*
import com.mineinabyss.chatty.helpers.*
import com.mineinabyss.chatty.listeners.ProxyPrivateMessages
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.geary.serialization.getOrSetPersisting
import com.mineinabyss.geary.serialization.setPersisting
import com.mineinabyss.idofront.commands.brigadier.IdoRootCommand
import com.mineinabyss.idofront.commands.brigadier.commands
import com.mineinabyss.idofront.commands.brigadier.resolve
import com.mineinabyss.idofront.commands.brigadier.suggests
import com.mineinabyss.idofront.entities.toPlayer
import com.mineinabyss.idofront.events.call
import com.mineinabyss.idofront.textcomponents.miniMsg
import com.mojang.brigadier.arguments.StringArgumentType
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.event.player.AsyncChatDecorateEvent
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import net.kyori.adventure.chat.ChatType
import net.kyori.adventure.chat.SignedMessage
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Registry
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

@Suppress("UnstableApiUsage", "NAME_SHADOWING")
object ChattyCommands {

    fun registerSignedCommands() {}

    fun registerCommands() {
        chatty.commands {
            "chatty" {
                "ping" {
                    "toggle" {
                        executes.asPlayer {
                            val gearyPlayer = player.toGeary()
                            val oldData = gearyPlayer.get<ChannelData>() ?: return@asPlayer
                            gearyPlayer.setPersisting(oldData.copy(disablePingSound = !oldData.disablePingSound))
                            player.sendFormattedMessage(chatty.messages.ping.toggledPingSound)
                        }
                    }
                    "sound" {
                        executes.asPlayer().args(
                            "sound" to StringArgumentType.word().suggests {
                                chatty.config.ping.alternativePingSounds.takeUnless { "all" in it }
                                    ?: RegistryAccess.registryAccess().getRegistry(RegistryKey.SOUND_EVENT).let {
                                        it.map { s -> it.getKey(s)?.asMinimalString() }
                                    }
                            }
                        ) { sound ->
                            val gearyPlayer = player.toGeary()
                            val oldData = gearyPlayer.get<ChannelData>() ?: return@args
                            if (sound in alternativePingSounds) {
                                gearyPlayer.setPersisting(oldData.copy(pingSound = sound))
                                player.sendFormattedMessage(chatty.messages.ping.changedPingSound)
                            } else player.sendFormattedMessage(chatty.messages.ping.invalidPingSound)
                        }
                    }
                }
                "channels" {
                    permission = null
                    executes {
                        (sender as? Player)?.sendFormattedMessage(chatty.messages.channels.availableChannels)
                            ?: sender.sendRichMessage(chatty.messages.channels.availableChannels)
                    }
                }
                "channel" {
                    permission = null
                    executes.asPlayer().args(
                        "channel" to ChattyChannelArgument()
                    ) { channel ->
                        if (channel.channelType != ChannelType.CUSTOM) swapChannel(player, channel)
                    }
                }
                "commandspy" {
                    executes.asPlayer() {
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
                    executes.asPlayer().args("channel" to ChattyChannelArgument().suggests {
                        suggest(
                            chatty.config.channels.entries.asSequence()
                                .filter { it.value.channelType != ChannelType.CUSTOM }
                                .filter { it.value.permission.isEmpty() || context.source.sender.hasPermission(it.value.permission) }
                                .sortedBy {
                                    it.key in setOf(
                                        defaultChannel().key,
                                        radiusChannel()?.key,
                                        adminChannel()?.key
                                    ).filterNotNull()
                                }.map { it.key }.toList()
                        )
                    }) { channel ->
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
                            false -> this.stripTags().length > chatty.config.nicknames.maxLength
                        }
                    }

                    executes.asPlayer().args(
                        "nickname" to StringArgumentType.string().suggests {
                            (context.source.executor as? Player)?.chattyNickname?.let {
                                suggestFiltering(it)
                            }
                        }.default { "" },
                        "player" to ArgumentTypes.player().resolve().map { it.single() }
                            .default { executor as Player },
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
                                if (player.uniqueId != applyTo.uniqueId) player.sendFormattedMessage(
                                    nickMessage.otherSuccess,
                                    applyTo
                                )
                                else player.sendFormattedMessage(nickMessage.selfSuccess)
                            }
                        }
                    }
                }
            }

            ("global" / "g") {
                permission = null
                handleShortCutChannel(globalChannel())
            }
            ("local" / "l") {
                permission = null
                handleShortCutChannel(radiusChannel())
            }
            ("admin" / "a") {
                permission = "chatty.channel.admin"
                handleShortCutChannel(adminChannel())
            }
            ("message" / "msg") {
                permission = null
                handleMessage()
            }
            ("reply" / "r") {
                permission = null
                handleReply()
            }
        }
    }

    private fun IdoRootCommand.handleShortCutChannel(channel: MutableMap.MutableEntry<String, ChattyChannel>?) {
        executes.asPlayer {
            swapChannel(player, channel?.value)
        }
        if (chatty.config.chat.disableChatSigning) {
            executes.asPlayer().args("message" to StringArgumentType.greedyString()) { message ->
                player.shortcutCommand(
                    channel,
                    message,
                    null
                )
            }
        } else {
            executes.asPlayer().args(
                "message" to ArgumentTypes.signedMessage()
            ) { message ->
                chatty.launch {
                    val signedMessage = message.resolveSignedMessage("message", context).await()
                    player.shortcutCommand(channel, null, signedMessage)
                }
            }
        }
    }

    private fun IdoRootCommand.handleMessage() {
        executes.asPlayer().args(
            "sendTo" to StringArgumentType.word().suggests {
                suggestFiltering((Bukkit.getOnlinePlayers().map { it.name } + ProxyPrivateMessages.networkPlayers).distinct())
            },
            "message" to StringArgumentType.greedyString(),
        ) { sendTo, message ->
            val target = Bukkit.getPlayerExact(sendTo)
            when {
                target != null -> player.handleSendingPrivateMessage(target, null, message, false)
                chatty.config.privateMessages.enabled && chatty.config.privateMessages.proxy ->
                    ProxyPrivateMessages.send(player, sendTo, message)
                else -> player.sendFormattedMessage(chatty.messages.privateMessages.invalidPlayer)
            }
        }
    }

    private fun IdoRootCommand.handleReply() {

        executes.asPlayer().args(
            "message" to StringArgumentType.greedyString()
        ) { message ->
            val lastMessager = player.toGeary().get<ChannelData>()?.lastMessager
            val target = lastMessager?.toPlayer()
            when {
                target != null -> player.handleSendingPrivateMessage(target, null, message, true)
                lastMessager != null && chatty.config.privateMessages.enabled && chatty.config.privateMessages.proxy ->
                    ProxyPrivateMessages.send(player, lastMessager.toString(), message)
                else -> player.sendFormattedMessage(chatty.messages.privateMessages.emptyReply)
            }
        }
    }

    private fun Player.shortcutCommand(
        channelEntry: Map.Entry<String, ChattyChannel>?,
        message: String?,
        signedMessage: SignedMessage?,
    ) {
        val (channelId, channel) = channelEntry
            ?: return sendFormattedMessage(chatty.messages.channels.noChannelWithName)
        val chattyData = toGeary().get<ChannelData>() ?: return
        val currentChannel = chattyData.channelId
        when {
            channel.permission.isNotBlank() && !hasPermission(channel.permission) ->
                sendFormattedMessage(chatty.messages.channels.missingChannelPermission)

            message.isNullOrEmpty() && signedMessage == null -> swapChannel(this, channel)
            else -> {
                if (chatty.config.chat.disableChatSigning && !message.isNullOrEmpty()) {
                    toGeary().setPersisting(chattyData.copy(channelId = channelId, lastChannelUsedId = channelId))
                    chatty.launch(chatty.asyncDispatcher) {
                        AsyncChatDecorateEvent(this@shortcutCommand, message.miniMsg()).call<AsyncChatDecorateEvent> {
                            GenericChattyChatEvent(this@shortcutCommand, result()).callEvent()
                            withContext(chatty.minecraftDispatcher) {
                                // chance that player logged out by now
                                toGearyOrNull()?.setPersisting(chattyData.copy(channelId = currentChannel))
                            }
                        }
                    }
                } else if (!chatty.config.chat.disableChatSigning && signedMessage != null) {
                    val audience = channel.getAudience(this)
                    audience.forEach {
                        it.sendMessage(
                            signedMessage,
                            ChatType.CHAT.bind(appendChannelFormat(Component.empty(), this, channel))
                        )
                    }
                }
            }
        }
    }

    fun swapChannel(player: Player, newChannel: ChattyChannel?) {
        when {
            newChannel == null ->
                player.sendFormattedMessage(chatty.messages.channels.noChannelWithName)

            newChannel.channelType != ChannelType.CUSTOM && newChannel.permission.isNotBlank() && !player.hasPermission(
                newChannel.permission
            ) ->
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

    private fun CommandSender.sendFormattedMessage(message: String, optionalPlayer: Player? = null) {
        (optionalPlayer ?: this as? Player)?.also { player ->
            this.sendMessage(translatePlaceholders(player, message).miniMsg(player.buildTagResolver(true)))
        }
    }

    private fun Player.sendFormattedPrivateMessage(
        messageFormat: String,
        signedMessage: SignedMessage?,
        message: String?,
        receiver: Player,
    ) {
        if (signedMessage != null) {
            this.sendMessage(
                signedMessage,
                ChatType.CHAT.bind(
                    translatePlaceholders(
                        receiver,
                        messageFormat
                    ).miniMsg(receiver.buildTagResolver(true))
                )
            )
        } else if (message != null) {
            this.sendMessage(privateMessageComponent(messageFormat, message, receiver))
        }
    }

    fun privateMessageComponent(messageFormat: String, message: String, formatPlayer: Player): Component =
        Component.textOfChildren(
            translatePlaceholders(formatPlayer, messageFormat).miniMsg(formatPlayer.buildTagResolver(true)),
            message.miniMsg(formatPlayer.buildTagResolver(true))
        )

    fun Player.receivedPrivateMessage(from: UUID) {
        replyMap[uniqueId] = handleReplyTimer(this)
        val geary = toGeary()
        geary.get<ChannelData>()?.let { geary.setPersisting(it.copy(lastMessager = from)) }
        if (chatty.config.privateMessages.messageReceivedSound.isNotEmpty())
            playSound(location, chatty.config.privateMessages.messageReceivedSound, 1f, 1f)
    }

    fun Player.playPrivateMessageSendSound() {
        if (chatty.config.privateMessages.messageSendSound.isNotEmpty())
            playSound(location, chatty.config.privateMessages.messageSendSound, 1f, 1f)
    }

    private val replyMap = mutableMapOf<UUID, Job>()
    private fun handleReplyTimer(player: Player): Job {
        val uuid = player.uniqueId
        replyMap[uuid]?.cancel()
        return chatty.launch {
            delay(chatty.config.privateMessages.messageReplyTime)
            replyMap.remove(uuid)
            player.toGearyOrNull()?.let { geary ->
                geary.get<ChannelData>()?.let { geary.setPersisting(it.copy(lastMessager = null)) }
            }
        }
    }

    private fun Player.handleSendingPrivateMessage(
        other: Player,
        signedMessage: SignedMessage? = null,
        message: String? = null,
        isReply: Boolean = false,
    ) {
        val chattyData = toGeary().get<ChannelData>() ?: return
        when {
            !chatty.config.privateMessages.enabled ->
                sendFormattedMessage(chatty.messages.privateMessages.disabled)

            isReply && chattyData.lastMessager == null ->
                sendFormattedMessage(chatty.messages.privateMessages.emptyReply)

            else -> {
                if ((message.isNullOrEmpty() && signedMessage == null) || this == other) return

                this.sendFormattedPrivateMessage(
                    chatty.config.privateMessages.messageSendFormat,
                    signedMessage,
                    message,
                    other
                )
                other.sendFormattedPrivateMessage(
                    chatty.config.privateMessages.messageReceiveFormat,
                    signedMessage,
                    message,
                    this
                )
                other.receivedPrivateMessage(uniqueId)
                playPrivateMessageSendSound()
            }
        }
    }
}