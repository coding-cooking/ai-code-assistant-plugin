package com.example.helloplugin

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.ui.Messages
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class AskAiAction : AnAction() {

    override fun update(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)
        e.presentation.isEnabled = editor != null
        val settings = AiSettings.getInstance()
        e.presentation.text = if (settings.isConfigured()) settings.provider.menuLabel else "Ask AI"
    }

    override fun actionPerformed(e: AnActionEvent) {
        val settings = AiSettings.getInstance()

        if (!settings.isConfigured()) {
            SetupWizardDialog(e.project).show()
            return
        }

        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val selectedText = editor.selectionModel.selectedText

        if (selectedText.isNullOrBlank()) {
            Messages.showInfoMessage(e.project, "Please select some code first.", "Ask AI")
            return
        }

        var result = ""

        ProgressManager.getInstance().run(object : Task.Backgroundable(
            e.project,
            "${settings.provider.shortName} is thinking…",
            false
        ) {
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = true
                result = callAI(selectedText, settings)
            }

            override fun onSuccess() {
                ApplicationManager.getApplication().invokeLater {
                    AiResultDialog(e.project, settings.provider.shortName, result).show()
                }
            }

            override fun onThrowable(error: Throwable) {
                ApplicationManager.getApplication().invokeLater {
                    Messages.showErrorDialog(e.project, "Request failed: ${error.message}", "Error")
                }
            }
        })
    }

    private fun escapeJson(text: String) = text
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t")

    private fun callAI(code: String, settings: AiSettings): String {
        val rawPrompt = "Please explain the following code concisely:\n\n```\n$code\n```"
        val prompt = escapeJson(rawPrompt)

        val client = HttpClient.newHttpClient()
        val request = if (settings.provider == AiProvider.ANTHROPIC) {
            buildAnthropicRequest(prompt, settings)
        } else {
            buildOpenAiRequest(prompt, settings)
        }

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return parseContent(response.body(), settings.provider)
    }

    private fun buildOpenAiRequest(prompt: String, settings: AiSettings): HttpRequest {
        val body = """{"model":"${settings.model}","messages":[{"role":"user","content":"$prompt"}]}"""
        return HttpRequest.newBuilder()
            .uri(URI.create(settings.effectiveApiUrl))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer ${settings.apiKey}")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build()
    }

    private fun buildAnthropicRequest(prompt: String, settings: AiSettings): HttpRequest {
        val body = """{"model":"${settings.model}","max_tokens":1024,"messages":[{"role":"user","content":"$prompt"}]}"""
        return HttpRequest.newBuilder()
            .uri(URI.create(settings.effectiveApiUrl))
            .header("Content-Type", "application/json")
            .header("x-api-key", settings.apiKey)
            .header("anthropic-version", "2023-06-01")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build()
    }

    private fun parseContent(json: String, provider: AiProvider): String {
        // Anthropic: {"content":[{"type":"text","text":"..."}]}
        if (!provider.isOpenAiCompatible) {
            val pattern = Regex(""""text"\s*:\s*"((?:[^"\\]|\\.)*)"""")
            val match = pattern.findAll(json).firstOrNull()
                ?: return "Could not parse response:\n$json"
            return unescape(match.groupValues[1])
        }
        // OpenAI-compatible: choices[].message.content
        val pattern = Regex(""""content"\s*:\s*"((?:[^"\\]|\\.)*)"""")
        val match = pattern.findAll(json).lastOrNull()
            ?: return "Could not parse response:\n$json"
        return unescape(match.groupValues[1])
    }

    private fun unescape(s: String) = s
        .replace("\\n", "\n")
        .replace("\\\"", "\"")
        .replace("\\\\", "\\")
}
