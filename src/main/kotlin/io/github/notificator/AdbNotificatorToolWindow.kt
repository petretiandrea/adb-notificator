package io.github.notificator

import com.intellij.icons.AllIcons
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.layout.*
import com.intellij.ui.table.JBTable
import io.github.notificator.model.DebugProcess
import io.github.notificator.model.Device
import io.github.notificator.model.NotificationField
import io.github.notificator.ui.NotificationCellFactory
import io.github.notificator.ui.NotificationFieldTableModel
import io.github.notificator.ui.UIExtension.button
import io.github.notificator.ui.UIExtension.onClick
import io.github.notificator.ui.UIExtension.comboBox
import io.github.notificator.ui.UIExtension.observe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing
import javax.swing.DefaultComboBoxModel
import javax.swing.JPanel


class AdbNotificatorToolWindow(private val viewModel: AdbNotificatorViewModel) {

    private val comboBoxDevices = DefaultComboBoxModel(arrayOf<Device>())
    var currentDeviceSelection: Device? = null

    private val comboBoxProcesses = DefaultComboBoxModel(arrayOf<DebugProcess>())
    var currentDebugProcess: DebugProcess? = null

    private val keyValuePayload = mutableListOf<NotificationField>()

    init {
       setupReactiveFlow()
    }

    // enable flow from viewmodel
    private fun setupReactiveFlow() {
        GlobalScope.launch(Dispatchers.Swing) {
            viewModel.availableDevice.collect {
                comboBoxDevices.removeAllElements()
                comboBoxDevices.addAll(it.toList())
            }
        }

        GlobalScope.launch(Dispatchers.IO) {
            viewModel.selectedDevice.flowOn(Dispatchers.Swing).collect {
                comboBoxProcesses.removeAllElements()
                it?.let { comboBoxProcesses.addAll(it.processes) }
            }
        }
    }

    fun content() : JPanel {
        return panel {
            row {
                label("Device")
                comboBox(comboBoxDevices, ::currentDeviceSelection, listCellRenderer { value, _, _ -> text = value.name })
                    .observe { selected ->
                        viewModel.onSelectDevice(selected?.serial.orEmpty())
                    }
                    .constraints(CCFlags.growX, CCFlags.pushX)
                label("Process")
                comboBox(comboBoxProcesses, ::currentDebugProcess, listCellRenderer { value, _, _ -> text = value.clientKey() })
                    .constraints(CCFlags.growX, CCFlags.pushX)
            }
            row {
                buildRootSection {
                    viewModel.enableDeviceRoot()
                }
            }
            titledRow("Notification Payload") {
                row {
                    component(notificationPayloadTable()).constraints(CCFlags.grow)
                }
            }
            row {
                button("Send notification").apply {
                    onClick {
                        if (keyValuePayload.isNotEmpty()) {
                            viewModel.sendPushNotification(keyValuePayload)
                        } else {
                            // SHOW ERROR
                        }
                    }
                }
            }
        }
    }

    // build notificationt table
    private fun notificationPayloadTable(): JPanel {
        val model = NotificationFieldTableModel(keyValuePayload)
        val table = JBTable(model).apply {
            fillsViewportHeight = true
            setDefaultEditor(Any::class.java, NotificationCellFactory.editor())
            setDefaultRenderer(Any::class.java, NotificationCellFactory.renderer())
        }
        val decorator = ToolbarDecorator.createDecorator(table)
        decorator.setAddAction {
            keyValuePayload.add(NotificationField("", ""))
            model.fireTableDataChanged();
        }
        decorator.setRemoveAction {
            keyValuePayload.removeAt(table.selectedRow);
            model.fireTableDataChanged();
        }
        return decorator.createPanel()
    }

    private fun LayoutBuilder.buildRootSection(onCheckPress: suspend () -> Unit): Row {
        return row {
            button("Enable ADB root").apply {
                onClick { onCheckPress() }
                component.icon = AllIcons.RunConfigurations.TestUnknown
            }
        }
    }

//    private fun createJsonEditor(project: Project): Editor? {
//        val document: Document = LanguageTextField.createDocument(
//            "", JsonLanguage.INSTANCE, project, SimpleDocumentCreator()
//        )
//        return EditorFactory.getInstance()
//            .createEditor(document, project, JsonFileType.INSTANCE, false)
//            .apply {
//                settings.apply<@NotNull EditorSettings> {
//                    isLineNumbersShown = true
//                    isLineMarkerAreaShown = true
//                    isFoldingOutlineShown = true
//                    isRightMarginShown = false
//                    isAdditionalPageAtBottom = false
//                }
//                component.apply<@NotNull JComponent> {
//                    minimumSize = Dimension(1000, 550)
//                    toolTipText = "Editor"
//                    setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null)
//                    setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null)
//                }
//            }
//    }
}
