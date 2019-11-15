package com.example.classroomreservation;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class ReservationRoomActivity extends AppCompatActivity {
    public static final String URI = "http://anbo-roomreservationv3.azurewebsites.net/api/reservations/room/";
    public static final String ROOM = "ROOM";
    private Room room;
    private boolean delete = false;
    private FloatingActionButton delfab;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.login_item:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                return true; // true: menu processing done, no further actions
            case R.id.room_item:
                Intent intentRoom = new Intent(this, RoomActivity.class);
                startActivity(intentRoom);
                return true; // true: menu processing done, no further actions
            case R.id.add_item:
                Intent intentAdd = new Intent(this, AddReservationActivity.class);
                startActivity(intentAdd);
                return true; // true: menu processing done, no further actions
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation_room);

        Intent intent = getIntent();
        room = (Room) intent.getSerializableExtra(ROOM);

        //FirebaseAuth.getInstance().getCurrentUser() != null)
        //deleteFab(null);

        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.addFab);
        delfab = (FloatingActionButton)findViewById(R.id.deleteFab);

        TextView roomLabelTxtView = findViewById(R.id.roomName);
        roomLabelTxtView.setText(room.toString());
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getDataUsingOkHttpEnqueue();
    }

    public void addReservation(View view) {
        Intent intent = new Intent(this, AddReservationActivity.class);
        intent.putExtra(AddReservationActivity.ROOMID, room.getId());
        startActivity(intent);
    }

    public void setDelete(View v) {
        delete = !delete;
        if (delete) {
            delfab.setImageDrawable(ContextCompat.getDrawable(this, android.R.drawable.ic_menu_delete));
        }
        else {
            delfab.setImageDrawable(ContextCompat.getDrawable(this, android.R.drawable.ic_delete));
        }
    }

    private void populateList(String jsonString) {
        Gson gson = new GsonBuilder().create();
        Log.d("TEE", jsonString);
        Reservation[] tempReservations = gson.fromJson(jsonString, Reservation[].class);
        final ArrayList<Reservation> reservations = new ArrayList<Reservation>(Arrays.asList(tempReservations));
        Collections.sort(reservations, new Comparator<Reservation>() {
            public int compare(Reservation o1, Reservation o2) {
                return o1.compareTo(o2);
            }
        });
        ListView listView = findViewById(R.id.listViewRoomRes);
        final ArrayAdapter<Reservation> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, reservations);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(ReservationRoomActivity.this, "Reservation clicked: " + parent.getItemAtPosition(position), Toast.LENGTH_SHORT).show();
                Reservation clickedRes = (Reservation) parent.getItemAtPosition(position);
                if (delete) {
                    Toast.makeText(ReservationRoomActivity.this, "Checking UID", Toast.LENGTH_SHORT).show();
                    Toast.makeText(ReservationRoomActivity.this, clickedRes.getUserId() + " : " + FirebaseAuth.getInstance().getCurrentUser().getUid(), Toast.LENGTH_SHORT).show();
                    if (clickedRes.getUserId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                        Toast.makeText(ReservationRoomActivity.this, "UID matched", Toast.LENGTH_SHORT).show();
                        deleteReservation(clickedRes);
                        reservations.remove(position);
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }

    private void getDataUsingOkHttpEnqueue() {
        OkHttpClient client = new OkHttpClient();
        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(URI + room.getId());
        Request request = requestBuilder.build();
        okhttp3.Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull final Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String jsonString = response.body().string();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            populateList(jsonString);
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView messageView = findViewById(R.id.main_message_textview);
                            messageView.setText(URI + "\n" + response.code() + " " + response.message());
                        }
                    });
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull final IOException ex) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView messageView = findViewById(R.id.main_message_textview);
                        messageView.setText(ex.getMessage());
                    }
                });
            }
        });
    }

    public void deleteReservation(Reservation reservation) {
        final String url = "http://anbo-roomreservationv3.azurewebsites.net/api/reservations/" + reservation.getId();
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).delete().build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull final IOException ex) {
                Toast.makeText(ReservationRoomActivity.this, "onFailure", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull final Response response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (response.isSuccessful()) {
                            Toast.makeText(ReservationRoomActivity.this, "Reservation deleted", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(ReservationRoomActivity.this, "Deletion failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });
    }

}