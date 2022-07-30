package org.andan.android.tvbrowser.sonycontrolplugin.di

import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Logger
import org.andan.android.tvbrowser.sonycontrolplugin.BuildConfig
import org.andan.android.tvbrowser.sonycontrolplugin.datastore.TokenStore
import org.andan.android.tvbrowser.sonycontrolplugin.network.AddTokenInterceptor
import org.andan.android.tvbrowser.sonycontrolplugin.network.SonyService
import org.andan.android.tvbrowser.sonycontrolplugin.network.SonyServiceClientContext
import org.andan.android.tvbrowser.sonycontrolplugin.network.TokenAuthenticator
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Singleton


@Module
class NetworkModule {

    @Singleton
    @Provides
    fun provideSonyServiceHolder(): SonyServiceClientContext {
        return SonyServiceClientContext()
    }

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
        serviceClientContext: SonyServiceClientContext,
        tokenStore: TokenStore,
        authenticator: TokenAuthenticator
    ): OkHttpClient {
        val client = OkHttpClient.Builder()
            .retryOnConnectionFailure(false)
            .connectTimeout(2, TimeUnit.SECONDS)
            .addInterceptor(AddTokenInterceptor(serviceClientContext, tokenStore))
        if (BuildConfig.DEBUG) {
            client.addInterceptor(loggingInterceptor)
        }
        return client.authenticator(authenticator).build()
    }

    // @Provides tell Dagger how to create instances of the type that this function
    // returns (i.e. LoginRetrofitService).
    // Function parameters are the dependencies of this type.
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