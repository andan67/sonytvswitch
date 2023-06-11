package org.andan.android.tvbrowser.sonycontrolplugin.repository

import kotlinx.coroutines.flow.Flow
import org.andan.android.tvbrowser.sonycontrolplugin.data.ControlDao
import org.andan.android.tvbrowser.sonycontrolplugin.data.ControlEntity
import org.andan.android.tvbrowser.sonycontrolplugin.data.ControlPreferenceStore
import org.andan.android.tvbrowser.sonycontrolplugin.network.*
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ControlsRepository @Inject constructor(
    val api: SonyService,
    val preferenceStore: ControlPreferenceStore,
    val sonyServiceContext: SonyServiceClientContext,
    val controlDao: ControlDao
) {




    init {
        Timber.d("init")
    }
}