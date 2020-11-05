package com.example.easynote.activity.mainactivity

import android.text.format.DateFormat.format
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.easynote.R
import com.example.easynote.model.Note
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.android.synthetic.main.note_item.view.*

class NotesAdapter(private val options: FirestoreRecyclerOptions<Note>, private val listener: NoteListener) :
    FirestoreRecyclerAdapter<Note, NotesAdapter.NotesViewHolder>(options) {

    private lateinit var noteListener: NoteListener

    interface NoteListener{
        fun onCheckBoxClicked(isChecked : Boolean, documentSnapshot: DocumentSnapshot)
        fun onNoteClicked(documentSnapshot: DocumentSnapshot)
        fun onItemSwiped(documentSnapshot: DocumentSnapshot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesViewHolder {
        val view  = LayoutInflater.from(parent.context).inflate(R.layout.note_item, parent, false)
        return NotesViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotesViewHolder, position: Int, note: Note) {
        holder.itemView.noteTextView.text = note.text
        holder.itemView.dateTextView.text = format("EEEE, MMM d, yyyy h:mm:ss a", note.created.toDate())
        holder.itemView.checkBox.isChecked = note.completed!!
    }

    inner class NotesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener{

        init {
            noteListener = listener
            itemView.setOnClickListener(this)

            itemView.checkBox.setOnCheckedChangeListener { compoundButton, state ->
                val snapshot = snapshots.getSnapshot(adapterPosition)
                val note = getItem(adapterPosition)
                if (note.completed != state){
                    noteListener.onCheckBoxClicked(state, snapshot)
                }
            }
        }

        override fun onClick(p0: View?) {
            val snapshot = snapshots.getSnapshot(adapterPosition)
            noteListener.onNoteClicked(snapshot)
        }

        fun deleteNote(){
            val snapshot = snapshots.getSnapshot(adapterPosition)
            noteListener.onItemSwiped(snapshot)
        }
    }

}