package com.example.helloplugin

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class SwitchProviderAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        SetupWizardDialog(e.project).show()
    }

    override fun update(e: AnActionEvent) {
        val settings = AiSettings.getInstance()
        val current = if (settings.isConfigured()) settings.provider.displayName else "not configured"
        e.presentation.text = "Switch AI Provider… ($current)"
    }
}
