@file:Suppress("UnstableApiUsage")

package com.mineinabyss.chatty

import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.chatty.components.*
import com.mineinabyss.chatty.helpers.*
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.idofront.commands.arguments.stringArg
import com.mineinabyss.idofront.commands.execution.IdofrontCommandExecutor
import com.mineinabyss.idofront.commands.extensions.actions.ensureSenderIsPlayer
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.entities.toPlayer
import com.mineinabyss.idofront.events.call
import com.mineinabyss.idofront.textcomponents.miniMsg
import com.mineinabyss.idofront.textcomponents.serialize
import io.papermc.paper.event.player.AsyncChatDecorateEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import kotlin.collections.set

class ChattyCommands : IdofrontCommandExecutor(), TabCompleter {
    override val commands = commands(chatty.plugin) {
        "chatty"(desc = "Chatty commands") {
            "reload" {
                action {
                    chatty.plugin.createChattyContext()
                    sender.sendConsoleMessage("<green>Chatty has been reloaded!")
                }
            }
            "ping"(desc = "Commands related to the chat-ping feature.") {
                "toggle"(desc = "Toggle the ping sound.") {
                    playerAction {
                        val gearyPlayer = player.toGeary()
                        val oldData = gearyPlayer.get<ChannelData>() ?: return@playerAction
                        gearyPlayer.setPersisting(oldData.copy(disablePingSound = !oldData.disablePingSound))
                        player.sendFormattedMessage(chatty.messages.ping.toggledPingSound)
                    }
                }
                "sound"(desc = "Change your pingsound") {
                    val soundName by stringArg()
                    playerAction {
                        val gearyPlayer = player.toGeary()
                        val oldData = gearyPlayer.get<ChannelData>() ?: return@playerAction
                        if (soundName in getAlternativePingSounds) {
                            gearyPlayer.setPersisting(oldData.copy(pingSound = soundName))
                            player.sendFormattedMessage(chatty.messages.ping.changedPingSound)
                        } else player.sendFormattedMessage(chatty.messages.ping.invalidPingSound)
                    }
                }
            }
            ("channels" / "ch")(desc = "List all channels") {
                action {
                    (sender as? Player)?.sendFormattedMessage(chatty.messages.channels.availableChannels)
                        ?: sender.sendRichMessage(chatty.messages.channels.availableChannels)
                }
            }
            ("nickname" / "nick") {
                playerAction {
                    val nickMessage = chatty.messages.nicknames
                    val nick = arguments.toSentence()
                    val bypassFormatPerm = player.hasPermission(ChattyPermissions.NICKNAME_OTHERS)

                    when {
                        !player.hasPermission(ChattyPermissions.NICKNAME) ->
                            player.sendFormattedMessage(nickMessage.selfDenied)

                        arguments.isEmpty() -> {
                            // Removes players displayname or sends error if sender is console
                            player.chattyNickname = null
                            player.sendFormattedMessage(nickMessage.selfEmpty)
                        }

                        arguments.first().startsWith(chatty.config.nicknames.nickNameOtherPrefix) -> {
                            val otherPlayer = arguments.getPlayerToNick()
                            val otherNick = nick.removePlayerToNickFromString()

                            when {
                                !player.hasPermission(ChattyPermissions.NICKNAME_OTHERS) ->
                                    player.sendFormattedMessage(nickMessage.otherDenied, otherPlayer)

                                otherPlayer == null || otherPlayer !in Bukkit.getOnlinePlayers() ->
                                    player.sendFormattedMessage(nickMessage.invalidPlayer, otherPlayer)

                                otherNick.isEmpty() -> {
                                    otherPlayer.chattyNickname = null
                                    otherPlayer.sendFormattedMessage(nickMessage.selfEmpty)
                                    player.sendFormattedMessage(nickMessage.otherEmpty, otherPlayer)
                                }

                                !bypassFormatPerm && !otherNick.verifyNickLength() ->
                                    player.sendFormattedMessage(nickMessage.tooLong)

                                otherNick.isNotEmpty() -> {
                                    otherPlayer.chattyNickname = otherNick
                                    player.sendFormattedMessage(nickMessage.otherSuccess, otherPlayer)
                                }
                            }
                        }

                        else -> {
                            if (!bypassFormatPerm && !nick.verifyNickLength()) {
                                player.sendFormattedMessage(nickMessage.tooLong)
                            } else {
                                player.chattyNickname = nick
                                player.sendFormattedMessage(nickMessage.selfSuccess)
                            }
                        }
                    }
                }
            }
            "commandspy" {
                playerAction {
                    val gearyPlayer = player.toGeary()
                    if (gearyPlayer.has<CommandSpy>()) {
                        gearyPlayer.remove<CommandSpy>()
                        player.sendFormattedMessage(chatty.messages.spying.commandSpyOff)
                    } else {
                        gearyPlayer.getOrSetPersisting { CommandSpy() }
                        player.sendFormattedMessage(chatty.messages.spying.commandSpyOn)
                    }
                }
            }
            "spy" {
                val channelName by stringArg()
                playerAction {
                    val channel = chatty.config.channels[channelName] ?: run {
                        player.sendFormattedMessage(chatty.messages.channels.noChannelWithName)
                        return@playerAction
                    }
                    val spy = player.toGeary().getOrSetPersisting { SpyOnChannels() }

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
            chatty.config.channels.values
                .flatMap { it.channelAliases + it.key }
                .forEach { channelName ->
                    channelName {
                        playerAction {
                            player.swapChannelCommand(chatty.config.channels[channelName])
                        }
                    }
                }
        }
        ("global" / "g") {
            playerAction {
                player.shortcutCommand(getGlobalChat(), arguments)
            }
        }
        ("local" / "l") {
            playerAction {
                player.shortcutCommand(getRadiusChannel(), arguments)
            }
        }
        ("admin" / "a") {
            playerAction {
                player.shortcutCommand(getAdminChannel(), arguments)
            }
        }
        ("message" / "msg")(desc = "Private message another player") {
            ensureSenderIsPlayer()
            val player by stringArg()
            action {
                (sender as? Player)?.handleSendingPrivateMessage(player.toPlayer() ?: return@action, arguments, false)
            }
        }
        ("reply" / "r")(desc = "Reply to your previous private message") {
            ensureSenderIsPlayer()
            action {
                val player = sender as? Player ?: return@action
                player.toGeary().get<ChannelData>()?.lastMessager?.toPlayer()
                    ?.let { player.handleSendingPrivateMessage(it, arguments, true) }
                    ?: player.sendFormattedMessage(chatty.messages.privateMessages.emptyReply)
            }
        }
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String> {
        val onlinePlayers = Bukkit.getOnlinePlayers().map { it.name }
        val otherPrefix = chatty.config.nicknames.nickNameOtherPrefix
        return when (command.name) {
            "chatty" -> {
                when (args.size) {
                    1 -> listOf(
                        "message",
                        "ping",
                        "reload",
                        "channels",
                        "nickname",
                        "spy",
                        "commandspy"
                    ).filter { s -> s.startsWith(args[0]) }

                    2 -> when (args[0]) {
                        "ping" -> listOf("toggle", "sound").filter { s -> s.startsWith(args[1]) }
                        "spy" ->
                            chatty.config.channels.entries.filter { s ->
                                s.key.startsWith(args[1], true) && s.value.channelType != ChannelType.GLOBAL
                            }.map { it.key }

                        else -> emptyList()
                    }

                    3 -> when {
                        args[1] == "sound" -> getAlternativePingSounds.filter { s -> s.startsWith(args[2], true) }
                        args[1].startsWith(otherPrefix) -> onlinePlayers.filter { s ->
                            s.replace(otherPrefix.toString(), "").startsWith(args[2], true)
                        }

                        else -> emptyList()
                    }

                    else -> emptyList()
                }
            }
            "message", "msg" -> onlinePlayers.filter { s -> s.startsWith(args[0], true) }.take(25)
            else -> emptyList()
        }
    }

    private fun Player.shortcutCommand(
        channel: Map.Entry<String, ChattyChannel>?,
        arguments: List<String>
    ) {
        val chattyData = toGeary().get<ChannelData>() ?: return
        val currentChannel = chattyData.channelId
        when {
            channel == null -> sendFormattedMessage(chatty.messages.channels.noChannelWithName)
            channel.value.permission.isNotBlank() && !hasPermission(channel.value.permission) ->
                sendFormattedMessage(chatty.messages.channels.missingChannelPermission)

            arguments.isEmpty() -> swapChannelCommand(channel.value)
            else -> {
                toGeary().setPersisting(chattyData.copy(channelId = channel.key, lastChannelUsedId = channel.key))
                chatty.plugin.launch(chatty.plugin.asyncDispatcher) {
                    GenericChattyDecorateEvent(this@shortcutCommand, arguments.toSentence().miniMsg()).call {
                        GenericChattyChatEvent(
                            this@shortcutCommand,
                            (this as AsyncChatDecorateEvent).result()
                        ).callEvent()
                    }
                }
                toGeary().setPersisting(chattyData.copy(channelId = currentChannel))
            }
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

    private fun Player.handleSendingPrivateMessage(other: Player, arguments: List<String>, isReply: Boolean = false) {
        val chattyData = toGeary().get<ChannelData>() ?: return
        when {
            !chatty.config.privateMessages.enabled ->
                sendFormattedMessage(chatty.messages.privateMessages.disabled)

            isReply && chattyData.lastMessager == null ->
                sendFormattedMessage(chatty.messages.privateMessages.emptyReply)

            !isReply && arguments.first().toPlayer() == null ->
                sendFormattedMessage(chatty.messages.privateMessages.invalidPlayer)

            else -> {
                val msg = if (isReply) arguments.toSentence() else arguments.removeFirstArgumentOfStringList()
                if (msg.isEmpty() || this == other) return

                replyMap[other] = handleReplyTimer(other, chattyData)

                this.sendFormattedPrivateMessage(chatty.config.privateMessages.messageSendFormat, msg, other)
                other.sendFormattedPrivateMessage(chatty.config.privateMessages.messageReceiveFormat, msg, this)
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

    private fun Player.sendFormattedMessage(message: String, optionalPlayer: Player? = null) =
        this.sendMessage(translatePlaceholders((optionalPlayer ?: this), message).parseTags(this, true))

    private fun Player.sendFormattedPrivateMessage(messageFormat: String, message: String, receiver: Player) =
        this.sendMessage(
            (translatePlaceholders(receiver, messageFormat).serialize() + message)
                .parseTags(receiver, true)
        )

    private fun CommandSender.sendConsoleMessage(message: String) = this.sendMessage(message.parseTags(null, true))

    private fun List<String>.removeFirstArgumentOfStringList(): String =
        this.filter { it != this.first() }.toSentence()


    private fun Player.swapChannelCommand(newChannel: ChattyChannel?) {
        when {
            newChannel == null ->
                sendFormattedMessage(chatty.messages.channels.noChannelWithName)

            newChannel.permission.isNotBlank() && !hasPermission(newChannel.permission) ->
                sendFormattedMessage(chatty.messages.channels.missingChannelPermission)

            else -> {
                val chattyData = toGeary().get<ChannelData>() ?: return
                toGeary().setPersisting(chattyData.copy(channelId = newChannel.key, lastChannelUsedId = newChannel.key))
                sendFormattedMessage(chatty.messages.channels.channelChanged)
            }
        }
    }

}
