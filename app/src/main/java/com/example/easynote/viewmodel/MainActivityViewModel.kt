package com.example.easynote.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.easynote.model.Note
import com.example.easynote.repository.Repository
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference

private const val TAG = "MainActivityViewModel"

class MainActivityViewModel(private val repository: Repository) : ViewModel() {

    //private val repository = Repository()

    fun addNote(note: Note) : Task<DocumentReference>{
        return repository.addNote(note)
    }

    fun getNoteLiveData(string: String): LiveData<Note>? {
        return repository.getNoteLiveData(string)
    }

    fun setNote(documentReference: DocumentReference,note : Note) : Task<Void>{
        return repository.setNote(documentReference, note)
    }

    fun updateNote(documentReference: DocumentReference, field: String, value : Any): Task<Void> {
        return repository.updateNote(documentReference, field, value)
    }

    fun deleteNote(documentReference: DocumentReference) : Task<Void>{
        return repository.deleteNote(documentReference)
    }

    fun queryNotesForRecyclerView(firebaseUser: FirebaseUser) : FirestoreRecyclerOptions<Note> {
        return repository.queryNotesForRecyclerView(firebaseUser)
    }


}