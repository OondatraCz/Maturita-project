package project.taskmaster.choremaster;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.checkerframework.checker.units.qual.C;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import project.taskmaster.choremaster.databinding.FragmentCalendarBinding;

public class CalendarFragment extends Fragment {
    private List<Task> allTasks = new ArrayList<>();
    private List<Calendar> lastCheckedDates = new ArrayList<>();
    private FragmentCalendarBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private SharedPreferences sharedPreferences;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat timeFormat;
    private Calendar todayCalender;
    private int daysToShow;
    private TaskAdapter adapter;
    private Calendar endDate;

    public CalendarFragment() {
    }
    public static CalendarFragment newInstance() {
        CalendarFragment fragment = new CalendarFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        sharedPreferences = getActivity().getSharedPreferences("ChoreMaster", Context.MODE_PRIVATE);
        dateFormat = new SimpleDateFormat("EEEE d.\nMMMM yyyy", Locale.getDefault());
        todayCalender = Calendar.getInstance();
        todayCalender.set(Calendar.HOUR_OF_DAY, 0);
        todayCalender.set(Calendar.MINUTE, 0);
        todayCalender.set(Calendar.SECOND, 0);
        todayCalender.set(Calendar.MILLISECOND, 0);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCalendarBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        String groupId = sharedPreferences.getString("activeGroupId", null);
        if(groupId == null){
            Toast.makeText(getActivity(), "You don't have any groups", Toast.LENGTH_SHORT).show();
            return view;
        }
        String userId = auth.getCurrentUser().getUid().toString();

        setDaysToShow();

        db.collection("groups").document(groupId).collection("tasks").whereEqualTo("assignedTo", userId).get()
                        .addOnCompleteListener(queryDocumentSnapshots -> {
                            if (queryDocumentSnapshots.isSuccessful() && queryDocumentSnapshots != null){
                                for (DocumentSnapshot snapshot : queryDocumentSnapshots.getResult()){
                                    Task task = snapshot.toObject(Task.class);
                                    task.setId(snapshot.getId());

                                    Date date = task.getDueDate().toDate();

                                    if(!task.getRepeatingMode().equals("none") && date.before(todayCalender.getTime())){
                                        Calendar nextDueDate = Calendar.getInstance();
                                        nextDueDate.setTime(task.getDueDate().toDate());
                                        if(task.getRepeatingMode().equals("weekly")){
                                            nextDueDate = getNextDueDate(todayCalender, task.getRepeatingMode(), task.getRepeatingValue(), false);
                                        } else {
                                            nextDueDate = getNextDueDate(nextDueDate, task.getRepeatingMode(), task.getRepeatingValue(), false);
                                        }

                                        nextDueDate.set(Calendar.HOUR_OF_DAY, date.getHours());
                                        nextDueDate.set(Calendar.MINUTE, date.getMinutes());
                                        updateTaskDueDate(task, nextDueDate);
                                        task.setDueDate(new Timestamp(nextDueDate.getTime()));
                                    }
                                    allTasks.add(task);
                                }
                                Collections.sort(allTasks, new Comparator<Task>() {
                                    @Override
                                    public int compare(Task t1, Task t2) {
                                        return t1.getDueDate().compareTo(t2.getDueDate());
                                    }
                                });
                                loadTasks();
                            }
                            else{
                                Toast.makeText(getActivity(), queryDocumentSnapshots.getException().getMessage().toString(), Toast.LENGTH_SHORT).show();
                            }
                        });

        binding.buttonLoadMore.setOnClickListener(v -> loadTasks());

        TaskAdapter.OnItemClickListener itemClickListener = new TaskAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Task task) {
                Intent intent = new Intent(getActivity(), TaskDetailActivity.class);
                intent.putExtra("taskId", task.getId());
                startActivity(intent);
            }
        };

        adapter = new TaskAdapter(new ArrayList<Task>(), itemClickListener, new ArrayList<String>());
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);

        return view;
    }

    private void loadTasks() {
        if(allTasks.isEmpty()){
            Toast.makeText(getActivity(), "There are no tasks in this group", Toast.LENGTH_SHORT).show();
            return;
        }

        daysToShow += 7;
        Toast.makeText(getActivity(), "Showing " + (int) Math.ceil((double) daysToShow / 7) + " weeks - " + daysToShow + " days ahead.", Toast.LENGTH_SHORT).show();

        endDate = (Calendar) todayCalender.clone();
        endDate.add(Calendar.DAY_OF_YEAR, daysToShow);

        endDate.set(Calendar.HOUR_OF_DAY, 23);
        endDate.set(Calendar.MINUTE, 59);
        endDate.set(Calendar.SECOND, 59);
        endDate.set(Calendar.MILLISECOND, 999);

        if(lastCheckedDates.size() != allTasks.size()){
            for (Task task : allTasks){
                Calendar date = Calendar.getInstance();
                date.setTime(task.getDueDate().toDate());
                lastCheckedDates.add(date);
            }
        }

        while(true) {
            Calendar smallestDate = Calendar.getInstance();
            smallestDate.setTime(lastCheckedDates.get(0).getTime());
            int index = 0;
            for (int i = 1; i < lastCheckedDates.size(); i++) {
                if (lastCheckedDates.get(i).before(smallestDate)) {
                    smallestDate.setTime(lastCheckedDates.get(i).getTime());
                    index = i;
                }
            }
            if(!smallestDate.after(endDate)){
                adapter.addTask(allTasks.get(index), dateFormat.format(lastCheckedDates.get(index).getTime()));
                if(allTasks.get(index).getRepeatingMode().equals("none")){
                    Calendar c = Calendar.getInstance();
                    Date d = new Date();
                    d.setYear(9999);
                    c.setTime(d);
                    lastCheckedDates.set(index, c);
                }
                else {
                    lastCheckedDates.set(index, getNextDueDate(smallestDate, allTasks.get(index).getRepeatingMode(), allTasks.get(index).getRepeatingValue(), true));
                }
            }
            else break;
        }
    }
    private Calendar getNextDueDate(Calendar dueDate, String repeatingMode, List<Integer> repeatingValue, boolean flag){

        dueDate.set(Calendar.SECOND, 0);
        dueDate.set(Calendar.MILLISECOND, 0);

        if(repeatingMode.equals("weekly")){
            Collections.sort(repeatingValue);
            int today = dueDate.get(Calendar.DAY_OF_WEEK);
            today -= 1;
            if(today == 0){
                today = 7;
            }
            int daysToAdd = 0;
            for (int day : repeatingValue){
                if(day > today){
                    daysToAdd = day - today;
                    break;
                }
            }
            if(daysToAdd == 0){
                daysToAdd = 7 - today + repeatingValue.get(0);
            }
            dueDate.add(Calendar.DAY_OF_YEAR, daysToAdd);
        } else if(repeatingMode.equals("days")){
            int days = repeatingValue.get(0);
            if (flag) {
                dueDate.add(Calendar.DAY_OF_YEAR, days);
            } else {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(todayCalender.getTime());
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                while (!dueDate.after(calendar)) {
                    dueDate.add(Calendar.DAY_OF_YEAR, days);
                }
            }
        }
        return dueDate;
    }

    private void updateTaskDueDate(Task task, Calendar nextDueDate) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String groupId = sharedPreferences.getString("activeGroupId", null);

        if (groupId != null && task.getId() != null) {
            Timestamp newDueDate = new Timestamp(nextDueDate.getTime());

            db.collection("groups").document(groupId)
                    .collection("tasks").document(task.getId())
                    .update("dueDate", newDueDate)
                    .addOnSuccessListener(aVoid -> Log.d("CalendarFragment", "Task due date successfully updated!"))
                    .addOnFailureListener(e -> Log.e("CalendarFragment", "Error updating task due date", e));
        }
    }

    private void setDaysToShow(){
        int today = todayCalender.get(Calendar.DAY_OF_WEEK);
        today -= 1;
        if(today == 0){
            today = 7;
        }
        daysToShow = -today;
    }
}