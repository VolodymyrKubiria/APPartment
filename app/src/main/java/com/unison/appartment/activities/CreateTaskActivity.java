package com.unison.appartment.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.unison.appartment.fragments.DatePickerFragment;
import com.unison.appartment.R;
import com.unison.appartment.model.Task;
import com.unison.appartment.utils.DateUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Locale;

/**
 * Classe che rappresenta l'Activity per creare un nuovo Task
 */
public class CreateTaskActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private final static String BUNDLE_KEY_DATE_STRING = "dateString";

    private Toolbar toolbar;
    private EditText inputName;
    private EditText inputDescription;
    private EditText inputPoints;
    private EditText inputDeadline;
    private String deadlineDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);

        // Supporto per la toolbar
        toolbar = findViewById(R.id.activity_create_task_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        inputName = findViewById(R.id.activity_create_task_input_name_value);
        inputDescription = findViewById(R.id.activity_create_task_input_description_value);
        inputPoints = findViewById(R.id.activity_create_task_input_points_value);

        // Viene mostrato il date picker
        inputDeadline = findViewById(R.id.activity_create_task_input_deadline_value);
        inputDeadline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        FloatingActionButton floatFinish = findViewById(R.id.activity_create_task_float_finish);
        floatFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createTask();
            }
        });

        /*
        Se lo schermo è stato ruotato mentre il date picker era aperto, l'activity è stata distrutta
        e ora sta venendo ricreata. Si vuole mantenere aperto lo stesso date picker, a cui però
        deve essere cambiato il listener dal momento che altrimenti farebbe riferimento all'activity
        distrutta non più esistente.
         */
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(DatePickerFragment.TAG_DATE_PICKER);
        if (fragment != null) {
            ((DatePickerFragment) fragment).setListener(this);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(BUNDLE_KEY_DATE_STRING, deadlineDate);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        deadlineDate = savedInstanceState.getString(BUNDLE_KEY_DATE_STRING);
    }

    public void createTask() {
        Task newTask = new Task(
                inputName.getText().toString(),
                inputDescription.getText().toString(),
                deadlineDate, // La data viene salvata in un formato indipendente dalla lingua utilizzata nel device
                Integer.valueOf(inputPoints.getText().toString())
        );
        Intent returnIntent = new Intent();
        returnIntent.putExtra("newTask", newTask);
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }

    public void showDatePickerDialog() {
        int year, month, day;
        final Calendar cal = Calendar.getInstance();

        // Se la data non è ancora stata selezionata, uso la data corrente come quella di default nel date picker
        // (non faccio niente perché Calendar.getInstance() mi restituisce già la data corrente)

        // Altrimenti, uso la data precedentemente selezionata come quella di default
        if (deadlineDate != null) {
            try {
                cal.setTime(DateUtils.parseDateWithStandardLocale(deadlineDate));
            }
            catch (ParseException e) {
                /*
                Questa eccezione non dovrebbe mai verificarsi in quanto il parsing viene eseguito
                con uno standard locale che è sempre uguale anche se cambia la lingua del device!
                 */
                Log.e(getClass().getCanonicalName(), e.getMessage());
            }
        }

        year = cal.get(Calendar.YEAR);
        month = cal.get(Calendar.MONTH);
        day = cal.get(Calendar.DAY_OF_MONTH);

        DatePickerFragment.newInstance(year, month, day, this).show(getSupportFragmentManager(), DatePickerFragment.TAG_DATE_PICKER);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, dayOfMonth);
        inputDeadline.setText(DateUtils.formatDateWithCurrentDefaultLocale(cal.getTime()));
        deadlineDate = DateUtils.formatDateWithStandardLocale(cal.getTime());
    }
}
