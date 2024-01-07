package project.taskmaster.choremaster;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import project.taskmaster.choremaster.databinding.ActivitySetUsernameBinding;

public class SetUsernameActivity extends AppCompatActivity {

    private ActivitySetUsernameBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySetUsernameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = binding.username.getText().toString();

                if (!username.matches(".*\\p{L}+.*")) {
                    Toast.makeText(SetUsernameActivity.this, "Nickname must contain letters!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(username.length() < 5) {
                    Toast.makeText(SetUsernameActivity.this, "Nickname must be at least 5 characters long!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(username.length() > 20) {
                    Toast.makeText(SetUsernameActivity.this, "Nickname must be maximally 20 characters long!", Toast.LENGTH_SHORT).show();
                    return;
                }

                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    String userId = currentUser.getUid();

                    Map<String, Object> defaultGroup = new HashMap<>();
                    defaultGroup.put("name", "My group");
                    defaultGroup.put("description", "-");
                    //defaultGroup.put("members", Collections.singletonMap(userId, new MemberDetails()));

                    Map<String, Object> memberDetails = new HashMap<>();
                    memberDetails.put("role", "admin");
                    memberDetails.put("score", 0);
                    memberDetails.put("joinedDate", Timestamp.now());

                    Map<String, Object> members = new HashMap<>();
                    members.put(userId, memberDetails);
                    defaultGroup.put("members", members);

                    FirebaseFirestore db = FirebaseFirestore.getInstance();

                    db.collection("groups")
                            .add(defaultGroup)
                            .addOnSuccessListener(documentReference -> {
                                Map<String, Object> userData = new HashMap<>();
                                userData.put("username", username);
                                userData.put("bio", binding.bio.getText().toString());
                                userData.put("groups", Collections.singletonList(documentReference.getId()));

                                db.collection("users").document(userId)
                                        .set(userData)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(SetUsernameActivity.this, "New user and group created successfully", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(SetUsernameActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });

                                Toast.makeText(SetUsernameActivity.this, "Default group created", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(SetUsernameActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                } else {
                    Toast.makeText(SetUsernameActivity.this, "User not logged in", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}