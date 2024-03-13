package project.taskmaster.choremaster;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

import project.taskmaster.choremaster.databinding.FragmentStatsBinding;

public class StatsFragment extends Fragment {
    private FragmentStatsBinding binding;
    private SharedPreferences sharedPreferences;
    private FirebaseFirestore db;

    public StatsFragment() {
    }
    public static StatsFragment newInstance() {
        StatsFragment fragment = new StatsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        sharedPreferences = getActivity().getSharedPreferences("ChoreMaster", Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentStatsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();

        String activeGroupId = sharedPreferences.getString("activeGroupId", null);

        if (!activeGroupId.isEmpty()) {
            db.collection("groups").document(activeGroupId).get().addOnSuccessListener(groupSnapshot -> {
                if (groupSnapshot.exists()) {
                    Map<String, Object> members = (Map<String, Object>) groupSnapshot.get("members");
                    if (members != null) {
                        StringBuilder statsBuilder = new StringBuilder();
                        for (String userId : members.keySet()) {
                            db.collection("users").document(userId).get().addOnSuccessListener(userSnapshot -> {
                                if (userSnapshot.exists()) {
                                    String userName = userSnapshot.getString("username");
                                    Map<String, Object> userDetails = (Map<String, Object>) members.get(userId);
                                    Long points = (Long) userDetails.get("points");
                                    Log.d("TAG", String.valueOf(points));
                                    statsBuilder.append(userName).append(": ").append(points).append(" points\n");

                                    // Once all users are processed, set the text in textViewStats
                                    if (statsBuilder.length() > 0) {
                                        binding.textViewStats.setText(statsBuilder.toString());
                                    }
                                }
                            }).addOnFailureListener(e -> {});
                        }
                    } else {
                        binding.textViewStats.setText("No members found.");
                    }
                } else {
                    binding.textViewStats.setText("Group not found.");
                }
            }).addOnFailureListener(e -> binding.textViewStats.setText("Error loading group: " + e.getMessage()));
        } else {
            binding.textViewStats.setText("No active group selected.");
        }

        return view;
    }
}