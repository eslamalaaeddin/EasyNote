package com.example.easynote

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color.red
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity(), FirebaseAuth.AuthStateListener, NotesAdapter.NoteListener {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    private var notesAdapter : NotesAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()

        notesRecyclerView.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )

        fab.setOnClickListener {
            showAlertDialog()
        }

        val itemTouchHelper =
            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val notesViewHolder = viewHolder as NotesAdapter.NotesViewHolder

                    notesViewHolder.deleteNote()
                }

                //We can implement it manually or use xabaras library
                override fun onChildDraw(
                    c: Canvas,
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    dX: Float,
                    dY: Float,
                    actionState: Int,
                    isCurrentlyActive: Boolean
                ) {
                    RecyclerViewSwipeDecorator
                        .Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addBackgroundColor(
                            ContextCompat.getColor(
                                this@MainActivity,
                                R.color.red
                            )
                        )
                        .addActionIcon(R.drawable.ic_delete_black_24dp)
                        .create()
                        .decorate()

                    super.onChildDraw(
                        c,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )
                }
            })

        itemTouchHelper.attachToRecyclerView(notesRecyclerView)
    }

    private fun showAlertDialog() {
        val noteEditText = EditText(this)
        AlertDialog.Builder(this)
            .setTitle("Add")
            .setView(noteEditText)
            .setPositiveButton(
                "Add"
            ) { _, _ -> addNote(noteEditText.text.toString()) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onStart() {
        super.onStart()
        auth.addAuthStateListener(this)
    }

    override fun onStop() {
        super.onStop()
        auth.removeAuthStateListener(this)
        notesAdapter?.let {
            it.stopListening()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.action_logout -> logoutAndGoToLoginRegisterActivity()
            R.id.action_Profile -> goToProfileActivity()
        }
        return true
    }

    private fun logoutAndGoToLoginRegisterActivity() {
        AuthUI.getInstance().signOut(this)
    }

    private fun goToProfileActivity(){
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
    }

    private fun goToLoginRegisterActivity() {
        startActivity(Intent(this, LoginRegisterActivity::class.java))
        finish()
    }

    override fun onAuthStateChanged(auth: FirebaseAuth) {
        if (auth.currentUser == null) {
            goToLoginRegisterActivity()
        }
        else{
            initRecyclerView(auth.currentUser!!)
        }
    }

    private fun addNote(text: String) {
        val userId = auth.currentUser?.uid.toString()
        val note = Note(text, false, userId)

        database.collection("notes")
            .add(note)
            .addOnSuccessListener {
                Toast.makeText(this, "Note added.", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(this, "${it.message}", Toast.LENGTH_SHORT).show()
            }

    }

    private fun initRecyclerView(firebaseUser: FirebaseUser){
        //1 creating the query
        val query = database.collection("notes")
            .whereEqualTo("userId", firebaseUser.uid)
            .orderBy("completed", Query.Direction.ASCENDING)
            .orderBy("created", Query.Direction.DESCENDING)

        //2 creating the options
        val options : FirestoreRecyclerOptions<Note> = FirestoreRecyclerOptions
                .Builder<Note>()
                .setQuery(query, Note::class.java)
                .build()
        //3 initializing the adapter
        notesAdapter = NotesAdapter(options, this)

        //4 attaching the adapter to recycler view
        notesRecyclerView.adapter = notesAdapter

        //5 listening to the adapter
        notesAdapter?.startListening()

    }

    override fun onCheckBoxClicked(isChecked: Boolean, documentSnapshot: DocumentSnapshot) {
        documentSnapshot.reference.update("completed", isChecked).addOnSuccessListener {
            Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onNoteClicked(documentSnapshot: DocumentSnapshot) {
        val note = documentSnapshot.toObject(Note::class.java)
        val text = note?.text
        val editText = EditText(this)
        editText.setText(text)
        note?.text?.length?.let { editText.setSelection(it) }


        AlertDialog.Builder(this)
            .setTitle("Update note")
            .setView(editText)
            .setPositiveButton(
                "Update"
            ) { _, _ ->
                val newText =  editText.text.toString()

                documentSnapshot.reference
                .update("text", newText).addOnSuccessListener {
                    Toast.makeText(this, "Note updated.", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(this, "${it.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onItemSwiped(documentSnapshot: DocumentSnapshot) {
        val note = documentSnapshot.toObject(Note::class.java)
        documentSnapshot.reference.delete().addOnSuccessListener {
            Snackbar.make(notesRecyclerView, "Note deleted", Snackbar.LENGTH_LONG)
                .setAction("Undo") {
                    if (note != null) {
                        documentSnapshot.reference.set(note)
                    }
                }
                .show()
        }.addOnFailureListener {
            Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
        }
    }


}