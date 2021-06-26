package io.github.notificator.ui

import java.awt.Component
import javax.swing.BoxLayout
import javax.swing.JPanel
import javax.swing.ListModel
import javax.swing.event.ListDataEvent
import javax.swing.event.ListDataListener

class VerticalView<T>(private val listModel: ListModel<T>,
                      private val render: (T, Int) -> Component) : JPanel() {

    private val listener = object : ListDataListener {
        override fun intervalAdded(e: ListDataEvent?) {
            e?.let { handleIntervalAdded(it) }
        }

        override fun intervalRemoved(e: ListDataEvent?) {
            e?.let { handleIntervalRemove(it) }
        }

        override fun contentsChanged(e: ListDataEvent?) {
            println("TODO(\"Not yet implemented\")")
        }
    }

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        listModel.addListDataListener(listener)
        handleIntervalAdded(ListDataEvent(listModel, ListDataEvent.INTERVAL_ADDED, 0, listModel.size - 1))
    }

    private fun handleIntervalAdded(event: ListDataEvent) {
        (event.index0 until event.index1 + 1).forEach {
            if(it > listModel.size - 1) {
                add(render(listModel.getElementAt(it), it))
            } else {
                add(render(listModel.getElementAt(it), it), it)
            }
        }
        updateUI()
    }

    private fun handleIntervalRemove(event: ListDataEvent) {
        (event.index0 until event.index1 + 1).forEach {
            remove(it)
        }
        updateUI()
    }
}