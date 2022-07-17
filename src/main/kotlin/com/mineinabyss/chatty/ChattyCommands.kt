package com.mineinabyss.chatty

import com.mineinabyss.chatty.helpers.chattyPlugin
import com.mineinabyss.idofront.commands.execution.IdofrontCommandExecutor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class ChattyCommands : IdofrontCommandExecutor(), TabCompleter {
    override val commands = commands(chattyPlugin) {
        "chatty" {

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
