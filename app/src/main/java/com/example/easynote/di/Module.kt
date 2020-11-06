package com.example.easynote.di

import com.example.easynote.repository.Repository
import com.example.easynote.viewmodel.MainActivityViewModel
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel {
        MainActivityViewModel(get())
    }
}

val repositoryModule = module {
    single {
        Repository(get())
    }
}

val databaseModule = module {
    fun provideDB(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }
    single { provideDB() }
}

val firebaseAuthModule = module {
    fun provideAuth():FirebaseAuth{
        return FirebaseAuth.getInstance()
    }
    single { provideAuth() }
}

val firebaseAuthUIModule = module {
    fun provideAuthUI() : AuthUI{
        return AuthUI.getInstance()
    }
    single { provideAuthUI() }
}

val firebaseStorageModule = module {
    fun provideStorage() : FirebaseStorage{
        return FirebaseStorage.getInstance()
    }
    single { provideStorage() }
}