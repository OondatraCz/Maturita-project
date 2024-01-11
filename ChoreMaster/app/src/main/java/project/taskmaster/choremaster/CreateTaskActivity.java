package project.taskmaster.choremaster;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import project.taskmaster.choremaster.databinding.ActivityCreateTaskBinding;

public class CreateTaskActivity extends AppCompatActivity {
    private Task task;
    private Calendar dueDateCalendar;
    ActivityCreateTaskBinding binding;
    private FirebaseAuth auth;
    private SharedPreferences sharedPreferences;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateTaskBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sharedPreferences = getSharedPreferences("ChoreMaster", MODE_PRIVATE);
        auth = FirebaseAuth.getInstance();

        Calendar dueDateCalendar = Calendar.getInstance();
        task = new Task();

        binding.btnDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dialog = new DatePickerDialog(CreateTaskActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        dueDateCalendar.set(Calendar.YEAR, year);
                        dueDateCalendar.set(Calendar.MONTH, month);
                        dueDateCalendar.set(Calendar.DAY_OF_MONTH, day);
                        binding.btnDate.setText(dueDateCalendar.getTime().toString());
                    }
                }, 2023, 10, 25);

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
                        binding.btnTime.setText(dueDateCalendar.getTime().toString());
                    }
                }, 12, 0, true);

                dialog.show();
            }
        });

        binding.btnSubmit.setOnClickListener(new View.OnClickListener() {
            String errText = "";
            @Override
            public void onClick(View v) {
                Map<String, Object> taskMap = new HashMap<>();
                if(binding.edtName == null){
                    Toast.makeText(CreateTaskActivity.this, "Nezadali jste jméno!", Toast.LENGTH_SHORT).show();
                    return;
                }
                taskMap.put("title", binding.edtName.getText().toString());
                taskMap.put("description", binding.edtDescription.getText().toString());
                taskMap.put("category", "basic");

                Timestamp dueDateTimestamp = new Timestamp(dueDateCalendar.getTime());

                taskMap.put("dueDate", dueDateTimestamp);
                taskMap.put("assignedTo", auth.getCurrentUser().getUid());
                taskMap.put("createdBy", auth.getCurrentUser().getUid());


                int radioID = binding.radioGroup.getCheckedRadioButtonId();
                String repeatingMode;
                if(radioID == binding.radioButton1.getId()){
                    repeatingMode = "days";
                } else if (radioID == binding.radioButton2.getId()){
                    repeatingMode = "weekly";
                } else {
                    repeatingMode = "";
                }

                task.setRepeatingMode(binding.edtRepeateVal.getText().toString());

                Map<String, Object> repetitionMap = new HashMap<>();
                repetitionMap.put("repeatingMode", repeatingMode);
                repetitionMap.put("details", Arrays.asList(binding.edtRepeateVal.getText().toString()));
                
                taskMap.put("repetition", repetitionMap);
                taskMap.put("lastCompleted", dueDateTimestamp);


                //String toastMassage = "jmeno: " + task.getTitle() + " Repeats: " + task.getRepeatingMode() + " Value: " + task.getRepeatingValue() + " dateTime: " + task.getDay() + "." + task.getMonth() + "." + task.getYear() + " | " + task.getHour() + ":" + task.getMinute() + " (" + task.getUserID() + " " + task.getGroupID() + " " + task.getCategory();
                Toast.makeText(CreateTaskActivity.this, task.toString(), Toast.LENGTH_LONG).show();

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                String groupId = sharedPreferences.getString("activeGroupId", "err");
                Toast.makeText(CreateTaskActivity.this, groupId, Toast.LENGTH_SHORT).show();
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
}