package com.example.mystudy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContactList extends Activity {

    private static final String TAG = "ContactList";
    private static final String fetchDataUrl = "http://13.59.255.194:5000/getList";
    private Context mContext;
    private RequestQueue mQueue;
    private JsonObjectRequest mJsonObjectRequest;
    private ArrayList<String> userList;
    private String id;
    private ListView listView;
    private SimpleAdapter simpleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_list);
        mContext = getApplicationContext();
        listView = findViewById(R.id.contact_list);
        id = getIntent().getStringExtra("id");
        getData();

        Button statusExample = findViewById(R.id.status_example);
        statusExample.setOnClickListener(statusExampleListener);
    }

    private Button.OnClickListener statusExampleListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setClass(ContactList.this, StatusExample.class);
            startActivity(intent);
        }
    };
    // TODO: maybe need to return list
    private void getData() {
        userList = new ArrayList<>();
        JSONObject data = new JSONObject();
        try {
            data.put("id", id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mQueue = Volley.newRequestQueue(mContext);

        mJsonObjectRequest = new JsonObjectRequest(Request.Method.POST, fetchDataUrl, data,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, response.toString());
                        try {
                            if (response.getString("response").equals("success")) {

                                JSONArray list = response.getJSONArray("list");
                                for (int i=0;i<list.length();i++) {
                                    JSONObject user = list.getJSONObject(i);
                                    String userId = user.getString("id");
                                    userList.add(userId);
                                }
                                // TODO: render List
                                renderList();
                            } else {
                                Log.d(TAG, "getList error");
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

    private void renderList() {
//        for (int i=0;i<userList.size();i++) {
//            Log.d(TAG, userList.get(i));
//        }
        List<Map<String, Object>> items = new ArrayList<>();
        for (int i=0;i<userList.size();i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", userList.get(i));
            items.add(item);
        }
        simpleAdapter = new SimpleAdapter(mContext,
                                          items,
                                          R.layout.contact_list_row,
                                          new String[]{"id"},
                                          new int[]{R.id.contact_list_row_textview});
        listView.setAdapter(simpleAdapter);
        listView.setOnItemClickListener(onClickListView);
    }

    private AdapterView.OnItemClickListener onClickListView = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Log.d(TAG, "press"+position);
            Log.d(TAG, "press"+id);
        }
    };
}
