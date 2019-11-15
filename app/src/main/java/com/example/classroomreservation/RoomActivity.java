package com.example.classroomreservation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RoomActivity extends AppCompatActivity {
    public static final String URI = "http://anbo-roomreservationv3.azurewebsites.net/api/rooms";


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getDataUsingOkHttpEnqueue();
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

    private void getDataUsingOkHttpEnqueue() {
        OkHttpClient client = new OkHttpClient();
        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(URI);
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
                            TextView messageView = findViewById(R.id.message_textview);
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
                        TextView messageView = findViewById(R.id.message_textview);
                        messageView.setText(ex.getMessage());
                    }
                });
            }
        });
    }

    private void populateList(String jsonString) {
        Gson gson = new GsonBuilder().create();
        Log.d("ROA", jsonString);
        final Room[] rooms = gson.fromJson(jsonString, Room[].class);
        ListView listView = findViewById(R.id.rooms_Listview);
        ArrayAdapter<Room> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, rooms);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(RoomActivity.this, "Room clicked: "+parent.getItemAtPosition(position), Toast.LENGTH_SHORT)
                        .show();
                Intent intent = new Intent(getBaseContext(), ReservationRoomActivity.class);
                Room room = (Room) parent.getItemAtPosition(position);
                intent.putExtra(ReservationRoomActivity.ROOM, room);
                startActivity(intent);
            }
        });
    }







}

