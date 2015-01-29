package com.num.myspeedtest.view.activities;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.num.myspeedtest.R;
import com.num.myspeedtest.controller.managers.TracerouteManager;
import com.num.myspeedtest.model.Traceroute;
import com.num.myspeedtest.view.adapters.TracerouteListAdapter;

import java.util.ArrayList;
import java.util.List;

public class TracerouteActivity extends ActionBarActivity {

    private Context context;

    /* variables for UI */
    private ListView listView;
    private EditText address;
    private Button enter;
    private ProgressBar progressBar;

    private List<Traceroute> traceroutes;
    private TracerouteListAdapter adapter;

    /* variables for getting traceroute */
    private final String default_address = "www.google.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this.getApplicationContext();
        setContentView(R.layout.activity_traceroute);

        /* setup for UI */
        progressBar = (ProgressBar) findViewById(R.id.traceroute_progress);
        progressBar.setVisibility(View.INVISIBLE);

        traceroutes = new ArrayList<>();
        adapter = new TracerouteListAdapter(context, traceroutes);

        listView = (ListView) findViewById(R.id.list_view_traceroute);
        listView.setAdapter(adapter);

        address = (EditText) findViewById(R.id.editText_traceroute);
        address.setText(default_address);
        address.setOnKeyListener(new View.OnKeyListener() {

            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    //hide keyboard
                    InputMethodManager imm =
                            (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);

                    adapter.clear();
                    progressBar.setVisibility(View.VISIBLE);

                    TracerouteHandler handler = new TracerouteHandler();
                    TracerouteManager manager = new TracerouteManager(handler);
                    manager.execute(address.getText().toString());
                    return true;
                }

                return false;
            }
        });

        enter = (Button) findViewById(R.id.button_traceroute);
        enter.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);

                adapter.clear();
                progressBar.setVisibility(View.VISIBLE);

                TracerouteHandler handler = new TracerouteHandler();
                TracerouteManager manager = new TracerouteManager(handler);
                manager.execute(address.getText().toString());

            }
        });

    }

    private class TracerouteHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Traceroute traceroute = msg.getData().getParcelable("traceroute");
            adapter.add(traceroute);
            if (msg.getData().getBoolean("isDone")) {
                progressBar.setVisibility(View.INVISIBLE);
            }
            System.out.println(traceroute);
        }

    }


}
