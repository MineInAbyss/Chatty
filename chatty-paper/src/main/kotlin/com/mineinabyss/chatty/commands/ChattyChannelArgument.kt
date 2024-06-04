package com.mineinabyss.chatty.commands

import com.mineinabyss.chatty.ChattyChannel
import com.mineinabyss.chatty.chatty
import com.mojang.brigadier.arguments.StringArgumentType
import io.papermc.paper.command.brigadier.argument.CustomArgumentType

class ChattyChannelArgument : CustomArgumentType.Converted<ChattyChannel, String> {

    override fun getExamples() = nativeType.examples

    override fun getNativeType() = StringArgumentType.greedyString()

    override fun convert(nativeType: String) = chatty.config.channels[nativeType]!!


}