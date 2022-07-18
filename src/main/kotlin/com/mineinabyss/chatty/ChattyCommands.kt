package com.mineinabyss.chatty

import com.mineinabyss.chatty.components.playerData
import com.mineinabyss.chatty.helpers.chattyConfig
import com.mineinabyss.chatty.helpers.getAllChannelNames
import com.mineinabyss.chatty.helpers.translatePlaceholders
import com.mineinabyss.idofront.commands.execution.IdofrontCommandExecutor
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.messaging.miniMsg
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.messaging.warn
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class ChattyCommands : IdofrontCommandExecutor(), TabCompleter {
    override val commands = commands(chattyPlugin) {
        /*getAllChannelNames().forEach { channelName ->
            channelName {
                playerAction {
                    (sender as Player).swapChannelCommand(channelName)
                }
            }
        }
        chattyConfig.channels.forEach { channel ->
            channel.channelCommand {
                playerAction {
                    (sender as Player).swapChannelCommand(channel.channelName)
                }
            }
            channel.channelCommandAliases.forEach { alias ->
                alias {
                    playerAction {
                        (sender as Player).swapChannelCommand(channel.channelName)
                    }
                }
            }
        }*/
        "chatty"(desc = "Chatty commands") {
            "channels"(desc = "List all channels") {
                playerAction {
                    sender.info("<gold>Available channels are:".miniMsg())
                    sender.info("<yellow>${getAllChannelNames()}".miniMsg())
                }
            }
            "nickname" {
                playerAction {
                    (sender as Player).displayName(arguments.joinToString { it }.miniMsg())
                    sender.success("Nickname set to ${player.displayName()}")
                }
            }
            "reload" {
                action {
                    ChattyConfig.reload()
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
                channel.channelCommandAliases.forEach { alias ->
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
                1 -> listOf("reload", "channels", "nickname")
                else -> emptyList()
            }
        } else emptyList()
    }
}

private fun Player.swapChannelCommand(channel: String) {
    val newChannel =
        chattyConfig.channels.firstOrNull {
            it.channelName == channel ||
                    it.channelCommand == channel ||
                    it.channelCommandAliases.contains(channel)
        }
    if (newChannel == null) {
        warn("No channel by the name <i>${channel}</i> exists.")
        warn("Valid channels are: ${getAllChannelNames()}")
    } else {
        sendMessage(translatePlaceholders(this, chattyConfig.channelChangedMessage.replace("%channel%", newChannel.channelName)))
        playerData.channel = newChannel
    }
}
