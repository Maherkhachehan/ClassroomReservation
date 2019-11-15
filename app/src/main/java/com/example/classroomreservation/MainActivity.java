package com.example.classroomreservation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.TextViewCompat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, GestureDetector.OnGestureListener {
    private final String TAG = "SIGNIN";

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener firbaseAuthListener;

    private EditText myPassword;
    private EditText myMail;
    private Button uden_login_button;
    private GestureDetector gesturesDetector;

    @Override
   public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gesturesDetector = new GestureDetector(this, this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //if(FirebaseAuth.getInstance().getCurrentUser() != null)
           // roomView(null);

        findViewById(R.id.button_Register).setOnClickListener(this);
        findViewById(R.id.button_login).setOnClickListener(this);
        findViewById(R.id.button_signOut).setOnClickListener(this);
        findViewById(R.id.uden_login_button).setOnClickListener(this);

        myMail = (EditText)findViewById(R.id.email_Edittext);
        myPassword = (EditText)findViewById(R.id.password_Edittext);

        firebaseAuth = FirebaseAuth.getInstance();

        firbaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null){
                    Log.d(TAG, "Succes" + user.getUid());
                } else {
                    Log.d(TAG, "Ingen Succes");
                }
            }
        };
        updateStatus();
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
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(firbaseAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firbaseAuthListener != null){
            firebaseAuth.removeAuthStateListener(firbaseAuthListener);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button_login:
                signUserIn();
                roomView(v);
                break;

            case R.id.button_Register:
                createUserAccount();
                break;

            case R.id.button_signOut:
                signUserOut();
                break;

            case R.id.uden_login_button:
                roomView(v);
                break;
        }
    }

    private boolean checkFormFields() {
        String email, password;

        email = myMail.getText().toString();
        password = myPassword.getText().toString();

        if (email.isEmpty()) {
            myMail.setError("Mangler Email");
            return false;
        }
        if (password.isEmpty()){
            myPassword.setError("Mangler Password");
            return false;
        }

        return true;
    }

    private void updateStatus() {
        TextView tvStat = (TextView)findViewById(R.id.status_Textview);
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            tvStat.setText("Signed in: " + user.getEmail());
        }
        else {
            tvStat.setText("Signed Out");
        }
    }

    private void updateStatus(String stat) {
        TextView tvStat = (TextView)findViewById(R.id.status_Textview);
        tvStat.setText(stat);
    }

    private void signUserIn() {
        if (!checkFormFields())
            return;

        Log.d("TEE", "Started logging in");

        String email = myMail.getText().toString();
        String password = myPassword.getText().toString();


        firebaseAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(this,
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(MainActivity.this, "Signed in", Toast.LENGTH_SHORT)
                                            .show();
                                }
                                else {
                                    Toast.makeText(MainActivity.this, "Sign in failed", Toast.LENGTH_SHORT)
                                            .show();
                                }

                                updateStatus();
                            }
                        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            updateStatus("Invalid password.");
                        }
                        else if (e instanceof FirebaseAuthInvalidUserException) {
                            updateStatus("No account with this email.");
                        }
                        else {
                            updateStatus(e.getLocalizedMessage());
                        }
                    }
                });
    }

    private void signUserOut() {
        firebaseAuth.signOut();
        updateStatus();
    }

    private void createUserAccount() {
        if (!checkFormFields()) {
            Log.d("TEE", "Check form fields failed");
            return;
        }

        Log.d("TEE", "Start creating password");

        String email = myMail.getText().toString();
        String password = myPassword.getText().toString();

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this,
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(MainActivity.this, "User created", Toast.LENGTH_SHORT)
                                            .show();
                                } else {
                                    Toast.makeText(MainActivity.this, "Account creation failed", Toast.LENGTH_SHORT)
                                            .show();
                                }
                            }
                        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, e.toString());
                        if (e instanceof FirebaseAuthUserCollisionException) {
                            updateStatus("This email address is already in use.");
                        }
                        else {
                            updateStatus(e.getLocalizedMessage());
                        }
                    }
                });
    }

    public void roomView(View v) {
        Intent i = new Intent(MainActivity.this, RoomActivity.class);
        //i.putStringArrayListExtra("logArray", log);
        MainActivity.this.startActivity(i);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        return gesturesDetector.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        //Toast.makeText(this, "onFling", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onFling " + e1.toString() + "::::" + e2.toString());

        boolean leftSwipe = e1.getX() > e2.getX();
        Log.d(TAG, "onFling left: " + leftSwipe);
        if (leftSwipe) {
            Intent intent = new Intent(this,RoomActivity.class);
            startActivity(intent);
            //ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(this);
            //Bundle options = activityOptionsCompat.toBundle();
            //startActivity(intent, options);

        }
        return true; // done
    }

    public void buttonClicked(View view) {
        Log.d(TAG, "button clicked");
    }
}


