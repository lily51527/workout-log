package idv.wennyli.workoutlog.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import idv.wennyli.workoutlog.BuildConfig
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ConfigModule {

    @Provides
    @Singleton
    @Named("appId")
    fun provideAppId(): String = BuildConfig.APP_ID
}