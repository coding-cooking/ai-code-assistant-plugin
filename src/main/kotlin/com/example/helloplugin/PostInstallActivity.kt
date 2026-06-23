package com.example.helloplugin

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class PostInstallActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        val settings = AiSettings.getInstance()
        if (!settings.hasCompletedOnboarding) {
            ApplicationManager.getApplication().invokeLater {
                SetupWizardDialog(project).show()
            }
        }
    }
}
