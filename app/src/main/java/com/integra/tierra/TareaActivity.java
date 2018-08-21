package com.integra.tierra;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.integra.tierra.db.DataSource;

import java.util.ArrayList;

public class TareaActivity extends AppCompatActivity {

    private LinearLayout linearForm;
    private LinearLayout linearFoto;
    private TextView lblEncabeza;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tarea);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        lblEncabeza = (TextView) findViewById(R.id.lblEncabezado);
        linearForm = (LinearLayout) findViewById(R.id.lForm);
        linearFoto = (LinearLayout) findViewById(R.id.lCamera);
        final Intent intent = new Intent(TareaActivity.this, CategoriaActivity.class);
        linearForm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.putExtra("action", 1);
                startActivity(intent);
            }
        });
        linearFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.putExtra("action", 2);
                startActivity(intent);
            }
        });
        IntentFilter filterClose = new IntentFilter("close");
        filterClose.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        LocalBroadcastManager.getInstance(this).registerReceiver(onClose, filterClose);
    }

    private BroadcastReceiver onClose = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("close")) {
                try {
                    if (intent.getBooleanExtra("close", false)) {
                        finish();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("ELSE1", "Llega2");
            }
        }
    };

}
