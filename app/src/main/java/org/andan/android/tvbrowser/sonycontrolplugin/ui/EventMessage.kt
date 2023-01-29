package org.andan.android.tvbrowser.sonycontrolplugin.ui

import kotlin.String

sealed class EventMessage

class StringEventMessage(val message: String ) : EventMessage() {
}

class IntEventMessage(val message: Int ) : EventMessage() {
}

