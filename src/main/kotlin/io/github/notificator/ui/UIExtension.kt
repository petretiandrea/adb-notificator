package io.github.notificator.ui

import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.layout.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing
import java.awt.List
import java.awt.event.ActionEvent
import javax.swing.ComboBoxModel
import javax.swing.JButton
import javax.swing.ListCellRenderer
import kotlin.reflect.KMutableProperty0

object UIExtension {
    fun <T> CellBuilder<ComboBox<T>>.observe(changeSelection: (T?) -> Unit) : CellBuilder<ComboBox<T>> = this.apply {
        component.addActionListener { changeSelection(component.model.selectedItem as? T) }
    }

    fun <T> Cell.comboBox(model: ComboBoxModel<T>, property: KMutableProperty0<T?>, renderer: ListCellRenderer<T?>): CellBuilder<ComboBox<T>> {
        return comboBox(
            model,
            { property.get() },
            { v -> property.set(v) },
            renderer
        )
    }

    fun Cell.button(text: String) : CellBuilder<JButton> {
        return button(text) { }
    }

    fun CellBuilder<JButton>.onClick(action: suspend (ActionEvent) -> Unit) {
        applyToComponent {
            addActionListener { event ->
                GlobalScope.launch(Dispatchers.Swing) {
                    action(event)
                }
            }
        }
    }
}