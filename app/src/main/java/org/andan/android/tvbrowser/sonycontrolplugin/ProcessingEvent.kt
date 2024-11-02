package org.andan.android.tvbrowser.sonycontrolplugin

sealed class ProcessingEvent(val message: String) {
  class None : ProcessingEvent("")

  class ControlAdded : ProcessingEvent("Control has been added")

  class PostAddedFetchesPerformed : ProcessingEvent("Fetched all data for added control")
}
