package com.example.timereggclock;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private TimerAdapter timerAdapter;
    private List<TimerItem> timerItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView timersListView = findViewById(R.id.timersListView);
        timerItems = new ArrayList<>();
        timerAdapter = new TimerAdapter();
        timersListView.setAdapter(timerAdapter);

        Button addTimerButton = findViewById(R.id.addTimerButton);
        addTimerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddTimerDialog();
            }
        });
    }

    private void showAddTimerDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.add_timer_dialog, null);

        final EditText timerNameEditText = dialogView.findViewById(R.id.editTimerName);
        final EditText hoursEditText = dialogView.findViewById(R.id.editHours);
        final EditText minutesEditText = dialogView.findViewById(R.id.editMinutes);
        final EditText secondsEditText = dialogView.findViewById(R.id.editSeconds);

        hoursEditText.setText("00");
        minutesEditText.setText("00");
        secondsEditText.setText("00");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Timer");
        builder.setView(dialogView);
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String timerName = timerNameEditText.getText().toString().trim();
                if (TextUtils.isEmpty(timerName)) {
                    timerName = "Timer";
                }
                int hours = Integer.parseInt(hoursEditText.getText().toString());
                int minutes = Integer.parseInt(minutesEditText.getText().toString());
                int seconds = Integer.parseInt(secondsEditText.getText().toString());
                long durationInMillis = TimeUnit.HOURS.toMillis(hours) + TimeUnit.MINUTES.toMillis(minutes) + TimeUnit.SECONDS.toMillis(seconds);

                TimerItem newTimer = new TimerItem(timerName, durationInMillis, new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                timerAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                });

                timerItems.add(newTimer);
                timerAdapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    private class TimerAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return timerItems.size();
        }

        @Override
        public TimerItem getItem(int position) {
            return timerItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(MainActivity.this).inflate(R.layout.timer_item, parent, false);
                holder = new ViewHolder();
                holder.titleTextView = convertView.findViewById(R.id.titleTextView);
                holder.timeTextView = convertView.findViewById(R.id.timeTextView);
                holder.startButton = convertView.findViewById(R.id.startButton);
                holder.stopButton = convertView.findViewById(R.id.stopButton);
                holder.deleteButton = convertView.findViewById(R.id.deleteButton);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final TimerItem timerItem = getItem(position);

            holder.titleTextView.setText(timerItem.getTitle());
            holder.timeTextView.setText(timerItem.getFormattedTimeRemaining());

            holder.startButton.setOnClickListener(v -> {
                if (!timerItem.isRunning()) {
                    timerItem.startTimer();
                    holder.startButton.setText("Pause");
                } else {
                    timerItem.pauseTimer();
                    holder.startButton.setText("Start");
                }
            });

            holder.stopButton.setOnClickListener(v -> {
                timerItem.resetTimer();
                holder.timeTextView.setText(timerItem.getFormattedTimeRemaining());
                holder.startButton.setText("Start");
            });

            holder.deleteButton.setOnClickListener(v -> {
                timerItem.shutDownExecutor();
                timerItems.remove(position);
                notifyDataSetChanged();
            });

            return convertView;
        }

        private class ViewHolder {
            TextView titleTextView;
            TextView timeTextView;
            Button startButton;
            Button stopButton;
            Button deleteButton;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (TimerItem timerItem : timerItems) {
            timerItem.shutDownExecutor();
        }
    }
}
