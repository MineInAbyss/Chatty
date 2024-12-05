package com.mineinabyss.chatty.commands

import com.mineinabyss.chatty.ChattyChannel
import com.mineinabyss.chatty.chatty
import com.mineinabyss.chatty.components.ChannelType
import com.mineinabyss.chatty.helpers.adminChannel
import com.mineinabyss.chatty.helpers.defaultChannel
import com.mineinabyss.chatty.helpers.radiusChannel
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.papermc.paper.command.brigadier.MessageComponentSerializer
import io.papermc.paper.command.brigadier.argument.CustomArgumentType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import java.util.concurrent.CompletableFuture

class ChattyChannelArgument : CustomArgumentType.Converted<ChattyChannel, String> {

    override fun getNativeType(): ArgumentType<String> {
        return StringArgumentType.word()
    }

    override fun <S : Any> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        val messageComponent = MessageComponentSerializer.message().serialize(Component.text("look at this cool green tooltip!", NamedTextColor.GREEN))
        chatty.config.channels.entries.asSequence()
            .filter { it.value.channelType != ChannelType.CUSTOM }
            .filter { it.value.permission.isEmpty() }
            .sortedBy {
                it.key in setOf(defaultChannel().key, radiusChannel()?.key, adminChannel()?.key).filterNotNull()
            }.forEach {
                builder.suggest(it.key, messageComponent)
            }

        return builder.buildFuture()
    }

    override fun convert(nativeType: String): ChattyChannel {
        return runCatching {
            chatty.config.channels[nativeType]!!
        }.getOrElse {
            val message = MessageComponentSerializer.message().serialize(Component.text("Invalid channel $nativeType", NamedTextColor.RED))
            throw CommandSyntaxException(SimpleCommandExceptionType(message), message)
        }
    }


}