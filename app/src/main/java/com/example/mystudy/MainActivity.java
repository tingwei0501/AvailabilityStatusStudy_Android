package com.example.mystudy;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /** TODO: shared preference to detect login */

        Button register = (Button) findViewById(R.id.register);
        register.setOnClickListener(registerListener);
        Button login = (Button) findViewById(R.id.login);
        login.setOnClickListener(loginListener);


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
