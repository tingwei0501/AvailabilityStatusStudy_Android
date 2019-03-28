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
//    private MyAdapter myAdapter;
//    private RecyclerView recyclerView;
    private SimpleAdapter simpleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_list);
        mContext = getApplicationContext();
        listView = findViewById(R.id.contact_list);
//        recyclerView = findViewById(R.id.recyclerView);
        id = getIntent().getStringExtra("id");
        getData();

        Button editProfile = findViewById(R.id.edit_profile_button);
        editProfile.setOnClickListener(editProfileListener);
        Button statusExample = findViewById(R.id.status_example);
        statusExample.setOnClickListener(statusExampleListener);
        Button questionnaire = findViewById(R.id.questionnaire);
        questionnaire.setOnClickListener(questionnaireListener);
    }

    private Button.OnClickListener questionnaireListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setClass(ContactList.this, Questionnaire.class);
            startActivity(intent);
        }
    };
    private Button.OnClickListener statusExampleListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setClass(ContactList.this, StatusExample.class);
            startActivity(intent);
        }
    };

    private Button.OnClickListener editProfileListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setClass(ContactList.this, EditProfilePage.class);
            startActivity(intent);
        }
    };

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
//                                myAdapter = new MyAdapter(userList);
//                                recyclerView = findViewById(R.id.recyclerView);
//                                final LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
//                                layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
//                                recyclerView.setLayoutManager(layoutManager);
//                                recyclerView.setAdapter(myAdapter);

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

//    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
//
//        private ArrayList<String> mData;
//        public class ViewHolder extends RecyclerView.ViewHolder {
//            public TextView mTextView;
//            public ViewHolder(View v) {
//                super(v);
//                mTextView = v.findViewById(R.id.contact_list_row_text);
//            }
//        }
//        public MyAdapter(ArrayList<String> data) {
//            mData = data;
//        }
//        @Override
//        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//            View v = LayoutInflater.from(parent.getContext())
//                    .inflate(R.layout.contact_list_row, parent, false);
//            ViewHolder vh = new ViewHolder(v);
//            return vh;
//        }
//        @Override
//        public void onBindViewHolder(ViewHolder holder, final int position) {
//            holder.mTextView.setText(mData.get(position));
//            holder.itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Toast.makeText(ContactList.this, "Item " + position + " is clicked.", Toast.LENGTH_SHORT).show();
//                }
//            });
//            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View v) {
//                    Toast.makeText(ContactList.this, "Item " + position + " is long clicked.", Toast.LENGTH_SHORT).show();
//                    return true;
//                }
//            });
//        }
//
//        @Override
//        public int getItemCount() {
//            return mData.size();
//        }
//    }
    private void renderList() {
        for (int i=0;i<userList.size();i++) {
            Log.d(TAG, userList.get(i));
        }
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
                                          new int[]{R.id.contact_list_row_text});
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
