package org.andan.android.tvbrowser.sonycontrolplugin.di

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.Nullable
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton


@Module
class AppModule(private val context: Context) {

    @Provides
    @Singleton
    fun provideAppContext() = context

    @Provides
    @Singleton
    @Nullable
    @Named("TokenStore")
    fun provideSharedPreferences(): SharedPreferences? {
        val preferences =  context.getSharedPreferences("TokenStore", Context.MODE_PRIVATE)
        preferences.edit().putString("TokenStore","auth=eab24792414cc9ad67fcf13e5693456c066b8a68a9728b6d037516fddbd7a656").apply()
        return preferences
    }

}