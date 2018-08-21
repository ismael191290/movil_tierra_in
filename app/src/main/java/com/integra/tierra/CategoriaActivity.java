package com.integra.tierra;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.integra.tierra.beans.Categoria;
import com.integra.tierra.db.DataSource;
import com.integra.tierra.tools.ConexionService;
import com.integra.tierra.tools.Tools;

import org.json.JSONException;

import java.util.ArrayList;

public class CategoriaActivity extends AppCompatActivity {

    private TextView lblEncabeza;
    private LinearLayout lFija,lMovil,lBrigada;
    private ImageView imgMenu;
    private Intent intent;
    private int partido;
    private ProgressDialog pDialog;
    private String partidoStr ="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categoria);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        partido=getIntent().getIntExtra("partido",0);
        lblEncabeza = (TextView)findViewById(R.id.lblEncabezado);
        imgMenu = (ImageView) findViewById(R.id.imgMenu);
        lFija = (LinearLayout)findViewById(R.id.lFija);
        lBrigada = (LinearLayout)findViewById(R.id.lBrigada);
        lMovil = (LinearLayout)findViewById(R.id.lMovil);
        partidoStr = getIntent().getStringExtra("partStr");
        imgMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CategoriaActivity.this,MenuActivity.class);
                startActivity(intent);
            }
        });
        intent = new Intent(CategoriaActivity.this,SubCategoriaActivity.class);
        //lblEncabeza.setText(dataSource.idEvent());
        lMovil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("PARTI",partido+"");
                intent.putExtra("idCatego",2);
                intent.putExtra("catego","Movil");
                intent.putExtra("partido",partido);
                startActivity(intent);
            }
        });
        lBrigada.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendPreRegistro(partido,partidoStr);
            }
        });
        lFija.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.putExtra("idCatego",1);
                intent.putExtra("catego","Fija");
                intent.putExtra("partido",partido);
                startActivity(intent);
            }
        });
        IntentFilter filterClose = new IntentFilter("close");
        filterClose.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        LocalBroadcastManager.getInstance(this).registerReceiver(onClose, filterClose);
    }

    private void sendPreRegistro(final int idPartido,final String ini){
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                try {
                    DataSource dataSource = new DataSource(CategoriaActivity.this);
                    int idUser = dataSource.idUser();
                    dataSource.closeDataBase();
                    return new ConexionService(CategoriaActivity.this).preRegistro("Activacion","E"+ini,idPartido,"",idUser);
                } catch (JSONException e) {
                    e.printStackTrace();
                    return new ArrayList<>();
                }
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                DataSource dataSource = new DataSource(CategoriaActivity.this);
                dataSource.deleteActiva();
                dataSource.closeDataBase();
                pDialog= Tools.esperaDialog(CategoriaActivity.this,"Generando Activación...");
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                Tools.cancelarDialog(pDialog);
                ArrayList respuesta = (ArrayList)o;
                if (respuesta.size() > 0) {
                    String res = respuesta.get(0).toString();
                    if (res.equals(getString(R.string.response_true))) {
                        if ((boolean) respuesta.get(1)) {
                            DataSource dataSource = new DataSource(CategoriaActivity.this);
                            dataSource.insertActiva(((String)respuesta.get(2)));
                            dataSource.closeDataBase();
                            Intent intent = new Intent(CategoriaActivity.this,SubCategoriaActivity.class);
                            intent.putExtra("idCatego",9);
                            intent.putExtra("catego","Brigada");
                            intent.putExtra("partido",partido);
                            startActivity(intent);
                        } else {
                            Tools.imprime((String) respuesta.get(2), CategoriaActivity.this);
                        }
                    } else if (res.equals(getString(R.string.response_server_error))) {
                        Tools.imprime(getString(R.string.str_error_server) + "\n (03:03-br:" + respuesta.get(1).toString() + ")", CategoriaActivity.this);
                    } else if (res.equals(getString(R.string.response_time_out))) {
                        Tools.imprime(getString(R.string.str_instente_mas_tarde) + "\n (03:03-br:" + respuesta.get(1) + ")", CategoriaActivity.this);
                    } else {
                        Tools.imprime(getString(R.string.str_list_error) + "\n (03:03:01-br)", CategoriaActivity.this);
                    }
                }
            }
        }.execute();
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
