package com.example.helloplugin

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.ComboBox
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.*

class AiSettingsConfigurable : Configurable {

    private lateinit var providerCombo: ComboBox<String>
    private lateinit var apiKeyField: JPasswordField
    private lateinit var modelCombo: ComboBox<String>
    private lateinit var customUrlField: JTextField
    private lateinit var customUrlLabel: JLabel

    override fun getDisplayName() = "AI Code Assistant"

    override fun createComponent(): JComponent {
        val panel = JPanel(GridBagLayout())
        val gbc = GridBagConstraints().apply {
            fill = GridBagConstraints.HORIZONTAL
            insets = Insets(6, 8, 6, 8)
        }

        fun row(label: String, comp: JComponent, y: Int) {
            gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0.0
            panel.add(JLabel(label), gbc)
            gbc.gridx = 1; gbc.weightx = 1.0
            panel.add(comp, gbc)
        }

        providerCombo = ComboBox(AiProvider.entries.map { it.displayName }.toTypedArray())
        apiKeyField = JPasswordField(30)
        modelCombo = ComboBox()
        customUrlField = JTextField()
        customUrlLabel = JLabel("API URL:")

        row("Provider:", providerCombo, 0)
        row("API Key:", apiKeyField, 1)
        row("Model:", modelCombo, 2)

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0
        panel.add(customUrlLabel, gbc)
        gbc.gridx = 1; gbc.weightx = 1.0
        panel.add(customUrlField, gbc)

        // 提示文字
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; gbc.weightx = 1.0
        val hint = JLabel("<html><font color='gray'>API Key 保存在本地，不会上传。</font></html>")
        panel.add(hint, gbc)

        // 选 Provider 时联动更新 Model 下拉和 URL 可见性
        providerCombo.addActionListener {
            val provider = AiProvider.entries[providerCombo.selectedIndex]
            updateModelCombo(provider)
            val isCustom = provider == AiProvider.CUSTOM
            customUrlLabel.isVisible = isCustom
            customUrlField.isVisible = isCustom
        }

        reset()
        return panel
    }

    private fun updateModelCombo(provider: AiProvider) {
        modelCombo.removeAllItems()
        if (provider.models.isEmpty()) {
            modelCombo.addItem("")
            modelCombo.isEditable = true
        } else {
            provider.models.forEach { modelCombo.addItem(it) }
            modelCombo.isEditable = false
        }
    }

    override fun isModified(): Boolean {
        val s = AiSettings.getInstance()
        return String(apiKeyField.password) != s.apiKey ||
                AiProvider.entries[providerCombo.selectedIndex].name != s.providerName ||
                (modelCombo.selectedItem as? String) != s.model ||
                customUrlField.text != s.customApiUrl
    }

    override fun apply() {
        val s = AiSettings.getInstance()
        s.apiKey = String(apiKeyField.password)
        s.providerName = AiProvider.entries[providerCombo.selectedIndex].name
        s.model = (modelCombo.selectedItem as? String) ?: ""
        s.customApiUrl = customUrlField.text
    }

    override fun reset() {
        val s = AiSettings.getInstance()
        val provider = s.provider
        providerCombo.selectedIndex = AiProvider.entries.indexOf(provider)
        apiKeyField.text = s.apiKey
        updateModelCombo(provider)
        if (provider.models.contains(s.model)) modelCombo.selectedItem = s.model
        else modelCombo.selectedItem = s.model
        customUrlField.text = s.customApiUrl
        val isCustom = provider == AiProvider.CUSTOM
        customUrlLabel.isVisible = isCustom
        customUrlField.isVisible = isCustom
    }
}
