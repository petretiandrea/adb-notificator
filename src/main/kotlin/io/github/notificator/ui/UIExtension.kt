package io.github.notificator.ui

import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.layout.Cell
import com.intellij.ui.layout.CellBuilder
import com.intellij.ui.layout.PropertyBinding
import com.intellij.ui.layout.listCellRenderer
import javax.swing.ComboBoxModel
import kotlin.reflect.KMutableProperty0

object UIExtension {


    fun <T> CellBuilder<ComboBox<T>>.observe(changeSelection: (T?) -> Unit) : CellBuilder<ComboBox<T>> = this.apply {
        component.addActionListener { changeSelection(component.model.selectedItem as? T) }
    }
}