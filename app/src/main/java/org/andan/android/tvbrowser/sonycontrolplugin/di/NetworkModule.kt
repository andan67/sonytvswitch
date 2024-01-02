package org.andan.android.tvbrowser.sonycontrolplugin.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Logger
import org.andan.android.tvbrowser.sonycontrolplugin.BuildConfig
import org.andan.android.tvbrowser.sonycontrolplugin.data.TokenStore
import org.andan.android.tvbrowser.sonycontrolplugin.network.AddTokenInterceptor
import org.andan.android.tvbrowser.sonycontrolplugin.network.SessionManager
import org.andan.android.tvbrowser.sonycontrolplugin.network.SonyService
import org.andan.android.tvbrowser.sonycontrolplugin.network.SonyServiceClientContext
import org.andan.android.tvbrowser.sonycontrolplugin.network.TokenAuthenticator
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Provider
import javax.inject.Qualifier
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class SessionTokens

    @SessionTokens
    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            corruptionHandler = ReplaceFileCorruptionHandler(
                produceNewData = { emptyPreferences() }
            ),
            produceFile = { context.preferencesDataStoreFile("token_store") }
        )
    }

    @Singleton
    @Provides
    fun provideSonyServiceHolder(): SonyServiceClientContext {
        return SonyServiceClientContext()
    }

    @Singleton
    @Provides
    fun provideSessionManager(@SessionTokens tokenStore: DataStore<Preferences>, sonyServiceProvider: Provider<SonyService>): SessionManager {
        return SessionManager(tokenStore, sonyServiceProvider)
    }

/*    @Singleton
    @Provides
    fun provideSessionManager(@SessionTokens tokenStore: DataStore<Preferences>): SessionManager {
        return SessionManager(tokenStore)
    }*/


    @Singleton
    @Provides
    fun provideInterceptor(): HttpLoggingInterceptor = HttpLoggingInterceptor(object : Logger {
        override fun log(message: String) {
            Timber.tag("OkHttp").d(message)
        }
    }).setLevel(level = HttpLoggingInterceptor.Level.BODY)

    @Singleton
    @Provides
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        sessionManager: SessionManager,
        authenticator: TokenAuthenticator
    ): OkHttpClient {
        val client = OkHttpClient.Builder()
            .retryOnConnectionFailure(false)
            .connectTimeout(2, TimeUnit.SECONDS)
            .addInterceptor(AddTokenInterceptor(sessionManager))
        if (BuildConfig.DEBUG) {
            client.addInterceptor(loggingInterceptor)
        }
        return client.authenticator(authenticator).build()
    }

    @Singleton
    @Provides
    fun provideSonyRetrofitService(okHttpClient: OkHttpClient): SonyService {
        // Whenever Dagger needs to provide an instance of type LoginRetrofitService,
        // this code (the one inside the @Provides method) is run.
        return Retrofit.Builder()
            .baseUrl("http://localhost/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(SonyService::class.java)
    }
}