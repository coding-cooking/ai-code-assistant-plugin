package com.example.helloplugin

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBScrollPane
import java.awt.*
import java.awt.datatransfer.StringSelection
import javax.swing.*
import javax.swing.border.EmptyBorder

class AiResultDialog(
    project: Project?,
    private val providerShortName: String,
    private val result: String
) : DialogWrapper(project) {

    init {
        title = "AI Explanation · $providerShortName"
        setOKButtonText("Close")
        init()
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout(0, 12))
        panel.preferredSize = Dimension(580, 400)
        panel.border = EmptyBorder(16, 16, 8, 16)

        // Header badge
        val header = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))
        val badge = JLabel("  $providerShortName  ")
        badge.font = Font(badge.font.name, Font.BOLD, 11)
        badge.foreground = Color(0x4B9EF8)
        badge.border = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color(0x4B9EF8), 1, true),
            BorderFactory.createEmptyBorder(2, 6, 2, 6)
        )
        header.add(badge)
        panel.add(header, BorderLayout.NORTH)

        // Result text
        val textArea = JTextArea(result)
        textArea.isEditable = false
        textArea.wrapStyleWord = true
        textArea.lineWrap = true
        textArea.font = Font(Font.SANS_SERIF, Font.PLAIN, 13)
        textArea.background = UIManager.getColor("Panel.background")
        textArea.border = EmptyBorder(10, 10, 10, 10)

        val scroll = JBScrollPane(textArea)
        scroll.border = BorderFactory.createLineBorder(Color(0x3C3F41), 1, true)
        panel.add(scroll, BorderLayout.CENTER)

        // Footer: copy + switch provider
        val footer = JPanel(BorderLayout())

        val switchLabel = JLabel("Switch AI Provider…")
        switchLabel.foreground = Color(0x6B9AC4)
        switchLabel.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        switchLabel.font = Font(switchLabel.font.name, Font.PLAIN, 11)
        switchLabel.addMouseListener(object : java.awt.event.MouseAdapter() {
            override fun mouseClicked(e: java.awt.event.MouseEvent) {
                close(OK_EXIT_CODE)
                SetupWizardDialog(null).show()
            }
        })

        val copyBtn = JButton("Copy")
        copyBtn.addActionListener {
            Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(result), null)
            copyBtn.text = "Copied ✓"
            Timer(1500) { copyBtn.text = "Copy" }.also { it.isRepeats = false; it.start() }
        }

        footer.add(switchLabel, BorderLayout.WEST)
        footer.add(copyBtn, BorderLayout.EAST)
        panel.add(footer, BorderLayout.SOUTH)

        return panel
    }

    override fun createActions() = arrayOf(okAction)
}
