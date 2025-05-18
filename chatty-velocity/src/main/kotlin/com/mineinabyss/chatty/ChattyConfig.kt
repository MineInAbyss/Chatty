package com.mineinabyss.chatty

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import com.charleskorn.kaml.encodeToStream
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.server.RegisteredServer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import java.nio.file.Path

@Serializable
data class AlreadyJoined(val alreadyJoined: MutableSet<String>) {
    companion object {
        fun readConfig(dataDirectory: Path): MutableSet<String> {
            val file = dataDirectory.resolve("already_joined.json").toFile().apply { parentFile.mkdirs() }
            if (!file.exists()) this::class.java.classLoader.getResourceAsStream("already_joined.json")?.use {
                file.writeBytes(it.readAllBytes())
            }
            return runCatching {
                file.inputStream().use {
                    Json.decodeFromStream<AlreadyJoined>(it).alreadyJoined
                }
            }.onFailure { it.printStackTrace() }.getOrDefault(mutableSetOf())
        }

        fun saveToFile(dataDirectory: Path) {
            val file = dataDirectory.resolve("already_joined.json").toFile().apply { parentFile.mkdirs() }
            file.createNewFile()
            file.outputStream().use {
                Json.encodeToStream(AlreadyJoined(alreadyJoined), it)
            }
        }
    }
}

@Serializable
data class ChattyConfig(
    val join: Join = Join(),
    val leave: Leave = Leave(),
    val switch: Switch = Switch(),
) {

    companion object {
        fun readConfig(dataDirectory: Path): ChattyConfig {
            val config = dataDirectory.resolve("config.yml").toFile().apply { parentFile.mkdirs() }
            if (!config.exists()) this::class.java.classLoader.getResourceAsStream("config.yml")?.use {
                config.writeBytes(it.readAllBytes())
            }
            return runCatching {
                config.inputStream().use {
                    Yaml.default.decodeFromStream<ChattyConfig>(it)
                }
            }.onFailure { it.printStackTrace() }.getOrDefault(ChattyConfig())
        }

        @OptIn(ExperimentalSerializationApi::class)
        fun saveToFile(dataDirectory: Path) {
            val file = dataDirectory.resolve("config.yml").toFile().apply { parentFile.mkdirs() }
            file.createNewFile()
            file.outputStream().use {
                Yaml.default.encodeToStream(chattyConfig, it)
            }
        }
    }

    @Serializable
    data class Join(
        val message: String = "<green>+</green> | <aqua><player>",
        val otherServerMessage: String = "<green>+</green> | <aqua><player> <gray>(</gray><aqua><server></aqua><gray>)",
        val firstJoinMessage: String = "<gradient:#058330:#ff9200>Welcome <player> to <server></gradient>",
    ) {
        fun message(player: Player, server: RegisteredServer): Component =
            MiniMessage.miniMessage().deserialize(
                message, Placeholder.parsed("player", player.username),
                Placeholder.parsed("server", server.serverInfo.name)
            )

        fun otherServerMessage(player: Player, server: RegisteredServer): Component =
            MiniMessage.miniMessage().deserialize(
                otherServerMessage, Placeholder.parsed("player", player.username),
                Placeholder.parsed("server", server.serverInfo.name)
            )

        fun firstMessage(player: Player, server: RegisteredServer): Component =
            MiniMessage.miniMessage().deserialize(
                firstJoinMessage, Placeholder.parsed("player", player.username),
                Placeholder.parsed("server", server.serverInfo.name)
            )
    }

    @Serializable
    data class Leave(
        val message: String = "<red>-</red> | <aqua><player>",
        val otherServerMessage: String = "<red>-</red> | <aqua><player> <gray>(</gray><aqua><server></aqua><gray>)",
    ) {
        fun message(player: Player, server: RegisteredServer): Component =
            MiniMessage.miniMessage().deserialize(
                message, Placeholder.parsed("player", player.username),
                Placeholder.parsed("server", server.serverInfo.name)
            )

        fun otherServerMessage(player: Player, server: RegisteredServer): Component =
            MiniMessage.miniMessage().deserialize(
                otherServerMessage, Placeholder.parsed("player", player.username),
                Placeholder.parsed("server", server.serverInfo.name)
            )
    }

    @Serializable
    data class Switch(
        val fromMessage: String = "<gray>↓</gray> ┃ <aqua><player> <gray>joined from</gray> <from_server> server.",
        val toMessage: String = "<gray>↑</gray> ┃ <aqua><player> <gray>left to</gray> <to_server> server.",
    ) {
        fun fromServerMessage(player: Player, fromServer: RegisteredServer, toServer: RegisteredServer): Component =
            MiniMessage.miniMessage().deserialize(
                fromMessage, Placeholder.parsed("player", player.username),
                Placeholder.parsed("from_server", fromServer.serverInfo.name), Placeholder.parsed("to_server", toServer.serverInfo.name)
            )

        fun toServerMessage(player: Player, fromServer: RegisteredServer, toServer: RegisteredServer): Component =
            MiniMessage.miniMessage().deserialize(
                toMessage, Placeholder.parsed("player", player.username),
                Placeholder.parsed("from_server", fromServer.serverInfo.name), Placeholder.parsed("to_server", toServer.serverInfo.name)
            )
    }
}