    package com.example.notesapp;

    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.TextView;

    import androidx.annotation.NonNull;
    import androidx.recyclerview.widget.RecyclerView;

    import java.util.List;

    public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

        private List<Note> noteList;
        private OnNoteClickListener onNoteClickListener;

        public NoteAdapter(List<Note> noteList, OnNoteClickListener onNoteClickListener) {
            this.noteList = noteList;
            this.onNoteClickListener = onNoteClickListener;
        }

        @NonNull
        @Override
        public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
            return new NoteViewHolder(view, onNoteClickListener);
        }

        @Override
        public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
            Note note = noteList.get(position);
            holder.textViewTitle.setText(note.getTitle());
            holder.textViewContent.setText(note.getContent());
            holder.textViewTimestamp.setText(note.getTimestamp());
        }

        @Override
        public int getItemCount() {
            return noteList.size();
        }

        public static class NoteViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

            TextView textViewTitle, textViewContent, textViewTimestamp;
            OnNoteClickListener onNoteClickListener;

            public NoteViewHolder(@NonNull View itemView, OnNoteClickListener onNoteClickListener) {
                super(itemView);
                textViewTitle = itemView.findViewById(R.id.textViewTitle);
                textViewContent = itemView.findViewById(R.id.textViewContent);
                textViewTimestamp = itemView.findViewById(R.id.textViewTimestamp);
                this.onNoteClickListener = onNoteClickListener;

                itemView.setOnClickListener(this);
                itemView.setOnLongClickListener(this);
            }

            @Override
            public void onClick(View v) {
                onNoteClickListener.onNoteClick(getAdapterPosition());
            }

            @Override
            public boolean onLongClick(View v) {
                onNoteClickListener.onNoteLongClick(getAdapterPosition());
                return true;
            }
        }

        public interface OnNoteClickListener {
            void onNoteClick(int position);
            void onNoteLongClick(int position);
        }
    }
