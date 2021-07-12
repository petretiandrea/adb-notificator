package io.github.notificator.ui

import io.github.notificator.model.NotificationField
import javax.swing.table.AbstractTableModel

class NotificationFieldTableModel(private var fields: List<NotificationField>) : AbstractTableModel() {

    companion object {
        const val COLUMNS = 2
    }

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
        return true
    }

    override fun getRowCount(): Int {
        return fields.size;
    }

    override fun getColumnCount(): Int {
        return COLUMNS;
    }

    override fun setValueAt(aValue: Any?, rowIndex: Int, columnIndex: Int) {
        val old = fields[rowIndex]
        fields = when(columnIndex) {
            0 -> fields.map { if(it == old) NotificationField(aValue.toString(), old.second) else it }
            1 -> fields.map { if(it == old) NotificationField(old.first, aValue.toString()) else it }
            else -> fields
        }
        fireTableCellUpdated(rowIndex, columnIndex)
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
        return when(columnIndex) {
            0 -> fields[rowIndex].first
            1 -> fields[rowIndex].second
            else -> ""
        }
    }

}