package project.taskmaster.choremaster;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.checkerframework.checker.units.qual.C;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import project.taskmaster.choremaster.databinding.ActivityCreateTaskBinding;

public class CreateTaskActivity extends AppCompatActivity {
    private Calendar dueDateCalendar;
    ActivityCreateTaskBinding binding;
    private FirebaseAuth auth;
    private SharedPreferences sharedPreferences;
    private String selectedUser = null;
    private List<String> userIds;
    private List<Integer> selectedDays = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateTaskBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sharedPreferences = getSharedPreferences("ChoreMaster", MODE_PRIVATE);
        auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String groupId = sharedPreferences.getString("activeGroupId", null);
        String[] categories = {"Basic", "Home", "Work", "Personal", "Shopping"};
        String selectedCategory = "Basic";

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCategory.setAdapter(categoryAdapter);

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        UserAdapter adapter = new UserAdapter(this, new ArrayList<>());
        binding.recyclerView.setAdapter(adapter);
        binding.spinnerCategory.setSelection(0);

        fetchGroupMembersAndPopulateSpinner();

        adapter.setClickListener(new UserAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                adapter.removeUser(position);
            }
        });

        binding.spinnerMembers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedUser = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedUser = null;
            }
        });

        binding.btnAddUser.setOnClickListener(v -> {
            if (selectedUser != null && !selectedUser.isEmpty()) {
                adapter.addUser(selectedUser);
            } else {
                Toast.makeText(CreateTaskActivity.this, "No user selected", Toast.LENGTH_SHORT).show();
            }
        });

        Calendar dueDateCalendar = Calendar.getInstance();

        binding.textViewDate.setText(dueDateCalendar.get(Calendar.DAY_OF_MONTH) + "." + dueDateCalendar.get(Calendar.MONTH) + "." + dueDateCalendar.get(Calendar.YEAR));

        binding.btnDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dialog = new DatePickerDialog(CreateTaskActivity.this, new DatePickerDialog.OnDateSetListener() {
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

        binding.btnTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog dialog = new TimePickerDialog(CreateTaskActivity.this, new TimePickerDialog.OnTimeSetListener() {
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

        binding.btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> taskMap = new HashMap<>();
                if(binding.edtName.getText().toString().trim().isEmpty()){
                    Toast.makeText(CreateTaskActivity.this, "Nezadali jste jméno!", Toast.LENGTH_SHORT).show();
                    return;
                }

                long points;
                try {
                    points = Long.parseLong(binding.edtPoints.getText().toString().trim());
                }
                catch (Exception e){
                    Toast.makeText(CreateTaskActivity.this, "The points value must be a number", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (points < 1 || points > 100){
                    Toast.makeText(CreateTaskActivity.this, "Points can only have a value between 1 and 100", Toast.LENGTH_SHORT).show();
                    return;
                }

                Timestamp dueDateTimestamp = new Timestamp(dueDateCalendar.getTime());
                dueDateCalendar.add(Calendar.DAY_OF_MONTH, 1);
                Timestamp lastCompletedTimestamp = new Timestamp(dueDateCalendar.getTime());


                taskMap.put("title", binding.edtName.getText().toString());
                taskMap.put("description", binding.edtDescription.getText().toString());
                taskMap.put("category", binding.spinnerCategory.getSelectedItem().toString());
                taskMap.put("dueDate", dueDateTimestamp);
                taskMap.put("assignedTo", userIds.get(binding.spinnerMembers.getSelectedItemPosition()));
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
                        Toast.makeText(CreateTaskActivity.this, "The repeate after days value must be a number", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (numberOfDays < 1 || numberOfDays > 1000){
                        Toast.makeText(CreateTaskActivity.this, "Points can only have a value between 1 and 100", Toast.LENGTH_SHORT).show();
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

                db.collection("groups").document(groupId).collection("tasks")
                        .add(taskMap)
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(CreateTaskActivity.this, "Task Added Successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(CreateTaskActivity.this, "Error adding task", Toast.LENGTH_SHORT).show();
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
            view.setBackgroundColor(ContextCompat.getColor(CreateTaskActivity.this, R.color.defaultButtonColor));
            selectedDays.remove(Integer.valueOf(dayInt));
        }
        else{
            view.setBackgroundColor(ContextCompat.getColor(CreateTaskActivity.this, R.color.selectedButtonColor));
            selectedDays.add(dayInt);
        }

    }

    private void fetchGroupMembersAndPopulateSpinner() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String groupId = sharedPreferences.getString("activeGroupId", null);
        if (groupId == null) {
            Toast.makeText(CreateTaskActivity.this, "No active group selected", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("groups").document(groupId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Map<String, Object> members = (Map<String, Object>) document.get("members");
                    if (members != null) {
                        userIds = new ArrayList<>(members.keySet());
                        fetchUserDetails();
                    }
                } else {
                    Toast.makeText(CreateTaskActivity.this, "Group not found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(CreateTaskActivity.this, "Error getting group members", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchUserDetails() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<String> userNames = new ArrayList<>();
        for (String userId : userIds) {
            db.collection("users").document(userId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String name = document.getString("username");
                        userNames.add(name);

                        if (userNames.size() == userIds.size()) {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, userNames);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            binding.spinnerMembers.setAdapter(adapter);
                        }
                    }
                }
            });
        }
    }


}