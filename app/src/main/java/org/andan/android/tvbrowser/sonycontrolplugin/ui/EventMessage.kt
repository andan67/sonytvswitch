package org.andan.android.tvbrowser.sonycontrolplugin.ui

sealed class EventMessage

class StringEventMessage(val message: String ) : EventMessage() {
}

class IntEventMessage(val message: Int ) : EventMessage() {
}

