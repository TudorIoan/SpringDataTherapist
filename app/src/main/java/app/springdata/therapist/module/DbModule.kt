package app.springdata.therapist.module

import android.content.Context
import app.springdata.therapist.db.AppDatabase
import app.springdata.therapist.db.PhotosDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DbModule {

    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    fun providePhotosDao(appDatabase: AppDatabase): PhotosDao {
        return appDatabase.photosDao()
    }
}