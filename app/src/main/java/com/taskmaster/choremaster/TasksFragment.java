package com.taskmaster.choremaster;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import com.taskmaster.choremaster.TaskModel;
import com.taskmaster.choremaster.databinding.FragmentTasksBinding;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TasksFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TasksFragment extends Fragment {

    private FragmentTasksBinding binding;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public TasksFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TaskFragment.
     */
    // TODO: Rename and change types and number of parameters
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTasksBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();

        TaskModel[] tasks = new TaskModel[]{
                new TaskModel("tasks1", "category", "lorem ipsum dolor sit amet consectertuar adipiscing elit.", "A", 5, 2023, 12, 30, 14, 30),
                new TaskModel("tasks2", "category", "lorem ipsum dolor sit amet consectertuar adipiscing elit.", "A", 5, 2023, 12, 30, 14, 30),
                new TaskModel("tasks3", "category", "lorem ipsum dolor sit amet consectertuar adipiscing elit.", "A", 5, 2023, 12, 30, 14, 30),
                new TaskModel("tasks4", "category", "lorem ipsum dolor sit amet consectertuar adipiscing elit.", "A", 5, 2023, 12, 30, 14, 30)
        };


        

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