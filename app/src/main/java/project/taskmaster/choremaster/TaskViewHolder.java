package project.taskmaster.choremaster;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class TaskViewHolder extends RecyclerView.ViewHolder {
    TextView nameTextView;
    TextView descriptionTextView;
    TextView dueDateTextView;

    public TaskViewHolder(View itemView) {
        super(itemView);
        nameTextView = itemView.findViewById(R.id.textViewTitle);
        descriptionTextView = itemView.findViewById(R.id.textViewDescription);
        dueDateTextView = itemView.findViewById(R.id.textViewDueDate);
    }
}
