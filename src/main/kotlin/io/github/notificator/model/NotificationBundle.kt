package io.github.notificator.model

class NotificationBundle {

    private var content: Map<String, Any> = mapOf()

    fun putBoolean(key: String, value: Boolean) {
        content += Pair(key, value)
    }


}