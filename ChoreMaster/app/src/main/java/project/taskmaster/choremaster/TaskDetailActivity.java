package project.taskmaster.choremaster;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioGroup;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import project.taskmaster.choremaster.databinding.ActivityTaskDetailBinding;

public class TaskDetailActivity extends AppCompatActivity {

    private ActivityTaskDetailBinding binding;
    private SharedPreferences sharedPreferences;
    private Calendar dueDateCalendar;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String selectedUser = null;
    private List<Integer> selectedDays;
    private List<String> userIds;
    private Task myTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTaskDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        sharedPreferences = getSharedPreferences("ChoreMaster", MODE_PRIVATE);

        ArrayList<String> categories = new ArrayList<>(Arrays.asList("Basic", "Home", "Work", "Personal", "Shopping"));
        String taskId = getIntent().getStringExtra("taskId");
        String groupId = sharedPreferences.getString("activeGroupId", null);
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCategory.setAdapter(categoryAdapter);
        dueDateCalendar = Calendar.getInstance();

        db.collection("groups").document(groupId).collection("tasks").document(taskId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    myTask = documentSnapshot.toObject(Task.class);
                    if (myTask != null) {

                        binding.edtName.setText(myTask.getTitle());
                        binding.edtDesc.setText(myTask.getDescription());
                        binding.spinnerCategory.setSelection(categories.indexOf(myTask.getCategory()));
                        binding.edtPoints.setText(String.valueOf(myTask.getPoints()));

                        binding.textViewTime.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(myTask.getDueDate().toDate()));
                        binding.textViewDate.setText(new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(myTask.getDueDate().toDate()));

                        fetchGroupMembersAndPopulateSpinner();

                        Log.d("a", myTask.toString());

                        switch (myTask.getRepeatingMode()){
                            case "days":
                                binding.radioGroup.check(binding.radioButton1.getId());
                                binding.edtRepeateVal.setText(myTask.getRepeatingValue().get(0).toString());
                                selectedDays = new ArrayList<>();
                                break;
                            case "weekly":
                                binding.radioGroup.check(binding.radioButton2.getId());
                                selectedDays = myTask.getRepeatingValue();
                                break;
                            case "none":
                                binding.radioGroup.check(binding.radioButton3.getId());
                                selectedDays = new ArrayList<>();
                                break;
                            default:
                                Toast.makeText(TaskDetailActivity.this, "smth weird is happening", Toast.LENGTH_SHORT).show();
                        }

                        setEditable(false);

                        if (isOwner(myTask)) {
                            binding.btnEdit.setVisibility(View.VISIBLE);
                            binding.btnDelete.setVisibility(View.VISIBLE);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading task", Toast.LENGTH_SHORT).show();
                });

        binding.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.collection("groups").document(groupId).collection("tasks").document(taskId).delete()
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(TaskDetailActivity.this, "Task deleted sucesfully", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> Toast.makeText(TaskDetailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });

