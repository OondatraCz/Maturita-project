package project.taskmaster.choremaster;

import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private List<Task> tasks;

    public interface OnItemClickListener {
        void onItemClick(Task task);
    }

    private OnItemClickListener listener;

    public TaskAdapter(List<Task> tasks, OnItemClickListener listener) {
        this.tasks = tasks;
        this.listener = listener;
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TaskViewHolder holder, int position) {
        holder.bind(tasks.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void updateTasks(List<Task> newTasks) {
        this.tasks.clear();
        this.tasks.addAll(newTasks);
        notifyDataSetChanged();
    }


    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle, textViewDescription, textViewDueDate;

        public TaskViewHolder(View view) {
            super(view);
            textViewTitle = view.findViewById(R.id.textViewTitle);
            textViewDescription = view.findViewById(R.id.textViewDescription);
            textViewDueDate = view.findViewById(R.id.textViewDueDate);
        }

        public void bind(Task task, OnItemClickListener listener) {
            textViewTitle.setText(task.getTitle());
            textViewDescription.setText(task.getDescription());
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            textViewDueDate.setText(sdf.format(task.getDueDate().toDate()));

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onItemClick(task);
                    }
                }
            });
        }
    }
}
