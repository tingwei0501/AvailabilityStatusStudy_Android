package labelingStudy.nctu.minuku_2;

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

import labelingStudy.nctu.minuku.config.Constants;

public class RegisterPage extends Activity {

    private static final String TAG = "RegisterPage";
    private Context mContext;
    private RequestQueue mQueue;
    private JsonObjectRequest mJsonObjectRequest;
    private String id = "";
    private String pwd = "";
    private String g = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_page);
        mContext = getApplicationContext();


        Button registerSubmit = findViewById(R.id.register_submit);
        Button testUsername = findViewById(R.id.test_username);


        registerSubmit.setOnClickListener(registerSubmitListener);
        testUsername.setOnClickListener(testUsernameListener);
    }

    private Button.OnClickListener testUsernameListener = new Button.OnClickListener() {

        @Override
        public void onClick(View v) {
            EditText username = findViewById(R.id.editText_register_username);
            id = username.getText().toString();

            if (!id.equals("")) {

                JSONObject data = new JSONObject();
                try {
                    data.put("id", id);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mQueue = Volley.newRequestQueue(mContext);

                mJsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Constants.testUsernameUrl, data,
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
            } else {
                Toast.makeText(RegisterPage.this,
                        "請輸入帳號", Toast.LENGTH_SHORT).show();
            }
        }
    };
    private Button.OnClickListener registerSubmitListener = new Button.OnClickListener() {
        private Intent intent;

        @Override
        public void onClick(View v) {
            intent = new Intent();
            intent.setClass(RegisterPage.this, LoginPage.class);

            EditText password = findViewById(R.id.editText_register_password);
            EditText group = findViewById(R.id.editText_register_group);
            EditText username = findViewById(R.id.editText_register_username);
            id = username.getText().toString();
            pwd = password.getText().toString();
            g = group.getText().toString();

            if (!id.equals("") && !pwd.equals("") && !g.equals("")) {
                SharedPreferences user = getSharedPreferences(Constants.sharedPrefString_User, MODE_PRIVATE);

                user.edit()
                        .putString("group", g)

                        /** For partial subject only: */
//                        .putBoolean("openNotification", false)
                        /** For partial subject only: */

                        .commit();
                JSONObject data = new JSONObject();
                try {
                    data.put("id", id);
                    data.put("password", pwd);

                    // TODO: For Main
                    data.put("group", g);
                    // TODO: For Main

                    /** For partial subject only: */
//                    data.put("code", g);
                    /** For partial subject only: */
                    //
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mQueue = Volley.newRequestQueue(mContext);
                mJsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Constants.registerUrl, data,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d(TAG, response.toString());
                                try {
                                    if (response.getString("response").equals("success")) {
                                        Toast.makeText(RegisterPage.this,
                                                "帳號註冊成功!", Toast.LENGTH_SHORT).show();
                                        startActivity(intent);
                                        RegisterPage.this.finish();
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
