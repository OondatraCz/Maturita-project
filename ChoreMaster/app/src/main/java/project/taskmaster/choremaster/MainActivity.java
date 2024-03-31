package project.taskmaster.choremaster;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import project.taskmaster.choremaster.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getSharedPreferences("ChoreMaster", MODE_PRIVATE);

        if (sharedPreferences.getString("allGroupIds", null) == null){
            loadAndStoreGroupData();
        } else {
            replaceFragment(new TasksFragment());
        }

        String deepLink = sharedPreferences.getString("deepLink", null);

        if (deepLink != null) {
            Uri data = Uri.parse(deepLink);
            if ("chore-master-project.web.app".equals(data.getHost()) && "/joinGroup".equals(data.getPath())) {
                addUserToGroup(data.getQueryParameter("groupId"));
            }
            sharedPreferences.edit().remove("deepLink").apply();
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.calendar) {
                replaceFragment(new CalendarFragment());
            } else if (itemId == R.id.tasks) {
                replaceFragment(new TasksFragment());
            } else if (itemId == R.id.stats) {
                replaceFragment(new StatsFragment());
            } else if (itemId == R.id.settings) {
                replaceFragment(new SettingsFragment());
            }

            return true;
        });
    }

    private void loadAndStoreGroupData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            List<String> groupIds = (List<String>) documentSnapshot.get("groups");

                            Map<String, String> groupIdNameMap = new LinkedHashMap<>();
                            for (String groupId : groupIds) {
                                db.collection("groups").document(groupId).get()
                                        .addOnSuccessListener(groupSnapshot -> {
                                            if (groupSnapshot.exists()) {
                                                String groupName = groupSnapshot.getString("name");
                                                groupIdNameMap.put(groupId, groupName);
                                                if (groupIdNameMap.size() == groupIds.size()) {
                                                    List<String> groupNames = new ArrayList<>();

                                                    for (String iGroupId : groupIds){
                                                        groupNames.add(groupIdNameMap.get(iGroupId));
                                                    }

                                                    sharedPreferences.edit().putString("activeGroupId", groupIds.get(0)).commit();
                                                    sharedPreferences.edit().putString("allGroupNames", TextUtils.join(",", groupNames)).commit();
                                                    sharedPreferences.edit().putString("allGroupIds", TextUtils.join(",", groupIds)).commit();
                                                    replaceFragment(new TasksFragment());
                                                }
                                            }
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                            }
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void addUserToGroup(String groupId) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("groups").document(groupId).get()
                .addOnSuccessListener(groupSnapshot -> {
                    if (groupSnapshot.exists()) {
                        String groupName = groupSnapshot.getString("name");

                        db.collection("users").document(userId)
                                .update("groups", FieldValue.arrayUnion(groupId))
                                .addOnSuccessListener(aVoid -> {
                                    Map<String, Object> userData = new HashMap<>();
                                    userData.put("points", 0);
                                    userData.put("joinedDate", new Timestamp(new Date()));
                                    userData.put("role", "member");
                                    db.collection("groups").document(groupId)
                                            .update("members." + userId, userData)
                                            .addOnSuccessListener(bVoid -> {
                                                Toast.makeText(getApplicationContext(), "You have been succesfully added to a new group", Toast.LENGTH_SHORT).show();
                                                sharedPreferences.edit().putString("activeGroupId", groupId).commit();
                                                sharedPreferences.edit().putString("allGroupIds", sharedPreferences.getString("allGroupIds", null) + "," + groupId).commit();
                                                sharedPreferences.edit().putString("allGroupNames", sharedPreferences.getString("allGroupNames", null) + "," + groupName).commit();
                                            })
                                            .addOnFailureListener(e -> Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show());
                                })
                                .addOnFailureListener(e -> Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(getApplicationContext(), "Group not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show());
    }


    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }
}