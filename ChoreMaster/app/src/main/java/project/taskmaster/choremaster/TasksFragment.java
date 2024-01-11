package project.taskmaster.choremaster;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;


import project.taskmaster.choremaster.databinding.FragmentTasksBinding;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TasksFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TasksFragment extends Fragment {

    private FragmentTasksBinding binding;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    private TaskAdapter adapter;
    private SharedPreferences sharedPreferences;
    private ListenerRegistration taskListener;

    public TasksFragment() {
        // Required empty public constructor
    }
    public static TasksFragment newInstance(String param1, String param2) {
        TasksFragment fragment = new TasksFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        sharedPreferences = getActivity().getSharedPreferences("ChoreMaster", Context.MODE_PRIVATE);
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        if (taskListener != null) {
             taskListener.remove();
        }
    }

    private void setupTaskListener() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String groupId = sharedPreferences.getString("activeGroupId", null);
        if (groupId != null) {
            final CollectionReference tasksRef = db.collection("groups").document(groupId).collection("tasks");
            taskListener = tasksRef.addSnapshotListener((queryDocumentSnapshots, e) -> {
                if (e != null) {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                List<Task> tasks = new ArrayList<>();
                if (queryDocumentSnapshots != null) {
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                        Task task = snapshot.toObject(Task.class);
                        if (task != null) {
                            tasks.add(task);
                        }
                    }
                }
                adapter.updateTasks(tasks);
            });
        }
    }

    private void createRecyclerViewAdapter() {
        adapter = new TaskAdapter(new ArrayList<>());
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTasksBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();

        adapter = new TaskAdapter(new ArrayList<Task>());
        binding.recyclerView.setAdapter(adapter);

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);

        createRecyclerViewAdapter();
        setupTaskListener();

        binding.btnAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CreateTaskActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }
}