package com.samriddha.runningapphilt.di

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.room.Room
import com.samriddha.runningapphilt.data.db.RunDatabase
import com.samriddha.runningapphilt.other.Constants.KEY_FIRST_TIME_TOGGLE
import com.samriddha.runningapphilt.other.Constants.KEY_USERNAME
import com.samriddha.runningapphilt.other.Constants.KEY_USER_WEIGHT
import com.samriddha.runningapphilt.other.Constants.RUN_DATABASE_NAME
import com.samriddha.runningapphilt.other.Constants.SHARED_PREF_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideRunDatabase(@ApplicationContext context: Context) =
        Room.databaseBuilder(
            context,
            RunDatabase::class.java,
            RUN_DATABASE_NAME
        ).build()

    @Singleton
    @Provides
    fun provideRunDao(runDatabase: RunDatabase) = runDatabase.getRunDao()

    @Provides
    @Singleton
    fun provideSharedPreferences(
        @ApplicationContext context: Context
    ) =
        context.getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE)

    @Singleton
    @Provides
    fun providesUserName(sharedPreferences: SharedPreferences) =
        sharedPreferences.getString(
            KEY_USERNAME,
            ""
        ) ?: ""

    @Singleton
    @Provides
    fun providesUserWeight(sharedPreferences: SharedPreferences) =
        sharedPreferences.getFloat(
            KEY_USER_WEIGHT,
            80f
        )

    @Singleton
    @Provides
    fun providesFirstTimeToggle(sharedPreferences: SharedPreferences) =
        sharedPreferences.getBoolean(
            KEY_FIRST_TIME_TOGGLE,
            true
        )


}