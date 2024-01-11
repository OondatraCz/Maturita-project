package project.taskmaster.choremaster;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import project.taskmaster.choremaster.databinding.ActivityTaskDetailBinding;

public class TaskDetailActivity extends AppCompatActivity {

    private ActivityTaskDetailBinding binding;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTaskDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String taskId = getIntent().getStringExtra("taskId");
        String groupId = sharedPreferences.getString("activeGroupId", null);

        setEditable(false);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("tasks").document(taskId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Task task = documentSnapshot.toObject(Task.class);
                    if (task != null) {
                        binding.txtTitle.setText(task.getTitle());
                        binding.edtDesc.setText(task.getDescription());
                        binding.edtCategory.setText(task.getCategory());

                        if (isOwner(task)) {
                            binding.btnEdit.setVisibility(View.VISIBLE);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading task", Toast.LENGTH_SHORT).show();
                });

        binding.btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isEditable = binding.txtTitle.isEnabled();
                setEditable(!isEditable);
            }
        });

        binding.btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.collection("groups").document(groupId).collection("tasks").document(taskId)
                        .update("title", binding.txtTitle.getText().toString(), "description", binding.edtDesc.getText().toString(), "Category", binding.edtCategory.getText().toString())
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(TaskDetailActivity.this, "Tasks succesfuly edited", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(TaskDetailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }

    private void setEditable(boolean isEditable) {
        binding.txtTitle.setEnabled(isEditable);
        binding.edtDesc.setEnabled(isEditable);
        binding.edtCategory.setEnabled(isEditable);

        // Optionally, change the appearance of the EditText to indicate editability
        /*int backgroundRes = isEditable ? R.drawable.edit_text_enabled : R.drawable.edit_text_disabled;
        binding.txtTitle.setBackgroundResource(backgroundRes);
        binding.edtDesc.setBackgroundResource(backgroundRes);
        binding.edtCategory.setBackgroundResource(backgroundRes);*/

        // Show or hide the submit button based on editability
        binding.btnSubmit.setVisibility(isEditable ? View.VISIBLE : View.GONE);
    }

    private boolean isOwner(Task task) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        return currentUser != null && task.getCreatedBy().equals(currentUser.getUid());
    }
}
