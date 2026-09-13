package com.immrtldragon.detoxspace.di

import com.immrtldragon.detoxspace.BuildConfig
import com.immrtldragon.detoxspace.data.SessionStore
import com.immrtldragon.detoxspace.data.remote.DetoxApi
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
    fun http(sessionStore: SessionStore): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val token = sessionStore.accessToken()
            val request = chain.request().newBuilder().apply {
                if (token != null) header("Authorization", "Bearer $token")
            }.build()
            chain.proceed(request)
        }.build()

    @Provides @Singleton
    fun retrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides @Singleton fun api(retrofit: Retrofit): DetoxApi = retrofit.create(DetoxApi::class.java)
}
