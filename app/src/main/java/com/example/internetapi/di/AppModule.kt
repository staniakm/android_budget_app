package com.example.internetapi.di

import com.example.internetapi.BuildConfig
import com.example.internetapi.api.*
import com.example.internetapi.constant.Constant
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun provideBaseUrl() = Constant.BASE_URL

    @Singleton
    @Provides
    fun provideOkHttpClient() = if (BuildConfig.DEBUG) {
        val loginInterceptor = HttpLoggingInterceptor()
        loginInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        OkHttpClient.Builder()
            .addInterceptor(loginInterceptor)
            .build()
    } else {
        OkHttpClient.Builder().build()
    }


    @Singleton
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient, BASE_URL: String): Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .build()

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): MediaApiService =
        retrofit.create(MediaApiService::class.java)

    @Provides
    @Singleton
    fun provideAccountApiService(retrofit: Retrofit): AccountApiService =
        retrofit.create(AccountApiService::class.java)

    @Provides
    @Singleton
    fun provideBudgetApiService(retrofit: Retrofit): BudgetApiService =
        retrofit.create(BudgetApiService::class.java)

    @Provides
    @Singleton
    fun provideInvoiceApiService(retrofit: Retrofit): InvoiceApiService =
        retrofit.create(InvoiceApiService::class.java)

    @Provides
    @Singleton
    fun provideShopApiService(retrofit: Retrofit): ShopApiService =
        retrofit.create(ShopApiService::class.java)

    @Provides
    @Singleton
    fun provideCategoryApiService(retrofit: Retrofit): CategoryApiService = retrofit.create(CategoryApiService::class.java)

    @Provides
    @Singleton
    fun provideApiHelper(apiHelper: MediaApiHelperImpl): MediaApiHelper = apiHelper

    @Provides
    @Singleton
    fun provideAccountApiHelper(apiHelper: AccountApiHelperImpl): AccountApiHelper = apiHelper

    @Provides
    @Singleton
    fun provideBudgetApiHelper(apiHelper: BudgetApiHelperImpl): BudgetApiHelper = apiHelper

    @Provides
    @Singleton
    fun provideInvoiceApiHelper(apiHelper: InvoiceApiHelperImpl): InvoiceApiHelper = apiHelper

    @Provides
    @Singleton
    fun provideShopApiHelper(apiHelper: ShopApiHelperImpl): ShopApiHelper = apiHelper

    @Provides
    @Singleton
    fun provideCategoryApiHelper(apiHelper: CategoryApiHelperImpl): CategoryApiHelper = apiHelper

}