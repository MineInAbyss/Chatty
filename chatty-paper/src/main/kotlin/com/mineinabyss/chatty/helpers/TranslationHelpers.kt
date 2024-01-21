package com.mineinabyss.chatty.helpers

import com.mineinabyss.chatty.ChattyChannel
import com.mineinabyss.chatty.ChattyConfig
import com.mineinabyss.chatty.ChattyChannel.Translation.*
import com.mineinabyss.chatty.chatty
import com.mineinabyss.chatty.components.ChattyTranslation
import com.mineinabyss.idofront.textcomponents.miniMsg
import com.mineinabyss.idofront.textcomponents.serialize
import korlibs.datastructure.CacheMap
import net.kyori.adventure.chat.SignedMessage
import net.kyori.adventure.text.Component

data class TranslatedMessage(val language: TranslationLanguage, val translatedMessage: Component)
val cachedTranslations = CacheMap<SignedMessage, TranslatedMessage>(8)
fun handleMessageTranslation(channel: ChattyChannel, sourceTranslation: ChattyTranslation?, audienceTranslation: ChattyTranslation?, component: Component, signedMessage: SignedMessage): Component {
    val targetLanguage = when (channel.translation.type) {
        // Force translation with targetLanguage
        TargetLanguageType.FORCE -> channel.translation.targetLanguage
        // If the sourceLanguage is the same as audienceLanguage, avoid translating
        TargetLanguageType.SKIP_SAME_LANGUAGE -> channel.translation.targetLanguage.takeUnless { sourceTranslation != null && sourceTranslation == audienceTranslation }
        // If the audience has no language set, or the source language is the same, avoid translating
        TargetLanguageType.ALL_SAME_LANGUAGE -> audienceTranslation?.language?.takeUnless { it != sourceTranslation?.language }
        // No translation
        TargetLanguageType.NONE -> null
    } ?: return component

    // Only translate if the audience has a different language set, or if it is set to translate same languages
    //if (sourceTranslation?.language == targetLanguage) return component
    //if (audienceTranslation?.translateSameLanguage != true && sourceTranslation?.language != null  && sourceTranslation.language == audienceTranslation?.language) return component

    // We cache translations to avoid translating the same message multiple times
    return cachedTranslations.computeIfAbsent(signedMessage) {
        TranslatedMessage(targetLanguage,
            Component.textOfChildren(chatty.translator.translateText(component.serialize(), sourceTranslation?.language?.languageCode, targetLanguage.languageCode).text.miniMsg().hoverEventShowText(component), Component.text("*"))
        )
    }.translatedMessage
}

enum class TranslationLanguage(val languageCode: String) {
    Bulgarian("BG"),
    Czech("CS"),
    Danish("DA"),
    German("DE"),
    Greek("EL"),
    English_UK("EN-GB"),
    English_US("EN-US"),
    Spanish("ES"),
    Estonian("ET"),
    Finnish("FI"),
    French("FR"),
    Hungarian("HU"),
    Indonesian("ID"),
    Italian("IT"),
    Japanese("JA"),
    Korean("KO"),
    Lithuanian("LT"),
    Latvian("LV"),
    Norwegian("NB"),
    Dutch("NL"),
    Polish("PL"),
    Portuguese_Brazillian("PT-BR"),
    Portuguese_Rest("PT-PT"),
    Romanian("RO"),
    Russian("RU"),
    Slovak("SK"),
    Slovenian("SL"),
    Swedish("SV"),
    Turkish("TR"),
    Ukrainian("UK"),
    Chinese("ZH")
}