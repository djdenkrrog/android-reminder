package com.example.djden.reminder;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class SecondFragment extends Fragment {

    private final static String LOG_TAG = "SecondFragment";

    public static final int REQUEST_EDIT_REM = 1;

    SQLiteDatabase db; //Открытие происходит в refreshReminders
    DatabaseHelper dbHelper;

    Cursor remindersCursor;
    SimpleCursorAdapter remindersAdapter;

    ListView remList;

    public SecondFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbHelper = new DatabaseHelper(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_second, container, false);

        remList = (ListView) view.findViewById(R.id.reminder_inactive_list);
        registerForContextMenu(remList);
        remList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Log.d(LOG_TAG, String.format("onItemClick: %s", id));
                Intent intent = new Intent(getContext(), ReminderShowActivity.class);
                intent.putExtra("reminderID", String.valueOf(id));
                // запуск activity
                startActivity(intent);
            }
        });
        refreshReminders();
        return view;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();

        if (v.getId() == R.id.reminder_inactive_list) {
            menu.setHeaderTitle(R.string.menu);
            inflater.inflate(R.menu.inactive_context, menu);
        }

        Log.d(LOG_TAG, String.format("onCreateContextMenu id: %s", v.getId()));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.inactive_menu_delete: //Delete
                deleteReminder(info.id);
                break;
            case R.id.inactive_menu_delete_all: //Delete all
                deleteReminderAll();
                break;
        }
        return true;
    }

    protected void deleteReminder(long id) {
        db.delete(DatabaseHelper.TABLE_REMINDERS, "_id=?", new String[]{Long.toString(id)});
        refreshReminders();
    }

    protected void deleteReminderAll() {
        db.delete(DatabaseHelper.TABLE_REMINDERS, "is_active=?;", new String[]{"F"});
        refreshReminders();
    }

    protected void refreshReminders() {
        // открываем подключение
        db = dbHelper.getReadableDatabase();
        //получаем данные из бд в виде курсора
        String sql = String.format(
                "select * from v_%s where is_active='F' order by d_start desc;",
                DatabaseHelper.TABLE_REMINDERS
        );
        try {
            remindersCursor = db.rawQuery(sql, null);
        } catch (SQLiteException ex) {
            Toast.makeText(getContext(), R.string.db_bad, Toast.LENGTH_LONG).show();
            if (remindersCursor != null) {
                remindersCursor.close();
            }
            return;
        }

        // определяем, какие столбцы из курсора будут выводиться в ListView
        String[] headers = new String[]{
                DatabaseHelper.REM_COLUMN_ID,
                DatabaseHelper.REM_COLUMN_MSG,
                DatabaseHelper.REM_COLUMN_D_START_FRMT,
                DatabaseHelper.REM_COLUMN_D_START,
                DatabaseHelper.REM_COLUMN_IS_ACTIVE,
                DatabaseHelper.REM_COLUMN_REPEAT
        };

        // создаем адаптер, передаем в него курсор
        remindersAdapter = new SimpleCursorAdapter(
                getContext(),
                R.layout.item_reminder,
                remindersCursor,
                headers,
                new int[]{R.id.reminder_id, R.id.reminder_msg, R.id.reminder_d_start},
                0
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                // Get the Item from ListView
                View view = super.getView(position, convertView, parent);

                ImageView reminderIcon = (ImageView) view.findViewById(R.id.reminder_icon_is_active);
                ImageView reminderIsSound = (ImageView) view.findViewById(R.id.reminder_is_sound);
                Cursor reminderItem = (Cursor) remindersAdapter.getItem(position);
                String iconRes = reminderItem.getString(reminderItem.getColumnIndex(DatabaseHelper.REM_COLUMN_IS_ACTIVE));
                String isSound = reminderItem.getString(reminderItem.getColumnIndex(DatabaseHelper.REM_COLUMN_IS_SOUND));

                TextView tvMsg = (TextView) view.findViewById(R.id.reminder_msg);
                TextView tvDateStart = (TextView) view.findViewById(R.id.reminder_d_start);
                tvMsg.setPaintFlags(tvMsg.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvDateStart.setPaintFlags(tvDateStart.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

                LinearLayout llIcon = (LinearLayout) view.findViewById(R.id.reminder_liner_layout_icon);
                llIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        changeIsActiveReminder(v);
                    }
                });

                reminderIcon.setImageResource(
                        iconRes.equalsIgnoreCase("T") ? R.mipmap.checkbox_off : R.mipmap.checkbox_on
                );

                reminderIsSound.setImageResource(
                        isSound.equalsIgnoreCase("T") ? R.drawable.sound_on : R.drawable.sound_off
                );

                LinearLayout linerLayout = (LinearLayout) view.findViewById(R.id.item_reminder_linear_layout);
                if (position % 2 != 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        linerLayout.setBackgroundColor(getResources().getColor(R.color.stripeRows, null));
                    } else {
                        linerLayout.setBackgroundColor(getResources().getColor(R.color.stripeRows));
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        linerLayout.setBackgroundColor(getResources().getColor(R.color.default_color, null));
                    } else {
                        linerLayout.setBackgroundColor(getResources().getColor(R.color.default_color));
                    }
                }

                return view;
            }

        };

        remList.setAdapter(remindersAdapter);
    }

    protected void changeIsActiveReminder(View v) {
        TextView tvMsg = (TextView) v.findViewById(R.id.reminder_id);
        String id = tvMsg.getText().toString();
        // действия, совершаемые после нажатия на кнопку
        // Создаем объект Intent для вызова новой Activity
        Intent intent = new Intent(getContext(), ReminderEditActivity.class);
        intent.putExtra("reminderID", id);
        // запуск activity
        startActivityForResult(intent, REQUEST_EDIT_REM);
        Log.d(LOG_TAG, String.format("id: %s", id));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_EDIT_REM && resultCode == getActivity().RESULT_OK) {
            refreshReminders();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (db != null) {
            db.close();
        }
        if (remindersCursor != null) {
            remindersCursor.close();
        }
    }
}
