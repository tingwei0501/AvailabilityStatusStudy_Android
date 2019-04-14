package com.example.mystudy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginPage extends Activity {

    private static final String TAG = "LoginPage";
    private static final String loginUrl = "http://13.59.255.194:5000/signIn";
    private Context mContext;
    private RequestQueue mQueue;
    private JsonObjectRequest mJsonObjectRequest;
    private String id;
    private String pwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);
        mContext = getApplicationContext();

        Button loginSubmit = findViewById(R.id.login_submit);
        loginSubmit.setOnClickListener(loginSubmitListener);
    }

    private Button.OnClickListener loginSubmitListener = new Button.OnClickListener() {

        private Intent successIntent;
        private Intent failedIntent;
        @Override
        public void onClick(View v) {
            EditText username = findViewById(R.id.editText_login_username);
            EditText password = findViewById(R.id.editText_login_password);
            id = username.getText().toString();
            pwd = password.getText().toString();


            successIntent = new Intent();
            successIntent.setClass(LoginPage.this, ContactList.class);
            failedIntent = new Intent();
            failedIntent.setClass(LoginPage.this, RegisterPage.class);

            if (id != null && pwd != null) {

                // pass to next Activity
                successIntent.putExtra("from", "login");
                successIntent.putExtra("id", id);
                // pass to next Activity

                // store to sharedPreference
                SharedPreferences user = getSharedPreferences("user", MODE_PRIVATE);
                user.edit()
                        .putString("id", id)
                        .putString("way", "text")
                        .putString("status", "")
                        .putInt("color", 0)
//                        .putInt("backgroundColor", 0)
                        .commit();
                // store to sharedPreference

                JSONObject data = new JSONObject();
                try {
                    data.put("id", id);
                    data.put("password", pwd);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mQueue = Volley.newRequestQueue(mContext);
                mJsonObjectRequest = new JsonObjectRequest(Request.Method.POST, loginUrl, data,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d(TAG, response.toString());
                                try {
                                    if (response.getString("response").equals("success")) {
                                        Toast.makeText(LoginPage.this,
                                                "登入成功!", Toast.LENGTH_SHORT).show();
                                        startActivity(successIntent);
                                    } else if (response.getString("response").equals("failed")) {
                                        Toast.makeText(LoginPage.this,
                                                "密碼錯誤!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(LoginPage.this,
                                                "此帳號不存在，請先註冊!", Toast.LENGTH_SHORT).show();
                                        startActivity(failedIntent);
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
                Toast.makeText(LoginPage.this,
                        "每隔都需要填寫!", Toast.LENGTH_SHORT).show();
            }
        }
    };
}
