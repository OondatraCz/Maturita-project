package project.taskmaster.choremaster;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import project.taskmaster.choremaster.databinding.ActivityManageProfileBinding;

public class ManageProfileActivity extends AppCompatActivity {
    private ActivityManageProfileBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loadUserProfile();

        binding.btnSubmit.setOnClickListener(v -> submitChanges());
    }
    private void loadUserProfile() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {

            db.collection("users").document(user.getUid()).get()
                    .addOnCompleteListener(result -> {
                       if (result.isSuccessful()){
                           binding.edtUsername.setText(result.getResult().getString("username"));
                           binding.edtBio.setText(result.getResult().getString("bio"));
                       } else {
                           Toast.makeText(ManageProfileActivity.this, "Failed to load username", Toast.LENGTH_SHORT).show();
                       }
                    });
        }
    }
    private void submitChanges() {
        String newUsername = binding.edtUsername.getText().toString().trim();
        String newBio = binding.edtBio.getText().toString().trim();

        if (!newUsername.matches(".*\\p{L}+.*")) {
            Toast.makeText(ManageProfileActivity.this, "Username must contain letters!", Toast.LENGTH_SHORT).show();
            return;
        }
        if(newUsername.length() < 5) {
            Toast.makeText(ManageProfileActivity.this, "Username must be at least 5 characters long!", Toast.LENGTH_SHORT).show();
            return;
        }
        if(newUsername.length() > 20) {
            Toast.makeText(ManageProfileActivity.this, "Username must be maximally 20 characters long!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (newBio.length() > 100){
            Toast.makeText(ManageProfileActivity.this, "Bio must be maximally 100 characters long!", Toast.LENGTH_SHORT).show();
            return;
        }
        FirebaseUser user = auth.getCurrentUser();

        if (user != null){
            db.collection("users").document(user.getUid())
                    .update("username", newUsername, "bio", newBio)
                    .addOnCompleteListener(result -> {
                        if (result.isSuccessful()){
                            Toast.makeText(ManageProfileActivity.this, "User data updated", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                        else Toast.makeText(ManageProfileActivity.this, result.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
