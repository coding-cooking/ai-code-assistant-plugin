package com.example.helloplugin

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@Service
@State(name = "AiAssistantSettings", storages = [Storage("ai-assistant.xml")])
class AiSettings : PersistentStateComponent<AiSettings.State> {

    data class State(
        var apiKey: String = "",
        var providerName: String = "",
        var model: String = "",
        var customApiUrl: String = "",
        var hasCompletedOnboarding: Boolean = false
    )

    private var myState = State()

    override fun getState(): State = myState
    override fun loadState(state: State) { myState = state }

    var apiKey: String
        get() = myState.apiKey
        set(value) { myState.apiKey = value }

    var providerName: String
        get() = myState.providerName
        set(value) { myState.providerName = value }

    var model: String
        get() = myState.model
        set(value) { myState.model = value }

    var customApiUrl: String
        get() = myState.customApiUrl
        set(value) { myState.customApiUrl = value }

    var hasCompletedOnboarding: Boolean
        get() = myState.hasCompletedOnboarding
        set(value) { myState.hasCompletedOnboarding = value }

    val provider: AiProvider
        get() = if (providerName.isBlank()) AiProvider.OPENAI else AiProvider.fromName(providerName)

    val effectiveApiUrl: String
        get() = if (provider == AiProvider.CUSTOM) customApiUrl else provider.apiUrl

    fun isConfigured() = apiKey.isNotBlank() && providerName.isNotBlank()

    companion object {
        fun getInstance(): AiSettings =
            ApplicationManager.getApplication().getService(AiSettings::class.java)
    }
}
