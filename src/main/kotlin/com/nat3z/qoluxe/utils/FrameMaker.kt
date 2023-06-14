package com.nat3z.qoluxe.utils


import java.awt.Dimension
import java.awt.Font
import java.awt.event.ActionListener
import java.util.function.Consumer
import javax.swing.*

class FrameMaker
/**
 * Creates the basic window
 */
    (internal var windowtitle: String, internal var windowDimensions: Dimension, internal var closeOperation: Int, internal var resizeable: Boolean) {
    internal var jlabels: MutableList<JLabel> = ArrayList()
    internal var jButtons: MutableList<JButton> = ArrayList()
    internal var jProgressBars: MutableList<JProgressBar> = ArrayList()
    internal var jTextFields: MutableList<JTextField> = ArrayList()
    internal var jRects: MutableList<JPanel> = ArrayList()

    var jFrame: JFrame
        internal set
    var jPanel: JPanel
        internal set

    init {
        this.jFrame = JFrame(windowtitle)
        this.jPanel = JPanel()
    }

    /**
     * Create text and add to window
     */
    fun addText(text: String, x: Int, y: Int, fontSize: Int, bold: Boolean): JLabel {
        println("     [+] Creating text \"$text\"")

        val label = JLabel(text)
        if (bold)
            label.font = Font("Arial", Font.BOLD, fontSize)
        else
            label.font = Font("Arial", Font.PLAIN, fontSize)

        val size = label.preferredSize
        label.setBounds(x, y, size.width, size.height)

        jlabels.add(label)
        return label
    }

    /**
     * Creates button with an ActionListener
     */
    fun addButton(text: String, x: Int, y: Int, scale: Int, listener: ActionListener): JButton {
        println("     [+] Creating button \"$text\"")
        val button = JButton(text)
        val size = button.preferredSize
        button.setBounds(x, y, scale, size.height)
        button.isFocusPainted = false
        button.addActionListener(listener)
        jButtons.add(button)
        return button
    }

    /**
     * Creates button with an ActionListener (Scalable) & Action can be Nullable
     */
    fun addButton(text: String, x: Int, y: Int, scaleX: Int, scaleY: Int, listener: ActionListener?): JButton {
        println("     [+] Creating button \"$text\"")
        val button = JButton(text)
        val size = button.preferredSize
        button.setBounds(x, y, scaleX, scaleY)
        button.isFocusPainted = false
        if (listener != null)
            button.addActionListener(listener)

        jButtons.add(button)
        return button
    }

    /**
     * Create a progress bar
     */
    fun addProgressBar(lengthOfTask: Int, x: Int, y: Int, scaleX: Int, scaleY: Int): JProgressBar {
        val progressBar = JProgressBar(0, lengthOfTask)
        progressBar.value = 0
        progressBar.setBounds(x, y, scaleX, scaleY)
        progressBar.isStringPainted = true
        jProgressBars.add(progressBar)
        return progressBar
    }

    /**
     * Create an image
     */
    fun addImage(image: ImageIcon, x: Int, y: Int, width: Int, height: Int): JLabel {
        val imageLabel = JLabel(image)
        imageLabel.setBounds(x, y, width, height)
        jlabels.add(imageLabel)
        return imageLabel
    }

    /**
     * Create a textbox
     */
    fun addTextField(x: Int, y: Int, scale: Int): JTextField {
        val field = JTextField()
        field.setBounds(x, y, scale, field.preferredSize.height)
        jTextFields.add(field)
        return field
    }

    /**
     * Clears the entire panel pack
     */
    fun clear() {
        jlabels.clear()
        jButtons.clear()
        jProgressBars.clear()
        jTextFields.clear()
        jRects.clear()
    }

    /**
     * Overrides the current jFrame with new elements
     */
    fun override(): JFrame {
        changeLook()

        jPanel = JPanel()
        jFrame.contentPane.removeAll()
        jFrame.repaint()

        jPanel.layout = null
        jlabels.forEach(Consumer<JLabel> { jPanel.add(it) })
        jButtons.forEach(Consumer<JButton> { jPanel.add(it) })
        jProgressBars.forEach(Consumer<JProgressBar> { jPanel.add(it) })
        jTextFields.forEach(Consumer<JTextField> { jPanel.add(it) })
        jRects.forEach(Consumer<JPanel> { jPanel.add(it) })

        jPanel.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
        jFrame.defaultCloseOperation = closeOperation
        jFrame.add(jPanel)
        jFrame.preferredSize = windowDimensions
        jFrame.pack()
        jFrame.isResizable = resizeable
        jFrame.isVisible = true
        return jFrame
    }

    /**
     * Packs everything together then displays the window
     */
    fun pack(): JFrame {
        changeLook()

        jFrame.contentPane

        jPanel.layout = null
        jlabels.forEach(Consumer<JLabel> { jPanel.add(it) })
        jButtons.forEach(Consumer<JButton> { jPanel.add(it) })
        jProgressBars.forEach(Consumer<JProgressBar> { jPanel.add(it) })
        jTextFields.forEach(Consumer<JTextField> { jPanel.add(it) })
        jRects.forEach(Consumer<JPanel> { jPanel.add(it) })

        jPanel.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
        jFrame.defaultCloseOperation = closeOperation
        jFrame.add(jPanel)
        jFrame.preferredSize = windowDimensions
        jFrame.isVisible = true
        jFrame.pack()
        jFrame.setLocationRelativeTo(null)
        jFrame.isResizable = resizeable

        return jFrame
    }

    /**
     * Changes from default (ugly) look to the windows/linux/mac look
     */
    private fun changeLook() {
        try {
            // Set System L&F
            UIManager.setLookAndFeel(
                UIManager.getSystemLookAndFeelClassName())
        } catch (e: UnsupportedLookAndFeelException) {
            e.printStackTrace()
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        } catch (e: InstantiationException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }

    }
}
