 package com.example.classroomreservation;

 import androidx.appcompat.app.AppCompatActivity;
 import androidx.appcompat.widget.Toolbar;

 import android.app.DatePickerDialog;
 import android.app.TimePickerDialog;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.TimePicker;

 import com.google.firebase.auth.FirebaseAuth;

 import org.json.JSONException;
 import org.json.JSONObject;

 import java.io.IOException;
 import java.sql.Time;
 import java.text.DateFormat;
 import java.util.Calendar;

 import okhttp3.MediaType;
 import okhttp3.OkHttpClient;
 import okhttp3.Request;
 import okhttp3.RequestBody;
 import okhttp3.Response;

 public class AddReservationActivity extends AppCompatActivity {
     private Calendar meetingStart = Calendar.getInstance();
     private Calendar meetingEnd = Calendar.getInstance();
     private Button fromdateButton;
     private Button fromtimeButton;

     private Button todateButton;
     private Button totimeButton;

     public static final String ROOMID = "ROOMID";

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
         setContentView(R.layout.activity_add_reservation);

         fromdateButton = findViewById(R.id.add_fromTime_DateButton);
         fromtimeButton = findViewById(R.id.add_fromTime_Timebutton);

         todateButton  = findViewById(R.id.add_toTime_DateButton);
         totimeButton  = findViewById(R.id.add_toTime_Timebutton);

         Toolbar toolbar = findViewById(R.id.toolbar);
         setSupportActionBar(toolbar);
     }



     public void addReservation(View view) {
         Intent intent = getIntent();
         int fromstart = (int) (meetingStart.getTimeInMillis()/1000);
         int tostart = (int) (meetingEnd.getTimeInMillis()/1000);
         String purpose = ((EditText) findViewById(R.id.add_reservation_purpose)).getText().toString();
         int roomid = intent.getIntExtra(ROOMID, -1);
         String userId = FirebaseAuth.getInstance().getUid();
         Log.d("TEE", purpose);
         TextView messageView = findViewById(R.id.add_reservation_message);
         try {
             JSONObject jsonObject = new JSONObject();
             jsonObject.put("fromTime", fromstart);
             jsonObject.put("toTime", tostart);
             jsonObject.put("userId", userId);
             jsonObject.put("purpose", purpose);
             jsonObject.put("roomId", roomid);
             String jsonDocument = jsonObject.toString();
             messageView.setText(jsonDocument);
             PostBookOkHttpTask task = new PostBookOkHttpTask();
             task.execute("http://anbo-roomreservationv3.azurewebsites.net/api/Reservations", jsonDocument);
         } catch (JSONException ex) {
             messageView.setText(ex.getMessage());
         }

     }

     private class PostBookOkHttpTask extends AsyncTask<String, Void, String> {
         @Override
         protected String doInBackground(String... strings) {
             String url = strings[0];
             String postdata = strings[1];
             MediaType MEDIA_TYPE = MediaType.parse("application/json");
             OkHttpClient client = new OkHttpClient();
             RequestBody body = RequestBody.create(postdata, MEDIA_TYPE);
             Request request = new Request.Builder()
                     .url(url)
                     .post(body)
                     .header("Accept", "application/json")
                     .header("Content-Type", "application/json")
                     .build();
             try {
                 Response response = client.newCall(request).execute();
                 if (response.isSuccessful()) {
                     String messageSucces =  "Added to Datbase " + response.body().string();
                     return messageSucces;
                 } else {
                     String message = url + "\n" + response.code() + " " + response.message();
                     return message;
                 }
             } catch (IOException ex) {
                 Log.e("TEE", ex.getMessage());
                 return ex.getMessage();
             }
         }

         @Override
         protected void onPostExecute(String jsonString) {
             super.onPostExecute(jsonString);
             TextView messageView = findViewById(R.id.add_reservation_message);
             messageView.setText(jsonString);
             Log.d("TEE", jsonString);
             //  finish();
         }

         @Override
         protected void onCancelled(String message) {
             super.onCancelled(message);
             TextView messageView = findViewById(R.id.add_reservation_message);
             messageView.setText(message);
             Log.d("TEE", message);
             //finish();
         }
     }

     public void datePickButtonClicked(View view) {
         DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
             @Override
             public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                 meetingStart.set(Calendar.YEAR, year);
                 meetingStart.set(Calendar.MONTH, month);
                 meetingStart.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                 DateFormat dateFormat = DateFormat.getDateInstance();
                 String dateString = dateFormat.format(meetingStart.getTimeInMillis());
                 fromdateButton.setText(dateString);
             }
         };
         Calendar calendar = Calendar.getInstance();
         int currentYear = calendar.get(Calendar.YEAR);
         int currentMonth = calendar.get(Calendar.MONTH);
         int currentDayOfMonth = calendar.get(Calendar.DATE);
         DatePickerDialog dialog = new DatePickerDialog(
                 this, dateSetListener, currentYear, currentMonth, currentDayOfMonth);
         dialog.show();
     }

     public void datePickButtonClickedTo(View view) {
         DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
             @Override
             public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                 meetingEnd.set(Calendar.YEAR, year);
                 meetingEnd.set(Calendar.MONTH, month);
                 meetingEnd.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                 DateFormat dateFormat = DateFormat.getDateInstance();
                 String dateString = dateFormat.format(meetingStart.getTimeInMillis());
                 todateButton.setText(dateString);
             }
         };
         Calendar calendar = Calendar.getInstance();
         int currentYear = calendar.get(Calendar.YEAR);
         int currentMonth = calendar.get(Calendar.MONTH);
         int currentDayOfMonth = calendar.get(Calendar.DATE);
         DatePickerDialog dialog = new DatePickerDialog(
                 this, dateSetListener, currentYear, currentMonth, currentDayOfMonth);
         dialog.show();
     }

     public void timePickButtonClicked(View view) {
         TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
             @Override
             public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                 meetingStart.set(Calendar.HOUR_OF_DAY, hourOfDay);
                 meetingStart.set(Calendar.MINUTE, minute);
                 DateFormat df = DateFormat.getTimeInstance();
                 String timeString = df.format(meetingStart.getTimeInMillis());
                 fromtimeButton.setText(timeString);
             }
         };
         Calendar calendar = Calendar.getInstance();
         int currentHourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
         int currentMinute = calendar.get(Calendar.MINUTE);
         TimePickerDialog dialog = new TimePickerDialog(this, timeSetListener, currentHourOfDay,
                 currentMinute, true);
         dialog.show();
     }

     public void timePickButtonClickedTo(View view) {
         TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
             @Override
             public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                 meetingEnd.set(Calendar.HOUR_OF_DAY, hourOfDay);
                 meetingEnd.set(Calendar.MINUTE, minute);
                 DateFormat df = DateFormat.getTimeInstance();
                 String timeString = df.format(meetingStart.getTimeInMillis());
                 totimeButton.setText(timeString);
             }
         };
         Calendar calendar = Calendar.getInstance();
         int currentHourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
         int currentMinute = calendar.get(Calendar.MINUTE);
         TimePickerDialog dialog = new TimePickerDialog(this, timeSetListener, currentHourOfDay,
                 currentMinute, true);
         dialog.show();
     }
 }