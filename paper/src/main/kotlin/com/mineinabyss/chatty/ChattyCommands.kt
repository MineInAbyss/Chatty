package com.mineinabyss.chatty

import com.mineinabyss.chatty.components.playerData
import com.mineinabyss.chatty.helpers.*
import com.mineinabyss.idofront.commands.arguments.stringArg
import com.mineinabyss.idofront.commands.execution.IdofrontCommandExecutor
import com.mineinabyss.idofront.commands.extensions.actions.ensureSenderIsPlayer
import com.mineinabyss.idofront.messaging.miniMsg
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.messaging.*
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
                        player.sendFormattedMessage(messages.toggledPingSound)
                    }
                }
                "sound"(desc = "Change your pingsound") {
                    val soundName by stringArg()
                    ensureSenderIsPlayer()
                    action {
                        val player = sender as? Player ?: return@action
                        if (soundName in getAlternativePingSounds) {
                            player.playerData.pingSound = soundName
                            player.sendFormattedMessage(messages.changedPingSound)
                        } else {
                            player.sendFormattedMessage(messages.invalidPingSound)
                        }
                    }
                }
            }
            "channels"(desc = "List all channels") {
                action {
                    (sender as? Player)?.sendFormattedMessage(messages.availableChannels)
                        ?: sender.sendMessage(messages.availableChannels)
                }
            }
            "nickname" {
                val nickname by stringArg()
                ensureSenderIsPlayer()
                action {
                    val player = sender as? Player ?: return@action
                    if (nickname.isEmpty()) player.displayName(player.name.miniMsg())
                    else player.displayName(arguments.joinToString().replace(", ", " ").miniMsg())
                    player.sendFormattedMessage(messages.nickNameChanged)
                }
            }
            "reload" {
                action {
                    ChattyConfig.reload()
                    ChattyConfig.load()
                    (sender as? Player)?.sendFormattedMessage(messages.configReloaded) ?: sender.sendMessage(messages.configReloaded.miniMsg())
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
                2 -> if (args[0] == "ping") listOf("toggle", "sound") else emptyList()
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
        sendFormattedMessage(messages.noChannelWithName)
    } else if (!hasPermission(newChannel.permission)) {
        sendFormattedMessage(messages.missingChannelPermission)
    } else {
        playerData.channelId = channelId
        sendFormattedMessage(messages.channelChanged)
    }
}
