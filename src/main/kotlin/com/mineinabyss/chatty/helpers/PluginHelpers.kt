package com.mineinabyss.chatty.helpers

import com.mineinabyss.chatty.ChattyConfig
import com.mineinabyss.chatty.ChattyPlugin
import com.mineinabyss.idofront.plugin.getPlugin

val chattyPlugin = getPlugin<ChattyPlugin>()
val chattyConfig = ChattyConfig.data
