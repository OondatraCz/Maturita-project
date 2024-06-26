package project.taskmaster.choremaster;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.checkerframework.checker.units.qual.C;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
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
    String userRole = "member";
    String groupId;
    String taskId;
    private Calendar todayCalender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTaskDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        todayCalender = Calendar.getInstance();

        sharedPreferences = getSharedPreferences("ChoreMaster", MODE_PRIVATE);

        ArrayList<String> categories = new ArrayList<>(Arrays.asList("Basic", "Home", "Work", "Personal", "Shopping"));
        taskId = getIntent().getStringExtra("taskId");
        groupId = sharedPreferences.getString("activeGroupId", null);
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCategory.setAdapter(categoryAdapter);
        dueDateCalendar = Calendar.getInstance();

        db.collection("groups").document(groupId).collection("tasks").document(taskId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    myTask = documentSnapshot.toObject(Task.class);
                    myTask.setId(documentSnapshot.getId());
                    if (myTask != null) {

                        db.collection("groups").document(groupId)
                                .get()
                                .addOnSuccessListener(snapshot -> {
                                    if (snapshot.exists()) {
                                        Map<String, Object> members = (Map<String, Object>) snapshot.get("members");
                                        if (members != null && members.containsKey(auth.getCurrentUser().getUid())) {
                                            Map<String, String> userDetails = (Map<String, String>) members.get(auth.getCurrentUser().getUid());
                                            userRole = userDetails.get("role");
                                            setButtonVisibility(userRole, myTask);
                                        }
                                        else{
                                            String storedString = sharedPreferences.getString("allGroupIds", "");
                                            List<String> allGroupIds = new ArrayList<>(Arrays.asList(storedString.split(",")));
                                            storedString = sharedPreferences.getString("allGroupNames", "");
                                            List<String> allGroupNames = new ArrayList<>(Arrays.asList(storedString.split(",")));
                                            allGroupNames.remove(allGroupIds.indexOf(groupId));
                                            sharedPreferences.edit().putString("allGroupNames", TextUtils.join(",", allGroupNames)).apply();
                                            sharedPreferences.edit().putString("allGroupIds", TextUtils.join(",", allGroupNames)).apply();

                                            if (allGroupIds.size() == 0){
                                                sharedPreferences.edit().remove("activeGroupId");

                                                Intent intent = new Intent(getApplicationContext(), SetUsernameActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);
                                            }
                                            else{
                                                sharedPreferences.edit().putString("activeGroupId", allGroupIds.get(0)).commit();
                                            }
                                        }
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("TAG", "Error getting group details: ", e);
                                });

                        Date date = myTask.getDueDate().toDate();

                        if(myTask.getRepeatingMode() != "none" && date.before(todayCalender.getTime())){
                            Calendar nextDueDate = Calendar.getInstance();
                            nextDueDate.setTime(myTask.getDueDate().toDate());
                            if(myTask.getRepeatingMode().equals("weekly")){
                                nextDueDate = getNextDueDate(todayCalender, myTask.getRepeatingMode(), myTask.getRepeatingValue());
                            } else {
                                nextDueDate = getNextDueDate(nextDueDate, myTask.getRepeatingMode(), myTask.getRepeatingValue());
                            }
                            nextDueDate.set(Calendar.HOUR_OF_DAY, date.getHours());
                            nextDueDate.set(Calendar.MINUTE, date.getMinutes());
                            updateTaskDueDate(myTask, nextDueDate);
                            myTask.setDueDate(new Timestamp(nextDueDate.getTime()));
                        }

                        binding.edtName.setText(myTask.getTitle());
                        binding.edtDesc.setText(myTask.getDescription());
                        binding.spinnerCategory.setSelection(categories.indexOf(myTask.getCategory()));
                        binding.edtPoints.setText(String.valueOf(myTask.getPoints()));

                        binding.textViewTime.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(myTask.getDueDate().toDate()));
                        binding.textViewDate.setText(new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(myTask.getDueDate().toDate()));

                        fetchGroupMembersAndPopulateSpinner();

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
                                Toast.makeText(TaskDetailActivity.this, "easter egg - you broke the matrix", Toast.LENGTH_SHORT).show();
                        }

                        if (myTask.getLastCompleted() != null && !myTask.getLastCompleted().isEmpty()) {
                            for (Timestamp timestamp : myTask.getLastCompleted()) {
                                TextView textView = new TextView(TaskDetailActivity.this);
                                String formattedDate = new SimpleDateFormat("EEEE d.M yyyy", Locale.getDefault()).format(timestamp.toDate());
                                textView.setText(formattedDate);
                                textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                binding.linearLayout.addView(textView);
                            }
                            binding.txtLastCompleted.setText(new SimpleDateFormat("EEEE d.M yyyy", Locale.getDefault()).format(myTask.getLastCompleted().get(myTask.getLastCompleted().size() - 1).toDate()));
                            binding.txtLastCompleted.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                        }

                        binding.linearLayout.setVisibility(View.INVISIBLE);

                        setEditable(false);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading task", Toast.LENGTH_SHORT).show();
                });

        binding.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(TaskDetailActivity.this)
                        .setTitle("Delete Task")
                        .setMessage("Are you sure you want to delete this task?")
                        .setPositiveButton("Delete", (dialog, which) -> deleteTask())
                        .setNegativeButton("Cancel", null)
                        .show();
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
                        binding.textViewTime.setText(new SimpleDateFormat("hh:mm", Locale.getDefault()).format(dueDateCalendar.getTime()));
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
                        binding.textViewDate.setText(new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(dueDateCalendar.getTime()));
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

                Calendar todayCalendar = Calendar.getInstance();
                todayCalendar.set(Calendar.HOUR_OF_DAY, 0);
                todayCalendar.set(Calendar.MINUTE, 0);
                todayCalendar.set(Calendar.SECOND, 0);
                todayCalendar.set(Calendar.MILLISECOND, 0);
                if (dueDateCalendar.before(todayCalendar)) {
                    Toast.makeText(TaskDetailActivity.this, "The due date can not be in the past!", Toast.LENGTH_SHORT).show();
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

                taskMap.put("title", binding.edtName.getText().toString());
                taskMap.put("description", binding.edtDesc.getText().toString());
                taskMap.put("category", binding.spinnerCategory.getSelectedItem().toString());
                taskMap.put("dueDate", dueDateTimestamp);
                taskMap.put("assignedTo", userIds.get(binding.userSpinner.getSelectedItemPosition()));
                taskMap.put("createdBy", auth.getCurrentUser().getUid());
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

                if(myTask.getLastCompleted() != null && !myTask.getLastCompleted().isEmpty() && fmt.format(myTask.getLastCompleted().get(myTask.getLastCompleted().size() - 1).toDate()).equals(date))
                {
                    Toast.makeText(TaskDetailActivity.this, "You cannot mark the task as completed", Toast.LENGTH_SHORT).show();
                    return;
                }

                Map<String, Object> updateMap = new HashMap<>();
                Calendar currentDate = Calendar.getInstance();
                currentDate.set(Calendar.MILLISECOND, 0);
                currentDate.set(Calendar.SECOND, 0);
                myTask.addLastCompleted(new Timestamp(currentDate.getTime()));
                updateMap.put("lastCompleted", myTask.getLastCompleted());

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

                    DocumentReference taskRef = db.collection("groups").document(groupId).collection("tasks").document(taskId);
                    transaction.update(taskRef, updateMap);

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

        binding.txtShowMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myTask.getLastCompleted().isEmpty()){
                    Toast.makeText(TaskDetailActivity.this, "The task hasn't been completed yet", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (binding.linearLayout.getVisibility() == View.INVISIBLE) {
                    binding.linearLayout.setVisibility(View.VISIBLE);
                    binding.txtShowMore.setText("Show less ▲");

                    ConstraintSet constraintSet = new ConstraintSet();
                    constraintSet.clone(binding.constraintLayout);
                    constraintSet.connect(binding.btnSubmit.getId(), ConstraintSet.TOP, binding.linearLayout.getId(), ConstraintSet.BOTTOM, 16 * (int)getResources().getDisplayMetrics().density);
                    constraintSet.connect(binding.btnComplete.getId(), ConstraintSet.TOP, binding.linearLayout.getId(), ConstraintSet.BOTTOM, 16 * (int)getResources().getDisplayMetrics().density);
                    constraintSet.applyTo(binding.constraintLayout);
                } else {
                    binding.linearLayout.setVisibility(View.INVISIBLE);
                    binding.txtShowMore.setText("Show more ▼");

                    ConstraintSet constraintSet = new ConstraintSet();
                    constraintSet.clone(binding.constraintLayout);
                    constraintSet.connect(binding.btnSubmit.getId(), ConstraintSet.TOP, binding.textView10.getId(), ConstraintSet.BOTTOM, 24 * (int)getResources().getDisplayMetrics().density);
                    constraintSet.connect(binding.btnComplete.getId(), ConstraintSet.TOP, binding.textView10.getId(), ConstraintSet.BOTTOM, 24 * (int)getResources().getDisplayMetrics().density);
                    constraintSet.applyTo(binding.constraintLayout);
                }
            }
        });
    }
    private void deleteTask(){
        db.collection("groups").document(groupId).collection("tasks").document(taskId).delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(TaskDetailActivity.this, "Task deleted successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(TaskDetailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
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

    private void setButtonVisibility(String userRole, Task task) {
        boolean isOwner = task.getCreatedBy().equals(auth.getCurrentUser().getUid());
        if (isOwner || "admin".equals(userRole)) {
            binding.btnEdit.setVisibility(View.VISIBLE);
            binding.btnDelete.setVisibility(View.VISIBLE);
        } else {
            binding.btnEdit.setVisibility(View.GONE);
            binding.btnDelete.setVisibility(View.GONE);
        }
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
    private Calendar getNextDueDate(Calendar dueDate, String repeatingMode, List<Integer> repeatingValue){

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
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(todayCalender.getTime());
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            while (!dueDate.after(calendar)) {
                dueDate.add(Calendar.DAY_OF_YEAR, days);
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
}
