package com.example.helloplugin

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBScrollPane
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.border.EmptyBorder

class SetupWizardDialog(private val project: Project?) : DialogWrapper(project) {

    private val cardLayout = CardLayout()
    private val cardPanel = JPanel(cardLayout)
    private var currentStep = 0

    // Step 2 fields
    private lateinit var providerList: JList<AiProvider>
    private lateinit var modelCombo: JComboBox<String>
    private lateinit var apiKeyField: JPasswordField
    private lateinit var getKeyLink: JLabel

    private val backBtn = JButton("Back")
    private val nextBtn = JButton("Next →")

    init {
        title = "AI Code Assistant Setup"
        isModal = true
        init()
    }

    override fun createCenterPanel(): JComponent {
        val root = JPanel(BorderLayout())
        root.preferredSize = Dimension(580, 420)

        cardPanel.add(buildWelcomeStep(), "step1")
        cardPanel.add(buildConfigStep(), "step2")
        root.add(cardPanel, BorderLayout.CENTER)

        return root
    }

    override fun createSouthPanel(): JComponent {
        val panel = JPanel(BorderLayout())
        panel.border = EmptyBorder(8, 16, 12, 16)

        val skipLabel = JLabel("Skip for now").also { l ->
            l.foreground = Color(0x6B9AC4)
            l.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            l.addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) { doSkip() }
            })
        }

        val btnPanel = JPanel(FlowLayout(FlowLayout.RIGHT, 8, 0))
        backBtn.isVisible = false
        backBtn.addActionListener { goStep(0) }
        nextBtn.addActionListener {
            if (currentStep == 0) goStep(1)
            else doFinish()
        }
        btnPanel.add(backBtn)
        btnPanel.add(nextBtn)

        panel.add(skipLabel, BorderLayout.WEST)
        panel.add(btnPanel, BorderLayout.EAST)
        return panel
    }

    private fun goStep(step: Int) {
        currentStep = step
        cardLayout.show(cardPanel, if (step == 0) "step1" else "step2")
        backBtn.isVisible = step == 1
        nextBtn.text = if (step == 0) "Get Started →" else "Finish"
    }

    override fun createActions() = emptyArray<Action>()

    // ── Step 1: Welcome ───────────────────────────────────────────────────────

    private fun buildWelcomeStep(): JPanel {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        panel.border = EmptyBorder(32, 48, 16, 48)
        panel.background = UIManager.getColor("Panel.background")

        fun centered(c: JComponent): JComponent {
            c.alignmentX = Component.CENTER_ALIGNMENT; return c
        }

        val icon = JLabel("✨")
        icon.font = Font(icon.font.name, Font.PLAIN, 48)
        panel.add(centered(icon))
        panel.add(Box.createVerticalStrut(16))

        val title = JLabel("Welcome to AI Code Assistant")
        title.font = Font(title.font.name, Font.BOLD, 20)
        panel.add(centered(title))
        panel.add(Box.createVerticalStrut(12))

        val features = listOf(
            "🔍  Select any code, right-click, and ask AI to explain it",
            "💬  Supports ChatGPT, Gemini, Claude, DeepSeek, Kimi & more",
            "🔑  Your API key stays local — never shared or uploaded",
            "🔄  Switch AI providers anytime from the right-click menu"
        )
        features.forEach { text ->
            val label = JLabel("<html><div style='margin:4px 0'>$text</div></html>")
            label.font = Font(label.font.name, Font.PLAIN, 13)
            panel.add(centered(label))
        }

        panel.add(Box.createVerticalGlue())
        nextBtn.text = "Get Started →"
        return panel
    }

    // ── Step 2: Provider + Key ─────────────────────────────────────────────────

    private fun buildConfigStep(): JPanel {
        val panel = JPanel(BorderLayout(16, 0))
        panel.border = EmptyBorder(20, 24, 16, 24)

        // Left: provider list
        val providers = AiProvider.entries.filter { it != AiProvider.CUSTOM }.toTypedArray()
        providerList = JList(providers)
        providerList.cellRenderer = ProviderListRenderer()
        providerList.selectionMode = ListSelectionModel.SINGLE_SELECTION
        providerList.selectedIndex = 0
        providerList.fixedCellHeight = 44

        val listScroll = JBScrollPane(providerList)
        listScroll.preferredSize = Dimension(200, 0)
        listScroll.border = BorderFactory.createLineBorder(Color(0x3C3F41))
        panel.add(listScroll, BorderLayout.WEST)

        // Right: model + key
        val right = JPanel()
        right.layout = BoxLayout(right, BoxLayout.Y_AXIS)

        val modelLabel = JLabel("Model")
        modelLabel.font = Font(modelLabel.font.name, Font.BOLD, 12)
        modelLabel.alignmentX = Component.LEFT_ALIGNMENT
        right.add(modelLabel)
        right.add(Box.createVerticalStrut(4))

        modelCombo = JComboBox()
        modelCombo.maximumSize = Dimension(Int.MAX_VALUE, 30)
        modelCombo.alignmentX = Component.LEFT_ALIGNMENT
        right.add(modelCombo)
        right.add(Box.createVerticalStrut(16))

        val keyLabel = JLabel("API Key")
        keyLabel.font = Font(keyLabel.font.name, Font.BOLD, 12)
        keyLabel.alignmentX = Component.LEFT_ALIGNMENT
        right.add(keyLabel)
        right.add(Box.createVerticalStrut(4))

        apiKeyField = JPasswordField()
        apiKeyField.maximumSize = Dimension(Int.MAX_VALUE, 30)
        apiKeyField.alignmentX = Component.LEFT_ALIGNMENT
        right.add(apiKeyField)
        right.add(Box.createVerticalStrut(6))

        getKeyLink = JLabel("Get API key →")
        getKeyLink.foreground = Color(0x6B9AC4)
        getKeyLink.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        getKeyLink.font = Font(getKeyLink.font.name, Font.PLAIN, 11)
        getKeyLink.alignmentX = Component.LEFT_ALIGNMENT
        right.add(getKeyLink)
        right.add(Box.createVerticalStrut(16))

        val hint = JLabel("<html><font color='gray' size='2'>Your key is stored locally and never uploaded.</font></html>")
        hint.alignmentX = Component.LEFT_ALIGNMENT
        right.add(hint)
        right.add(Box.createVerticalGlue())

        panel.add(right, BorderLayout.CENTER)

        fun onProviderSelected(p: AiProvider) {
            modelCombo.removeAllItems()
            p.models.forEach { modelCombo.addItem(it) }
            getKeyLink.text = if (p.keyUrl.isNotEmpty())
                "Get API key at ${p.keyUrl.removePrefix("https://").substringBefore("/")}" else ""
            getKeyLink.toolTipText = p.keyUrl
            apiKeyField.text = ""
        }

        providerList.addListSelectionListener {
            val p = providerList.selectedValue ?: return@addListSelectionListener
            onProviderSelected(p)
        }

        // Trigger initial state explicitly
        onProviderSelected(providers.first())

        return panel
    }

    private fun doFinish() {
        val provider = providerList.selectedValue
        val key = String(apiKeyField.password).trim()
        if (key.isEmpty()) {
            JOptionPane.showMessageDialog(contentPane, "Please enter your API key.", "Missing API Key", JOptionPane.WARNING_MESSAGE)
            return
        }
        val settings = AiSettings.getInstance()
        settings.providerName = provider.name
        settings.model = (modelCombo.selectedItem as? String) ?: provider.defaultModel
        settings.apiKey = key
        settings.hasCompletedOnboarding = true
        close(OK_EXIT_CODE)
    }

    private fun doSkip() {
        AiSettings.getInstance().hasCompletedOnboarding = true
        close(CANCEL_EXIT_CODE)
    }

    // ── Provider list cell renderer ────────────────────────────────────────────

    inner class ProviderListRenderer : ListCellRenderer<AiProvider> {
        override fun getListCellRendererComponent(
            list: JList<out AiProvider>, value: AiProvider, index: Int,
            isSelected: Boolean, cellHasFocus: Boolean
        ): Component {
            val panel = JPanel(BorderLayout())
            panel.border = EmptyBorder(8, 12, 8, 12)
            panel.background = if (isSelected) Color(0x2D5A8E) else list.background

            val name = JLabel(value.displayName)
            name.font = Font(name.font.name, Font.BOLD, 12)
            name.foreground = if (isSelected) Color.WHITE else list.foreground

            val sub = JLabel(value.shortName)
            sub.font = Font(sub.font.name, Font.PLAIN, 11)
            sub.foreground = if (isSelected) Color(0xCCDDEE) else Color.GRAY

            val text = JPanel(GridLayout(2, 1))
            text.isOpaque = false
            text.add(name)
            text.add(sub)
            panel.add(text, BorderLayout.CENTER)
            return panel
        }
    }
}
