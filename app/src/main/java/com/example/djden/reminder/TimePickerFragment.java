package com.example.djden.reminder;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener, TimePickerDialog.OnCancelListener {

    interface OnEnableBtnSave {
        void onEnableBtnSave();
    }

    public TimePickerFragment() {
        // Required empty public constructor
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();

        c.add(Calendar.MINUTE, BuildConfig.DEBUG ? 1 : 6);

        // Get current Time
        int mHour = c.get(Calendar.HOUR_OF_DAY);
        int mMinute = c.get(Calendar.MINUTE);

        // Create a new instance of DatePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, mHour, mMinute, true);
    }

    // TimePickerDialog.OnTimeSetListener
    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        TextView time;
        switch (getActivity().getLocalClassName()) {
            case "ReminderAddActivity":
                time = (TextView) getActivity().findViewById(R.id.reminder_add_time);
                time.setText(String.format("%02d:%02d", hourOfDay, minute));
                break;
            case "ReminderEditActivity":
                time = (TextView) getActivity().findViewById(R.id.reminder_edit_time);
                time.setText(String.format("%02d:%02d", hourOfDay, minute));
                break;
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ((OnEnableBtnSave) getActivity()).onEnableBtnSave();

        Log.d("TIME_FRAGMENT", String.format("Fragment class name: %s destroyed", getActivity().getLocalClassName()));
    }
}