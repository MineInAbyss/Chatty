@file:Suppress("UnstableApiUsage")

package com.mineinabyss.chatty

import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.chatty.components.*
import com.mineinabyss.chatty.helpers.*
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.idofront.commands.arguments.optionArg
import com.mineinabyss.idofront.commands.arguments.stringArg
import com.mineinabyss.idofront.commands.execution.IdofrontCommandExecutor
import com.mineinabyss.idofront.commands.extensions.actions.ensureSenderIsPlayer
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.config.config
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
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

class ChattyCommands : IdofrontCommandExecutor(), TabCompleter {
    override val commands = commands(chatty) {
        "chatty"(desc = "Chatty commands") {
            "reload" {
                val option by optionArg(listOf("all", "config", "messages", "emotefixer")) { default = "all" }
                action {
                    when (option) {
                        "all" -> {
                            chatty.config = config("config") { chatty.fromPluginPath(loadDefault = true) }
                            chatty.messages = config("messages") { chatty.fromPluginPath(loadDefault = true) }
                            chatty.emotefixer = config("emotefixer") { chatty.fromPluginPath(loadDefault = true) }
                            sender.sendConsoleMessage("<green>Reloaded everything!")
                        }

                        "config" -> {
                            chatty.config = config("config") { chatty.fromPluginPath(loadDefault = true) }
                            sender.sendConsoleMessage("<green>Reloaded configs!")
                        }

                        "messages" -> {
                            chatty.messages = config("messages") { chatty.fromPluginPath(loadDefault = true) }
                            sender.sendConsoleMessage("<green>Reloaded messages!")
                        }

                        "emotefixer" -> {
                            chatty.emotefixer = config("emotefixer") { chatty.fromPluginPath(loadDefault = true) }
                            sender.sendConsoleMessage("<green>Reloaded emotefixer!")
                        }
                    }
                }
                // chatty.config.reload() is a thing but does not regen or remove stuff so
//                    chatty.config.reload()
//                    chatty.messages.reload()
//                    chatty.emoteFixer.reload()
            }
            ("message" / "msg")(desc = "Private message another player") {
                ensureSenderIsPlayer()
                val player by stringArg()
                action {
                    (sender as? Player)?.handleSendingPrivateMessage(
                        player.toPlayer() ?: return@action,
                        arguments,
                        false
                    )
                }
            }
            ("reply" / "r")(desc = "Reply to your previous private message") {
                ensureSenderIsPlayer()
                action {
                    val player = sender as? Player ?: return@action
                    player.chattyData.lastMessager?.let { player.handleSendingPrivateMessage(it, arguments, true) }
                        ?: player.sendFormattedMessage(chattyMessages.privateMessages.emptyReply)
                }
            }
            "ping"(desc = "Commands related to the chat-ping feature.") {
                "toggle"(desc = "Toggle the ping sound.") {
                    ensureSenderIsPlayer()
                    action {
                        val player = sender as? Player ?: return@action
                        player.chattyData.disablePingSound = !player.chattyData.disablePingSound
                        player.sendFormattedMessage(chattyMessages.ping.toggledPingSound)
                    }
                }
                "sound"(desc = "Change your pingsound") {
                    val soundName by stringArg()
                    ensureSenderIsPlayer()
                    action {
                        val player = sender as? Player ?: return@action
                        if (soundName in getAlternativePingSounds) {
                            player.chattyData.pingSound = soundName
                            player.sendFormattedMessage(chattyMessages.ping.changedPingSound)
                        } else {
                            player.sendFormattedMessage(chattyMessages.ping.invalidPingSound)
                        }
                    }
                }
            }
            ("channels" / "ch")(desc = "List all channels") {
                action {
                    (sender as? Player)?.sendFormattedMessage(chattyMessages.channels.availableChannels)
                        ?: sender.sendMessage(chattyMessages.channels.availableChannels)
                }
            }
            ("nickname" / "nick") {
                action {
                    val nickMessage = chattyMessages.nicknames
                    val nick = arguments.toSentence()
                    val player = sender as? Player
                    val bypassFormatPerm = player?.hasPermission(ChattyPermissions.NICKNAME_OTHERS) == true

                    when {
                        player is Player && !player.hasPermission(ChattyPermissions.NICKNAME) ->
                            player.sendFormattedMessage(nickMessage.selfDenied)

                        arguments.isEmpty() -> {
                            // Removes players displayname or sends error if sender is console
                            player?.chattyNickname = null
                            player?.sendFormattedMessage(nickMessage.selfEmpty)
                                ?: sender.sendConsoleMessage(nickMessage.consoleNicknameSelf)
                        }

                        arguments.first().startsWith(chattyConfig.nicknames.nickNameOtherPrefix) -> {
                            val otherPlayer = arguments.getPlayerToNick()
                            val otherNick = nick.removePlayerToNickFromString()

                            when {
                                player?.hasPermission(ChattyPermissions.NICKNAME_OTHERS) == false ->
                                    player.sendFormattedMessage(nickMessage.otherDenied, otherPlayer)

                                otherPlayer == null || otherPlayer !in Bukkit.getOnlinePlayers() ->
                                    player?.sendFormattedMessage(nickMessage.invalidPlayer, otherPlayer)

                                otherNick.isEmpty() -> {
                                    otherPlayer.chattyNickname = null
                                    otherPlayer.sendFormattedMessage(nickMessage.selfEmpty)
                                    player?.sendFormattedMessage(nickMessage.otherEmpty, otherPlayer)
                                }

                                !bypassFormatPerm && !otherNick.verifyNickLength() ->
                                    player?.sendFormattedMessage(nickMessage.tooLong)

                                otherNick.isNotEmpty() -> {
                                    otherPlayer.chattyNickname = otherNick
                                    player?.sendFormattedMessage(nickMessage.otherSuccess, otherPlayer)
                                }
                            }
                        }

                        else -> {
                            if (!bypassFormatPerm && !nick.verifyNickLength()) {
                                player?.sendFormattedMessage(nickMessage.tooLong)
                            } else {
                                player?.chattyNickname = nick
                                player?.sendFormattedMessage(nickMessage.selfSuccess)
                            }
                        }
                    }
                }
            }
            "commandspy" {
                playerAction {
                    val player = sender as? Player ?: return@playerAction
                    if (player.toGeary().has<CommandSpy>()) {
                        player.toGeary().remove<CommandSpy>()
                        player.sendFormattedMessage(chattyMessages.spying.commandSpyOff)
                    } else {
                        player.toGeary().getOrSetPersisting { CommandSpy() }
                        player.sendFormattedMessage(chattyMessages.spying.commandSpyOn)
                    }
                }
            }
            "spy" {
                val channel by stringArg()
                ensureSenderIsPlayer()
                action {
                    val player = sender as? Player ?: return@action
                    val spy = player.toGeary().getOrSetPersisting { SpyOnChannels() }

                    when {
                        channel !in chattyConfig.channels.keys ->
                            player.sendFormattedMessage(chattyMessages.channels.noChannelWithName)

                        getChannelFromId(channel)?.channelType == ChannelType.GLOBAL ->
                            player.sendFormattedMessage(chattyMessages.spying.cannotSpyOnChannel)

                        !player.hasPermission(getChannelFromId(channel)?.permission.toString()) ->
                            player.sendFormattedMessage(chattyMessages.spying.cannotSpyOnChannel)

                        channel in spy.channels -> {
                            player.sendFormattedMessage(chattyMessages.spying.stopSpyingOnChannel)
                            spy.channels.remove(channel)
                        }

                        else -> {
                            spy.channels.add(channel)
                            player.sendFormattedMessage(chattyMessages.spying.startSpyingOnChannel)
                        }
                    }
                }
            }
            getAllChannelNames().forEach { channelName ->
                channelName {
                    ensureSenderIsPlayer()
                    action {
                        val player = sender as? Player ?: return@action
                        player.swapChannelCommand(channelName)
                    }
                }
            }
            chattyConfig.channels.forEach { (channelId, channel) ->
                channel.channelAliases.forEach { alias ->
                    alias {
                        ensureSenderIsPlayer()
                        action {
                            val player = sender as? Player ?: return@action
                            player.swapChannelCommand(channelId)
                        }
                    }
                }
            }
        }
        ("global" / "g") {
            ensureSenderIsPlayer()
            action {
                (sender as? Player)?.shortcutCommand(getGlobalChat(), arguments)
            }
        }
        ("local" / "l") {
            ensureSenderIsPlayer()
            action {
                (sender as? Player)?.shortcutCommand(getRadiusChannel(), arguments)
            }
        }
        ("admin" / "a") {
            ensureSenderIsPlayer()
            action {
                (sender as? Player)?.shortcutCommand(getAdminChannel(), arguments)
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
                player.chattyData.lastMessager?.let { player.handleSendingPrivateMessage(it, arguments, true) }
                    ?: player.sendFormattedMessage(chattyMessages.privateMessages.emptyReply)
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
        val otherPrefix = chattyConfig.nicknames.nickNameOtherPrefix
        return if (command.name == "chatty") {
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
                    "message", "msg" -> onlinePlayers.filter { s -> s.startsWith(args[1], true) }
                    "spy" ->
                        chattyConfig.channels.keys.toList().filter { s ->
                            s.startsWith(
                                args[1],
                                true
                            ) && getChannelFromId(s)?.channelType != ChannelType.GLOBAL
                        }

                    "reload", "rl" -> listOf(
                        "all",
                        "config",
                        "messages",
                        "emotefixer"
                    ).filter { s -> s.startsWith(args[1]) }

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
        } else emptyList()
    }

    private fun Player.shortcutCommand(
        channel: Map.Entry<String, ChattyConfig.ChattyChannel>?,
        arguments: List<String>
    ) {
        val currentChannel = chattyData.channelId
        when {
            channel == null -> sendFormattedMessage(chattyMessages.channels.noChannelWithName)
            channel.value.permission.isNotBlank() && !hasPermission(channel.value.permission) ->
                sendFormattedMessage(chattyMessages.channels.missingChannelPermission)

            arguments.isEmpty() -> swapChannelCommand(channel.key)
            else -> {
                chattyData.channelId = channel.key
                chattyData.lastChannelUsed = channel.key
                chatty.launch(chatty.asyncDispatcher) {
                    GenericChattyDecorateEvent(this@shortcutCommand, arguments.toSentence().miniMsg()).call {
                        GenericChattyChatEvent(this@shortcutCommand, (this as AsyncChatDecorateEvent).result()).callEvent()
                    }
                }
                chattyData.channelId = currentChannel
            }
        }
    }

    private val replyMap = mutableMapOf<Player, Job>()
    private fun Player.handleReplyTimer(): Job {
        if (this in replyMap) return replyMap[this]!!
        replyMap[this]?.cancel()
        return chatty.launch(chatty.asyncDispatcher) {
            delay(chattyConfig.privateMessages.messageReplyTime)
            replyMap[this@handleReplyTimer]?.cancel()
            replyMap.remove(this@handleReplyTimer)
            this@handleReplyTimer.chattyData.lastMessager = null
        }
    }

    private fun Player.handleSendingPrivateMessage(player: Player, arguments: List<String>, isReply: Boolean = false) {
        when {
            !chattyConfig.privateMessages.enabled ->
                sendFormattedMessage(chattyMessages.privateMessages.disabled)

            isReply && this.chattyData.lastMessager == null ->
                sendFormattedMessage(chattyMessages.privateMessages.emptyReply)

            !isReply && arguments.first().toPlayer() == null ->
                sendFormattedMessage(chattyMessages.privateMessages.invalidPlayer)

            else -> {
                val msg = if (isReply) arguments.toSentence() else arguments.removeFirstArgumentOfStringList()
                if (msg.isEmpty() || this == player) return

                replyMap[player] = player.handleReplyTimer()

                this.sendFormattedPrivateMessage(chattyConfig.privateMessages.messageSendFormat, msg, player)
                player.sendFormattedPrivateMessage(chattyConfig.privateMessages.messageReceiveFormat, msg, this)
                player.chattyData.lastMessager = this
                if (chattyConfig.privateMessages.messageSendSound.isNotEmpty())
                    this.playSound(player.location, chattyConfig.privateMessages.messageSendSound, 1f, 1f)
                if (chattyConfig.privateMessages.messageReceivedSound.isNotEmpty())
                    player.playSound(player.location, chattyConfig.privateMessages.messageReceivedSound, 1f, 1f)
            }
        }
    }

    private fun Player.sendFormattedMessage(message: String, optionalPlayer: Player? = null) =
        this.sendMessage(translatePlaceholders((optionalPlayer ?: this), message).parseTags(this, true))

    private fun Player.sendFormattedPrivateMessage(messageFormat: String, message: String, receiver: Player) =
        this.sendMessage((translatePlaceholders(receiver, messageFormat).serialize() + message).parseTags(receiver, true))

    private fun CommandSender.sendConsoleMessage(message: String) = this.sendMessage(message.parseTags(null, true))

    private fun List<String>.removeFirstArgumentOfStringList(): String =
        this.filter { it != this.first() }.toSentence()
}
