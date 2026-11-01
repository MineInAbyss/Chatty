import com.mineinabyss.chatty.ChattyConfig
import com.mineinabyss.chatty.ChattyMessages
import com.mineinabyss.chatty.helpers.DiscordEmoteFixer
import com.mineinabyss.geary.papermc.schemas.GearyPrefabSchemas.gearyPrefabs
import com.mineinabyss.jsonschema.writer.SchemaWriter
import com.mineinabyss.jsonschema.writer.writeSchemas
import kotlinx.serialization.KSerializer

fun main(args: Array<String>) = writeSchemas(args) {
    config("config", "config.yml") { ChattyConfig.serializer() }
    config("messages", "messages.yml") { ChattyMessages.serializer() }
    config("emotefixer", "emotefixer.yml") { DiscordEmoteFixer.serializer() }
    gearyPrefabs("chatty", listOf("com.mineinabyss.chatty"))
}

// Some serializers touch Bukkit registries while initializing, which fails without a server
private fun SchemaWriter.config(name: String, path: String, serializer: () -> KSerializer<*>) =
    runCatching { schema("chatty/$name", listOf("**/plugins/Chatty/$path"), serializer()) }
        .onFailure { println("Warn: Could not generate schema chatty/$name: $it") }
