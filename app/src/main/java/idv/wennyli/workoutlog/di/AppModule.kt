package idv.wennyli.workoutlog.di

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import idv.wennyli.workoutlog.data.repository.BodyMeasurementRepository
import idv.wennyli.workoutlog.data.repository.BodyMeasurementRepositoryImpl
import idv.wennyli.workoutlog.data.repository.UserProfileRepository
import idv.wennyli.workoutlog.data.repository.UserProfileRepositoryImpl
import idv.wennyli.workoutlog.data.repository.WorkoutRepository
import idv.wennyli.workoutlog.data.repository.WorkoutRepositoryImpl
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = Firebase.auth

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = Firebase.firestore

    @Provides
    @Singleton
    fun provideWorkoutRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore,
        @Named("appId") appId: String
    ): WorkoutRepository = WorkoutRepositoryImpl(auth, firestore, appId)

    @Provides
    @Singleton
    fun provideUserProfileRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore,
        @Named("appId") appId: String
    ): UserProfileRepository = UserProfileRepositoryImpl(auth, firestore, appId)

    @Provides
    @Singleton
    fun provideBodyMeasurementRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore,
        @Named("appId") appId: String
    ): BodyMeasurementRepository = BodyMeasurementRepositoryImpl(auth, firestore, appId)
}