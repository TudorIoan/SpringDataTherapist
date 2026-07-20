package app.springdata.therapist.module

import app.springdata.therapist.api.PhotosService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class NetworkModule {
    @Singleton
    @Provides
    fun providePhotosService(): PhotosService {
        return PhotosService.create()
    }
}