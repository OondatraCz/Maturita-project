package project.taskmaster.choremaster;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import project.taskmaster.choremaster.databinding.ActivityManageGroupBinding;

public class ManageGroupActivity extends AppCompatActivity {

    private ActivityManageGroupBinding binding;
    private FirebaseFirestore db;
    private UserAdapter adapter;
    private String groupId;
    private SharedPreferences sharedPreferences;
    private FirebaseAuth auth;
    private List<String> userIds;
    private List<String> userNames;
    private List<String> groupIds;
    private List<String> groupNames;
    private List<String> roleList;
    private boolean actionMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageGroupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        auth = FirebaseAuth.getInstance();

        db = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences("ChoreMaster", MODE_PRIVATE);

        groupId = sharedPreferences.getString("activeGroupId", null);

        String storedString = sharedPreferences.getString("allGroupIds", "");

        if (!storedString.equals("")) {
            groupIds = new ArrayList<String>(Arrays.asList(storedString.split(",")));
        }

        storedString = sharedPreferences.getString("allGroupNames", "");
        if (!storedString.equals("")) {
            groupNames = new ArrayList<String>(Arrays.asList(storedString.split(",")));
        }

        if(!groupId.equals(null)){
            userNames = new ArrayList<>();
            roleList = new ArrayList<>();

            binding.btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String name = binding.editTextName.getText().toString().trim();
                    if (name.isEmpty()){
                        Toast.makeText(ManageGroupActivity.this, "Name can't be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Map<String, Object> groupInfo = new HashMap<>();
                    groupInfo.put("name", name);
                    groupInfo.put("description", binding.editTextDescription.getText().toString().trim());
                    db.collection("groups").document(groupId)
                            .update(groupInfo)
                            .addOnSuccessListener(aVoid -> {
                                groupNames.set(groupIds.indexOf(groupId), name);
                                sharedPreferences.edit().putString("allGroupNames", TextUtils.join(",", groupNames)).commit();
                                Toast.makeText(ManageGroupActivity.this, "Group info updated", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> Toast.makeText(ManageGroupActivity.this, "Error updating group", Toast.LENGTH_SHORT).show());
                }
            });

            setupRecyclerView();
            fetchGroupMembers();

            binding.btnOption.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    actionMode = !actionMode;
                    adapter.setActionMode(actionMode);

                    if (actionMode) {
                        binding.txtOption.setText("Removal Mode");
                    } else {
                        binding.txtOption.setText("Role Change Mode");
                    }
                }
            });
        }
    }

    private void setupRecyclerView() {
        adapter = new UserAdapter(this, userNames, roleList, actionMode);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
        adapter.setClickListener((view, position) -> {
            String userId = userIds.get(position);
            if(actionMode){
                new AlertDialog.Builder(this)
                        .setTitle("Remove User")
                        .setMessage("Are you sure you want to remove this user from the group?")
                        .setPositiveButton("Remove", (dialog, which) -> removeUserFromGroup(userId, position))
                        .setNegativeButton("Cancel", null)
                        .show();
            }
            else{
                if(roleList.get(position).equals("admin")){
                    new AlertDialog.Builder(this)
                            .setTitle("Change Role")
                            .setMessage("Are you sure you want to change this user's role to member?")
                            .setPositiveButton("Change", (dialog, which) -> changeUserRole(userId, "member"))
                            .setNegativeButton("Cancel", null)
                            .show();
                }
                else{
                    new AlertDialog.Builder(this)
                            .setTitle("Change Role")
                            .setMessage("Are you sure you want to change this user's role to admin?")
                            .setPositiveButton("Change", (dialog, which) -> changeUserRole(userId, "admin"))
                            .setNegativeButton("Cancel", null)
                            .show();
                }
            }
        });
    }

    private void changeUserRole(String userId, String userRole) {
        if (userId.equals(auth.getCurrentUser().getUid())){
            Toast.makeText(ManageGroupActivity.this, "You can't make yourself member", Toast.LENGTH_SHORT).show();
            return;
        }
        db.collection("groups").document(groupId)
                .update("members." + userId + ".role", userRole)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "User role changed", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Error changing user role", Toast.LENGTH_SHORT).show());
    }
    private void fetchGroupMembers() {
        db.collection("groups").document(groupId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> members = (Map<String, Object>) documentSnapshot.get("members");
                        binding.editTextName.setText(documentSnapshot.getString("name"));
                        binding.editTextDescription.setText(documentSnapshot.getString("description"));
                        if (members != null) {
                            userIds = new ArrayList<>(members.keySet());
                            for (String userId : userIds) {
                                Map<String, String> memberDetails = (Map<String, String>) members.get(userId);
                                roleList.add(memberDetails.get("role"));
                            }
                            fetchUserNames();
                        }
                    } else {
                        Toast.makeText(this, "Group not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error getting group members", Toast.LENGTH_SHORT).show());
    }

    private void removeUserFromGroup(String userId, int position) {
        if (userId.equals(auth.getCurrentUser().getUid())){
            Toast.makeText(ManageGroupActivity.this, "You can't remove yourself from the group", Toast.LENGTH_SHORT).show();
            return;
        }
        db.collection("groups").document(groupId)
                .update("members." + userId, FieldValue.delete())
                .addOnSuccessListener(aVoid -> {
                    db.collection("users").document(userId)
                            .update("groups", FieldValue.arrayRemove(groupId))
                            .addOnSuccessListener(bVoid -> {
                                adapter.removeUser(position);
                                Toast.makeText(this, "User removed from group", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Error removing user", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error removing user", Toast.LENGTH_SHORT).show());
    }
    private void fetchUserNames() {
        Map<String, String> users = new HashMap<>();
        for (String memberId : userIds) {
            db.collection("users").document(memberId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            users.put(memberId, documentSnapshot.getString("username"));

                            if(users.size() == userIds.size()){
                                for (String userId : userIds){
                                    adapter.addUser(users.get(userId), roleList.get(userIds.indexOf(userId)));
                                }
                            }
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error getting member details", Toast.LENGTH_SHORT).show());
        }
    }
}
