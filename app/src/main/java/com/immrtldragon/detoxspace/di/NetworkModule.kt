package com.immrtldragon.detoxspace.di

import com.immrtldragon.detoxspace.BuildConfig
import com.immrtldragon.detoxspace.data.SessionStore
import com.immrtldragon.detoxspace.data.TokenAuthenticator
import com.immrtldragon.detoxspace.data.remote.DetoxApi
import com.immrtldragon.detoxspace.data.remote.RefreshApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides @Singleton
    fun refreshApi(): RefreshApi = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(OkHttpClient.Builder().build())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(RefreshApi::class.java)

    @Provides @Singleton
    fun http(sessionStore: SessionStore, authenticator: TokenAuthenticator): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val token = sessionStore.accessToken()
            val request = chain.request().newBuilder().apply {
                if (token != null) header("Authorization", "Bearer $token")
            }.build()
            chain.proceed(request)
        }
        .authenticator(authenticator)
        .build()

    @Provides @Singleton
    fun retrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides @Singleton fun api(retrofit: Retrofit): DetoxApi = retrofit.create(DetoxApi::class.java)
}
