package project.taskmaster.choremaster;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


import project.taskmaster.choremaster.databinding.FragmentTasksBinding;

public class TasksFragment extends Fragment {
    private List<Task> allTasks = new ArrayList<>();
    private String groupId;
    private enum TaskFilter { ALL, MY_TASKS, OWNED_TASKS };
    private TaskFilter taskFilter = TaskFilter.ALL;
    private FragmentTasksBinding binding;
    private TaskAdapter adapter;
    private SharedPreferences sharedPreferences;
    private ListenerRegistration taskListener;
    private String currentUserId;

    public TasksFragment() {    }
    public static TasksFragment newInstance(String param1, String param2) {
        TasksFragment fragment = new TasksFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        if (groupId != null) {
            final CollectionReference tasksRef = db.collection("groups").document(groupId).collection("tasks");
            taskListener = tasksRef.addSnapshotListener((queryDocumentSnapshots, e) -> {
                if (e != null) {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (queryDocumentSnapshots != null) {
                    for (DocumentChange change : queryDocumentSnapshots.getDocumentChanges()) {
                        Task task = change.getDocument().toObject(Task.class);
                        task.setId(change.getDocument().getId());

                        switch (change.getType()) {
                            case ADDED:
                                allTasks.add(task);
                                break;
                            case MODIFIED:
                                for (int i = 0; i < allTasks.size(); i++) {
                                    if (allTasks.get(i).getId().equals(task.getId())) {
                                        allTasks.set(i, task);
                                        break;
                                    }
                                }
                                break;
                            case REMOVED:
                                for (int i = 0; i < allTasks.size(); i++) {
                                    if (allTasks.get(i).getId().equals(task.getId())) {
                                        allTasks.remove(i);
                                        break;
                                    }
                                }
                                break;
                        }
                    }
                    applyCurrentFilter();
                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTasksBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        groupId = sharedPreferences.getString("activeGroupId", null);
        if (groupId == null){
            Toast.makeText(getActivity(), "You don't have any groups", Toast.LENGTH_SHORT).show();
            return view;
        }

        TaskAdapter.OnItemClickListener itemClickListener = new TaskAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Task task) {
                Intent intent = new Intent(getActivity(), TaskDetailActivity.class);
                intent.putExtra("taskId", task.getId());
                startActivity(intent);
            }
        };

        binding.btnAllTasks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                taskFilter = TaskFilter.ALL;
                binding.btnOwnedTasks.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.defaultButtonColor));
                binding.btnMyTasks.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.defaultButtonColor));
                binding.btnAllTasks.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.selectedButtonColor));
                applyCurrentFilter();
            }
        });

        binding.btnMyTasks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                taskFilter = TaskFilter.MY_TASKS;
                binding.btnOwnedTasks.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.defaultButtonColor));
                binding.btnMyTasks.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.selectedButtonColor));
                binding.btnAllTasks.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.defaultButtonColor));
                applyCurrentFilter();
            }
        });

        binding.btnOwnedTasks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                taskFilter = TaskFilter.OWNED_TASKS;
                binding.btnOwnedTasks.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.selectedButtonColor));
                binding.btnMyTasks.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.defaultButtonColor));
                binding.btnAllTasks.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.defaultButtonColor));
                applyCurrentFilter();
            }
        });

        adapter = new TaskAdapter(new ArrayList<Task>(), itemClickListener, new ArrayList<String>());
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);
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

    private void applyCurrentFilter() {
        List<Task> filteredTasks = new ArrayList<>();
        List<String> customTexts = new ArrayList<>();

        switch (taskFilter) {
            case MY_TASKS:
                for (Task task : allTasks) {
                    if (currentUserId.equals(task.getAssignedTo())) {
                        filteredTasks.add(task);
                        customTexts.add(task.getCategory());
                        //customTexts.add(GetCustomText(task));
                    }
                }
                break;
            case OWNED_TASKS:
                for (Task task : allTasks) {
                    if (currentUserId.equals(task.getCreatedBy())) {
                        filteredTasks.add(task);
                        customTexts.add(task.getCategory());
                        //customTexts.add(GetCustomText(task));
                    }
                }
                break;
            default:
                for (Task task : allTasks) {
                    filteredTasks.add(task);
                    customTexts.add(task.getCategory());
                    //customTexts.add(GetCustomText(task));
                }
                break;
        }

        adapter.updateTasks(filteredTasks, customTexts);
    }

    private String GetCustomText(Task task) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        String today = sdf.format(new Date());
        String lastCompleted = sdf.format(task.getLastCompleted().get(task.getLastCompleted().size() - 1).toDate());
        String dueDate = sdf.format(task.getDueDate().toDate());

        if ((lastCompleted.equals(dueDate) && task.getRepeatingMode() == "none") || (lastCompleted.equals(today) && dueDate.equals(today))) {
            return "Done";
        } else if (dueDate.equals(today) && !lastCompleted.equals(today)){
            return "Due today";
        } else if (dueDate.compareTo(today) > 0 || lastCompleted.compareTo(today) == 1) {
            return "Upcoming";
        } else {
            return "Failed\nUpcoming";
        }
    }
}