package com.mineinabyss.chatty

import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.chatty.components.SpyOnLocal
import com.mineinabyss.chatty.components.chattyData
import com.mineinabyss.chatty.helpers.*
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.idofront.commands.arguments.stringArg
import com.mineinabyss.idofront.commands.execution.IdofrontCommandExecutor
import com.mineinabyss.idofront.commands.extensions.actions.ensureSenderIsPlayer
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.messaging.miniMsg
import io.papermc.paper.chat.ChatRenderer
import io.papermc.paper.event.player.AsyncChatEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class ChattyCommands : IdofrontCommandExecutor(), TabCompleter {
    override val commands = commands(chatty) {
        "chatty"(desc = "Chatty commands") {
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
                    player.chattyData.lastMessager?.let { player.handleSendingPrivateMessage(it, arguments, true) } ?:
                    player.sendFormattedMessage(chattyMessages.privateMessages.emptyReply)
                }
            }
            permission("chatty.ping")
            "ping"(desc = "Commands related to the chat-ping feature.") {
                "toggle"(desc = "Toggle the ping sound.") {
                    ensureSenderIsPlayer()
                    permission("chatty.ping.toggle")
                    action {
                        val player = sender as? Player ?: return@action
                        player.chattyData.disablePingSound = !player.chattyData.disablePingSound
                        player.sendFormattedMessage(chattyMessages.ping.toggledPingSound)
                    }
                }
                "sound"(desc = "Change your pingsound") {
                    val soundName by stringArg()
                    ensureSenderIsPlayer()
                    permission("chatty.ping.sound")
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
                permission("")
                action {
                    (sender as? Player)?.sendFormattedMessage(chattyMessages.channels.availableChannels)
                        ?: sender.sendMessage(chattyMessages.channels.availableChannels)
                }
            }
            ("nickname" / "nick") {
                permission("chatty.nickname")
                action {
                    val nickMessage = chattyMessages.nicknames
                    val nickConfig = chattyConfig.nicknames
                    val nick = arguments.joinToString(" ")
                    val player = sender as? Player
                    val bypassFormatPerm = player?.checkPermission(nickConfig.bypassFormatPermission) == true

                    when {
                        player is Player && !player.checkPermission(nickConfig.permission) ->
                            player.sendFormattedMessage(nickMessage.selfDenied)
                        arguments.isEmpty() -> {
                            // Removes players displayname or sends error if sender is console
                            player?.displayName(player.name.miniMsg())
                            player?.sendFormattedMessage(nickMessage.selfEmpty)
                                ?: sender.sendConsoleMessage(nickMessage.consoleNicknameSelf)
                        }
                        arguments.first().startsWith(nickConfig.nickNameOtherPrefix) -> {
                            val otherPlayer = arguments.getPlayerToNick()
                            val otherNick = nick.removePlayerToNickFromString()

                            when {
                                player?.checkPermission(nickConfig.nickOtherPermission) == false ->
                                    player.sendFormattedMessage(nickMessage.otherDenied, otherPlayer)
                                otherPlayer == null || otherPlayer !in Bukkit.getOnlinePlayers() ->
                                    player?.sendFormattedMessage(nickMessage.invalidPlayer, otherPlayer)
                                otherNick.isEmpty() -> {
                                    otherPlayer.displayName(player?.name?.miniMsg())
                                    player?.sendFormattedMessage(nickMessage.otherEmpty, otherPlayer)
                                }
                                !bypassFormatPerm && !otherNick.verifyNickStyling() ->
                                    player?.sendFormattedMessage(nickMessage.disallowedStyling)
                                !bypassFormatPerm && !otherNick.verifyNickLength() ->
                                    player?.sendFormattedMessage(nickMessage.tooLong)
                                otherNick.isNotEmpty() -> {
                                    otherPlayer.chattyData.nickName = otherNick
                                    otherPlayer.displayName(otherNick.miniMsg())
                                    player?.sendFormattedMessage(nickMessage.otherSuccess, otherPlayer)
                                }
                            }
                        }
                        else -> {
                            if (!bypassFormatPerm && !nick.verifyNickStyling()) {
                                player?.sendFormattedMessage(nickMessage.disallowedStyling)
                            } else if (!bypassFormatPerm && !nick.verifyNickLength()) {
                                player?.sendFormattedMessage(nickMessage.tooLong)
                            } else {
                                player?.displayName(nick.miniMsg())
                                player?.chattyData?.nickName = nick
                                player?.sendFormattedMessage(nickMessage.selfSuccess)
                            }
                        }
                    }
                }
            }
            ("reload" / "rl") {
                permission("chatty.reload")
                "config" {
                    permission("chatty.reload.config")
                    action {
                        ChattyConfig.reload()
                        ChattyConfig.load()
                        (sender as? Player)?.sendFormattedMessage(chattyMessages.other.configReloaded)
                            ?: sender.sendConsoleMessage(chattyMessages.other.configReloaded)
                    }
                }
                "messages" {
                    permission("chatty.reload.messages")
                    action {
                        ChattyMessages.reload()
                        ChattyMessages.load()
                        (sender as? Player)?.sendFormattedMessage(chattyMessages.other.messagesReloaded)
                            ?: sender.sendConsoleMessage(chattyMessages.other.messagesReloaded)
                    }
                }

            }
            "spy" {
                permission("chatty.spy")
                playerAction {
                    val player = sender as? Player ?: return@playerAction
                    val spy = player.toGeary().has<SpyOnLocal>()
                    if (spy) {
                        player.toGeary().remove<SpyOnLocal>()
                        player.sendFormattedMessage("<gold>You are no longer spying on chat.")
                    } else {
                        player.toGeary().setPersisting(SpyOnLocal())
                        player.sendFormattedMessage("<gold>You are no longer spying on chat.")
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
                player.chattyData.lastMessager?.let { player.handleSendingPrivateMessage(it, arguments, true) } ?:
                player.sendFormattedMessage(chattyMessages.privateMessages.emptyReply)
            }
        }
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String> {
        return if (command.name == "chatty") {
            when (args.size) {
                1 -> listOf("message", "ping", "reload", "channels", "nickname", "spy")
                2 -> when (args[0]) {
                    "ping" -> listOf("toggle", "sound")
                    "reload", "rl" -> listOf("config", "messages")
                    "message", "msg" -> Bukkit.getOnlinePlayers().map { it.name }.filter { s -> s.startsWith(args[1]) }
                    else -> emptyList()
                }
                3 -> when {
                    args[1] == "sound" -> getAlternativePingSounds
                    args[1].startsWith(chattyConfig.nicknames.nickNameOtherPrefix) ->
                        Bukkit.getOnlinePlayers().map { it.name }.filter { s ->
                            s.replace(chattyConfig.nicknames.nickNameOtherPrefix.toString(), "").startsWith(args[1])
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
        val msg = arguments.joinToString(" ").miniMsg()

        if (channel?.value?.permission?.isNotEmpty() == true && !checkPermission(channel.value.permission))
            sendFormattedMessage(chattyMessages.channels.missingChannelPermission)
        else if (channel?.key != null && arguments.isEmpty())
            swapChannelCommand(channel.key)
        else if (channel?.key != null && arguments.isNotEmpty()) {
            chattyData.channelId = channel.key
            chatty.launch(chatty.asyncDispatcher) {
                AsyncChatEvent(
                    true, this@shortcutCommand, mutableSetOf(), ChatRenderer.defaultRenderer(), msg, msg
                ).callEvent()
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
        if (!chattyConfig.privateMessages.enabled) {
            this.sendFormattedMessage(chattyMessages.privateMessages.disabled)
        } else if (isReply && this.chattyData.lastMessager == null) {
            this.sendFormattedMessage(chattyMessages.privateMessages.emptyReply)
        } else if (arguments.first().toPlayer() == null && !isReply) {
            this.sendFormattedMessage(chattyMessages.privateMessages.invalidPlayer)
        } else {
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
