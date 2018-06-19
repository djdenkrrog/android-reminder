package com.example.djden.reminder;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    interface OnEnableBtnSave{
        void onEnableBtnSave();
    }

    public DatePickerFragment() {
        // Required empty public constructor
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();

        //Get current date
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    //DatePickerDialog.OnDateSetListener
    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "dd-MM-yyyy", Locale.getDefault());
        TextView date;

        switch (getActivity().getLocalClassName()) {
            case "ReminderAddActivity":
                date = (TextView) getActivity().findViewById(R.id.reminder_add_date);
                date.setText(dateFormat.format(calendar.getTime()));
                break;
            case "ReminderEditActivity":
                date = (TextView) getActivity().findViewById(R.id.reminder_edit_date);
                date.setText(dateFormat.format(calendar.getTime()));
                break;

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ((OnEnableBtnSave) getActivity()).onEnableBtnSave();

        Log.d("DATE_FRAGMENT", String.format("Fragment class name: %s destroyed", getActivity().getLocalClassName()));
    }
}