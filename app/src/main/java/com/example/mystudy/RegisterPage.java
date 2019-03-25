package com.example.mystudy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegisterPage extends Activity {

    private static final String TAG = "RegisterPage";
    private static final String registerUrl = "http://13.59.255.194:5000/signUp";
    private static final String testUsernameUrl = "http://13.59.255.194:5000/idCheck";
    private Context mContext;
    private RequestQueue mQueue;
    private JsonObjectRequest mJsonObjectRequest;
    private String id;
    private String pwd;
    private String g;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_page);
        mContext = getApplicationContext();


        Button registerSubmit = (Button) findViewById(R.id.register_submit);
        Button testUsername = (Button) findViewById(R.id.test_username);


        registerSubmit.setOnClickListener(registerSubmitListener);
        testUsername.setOnClickListener(testUsernameListener);
    }

    private Button.OnClickListener testUsernameListener = new Button.OnClickListener() {

        @Override
        public void onClick(View v) {
            EditText username = (EditText) findViewById(R.id.editText_register_username);
            id = username.getText().toString();

            if (id != null) {

                JSONObject data = new JSONObject();
                try {
                    data.put("id", id);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mQueue = Volley.newRequestQueue(mContext);

                mJsonObjectRequest = new JsonObjectRequest(Request.Method.POST, testUsernameUrl, data,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d(TAG, response.toString());
                                try {
                                    if (response.getString("response").equals("success")) {
                                        Toast.makeText(RegisterPage.this,
                                                "帳號可以使用!", Toast.LENGTH_SHORT).show();
                                    } else if (response.getString("response").equals("failed")) {
                                        Toast.makeText(RegisterPage.this,
                                                "此帳號已存在，請使用新帳號!", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d(TAG, "Error: " + error.getMessage());
                            }
                        });
                mQueue.add(mJsonObjectRequest);
            }
        }
    };
    private Button.OnClickListener registerSubmitListener = new Button.OnClickListener() {
        private Intent intent;

        @Override
        public void onClick(View v) {
            intent = new Intent();
            intent.setClass(RegisterPage.this, LoginPage.class);

            EditText password = (EditText) findViewById(R.id.editText_register_password);
            EditText group = (EditText) findViewById(R.id.editText_register_group);
            pwd = password.getText().toString();
            g = group.getText().toString();

            if (id != null && pwd != null && g != null) {
                JSONObject data = new JSONObject();
                try {
                    data.put("id", id);
                    data.put("password", pwd);
                    data.put("group", g);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mQueue = Volley.newRequestQueue(mContext);
                mJsonObjectRequest = new JsonObjectRequest(Request.Method.POST, registerUrl, data,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d(TAG, response.toString());
                                try {
                                    if (response.getString("response").equals("success")) {
                                        Toast.makeText(RegisterPage.this,
                                                "帳號註冊成功!", Toast.LENGTH_SHORT).show();
                                        startActivity(intent);
                                    } else if (response.getString("response").equals("failed")) {
                                        Toast.makeText(RegisterPage.this,
                                                "註冊失敗，此帳號已存在", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(RegisterPage.this,
                                                "註冊成功，但是使用者沒有上傳頭像!", Toast.LENGTH_SHORT).show();
                                        startActivity(intent);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d(TAG, "Error: " + error.getMessage());
                            }
                        });
                mQueue.add(mJsonObjectRequest);

            } else {
                Toast.makeText(RegisterPage.this,
                        "每隔都需要填寫!", Toast.LENGTH_SHORT).show();
            }
        }
    };
}
