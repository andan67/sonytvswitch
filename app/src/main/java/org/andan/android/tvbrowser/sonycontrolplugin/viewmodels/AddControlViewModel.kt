package org.andan.android.tvbrowser.sonycontrolplugin.viewmodels

import androidx.lifecycle.ViewModel
import org.andan.android.tvbrowser.sonycontrolplugin.SonyControlApplication
import org.andan.android.tvbrowser.sonycontrolplugin.repository.SonyControlRepository

data class AddControlUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isUserLoggedIn: Boolean = false
)

class AddControlViewModel : ViewModel()  {
    //TODO Inject repository
    private val sonyControlRepository: SonyControlRepository =
        SonyControlApplication.get().appComponent.sonyRepository()



}