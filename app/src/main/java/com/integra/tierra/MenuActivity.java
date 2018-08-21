package com.integra.tierra;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import com.integra.tierra.db.DataSource;
import com.integra.tierra.service.ServicesUbicacion;
import com.integra.tierra.tools.ConexionService;
import com.integra.tierra.tools.Tools;
import org.json.JSONArray;
import org.json.JSONException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;

public class MenuActivity extends AppCompatActivity implements View.OnClickListener{

    private DataSource dataSource;
    private Button btnSOS, btnCall;
    private TextView lblCatego, lblFecha;
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        btnCall = (Button) findViewById(R.id.btnCall);
        btnSOS = (Button) findViewById(R.id.btnSOS);
        lblCatego = (TextView) findViewById(R.id.lblCatego);
        lblFecha = (TextView) findViewById(R.id.lblFecha);
        lblCatego.setOnClickListener(this);
        btnCall.setOnClickListener(this);
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
        btnSOS.setOnClickListener(this);
        lblFecha.setText(df.format(gregorianCalendar.getTime()));

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnCall:
                if (Tools.checkNow(MenuActivity.this)) {
                    if (ServicesUbicacion.myLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        sendAlerta("Contactar con la empresa", 4);
                    } else {
                        Tools.encenderGPS(MenuActivity.this);
                    }
                } else {
                    Tools.imprime("No cuenta con una conexión a internet", MenuActivity.this);
                }
                break;
            case R.id.btnSOS:
                //disableCellPhone();
                if (Tools.checkNow(MenuActivity.this)) {
                    if (ServicesUbicacion.myLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        sendAlerta("S.O.S", 3);
                    } else {
                        Tools.encenderGPS(MenuActivity.this);
                    }
                } else {
                    Tools.imprime("No cuenta con una conexión a internet", MenuActivity.this);
                }
                break;
            case R.id.lblCatego:
                finish();
                break;
            default:
                break;
        }
    }

    private void disableCellPhone(){
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My Tag");
        wl.acquire();
        wl.release();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (!ServicesUbicacion.myLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Tools.encenderGPS(MenuActivity.this);
        }
    }

    private void sendAlerta(final String desc, final int tipo) {
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                try {
                    dataSource = new DataSource(MenuActivity.this);
                    int idU = dataSource.idUser();
                    dataSource.closeDataBase();
                    return new ConexionService(MenuActivity.this).incidencia(tipo, "insertAlerta",
                            idU, ServicesUbicacion.lat, ServicesUbicacion.lang, 5, "1",  desc, new JSONArray(), null,null,null);
                } catch (JSONException e) {
                    e.printStackTrace();
                    return new ArrayList<>();
                }
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pDialog = Tools.esperaDialog(MenuActivity.this, "Enviando alerta...");
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                Tools.cancelarDialog(pDialog);
                if(tipo==3){
                    Tools.closeSession(MenuActivity.this,MenuActivity.this);
                }
                ArrayList respuesta = (ArrayList) o;
                if (respuesta.size() > 0) {
                    String res = respuesta.get(0).toString();
                    if (res.equals(getString(R.string.response_true))) {
                        if ((boolean) respuesta.get(1)) {
                            if (((String) respuesta.get(2)).length() > 10) {
                                Tools.imprime("Alerta enviada", MenuActivity.this);
                            } else {
                                Tools.imprime("La alerta no pudo ser enviada", MenuActivity.this);
                            }
                        } else {
                            Tools.imprime((String) respuesta.get(2), MenuActivity.this);
                        }
                    } else if (res.equals(getString(R.string.response_server_error))) {
                        Tools.imprime(getString(R.string.str_error_server) + "\n (03:03:" + respuesta.get(1).toString() + ")", MenuActivity.this);
                    } else if (res.equals(getString(R.string.response_time_out))) {
                        Tools.imprime(getString(R.string.str_instente_mas_tarde) + "\n (03:03:" + respuesta.get(1) + ")", MenuActivity.this);
                    } else {
                        Tools.imprime(getString(R.string.str_list_error) + "\n (03:03:01)", MenuActivity.this);
                    }
                }
            }

        }.execute();
    }
}
