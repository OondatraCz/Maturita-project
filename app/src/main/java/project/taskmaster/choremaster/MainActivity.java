package project.taskmaster.choremaster;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import project.taskmaster.choremaster.databinding.ActivityMainBinding;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = getSharedPreferences("AppName", MODE_PRIVATE);
        String deepLink = sharedPreferences.getString("deepLink", null);

        if (deepLink != null) {
            Uri data = Uri.parse(deepLink);
            if ("chore-master-project.firebaseapp.com".equals(data.getHost()) && "/joinGroup".equals(data.getPath())) {
                addUserToGroup(data.getQueryParameter("groupId"));
            }
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        replaceFragment(new HomeFragment());

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.home) {
                replaceFragment(new HomeFragment());
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

    private void addUserToGroup(String groupId) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DocumentReference userRef = FirebaseFirestore.getInstance().collection("users").document(userId);

            userRef.update("groups", FieldValue.arrayUnion(groupId))
                    .addOnSuccessListener(aVoid -> {
                        addUserToGroupInFirestore(groupId, userId);
                    })
                    .addOnFailureListener(e -> Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void addUserToGroupInFirestore(String groupId, String userId) {
        DocumentReference groupRef = FirebaseFirestore.getInstance().collection("groups").document(groupId);

        // Prepare the user data for the group
        Map<String, Object> userData = new HashMap<>();
        userData.put("score", 0);  // Assuming starting score is 0
        userData.put("joinedDate", new Timestamp(new Date()));
        userData.put("role", "member");

        // Add the user to the group's members map
        groupRef.update("members." + userId, userData)
                .addOnSuccessListener(aVoid -> Toast.makeText(getApplicationContext(), "You have been succesfully added to a new group", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show());
    }


    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }
}