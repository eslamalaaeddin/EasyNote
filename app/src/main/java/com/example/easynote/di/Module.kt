package com.example.easynote.di

import com.example.easynote.repository.Repository
import com.example.easynote.viewmodel.MainActivityViewModel
import com.google.firebase.firestore.FirebaseFirestore
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