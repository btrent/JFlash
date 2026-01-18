package com.jflash.di

import android.content.Context
import com.jflash.data.database.JFlashDatabase
import com.jflash.data.database.JapaneseDbHelper
import com.jflash.data.database.JapaneseEntryProvider
import com.jflash.data.database.dao.CardDao
import com.jflash.data.database.dao.DailyStatsDao
import com.jflash.data.database.dao.ListDao
import com.jflash.data.repository.SentenceRepository
import com.jflash.data.repository.SentenceRepositoryImpl
import com.jflash.domain.usecase.FSRSAlgorithm
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): JFlashDatabase {
        return JFlashDatabase.getDatabase(context)
    }
    
    @Provides
    fun provideListDao(database: JFlashDatabase): ListDao {
        return database.listDao()
    }
    
    @Provides
    fun provideCardDao(database: JFlashDatabase): CardDao {
        return database.cardDao()
    }
    
    @Provides
    fun provideDailyStatsDao(database: JFlashDatabase): DailyStatsDao {
        return database.dailyStatsDao()
    }
    
    @Provides
    @Singleton
    fun provideFSRSAlgorithm(): FSRSAlgorithm {
        return FSRSAlgorithm()
    }
    
    @Provides
    @Singleton
    fun provideJapaneseDbHelper(@ApplicationContext context: Context): JapaneseDbHelper {
        return JapaneseDbHelper(context)
    }

    @Provides
    fun provideJapaneseEntryProviderFactory(@ApplicationContext context: Context): () -> JapaneseEntryProvider {
        return { JapaneseDbHelper(context) }
    }
    
    @Provides
    @Singleton
    fun provideSentenceRepository(japaneseDbHelper: JapaneseDbHelper): SentenceRepository {
        return SentenceRepositoryImpl(japaneseDbHelper)
    }
    
    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }
}