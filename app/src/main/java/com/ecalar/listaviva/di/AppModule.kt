package com.ecalar.listaviva.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase
import com.ecalar.listaviva.data.repository.AuthRepositoryImpl
import com.ecalar.listaviva.data.repository.CatalogoRepositoryImpl
import com.ecalar.listaviva.data.repository.DespensaRepositoryImpl
import com.ecalar.listaviva.data.repository.FamiliaRepositoryImpl
import com.ecalar.listaviva.data.repository.ListaCompraRepositoryImpl
import com.ecalar.listaviva.data.repository.UserPreferencesRepositoryImpl
import com.ecalar.listaviva.domain.repository.AuthRepository
import com.ecalar.listaviva.domain.repository.CatalogoRepository
import com.ecalar.listaviva.domain.repository.DespensaRepository
import com.ecalar.listaviva.domain.repository.FamiliaRepository
import com.ecalar.listaviva.domain.repository.ListaCompraRepository
import com.ecalar.listaviva.domain.repository.UserPreferencesRepository
import com.google.firebase.firestore.ktx.persistentCacheSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // --- FIREBASE INSTANCES ---

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = Firebase.auth

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        val firestore = Firebase.firestore
        val settings = firestoreSettings {
            setLocalCacheSettings(persistentCacheSettings {})
        }
        firestore.firestoreSettings = settings
        return firestore
    }

    // --- PREFERENCES ---

    @Provides
    @Singleton
    fun provideUserPreferencesRepository(@ApplicationContext context: Context): UserPreferencesRepository {
        return UserPreferencesRepositoryImpl(context)
    }

    // --- REPOSITORIES ---

    @Provides
    @Singleton
    fun provideAuthRepository(auth: FirebaseAuth): AuthRepository {
        return AuthRepositoryImpl(auth)
    }

    @Provides
    @Singleton
    fun provideFamiliaRepository(firestore: FirebaseFirestore): FamiliaRepository {
        return FamiliaRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideDespensaRepository(firestore: FirebaseFirestore): DespensaRepository {
        return DespensaRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideListaCompraRepository(firestore: FirebaseFirestore): ListaCompraRepository {
        return ListaCompraRepositoryImpl(firestore)
    }
    @Provides
    @Singleton
    fun provideCatalogoRepository(firestore: FirebaseFirestore): CatalogoRepository {
        return CatalogoRepositoryImpl(firestore)
    }
}