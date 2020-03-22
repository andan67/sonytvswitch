package org.andan.android.tvbrowser.sonycontrolplugin.di

import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.andan.android.tvbrowser.sonycontrolplugin.BuildConfig
import org.andan.android.tvbrowser.sonycontrolplugin.datastore.ControlPreferenceStore
import org.andan.android.tvbrowser.sonycontrolplugin.datastore.TokenStore
import org.andan.android.tvbrowser.sonycontrolplugin.network.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class NetworkModule {

    @Singleton
    @Provides
    fun provideSonyServiceHolder(): SonyServiceHolder {
        return SonyServiceHolder()
    }


    /*@Singleton
    @Provides
    fun provideTokenStore(): TokenStore{
        return TokenStore("auth=16f2695f210e5c7ce96f9b023d15812caf3920fe7e89be2e726b8339a564c83f")
    }*/

    /*
    @Singleton
    @Provides
    fun provideAddTokenInterceptor(tokenRepository: TokenRepository): AddTokenInterceptor {
        return AddTokenInterceptor(tokenRepository)
    }*/

    @Singleton
    @Provides
    fun provideHttpLoggingInterceptor(tokenStore: ControlPreferenceStore): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
    }

   /* @Singleton
    @Provides
    fun provideTokenAuthenticator(serviceHolder: SonyServiceHolder, tokenRepository: TokenRepository): Authenticator {
        return TokenAuthenticator(serviceHolder, tokenRepository)
    }*/

    @Singleton
    @Provides
    fun provideOkHttpClient(tokenInterceptor: AddTokenInterceptor, loggingInterceptor: HttpLoggingInterceptor, serviceHolder: SonyServiceHolder, tokenStore: TokenStore,
                            authenticator: TokenAuthenticator
    ): OkHttpClient {
        val client = OkHttpClient.Builder()
            .connectTimeout(2,TimeUnit.SECONDS)
            .addInterceptor(AddTokenInterceptor(serviceHolder, tokenStore))
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
    fun provideSonyRetrofitService(okHttpClient :OkHttpClient): SonyService {
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