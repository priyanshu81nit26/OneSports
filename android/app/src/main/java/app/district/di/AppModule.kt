package app.district.di

import android.content.Context
import app.district.data.FirebaseRepository
import app.district.data.DistrictRepository
import app.district.data.PrefsManager
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
    fun providePrefsManager(@ApplicationContext context: Context): PrefsManager {
        return PrefsManager(context)
    }

    // ── Backend selection ─────────────────────────────────────────────
    // DEMO build (Firebase): bind FirebaseRepository (default, below).
    // FULL build (REST API): comment the Firebase binding and uncomment the
    // ApiRepository one. ApiRepository is constructor-injected with PrefsManager.
    //
    //   @Provides @Singleton
    //   fun provideRepository(impl: app.district.data.ApiRepository): DistrictRepository = impl
    //
    @Provides
    @Singleton
    fun provideRepository(impl: FirebaseRepository): DistrictRepository = impl
}
