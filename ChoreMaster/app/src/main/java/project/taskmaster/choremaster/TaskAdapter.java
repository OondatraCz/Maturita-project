package project.taskmaster.choremaster;

import androidx.recyclerview.widget.RecyclerView;

import java.util.Date;
import java.util.List;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Locale;

import project.taskmaster.choremaster.databinding.ItemTaskBinding;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private List<Task> tasks;
    private OnItemClickListener listener;
    private List<String> customTexts;

    public interface OnItemClickListener {
        void onItemClick(Task task);
    }
    public interface StateTextProvider {
        String getStateText(Task task);
    }

    public TaskAdapter(List<Task> tasks, OnItemClickListener listener, List<String> customTexts) {
        this.tasks = tasks;
        this.listener = listener;
        this.customTexts = customTexts;
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ItemTaskBinding itemBinding = ItemTaskBinding.inflate(layoutInflater, parent, false);
        return new TaskViewHolder(itemBinding);
    }

    @Override
    public void onBindViewHolder(TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        String customText = customTexts.get(position); // Get the custom text by position
        holder.bind(task, customText, listener);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void updateTasks(List<Task> newTasks, List<String> newCustomTexts) {
        tasks.clear();
        tasks.addAll(newTasks);
        customTexts.clear();
        customTexts.addAll(newCustomTexts);
        notifyDataSetChanged();
    }

    public void addTask(Task newTask, String newCustomText) {
        tasks.add(newTask);
        customTexts.add(newCustomText);
        notifyItemInserted(tasks.size() - 1);
    }


    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        private ItemTaskBinding binding;

        public TaskViewHolder(ItemTaskBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(final Task task, final String customText, final OnItemClickListener listener) {
            binding.textViewTitle.setText(task.getTitle());
            binding.textViewDescription.setText(task.getDescription());
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            binding.textViewDueDate.setText(sdf.format(task.getDueDate().toDate()));
            binding.textViewRightSide.setText(customText);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(task);
                }
            });
        }
    }
}
