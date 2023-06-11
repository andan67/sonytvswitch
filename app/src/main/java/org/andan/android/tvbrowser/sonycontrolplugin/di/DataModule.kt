package org.andan.android.tvbrowser.sonycontrolplugin.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.andan.android.tvbrowser.sonycontrolplugin.data.Constants.CONTROL_DATABASE
import org.andan.android.tvbrowser.sonycontrolplugin.data.ControlDatabase
import org.andan.android.tvbrowser.sonycontrolplugin.data.ControlEntity
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DataModule {

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context) : DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            corruptionHandler = ReplaceFileCorruptionHandler(
                produceNewData = { emptyPreferences() }
            ),
            produceFile = { context.preferencesDataStoreFile("settings") }
        )
    }

    @Provides
    @Singleton
    fun provide(@ApplicationContext context: Context) = Room.databaseBuilder(
        context, ControlDatabase::class.java, CONTROL_DATABASE)
        .allowMainThreadQueries()
        .fallbackToDestructiveMigration()
        .build()
    //This annotation marks the method provideDao as a provider of noteDoa.

    @Provides
    @Singleton
    fun provideDao(db: ControlDatabase) = db.controlDao()

}