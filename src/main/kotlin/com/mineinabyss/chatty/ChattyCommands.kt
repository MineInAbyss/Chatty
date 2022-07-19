package com.mineinabyss.chatty

import com.mineinabyss.chatty.components.playerData
import com.mineinabyss.chatty.helpers.*
import com.mineinabyss.idofront.commands.arguments.stringArg
import com.mineinabyss.idofront.commands.execution.IdofrontCommandExecutor
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
                    playerAction {
                        val player = sender as Player
                        player.playerData.disablePingSound = !player.playerData.disablePingSound
                        player.success("Ping sound is now <i>${if (player.playerData.disablePingSound) "disabled" else "enabled"}.")
                    }
                }
                "sound"(desc = "Change your pingsound") {
                    val soundName by stringArg()
                    playerAction {
                        val player = sender as Player
                        if (soundName in getAlternativePingSounds) {
                            player.playerData.pingSound = soundName
                            player.success("Ping sound set to <i>$soundName")
                        } else {
                            player.error("<i>$soundName</i> is not a valid ping sound.")
                        }
                    }
                }
            }
            "channels"(desc = "List all channels") {
                playerAction {
                    sender.info("<gold>Available channels are:".miniMsg())
                    sender.info("<yellow>${getAllChannelNames()}".miniMsg())
                }
            }
            "nickname" {
                val nickname by stringArg()
                action {
                    val player = sender as Player
                    if (nickname.isEmpty()) player.displayName(player.name.miniMsg())
                    else player.displayName(arguments.joinToString().replace(", ", " ").miniMsg())
                    sender.success("Nickname set to <white><i>${player.displayName().deserialize()}</i></white>.")
                }
            }
            "reload" {
                action {
                    ChattyConfig.reload()
                    ChattyConfig.load()
                    sender.info("<gold>Chatty config reloaded")
                }
            }
            getAllChannelNames().forEach { channelName ->
                channelName {
                    playerAction {
                        (sender as Player).swapChannelCommand(channelName)
                    }
                }
            }
            chattyConfig.channels.forEach { channel ->
                channel.channelAliases.forEach { alias ->
                    alias {
                        playerAction {
                            (sender as Player).swapChannelCommand(channel.channelName)
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

private fun Player.swapChannelCommand(channel: String) {
    val newChannel = chattyConfig.channels
        .firstOrNull { it.channelName == channel || it.channelAliases.contains(channel) }

    if (newChannel == null) {
        warn("No channel by the name <i>${channel}</i> exists.")
        warn("Valid channels are: ${getAllChannelNames()}")
    } else {
        sendMessage(translatePlaceholders(this, messages.channelChangedMessage.replace("%chatty_channel%", newChannel.channelName)))
        playerData.channel = newChannel
    }
}
