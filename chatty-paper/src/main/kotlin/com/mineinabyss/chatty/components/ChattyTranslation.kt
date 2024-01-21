package com.mineinabyss.chatty.components

import com.deepl.api.Language
import com.deepl.api.LanguageCode
import com.mineinabyss.chatty.chatty
import com.mineinabyss.chatty.helpers.TranslationLanguage
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("chatty:translation")
data class ChattyTranslation(val language: TranslationLanguage = chatty.config.defaultTranslationLanguage, val translateSameLanguage: Boolean = false)