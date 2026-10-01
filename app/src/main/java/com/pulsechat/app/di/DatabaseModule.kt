package com.pulsechat.app.di

import android.content.Context
import androidx.room.Room
import com.pulsechat.app.data.local.MessageDao
import com.pulsechat.app.data.local.PulseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PulseDatabase {
        return Room.databaseBuilder(
            context,
            PulseDatabase::class.java,
            "pulse_chat.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideMessageDao(db: PulseDatabase): MessageDao = db.messageDao()
}
