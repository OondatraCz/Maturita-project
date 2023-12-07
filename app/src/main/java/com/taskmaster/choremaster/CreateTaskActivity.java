package com.taskmaster.choremaster;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import com.taskmaster.choremaster.databinding.ActivityCreateTaskBinding;

public class CreateTaskActivity extends AppCompatActivity {
    private TaskModel task;
    ActivityCreateTaskBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateTaskBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        task = new TaskModel();

        binding.btnDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dialog = new DatePickerDialog(CreateTaskActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        task.setYear(year);
                        task.setMonth(month);
                        task.setDay(day);
                        binding.btnDate.setText(year + " " + month + " " + day);
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
                        task.setHour(hour);
                        task.setMinute(minute);
                        binding.btnTime.setText(hour + ":" + minute);
                    }
                }, 12, 0, true);

                dialog.show();
            }
        });

        binding.btnSubmit.setOnClickListener(new View.OnClickListener() {
            String errText = "";
            @Override
            public void onClick(View v) {
                if(binding.edtName == null){
                    errText += "Nezadali jste jméno! ";
                }else{
                    task.setTitle(binding.edtName.getText().toString());
                }

                int radioID = binding.radioGroup.getCheckedRadioButtonId();
                if(radioID == binding.radioButton1.getId()){
                    task.setRepeatingMode("days");
                } else if (radioID == binding.radioButton2.getId()){
                    task.setRepeatingMode("weekly");
                } else {
                    task.setRepeatingMode("none");
                }
                task.setCategory("Obecná");
                task.setUserID(-1);
                task.setGroupID(-1);

                int cislo;
                try {
                    cislo = Integer.parseInt(binding.edtRepeateVal.getText().toString());
                    task.setRepeatingValue(cislo);
                }
                catch (Exception e){
                    errText += "Spatne cislo zadano";
                }


                String toastMassage = "jmeno: " + task.getTitle() + " Repeats: " + task.getRepeatingMode() + " Value: " + task.getRepeatingValue() + " dateTime: " + task.getDay() + "." + task.getMonth() + "." + task.getYear() + " | " + task.getHour() + ":" + task.getMinute() + " (" + task.getUserID() + " " + task.getGroupID() + " " + task.getCategory();
                Toast.makeText(CreateTaskActivity.this, toastMassage, Toast.LENGTH_LONG).show();

                //DatabaseHelper databaseHelper = new DatabaseHelper(CreateTaskActivity.this);
            }
        });
    }
}