        binding.btnTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog dialog = new TimePickerDialog(TaskDetailActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hour, int minute) {
                        dueDateCalendar.set(Calendar.HOUR_OF_DAY, hour);
                        dueDateCalendar.set(Calendar.MINUTE, minute);
                        binding.textViewTime.setText(dueDateCalendar.get(Calendar.HOUR_OF_DAY) + ":" + dueDateCalendar.get(Calendar.MINUTE));
                    }
                }, 12, 0, true);

                dialog.show();
            }
        });

        binding.btnDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dialog = new DatePickerDialog(TaskDetailActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        dueDateCalendar.set(Calendar.YEAR, year);
                        dueDateCalendar.set(Calendar.MONTH, month);
                        dueDateCalendar.set(Calendar.DAY_OF_MONTH, day);
                        binding.textViewDate.setText(dueDateCalendar.get(Calendar.DAY_OF_MONTH) + "." + dueDateCalendar.get(Calendar.MONTH) + "." + dueDateCalendar.get(Calendar.YEAR));
                    }
                }, dueDateCalendar.get(Calendar.YEAR), dueDateCalendar.get(Calendar.MONTH), dueDateCalendar.get(Calendar.DAY_OF_MONTH));

                dialog.show();
            }
        });

        binding.radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == binding.radioButton1.getId()){
                    binding.edtRepeateVal.setVisibility(View.VISIBLE);
                    binding.buttonGrid.setVisibility(View.GONE);
                } else if (checkedId == binding.radioButton2.getId()){
                    binding.edtRepeateVal.setVisibility(View.GONE);
                    binding.buttonGrid.setVisibility(View.VISIBLE);
                } else {
                    binding.edtRepeateVal.setVisibility(View.GONE);
                    binding.buttonGrid.setVisibility(View.GONE);
                }
            }
        });

        binding.btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isEditable = binding.edtName.isEnabled();
                setEditable(!isEditable);
            }
        });

        binding.btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> taskMap = new HashMap<>();
                if(binding.edtName.getText().toString().trim().isEmpty()){
                    Toast.makeText(TaskDetailActivity.this, "Nezadali jste jméno!", Toast.LENGTH_SHORT).show();
                    return;
                }

                long points;
                try {
                    points = Long.parseLong(binding.edtPoints.getText().toString().trim());
                }
                catch (Exception e){
                    Toast.makeText(TaskDetailActivity.this, "The points value must be a number", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (points < 1 || points > 100){
                    Toast.makeText(TaskDetailActivity.this, "Points can only have a value between 1 and 100", Toast.LENGTH_SHORT).show();
                    return;
                }

                Timestamp dueDateTimestamp = new Timestamp(dueDateCalendar.getTime());
                dueDateCalendar.add(Calendar.DAY_OF_MONTH, 1);
                Timestamp lastCompletedTimestamp = new Timestamp(dueDateCalendar.getTime());

                taskMap.put("title", binding.edtName.getText().toString());
                taskMap.put("description", binding.edtDesc.getText().toString());
                taskMap.put("category", binding.spinnerCategory.getSelectedItem().toString());
                taskMap.put("dueDate", dueDateTimestamp);
                taskMap.put("assignedTo", userIds.get(binding.userSpinner.getSelectedItemPosition()));
                taskMap.put("createdBy", auth.getCurrentUser().getUid());
                taskMap.put("lastCompleted", lastCompletedTimestamp);
                taskMap.put("points", points);

                int radioID = binding.radioGroup.getCheckedRadioButtonId();
                if(radioID == binding.radioButton1.getId()){
                    taskMap.put("repeatingMode", "days");
                    long numberOfDays;
                    try {
                        numberOfDays = Long.parseLong(binding.edtRepeateVal.getText().toString().trim());
                    }
                    catch (Exception e){
                        Toast.makeText(TaskDetailActivity.this, "The repeate after days value must be a number", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (numberOfDays < 1 || numberOfDays > 1000){
                        Toast.makeText(TaskDetailActivity.this, "Points can only have a value between 1 and 100", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    taskMap.put("repeatingValue", Arrays.asList(numberOfDays));
                } else if (radioID == binding.radioButton2.getId()){
                    taskMap.put("repeatingMode", "weekly");
                    taskMap.put("repeatingValue", selectedDays);
                } else {
                    taskMap.put("repeatingMode", "none");
                    taskMap.put("repeatingValue", new ArrayList<>());
                }

                db.collection("groups").document(groupId).collection("tasks").document(taskId)
                        .set(taskMap)
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(TaskDetailActivity.this, "Task Edited Successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(TaskDetailActivity.this, "Error adding task", Toast.LENGTH_SHORT).show();
                        });
            }
        });

        binding.btnComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
                String date = fmt.format(new Date());

                if(fmt.format(myTask.getLastCompleted().toDate()).equals(date) || !fmt.format(myTask.getDueDate().toDate()).equals(date)){
                    Toast.makeText(TaskDetailActivity.this, "You cannot mark the task as completed", Toast.LENGTH_SHORT).show();
                    return;
                }

                Map<String, Object> update = new HashMap<>();
                update.put("lastCompleted", new Timestamp(new Date()));

                final String assignedUserId = myTask.getAssignedTo();
                final DocumentReference groupRef = db.collection("groups").document(groupId);

                db.runTransaction(transaction -> {
                    DocumentSnapshot groupSnapshot = transaction.get(groupRef);
                    Map<String, Object> members = (Map<String, Object>) groupSnapshot.get("members");
                    if (members != null && members.containsKey(assignedUserId)) {
                        Map<String, Object> userDetails = (Map<String, Object>) members.get(assignedUserId);
                        long currentPoints = (long)userDetails.get("points");
                        long newPoints = currentPoints + myTask.getPoints();

                        userDetails.put("points", newPoints);
                        members.put(assignedUserId, userDetails);
                        transaction.update(groupRef, "members", members);
                    }

                    Map<String, Object> update1 = new HashMap<>();
                    update1.put("lastCompleted", new Timestamp(new Date()));
                    DocumentReference taskRef = db.collection("groups").document(groupId).collection("tasks").document(taskId);
                    transaction.update(taskRef, update1);

                    return null;
                }).addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        Toast.makeText(TaskDetailActivity.this, "Task marked as completed", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    else{
                        Toast.makeText(TaskDetailActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    public void onBtnDayClicked(View view){
        Button clickedButton = (Button)view;
        String day = clickedButton.getText().toString();
        int dayInt = 0;
        switch(day) {
            case "Mo":
                dayInt = 1;
                break;
            case "Tu":
                dayInt = 2;
                break;
            case "We":
                dayInt = 3;
                break;
            case "Th":
                dayInt = 4;
                break;
            case "Fr":
                dayInt = 5;
                break;
            case "Sa":
                dayInt = 6;
                break;
            case "Su":
                dayInt = 7;
                break;
        }
        if(selectedDays.contains(dayInt)){
            view.setBackgroundColor(ContextCompat.getColor(TaskDetailActivity.this, R.color.defaultButtonColor));
            selectedDays.remove(Integer.valueOf(dayInt));
        }
        else{
            view.setBackgroundColor(ContextCompat.getColor(TaskDetailActivity.this, R.color.selectedButtonColor));
            selectedDays.add(dayInt);
        }
    }
    private void setEditable(boolean isEditable) {
        binding.edtName.setEnabled(isEditable);
        binding.edtDesc.setEnabled(isEditable);
        binding.spinnerCategory.setEnabled(isEditable);
        binding.btnSubmit.setVisibility(isEditable ? View.VISIBLE : View.GONE);

        if (myTask.getAssignedTo().equals(auth.getCurrentUser().getUid())){
            binding.btnComplete.setVisibility(isEditable ? View.GONE : View.VISIBLE);
        }
        binding.btnTime.setVisibility(isEditable ? View.VISIBLE : View.INVISIBLE);
        binding.btnDate.setVisibility(isEditable ? View.VISIBLE : View.INVISIBLE);
        binding.radioGroup.setEnabled(isEditable);
        binding.userSpinner.setEnabled(isEditable);
        binding.edtPoints.setEnabled(isEditable);

        binding.radioButton1.setEnabled(isEditable);
        binding.radioButton2.setEnabled(isEditable);
        binding.radioButton3.setEnabled(isEditable);

        Map<Integer, Button> buttonMap = new HashMap<>();
        buttonMap.put(1, binding.button1);
        buttonMap.put(2, binding.button2);
        buttonMap.put(3, binding.button3);
        buttonMap.put(4, binding.button4);
        buttonMap.put(5, binding.button5);
        buttonMap.put(6, binding.button6);
        buttonMap.put(7, binding.button7);

        for (Map.Entry<Integer, Button> entry : buttonMap.entrySet()) {
            Button dayButton = entry.getValue();
            if (selectedDays.contains(entry.getKey())) {
                dayButton.setBackgroundColor(ContextCompat.getColor(TaskDetailActivity.this, R.color.selectedButtonColor));
            } else {
                dayButton.setBackgroundColor(ContextCompat.getColor(TaskDetailActivity.this, R.color.defaultButtonColor));
            }
            dayButton.setEnabled(isEditable);
        }

        binding.edtRepeateVal.setEnabled(isEditable);
    }

    private boolean isOwner(Task task) {
        FirebaseUser currentUser = auth.getCurrentUser();
        return currentUser != null && task.getCreatedBy().equals(currentUser.getUid());
    }

    private void fetchGroupMembersAndPopulateSpinner() {
        String groupId = sharedPreferences.getString("activeGroupId", null);
        if (groupId == null) {
            Toast.makeText(TaskDetailActivity.this, "No active group selected", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("groups").document(groupId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Map<String, Object> members = (Map<String, Object>) documentSnapshot.get("members");
                if (members != null) {
                    userIds = new ArrayList<>(members.keySet());
                    fetchUserDetails();
                } else {
                    Toast.makeText(TaskDetailActivity.this, "Members data is empty", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(TaskDetailActivity.this, "Group not found", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> Toast.makeText(TaskDetailActivity.this, "Error getting group members: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void fetchUserDetails() {
        Map<String, String> userIdToNameMap = new HashMap<>();
        List<String> userNames = new ArrayList<>();

        for (String userId : userIds) {
            db.collection("users").document(userId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String name = document.getString("username");
                        userIdToNameMap.put(userId, name);

                        if (userIdToNameMap.size() == userIds.size()) {
                            for (String id : userIds) {
                                userNames.add(userIdToNameMap.get(id));
                            }

                            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, userNames);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            binding.userSpinner.setAdapter(adapter);
                            binding.userSpinner.setSelection(userIds.indexOf(myTask.getAssignedTo()));
                        }
                    }
                } else {
                    Log.e("TaskDetailActivity", "Error getting user details", task.getException());
                }
            });
        }
    }

}
