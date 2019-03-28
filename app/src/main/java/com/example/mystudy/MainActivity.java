package com.example.mystudy;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // use shared preference to detect login
        id = getSharedPreferences("user", MODE_PRIVATE)
                .getString("id", "");
        if (id.equals("")) {
            Button register = findViewById(R.id.register);
            register.setOnClickListener(registerListener);
            Button login = findViewById(R.id.login);
            login.setOnClickListener(loginListener);
        } else {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, ContactList.class);
            intent.putExtra("id", id);
            startActivity(intent);
        }

    }
    private Button.OnClickListener registerListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, RegisterPage.class);
            startActivity(intent);
//            MainActivity.this.finish();
        }
    };
    private Button.OnClickListener loginListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, LoginPage.class);
            startActivity(intent);
//            MainActivity.this.finish();
        }
    };
}
