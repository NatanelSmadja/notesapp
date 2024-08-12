package com.example.notesapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddEditNoteActivity extends AppCompatActivity {

    private EditText editTextNoteTitle, editTextNoteContent;
    private Button buttonSaveNote;
    private DatabaseReference mDatabase;
    private FirebaseAuth Auth;
    private String noteId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_note);

        Auth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        editTextNoteTitle = findViewById(R.id.editTextNoteTitle);
        editTextNoteContent = findViewById(R.id.editTextNoteContent);
        buttonSaveNote = findViewById(R.id.buttonSaveNote);

        buttonSaveNote.setOnClickListener(v -> {
            Log.d("AddEditNoteActivity", "Save Note button clicked");
            saveNote();
        });

        // Check if we are editing an existing note
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("noteId")) {
            noteId = intent.getStringExtra("noteId");
            editTextNoteTitle.setText(intent.getStringExtra("noteTitle"));
            editTextNoteContent.setText(intent.getStringExtra("noteContent"));
        }

        buttonSaveNote.setOnClickListener(v -> saveNote());
    }

    private void saveNote() {
        String title = editTextNoteTitle.getText().toString().trim();
        String content = editTextNoteContent.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content)) {
            Toast.makeText(AddEditNoteActivity.this, "Both fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // בדיקה עם המשתמש מחובר ומקבל את ה userid שלו
        FirebaseUser currentUser = Auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        Note note = new Note(title, content, timestamp);

        if (noteId != null) { // עריכה של פתק נוכחי
            mDatabase.child("notes").child(userId).child(noteId).setValue(note).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(AddEditNoteActivity.this, "Note updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AddEditNoteActivity.this, "Failed to update note", Toast.LENGTH_SHORT).show();
                }
            });
        } else { // יצירה של פתק חדש
            noteId = mDatabase.child("notes").child(userId).push().getKey();
            if (noteId == null) {
                Toast.makeText(this, "Failed to generate note ID", Toast.LENGTH_SHORT).show();
                return;
            }
            note.setNoteId(noteId);
            mDatabase.child("notes").child(userId).child(noteId).setValue(note).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(AddEditNoteActivity.this, "Note saved successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AddEditNoteActivity.this, "Failed to save note", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}