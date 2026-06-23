package com.example.helloplugin

enum class AiProvider(
    val displayName: String,
    val shortName: String,
    val defaultModel: String,
    val apiUrl: String,
    val models: List<String>,
    val keyUrl: String,
    val isOpenAiCompatible: Boolean = true
) {
    OPENAI(
        displayName = "OpenAI",
        shortName = "ChatGPT",
        defaultModel = "gpt-4o-mini",
        apiUrl = "https://api.openai.com/v1/chat/completions",
        models = listOf("gpt-4o-mini", "gpt-4o", "gpt-4o-2024-11-20", "gpt-4-turbo", "gpt-3.5-turbo"),
        keyUrl = "https://platform.openai.com/api-keys"
    ),
    GOOGLE(
        displayName = "Google",
        shortName = "Gemini",
        defaultModel = "gemini-2.0-flash",
        apiUrl = "https://generativelanguage.googleapis.com/v1beta/openai/chat/completions",
        models = listOf("gemini-2.0-flash", "gemini-2.0-flash-lite", "gemini-1.5-flash", "gemini-1.5-pro"),
        keyUrl = "https://aistudio.google.com/apikey"
    ),
    ANTHROPIC(
        displayName = "Anthropic",
        shortName = "Claude",
        defaultModel = "claude-3-5-haiku-20241022",
        apiUrl = "https://api.anthropic.com/v1/messages",
        models = listOf("claude-3-5-haiku-20241022", "claude-3-5-sonnet-20241022", "claude-3-7-sonnet-20250219", "claude-opus-4-8"),
        keyUrl = "https://console.anthropic.com/settings/keys",
        isOpenAiCompatible = false
    ),
    DEEPSEEK(
        displayName = "DeepSeek",
        shortName = "DeepSeek",
        defaultModel = "deepseek-chat",
        apiUrl = "https://api.deepseek.com/v1/chat/completions",
        models = listOf("deepseek-chat", "deepseek-reasoner"),
        keyUrl = "https://platform.deepseek.com/api_keys"
    ),
    MOONSHOT(
        displayName = "Moonshot (Kimi)",
        shortName = "Kimi",
        defaultModel = "moonshot-v1-8k",
        apiUrl = "https://api.moonshot.cn/v1/chat/completions",
        models = listOf("moonshot-v1-8k", "moonshot-v1-32k", "moonshot-v1-128k"),
        keyUrl = "https://platform.moonshot.cn/console/api-keys"
    ),
    ZHIPU(
        displayName = "ZhipuAI (GLM)",
        shortName = "GLM",
        defaultModel = "glm-4-flash",
        apiUrl = "https://open.bigmodel.cn/api/paas/v4/chat/completions",
        models = listOf("glm-4-flash", "glm-4.7-flash", "glm-4-plus", "glm-4-long", "glm-z1-flash"),
        keyUrl = "https://bigmodel.cn/usercenter/proj-mgmt/apikeys"
    ),
    CUSTOM(
        displayName = "Custom",
        shortName = "AI",
        defaultModel = "",
        apiUrl = "",
        models = emptyList(),
        keyUrl = ""
    );

    val menuLabel get() = "Ask $shortName"

    companion object {
        fun fromName(name: String) = entries.find { it.name == name } ?: OPENAI
    }
}
