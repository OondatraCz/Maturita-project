package project.taskmaster.choremaster;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import project.taskmaster.choremaster.databinding.ActivityCreateGroupBinding;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CreateGroupActivity extends AppCompatActivity {
    private ActivityCreateGroupBinding binding;
    private SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        sharedPreferences = getApplicationContext().getSharedPreferences("ChoreMaster", Context.MODE_PRIVATE);

        binding.Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String groupName = binding.groupName.getText().toString();
                if(groupName.isEmpty()){
                    Toast.makeText(CreateGroupActivity.this, "Name can't be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                String userId = currentUser.getUid();

                Map<String, Object> groupData = new HashMap<>();
                groupData.put("name", groupName);
                groupData.put("description", binding.description.getText().toString());

                Map<String, Object> memberDetails = new HashMap<>();
                memberDetails.put("role", "admin");
                memberDetails.put("points", 0);
                memberDetails.put("joinedDate", Timestamp.now());

                Map<String, Object> members = new HashMap<>();
                members.put(userId, memberDetails);

                groupData.put("members", members);

                db.collection("groups").add(groupData)
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(CreateGroupActivity.this, "New group created succesfully", Toast.LENGTH_SHORT).show();
                            String groupId = documentReference.getId();
                            db.collection("users").document(userId)
                                    .update("groups", FieldValue.arrayUnion(groupId))
                                    .addOnSuccessListener(aVoid -> {
                                        sharedPreferences.edit().putString("activeGroupId", groupId).apply();
                                        String groupNames = sharedPreferences.getString("allGroupNames", "");
                                        String allGroupNames;
                                        if (!groupNames.isEmpty()){
                                            allGroupNames = groupNames + "," + groupName;
                                        }
                                        else{
                                            allGroupNames = groupName;
                                        }

                                        String groupIds = sharedPreferences.getString("allGroupIds", "");
                                        String allGroupIds;
                                        if (!groupIds.isEmpty()){
                                            allGroupIds = groupIds + "," + groupId;
                                        }
                                        else{
                                            allGroupIds = groupId;
                                        }

                                        sharedPreferences.edit().putString("allGroupNames", allGroupNames).apply();
                                        sharedPreferences.edit().putString("allGroupIds", allGroupIds).apply();

                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        setResult(RESULT_OK, intent);
                                        startActivity(intent);
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(CreateGroupActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });

                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(CreateGroupActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });

    }
}