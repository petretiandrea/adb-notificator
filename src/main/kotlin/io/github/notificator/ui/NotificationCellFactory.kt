package io.github.notificator.ui

import com.intellij.ui.components.fields.ExpandableTextField
import java.awt.Component
import javax.swing.AbstractCellEditor
import javax.swing.JTable
import javax.swing.JTextField
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.TableCellEditor
import javax.swing.table.TableCellRenderer


object NotificationCellFactory {

    fun editor() : TableCellEditor = object : AbstractCellEditor(), TableCellEditor {
        private var editor: JTextField? = null

        override fun getCellEditorValue(): Any {
            return editor?.text ?: ""
        }

        override fun getTableCellEditorComponent(
            table: JTable?,
            value: Any?,
            isSelected: Boolean,
            row: Int,
            column: Int
        ): Component {
            editor = createTextField(row, column, value)
            return editor!!
        }
    }

    fun renderer() : TableCellRenderer = object : DefaultTableCellRenderer() {
        override fun getTableCellRendererComponent(
            table: JTable?,
            value: Any?,
            isSelected: Boolean,
            hasFocus: Boolean,
            row: Int,
            column: Int
        ): Component {
            return createTextField(row, column, value)
        }
    }

    private fun createTextField(row: Int, col: Int, value: Any?): JTextField =
        (if(col == 1) ExpandableTextField() else JTextField()).apply {
            isOpaque = false
            text = value.toString()
        }
}