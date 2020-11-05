package com.example.easynote.livedata

import androidx.lifecycle.LiveData
import com.example.easynote.model.Note
import com.google.firebase.firestore.*


class NoteLiveData (private val documentReference: DocumentReference) : LiveData<Note>(), EventListener<DocumentSnapshot> {

    private var listenerRegistration : ListenerRegistration? = null

    override fun onActive() {
        super.onActive()
        listenerRegistration = documentReference.addSnapshotListener(this)
    }

    override fun onInactive() {
        super.onInactive()
        listenerRegistration?.remove()
    }

    override fun onEvent(snapshot: DocumentSnapshot?, error: FirebaseFirestoreException?) {
        val note = snapshot?.toObject(Note::class.java)
        postValue(note)
    }



}