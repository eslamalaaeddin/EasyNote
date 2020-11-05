package com.example.easynote.activity.mainactivity

import android.content.Intent
import android.graphics.Canvas
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.easynote.R
import com.example.easynote.activity.LoginRegisterActivity
import com.example.easynote.activity.ProfileActivity
import com.example.easynote.model.Note
import com.example.easynote.viewmodel.MainActivityViewModel
import com.firebase.ui.auth.AuthUI
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), FirebaseAuth.AuthStateListener,
    NotesAdapter.NoteListener {
    private lateinit var auth: FirebaseAuth
    private var notesAdapter: NotesAdapter? = null
    private lateinit var viewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)

        setSupportActionBar(toolbar)

        auth = FirebaseAuth.getInstance()

        notesRecyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

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
                        .Builder(
                            c,
                            recyclerView,
                            viewHolder,
                            dX,
                            dY,
                            actionState,
                            isCurrentlyActive
                        )
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

    private fun goToProfileActivity() {
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
        } else {
            initRecyclerView(auth.currentUser!!)
        }
    }

    private fun addNote(text: String) {
        val userId = auth.currentUser?.uid.toString()
        val note = Note(text, false, userId)

        viewModel.addNote(note).addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(this, "Note added.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "${it.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun initRecyclerView(firebaseUser: FirebaseUser) {
        val options = viewModel.queryNotesForRecyclerView(firebaseUser)

        //3 initializing the adapter
        notesAdapter = NotesAdapter(options, this)

        //4 attaching the adapter to recycler view
        notesRecyclerView.adapter = notesAdapter

        //5 listening to the adapter
        notesAdapter?.startListening()

    }

    override fun onCheckBoxClicked(isChecked: Boolean, documentSnapshot: DocumentSnapshot) {
        viewModel.updateNote(documentSnapshot.reference,"completed", isChecked).addOnFailureListener {
            Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onNoteClicked(documentSnapshot: DocumentSnapshot) {
        val editText = EditText(this)
        val noteLiveData = viewModel.getNoteLiveData(documentSnapshot.id)
        noteLiveData?.observe(this, { note ->
            val text = note?.text
            editText.setText(text)
            note?.text?.length?.let { editText.setSelection(it) }

        })

        AlertDialog.Builder(this)
            .setTitle("Update note")
            .setView(editText)
            .setPositiveButton("Update") { _, _ ->
                val newText = editText.text.toString()
                viewModel.updateNote(documentSnapshot.reference,"text",newText).addOnSuccessListener {
                    Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()

    }

    override fun onItemSwiped(documentSnapshot: DocumentSnapshot) {
        val note = documentSnapshot.toObject(Note::class.java)
        viewModel.deleteNote(documentSnapshot.reference).addOnSuccessListener {
            Snackbar.make(notesRecyclerView, "Note deleted", Snackbar.LENGTH_LONG)
                .setAction("Undo") {
                    if (note != null) {
                        viewModel.setNote(documentSnapshot.reference, note)
                    }
                }
                .show()
        }.addOnFailureListener {
            Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
        }

    }


}