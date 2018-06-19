package com.example.djden.reminder;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class RepeatDialog extends DialogFragment {

    private String currReminderRepeat;
    private String currReminderRepeatTxt;

    static final String[] listRepeats = new String[]{
            "HOUR",
            "DAY",
            "WEEK",
            "MONTH",
            "MONTH_LAST_DAY"
    };

    static final Integer[] listRepeatsRes = {
            R.string.repeat_hour,
            R.string.repeat_day,
            R.string.repeat_week,
            R.string.repeat_month,
            R.string.repeat_month_las_day
    };

    public RepeatDialog() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        // request a window without the title
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.repeat_dialog, container, false);

        final Button btSave = (Button) view.findViewById(R.id.repeat_dialog_btn_save);
        btSave.setEnabled(false);
        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() instanceof ReminderAddActivity) {
                    //System.out.println("Import currFileName: " + currFileName);
                    TextView tv = (TextView) getActivity().findViewById(R.id.reminder_add_repeat);
                    tv.setText(currReminderRepeat);

                    TextView tvTxt = (TextView) getActivity().findViewById(R.id.reminder_add_repeat_txt);
                    tvTxt.setText(currReminderRepeatTxt);
                } else if (getActivity() instanceof ReminderEditActivity) {
                    TextView tv = (TextView) getActivity().findViewById(R.id.reminder_edit_repeat);
                    tv.setText(currReminderRepeat);

                    TextView tvTxt = (TextView) getActivity().findViewById(R.id.reminder_edit_repeat_txt);
                    tvTxt.setText(currReminderRepeatTxt);
                }

                RepeatDialog.this.dismiss();
            }
        });

        final Button btCancel = (Button) view.findViewById(R.id.repeat_dialog_btn_cancel);
        btCancel.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View v) {
                RepeatDialog.this.dismiss();
            }
        });

        ListView lv = (ListView) view.findViewById(R.id.repeat_dialog_list);

        ArrayAdapter<String> repeatsAdapter = new ArrayAdapter<String>(
                view.getContext(),
                R.layout.item_repeat,
                R.id.repeat_name,
                new String[]{
                        getString(listRepeatsRes[0]),
                        getString(listRepeatsRes[1]),
                        getString(listRepeatsRes[2]),
                        getString(listRepeatsRes[3]),
                        getString(listRepeatsRes[4])
                }
        ) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                // Get the Item from ListView
                View view = super.getView(position, convertView, parent);

                return view;
            }
        };
        lv.setAdapter(repeatsAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                view.setSelected(true);
                currReminderRepeat = listRepeats[position];
                currReminderRepeatTxt = getString(listRepeatsRes[position]);
                btSave.setEnabled(true);
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }

}
