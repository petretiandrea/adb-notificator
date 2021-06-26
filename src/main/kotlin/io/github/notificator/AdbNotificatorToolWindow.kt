package io.github.notificator

import com.intellij.icons.AllIcons
import com.intellij.json.JsonFileType
import com.intellij.json.JsonLanguage
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.EditorSettings
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.LanguageTextField
import com.intellij.ui.LanguageTextField.SimpleDocumentCreator
import com.intellij.ui.layout.CCFlags
import com.intellij.ui.layout.GrowPolicy
import com.intellij.ui.layout.listCellRenderer
import com.intellij.ui.layout.panel
import io.github.notificator.model.Adb
import io.github.notificator.ui.UIExtension.observe
import io.github.notificator.ui.VerticalView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing
import org.jetbrains.annotations.NotNull
import java.awt.Dimension
import java.awt.KeyboardFocusManager
import javax.swing.DefaultComboBoxModel
import javax.swing.DefaultListModel
import javax.swing.JComponent
import javax.swing.JPanel

typealias NotificationField = Pair<String, String>

class AdbNotificatorToolWindow(private val viewModel: AdbNotificatorViewModel) {


    private val comboBoxDevices = DefaultComboBoxModel(arrayOf<Adb.Device>())
    private var currentDeviceSelection: Adb.Device? = null

    private val comboBoxProcesses = DefaultComboBoxModel(arrayOf<DebugProcess>())
    private var currentDebugProcess: DebugProcess? = null

    private val keyValuePayload = DefaultListModel<NotificationField>()

    init {
        GlobalScope.launch(Dispatchers.IO) {
            viewModel.availableDevices.flowOn(Dispatchers.Swing).collect {
                comboBoxDevices.removeAllElements()
                comboBoxDevices.addAll(it.toList())
            }
        }

        GlobalScope.launch(Dispatchers.IO) {
            viewModel.attachedDevice.flowOn(Dispatchers.Swing).collect {
                when(it) {
                    is NoAttachedDevice -> comboBoxProcesses.removeAllElements()
                    is AttachedDevice -> {
                        comboBoxProcesses.removeAllElements()
                        comboBoxProcesses.addAll(it.process)
                    }
                }
            }
        }
    }

    private val jsonPayloadEditor = createJsonEditor(viewModel.project)


    fun content() : JPanel {
        return panel {
            row {
                comboBox(comboBoxDevices, { currentDeviceSelection }, { value -> currentDeviceSelection = value }, listCellRenderer { value, _, _ -> text = value.name }).observe { selected ->
                    viewModel.onSelectDevice(selected?.serial.orEmpty())
                }.growPolicy(GrowPolicy.MEDIUM_TEXT)
                comboBox(comboBoxProcesses, { currentDebugProcess }, { v -> currentDebugProcess = v }, listCellRenderer { value, _, _ -> text = value.clientKey() })
                    .growPolicy(GrowPolicy.MEDIUM_TEXT)
            }
            titledRow("Notification Payload") {
                /*row {
                    component(jsonPayloadEditor!!.component).constraints(CCFlags.growY)
                }*/
                row {
                    component(VerticalView(keyValuePayload) { value, index ->
                        buildNotificationField(value) {
                            keyValuePayload.set(index, value)
                        }
                    }).constraints(CCFlags.growY)
                }
                row {
                    button("Add Field") {
                        keyValuePayload.addElement(Pair("", ""))
                    }.apply { component.icon = AllIcons.General.Add }
                }
            }
            row {
                button("Send notification") {
                    if (jsonPayloadEditor?.document?.text.isNullOrEmpty()) {
                        // TODO: show error
                    }
                }
            }
        }
    }

    private fun buildNotificationField(field: NotificationField, onUpdate: (NotificationField) -> Unit): DialogPanel {
        return panel {
            row {
                textField({ field.first }, { v -> onUpdate(NotificationField(v, field.second)) })
                expandableTextField({ field.second }, { v -> onUpdate(NotificationField(field.first, v)) })
                button("Cancella") {

                }.apply { component.icon = AllIcons.General.Remove }
            }
        }
    }

    private fun createJsonEditor(project: Project): Editor? {
        val document: Document = LanguageTextField.createDocument(
            "", JsonLanguage.INSTANCE, project, SimpleDocumentCreator()
        )
        return EditorFactory.getInstance()
            .createEditor(document, project, JsonFileType.INSTANCE, false)
            .apply {
                settings.apply<@NotNull EditorSettings> {
                    isLineNumbersShown = true
                    isLineMarkerAreaShown = true
                    isFoldingOutlineShown = true
                    isRightMarginShown = false
                    isAdditionalPageAtBottom = false
                }
                component.apply<@NotNull JComponent> {
                    minimumSize = Dimension(1000, 550)
                    toolTipText = "Editor"
                    setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null)
                    setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null)
                }
            }
    }
}
