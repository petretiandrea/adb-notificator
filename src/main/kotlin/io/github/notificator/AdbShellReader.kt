package io.github.notificator

import com.android.ddmlib.MultiLineReceiver
import java.util.regex.Pattern

class AdbShellReader : MultiLineReceiver() {

    companion object {
        private const val SUCCESS_OUTPUT = "Success" //$NON-NLS-1$
        private val FAILURE_PATTERN = Pattern.compile("Failure\\s+\\[(.*)\\]") //$NON-NLS-1$
    }

    private val _adbOutputLines: MutableList<String> = ArrayList()
    private var errorMessage: String? = null

    val adbOutputLines: List<String>
        get() = _adbOutputLines

    override fun processNewLines(lines: Array<String>) {
        _adbOutputLines.addAll(listOf(*lines))
        for (line in lines) {
            if (line.isNotEmpty()) {
                errorMessage = if (line.startsWith(SUCCESS_OUTPUT)) {
                    null
                } else {
                    val m = FAILURE_PATTERN.matcher(line)
                    if (m.matches()) {
                        m.group(1)
                    } else {
                        "Unknown failure"
                    }
                }
            }
        }
    }

    override fun isCancelled() = false
}
