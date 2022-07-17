package com.mineinabyss.chatty

import com.mineinabyss.chatty.components.playerData
import com.mineinabyss.chatty.helpers.chattyConfig
import com.mineinabyss.chatty.helpers.chattyPlugin
import com.mineinabyss.chatty.helpers.getAllChannelNames
import com.mineinabyss.chatty.helpers.getDefaultChat
import com.mineinabyss.idofront.commands.execution.IdofrontCommandExecutor
import com.mineinabyss.idofront.commands.extensions.actions.ensureSenderIsPlayer
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.messaging.miniMsg
import com.mineinabyss.idofront.messaging.warn
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class ChattyCommands : IdofrontCommandExecutor(), TabCompleter {
    override val commands = commands(chattyPlugin) {

        "chatty"(desc = "Chatty commands") {
            ensureSenderIsPlayer()
            action {
                (sender as Player).swapChannelCommand(arguments)
            }
            "channels"(desc = "List all channels") {
                action {
                    sender.info("<gold>Available channels are:".miniMsg())
                    getAllChannelNames().forEach {
                        sender.info("<yellow>$it".miniMsg())
                    }
                }
            }
        }
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): MutableList<String>? {
        TODO("Not yet implemented")
    }
}

private fun Player.swapChannelCommand(arguments: List<String>) {
    if (arguments.isEmpty()) {
        performCommand("chatty")
        return
    }

    val newChannel =
        chattyConfig.channels.firstOrNull {
            it.channelCommand == arguments[0] || it.channelCommandAliases.contains(arguments[0])
        }
    val defaultChannel = getDefaultChat()
    if (newChannel == null) {
        error("Invalid channel. Valid channels: ${chattyConfig.channels.joinToString(", ")}")
        warn("Set channel to ${defaultChannel.channelName}")
        playerData.channel = defaultChannel
    } else {
        sendMessage(chattyConfig.channelChangedMessage.replace("%channel%", newChannel.channelName))
        playerData.channel = newChannel
    }
}
