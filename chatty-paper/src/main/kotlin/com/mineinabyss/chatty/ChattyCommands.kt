package com.mineinabyss.chatty

import com.mineinabyss.chatty.components.playerData
import com.mineinabyss.chatty.helpers.*
import com.mineinabyss.idofront.commands.arguments.stringArg
import com.mineinabyss.idofront.commands.execution.IdofrontCommandExecutor
import com.mineinabyss.idofront.commands.extensions.actions.ensureSenderIsPlayer
import com.mineinabyss.idofront.messaging.miniMsg
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class ChattyCommands : IdofrontCommandExecutor(), TabCompleter {
    override val commands = commands(chattyPlugin) {
        "chatty"(desc = "Chatty commands") {
            "ping"(desc = "Commands related to the chat-ping feature.") {
                "toggle"(desc = "Toggle the ping sound.") {
                    ensureSenderIsPlayer()
                    action {
                        val player = sender as? Player ?: return@action
                        player.playerData.disablePingSound = !player.playerData.disablePingSound
                        player.sendFormattedMessage(chattyMessages.ping.toggledPingSound)
                    }
                }
                "sound"(desc = "Change your pingsound") {
                    val soundName by stringArg()
                    ensureSenderIsPlayer()
                    action {
                        val player = sender as? Player ?: return@action
                        if (soundName in getAlternativePingSounds) {
                            player.playerData.pingSound = soundName
                            player.sendFormattedMessage(chattyMessages.ping.changedPingSound)
                        } else {
                            player.sendFormattedMessage(chattyMessages.ping.invalidPingSound)
                        }
                    }
                }
            }
            "channels"(desc = "List all channels") {
                action {
                    (sender as? Player)?.sendFormattedMessage(chattyMessages.channels.availableChannels)
                        ?: sender.sendMessage(chattyMessages.channels.availableChannels)
                }
            }
            "nickname" {
                action {
                    val nickMessage = chattyMessages.nicknames
                    val nickConfig = chattyConfig.nicknames
                    val nick = arguments.joinToString(" ")
                    val player = sender as? Player
                    val bypassFormatPerm = player?.hasPermission(nickConfig.bypassFormatPermission) == true

                    when {
                        player is Player && !player.hasPermission(nickConfig.permission) ->
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
                                player?.hasPermission(nickConfig.nickOtherPermission) == false ->
                                    player.sendFormattedMessage(nickMessage.otherDenied, otherPlayer)
                                !Bukkit.getOnlinePlayers().contains(otherPlayer) ->
                                    player?.sendFormattedMessage(nickMessage.invalidPlayer, otherPlayer)
                                otherNick.isEmpty() -> {
                                    otherPlayer?.displayName(otherPlayer.name.miniMsg())
                                    player?.sendFormattedMessage(nickMessage.otherEmpty, otherPlayer)
                                }
                                !bypassFormatPerm && !otherNick.verifyNickStyling() ->
                                    player?.sendFormattedMessage(nickMessage.disallowedStyling)
                                !bypassFormatPerm && !otherNick.verifyNickLength() ->
                                    player?.sendFormattedMessage(nickMessage.tooLong)
                                otherNick.isNotEmpty() -> {
                                    otherPlayer?.displayName(otherNick.miniMsg())
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
                                player?.sendFormattedMessage(nickMessage.selfSuccess)
                            }
                        }
                    }
                }
            }
            "reload" {
                "config" {
                    action {
                        ChattyConfig.reload()
                        ChattyConfig.load()
                        (sender as? Player)?.sendFormattedMessage(chattyMessages.other.configReloaded)
                            ?: sender.sendConsoleMessage(chattyMessages.other.configReloaded)
                    }
                }
                "messages" {
                    action {
                        ChattyMessages.reload()
                        ChattyMessages.load()
                        (sender as? Player)?.sendFormattedMessage(chattyMessages.other.messagesReloaded)
                            ?: sender.sendConsoleMessage(chattyMessages.other.messagesReloaded)
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
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String> {
        return if (command.name == "chatty") {
            when (args.size) {
                1 -> listOf("ping", "reload", "channels", "nickname")
                2 -> when (args[0]) {
                    "ping" -> listOf("toggle", "sound")
                    "reload" -> listOf("config", "messages")
                    else -> emptyList()
                }
                3 ->
                    if (args[0] == "ping" && args[1] == "sound") getAlternativePingSounds
                    else emptyList()
                else -> emptyList()
            }
        } else emptyList()
    }
}

private fun Player.swapChannelCommand(channelId: String) {
    val newChannel = getChannelFromId(channelId)

    if (newChannel == null) {
        sendFormattedMessage(chattyMessages.channels.noChannelWithName)
    } else if (!hasPermission(newChannel.permission)) {
        sendFormattedMessage(chattyMessages.channels.missingChannelPermission)
    } else {
        playerData.channelId = channelId
        sendFormattedMessage(chattyMessages.channels.channelChanged)
    }
}
