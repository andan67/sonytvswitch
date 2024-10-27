package org.andan.android.tvbrowser.sonycontrolplugin

sealed class ProcessingEvents(val message: String) {
  class Initial : ProcessingEvents("")

  class ControlAdded : ProcessingEvents("Control has been added")

  class PostAddedFetchesPerformed : ProcessingEvents("Fetched all data for added control")
}
