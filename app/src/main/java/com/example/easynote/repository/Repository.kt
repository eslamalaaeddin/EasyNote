package com.example.easynote.repository

import androidx.lifecycle.LiveData
import com.example.easynote.model.Note
import com.example.easynote.livedata.NoteLiveData
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class Repository (private val db : FirebaseFirestore) {

    fun addNote(note : Note) : Task<DocumentReference>{
       return db.collection("notes").add(note)
    }

    fun getNoteLiveData(string: String): LiveData<Note>? {
        val documentReference = db.collection("notes").document(string)
        return NoteLiveData(documentReference)
    }

    fun setNote(documentReference: DocumentReference, note: Note): Task<Void> {
        return documentReference.set(note)
    }

    fun updateNote(documentReference: DocumentReference, field: String, value : Any) : Task<Void>{
        return documentReference.update(field, value)
    }

    fun deleteNote(documentReference: DocumentReference): Task<Void> {
        return documentReference.delete()
    }

    fun queryNotesForRecyclerView(firebaseUser: FirebaseUser) : FirestoreRecyclerOptions<Note>{
        //1 creating the query
        val query = db.collection("notes")
            .whereEqualTo("userId", firebaseUser.uid)
            .orderBy("completed", Query.Direction.ASCENDING)
            .orderBy("created", Query.Direction.DESCENDING)

        //2 creating the options

        return FirestoreRecyclerOptions
            .Builder<Note>()
            .setQuery(query, Note::class.java)
            .build()
    }

}