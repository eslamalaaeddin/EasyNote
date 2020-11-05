package com.example.easynote.model

import com.google.firebase.Timestamp
import java.util.*

data class Note(
    var text: String? = "",
    var completed: Boolean? = false,
    var userId: String? = "",
    val created: Timestamp = Timestamp(
        Date()
    )
)
