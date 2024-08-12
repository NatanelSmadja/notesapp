package com.example.notesapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NoteAdapter.OnNoteClickListener {

    private RecyclerView recyclerViewNotes;
    private NoteAdapter noteAdapter;
    private List<Note> noteList;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private Button buttonAddNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        recyclerViewNotes = findViewById(R.id.recyclerViewNotes);
        recyclerViewNotes.setLayoutManager(new GridLayoutManager(this, 2));
        noteList = new ArrayList<>();
        noteAdapter = new NoteAdapter(noteList, this);
        recyclerViewNotes.setAdapter(noteAdapter);
        buttonAddNote = findViewById(R.id.buttonAddNote);

        // Check if the user is logged in
        if (mAuth.getCurrentUser() == null) {
            // Redirect to login activity
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        if (mAuth.getCurrentUser() != null) {
            loadUserData();
        }

        buttonAddNote.setOnClickListener(v -> {
            Log.d("MainActivity", "Add Note button clicked");
            // Navigate to AddEditNoteActivity
            Intent intent = new Intent(MainActivity.this, AddEditNoteActivity.class);
            startActivity(intent);
        });


        // מעלה את הפתקים עם המשתמש מחובר
        loadNotes();
    }

    private void loadUserData() {
        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String firstName = snapshot.child("firstName").getValue(String.class);
                    if (firstName != null) {
                        TextView greetingTextView = findViewById(R.id.textViewWelcome);
                        greetingTextView.setText("Hi, " + firstName);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    private void loadNotes() {
        String userId = mAuth.getCurrentUser().getUid();
        mDatabase.child("notes").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                noteList.clear();
                for (DataSnapshot noteSnapshot : snapshot.getChildren()) {
                    Note note = noteSnapshot.getValue(Note.class);
                    if (note != null) {
                        note.setNoteId(noteSnapshot.getKey()); // Set the note ID
                        noteList.add(note);
                    }
                }
                noteAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Failed to load notes.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onNoteClick(int position) {
        Note note = noteList.get(position);
        Intent intent = new Intent(MainActivity.this, AddEditNoteActivity.class);
        intent.putExtra("noteId", note.getNoteId());
        intent.putExtra("noteTitle", note.getTitle());
        intent.putExtra("noteContent", note.getContent());
        startActivity(intent);
    }

    @Override
    public void onNoteLongClick(int position) {
        Note note = noteList.get(position);
        String userId = mAuth.getCurrentUser().getUid();

        mDatabase.child("notes").child(userId).child(note.getNoteId()).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                loadNotes(); // Reload the notes from Firebase to refresh the list
                Toast.makeText(MainActivity.this, "Note deleted successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Failed to delete note", Toast.LENGTH_SHORT).show();
            }
        });
    }
}