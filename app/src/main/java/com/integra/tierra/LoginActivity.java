package com.integra.tierra;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.integra.tierra.db.DataSource;
import com.integra.tierra.service.ServicesUbicacion;
import com.integra.tierra.tools.ConexionService;
import com.integra.tierra.tools.PermissionUtil;
import com.integra.tierra.tools.Tools;
import org.json.JSONArray;
import org.json.JSONException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class LoginActivity extends AppCompatActivity {

    private Button btnLogin;
    private EditText txtPass;
    private ProgressDialog pDialog;
    private TextInputLayout txiPass;
    private final static int TAKE_PICTURE = 599;
    private Uri uri;
    private static final int REQUEST_CALENDAR = 0;
    private boolean gps = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        verificar();
        Intent intent = new Intent(LoginActivity.this, ServicesUbicacion.class);
        intent.putExtra("BanderaPosicion",false);
        startService(intent);
        txtPass = (EditText)findViewById(R.id.txtPass);
        txiPass = (TextInputLayout)findViewById(R.id.inPass);
        btnLogin = (Button)findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Tools.validarVacios(txtPass.getText().toString().trim())){
                    if (Tools.checkNow(LoginActivity.this)){
                        if (gps){
                            login();
                        }else{
                            Tools.notiLocal("gps_disable","showGPS",true,LoginActivity.this);
                        }
                    }
                }else{
                    txiPass.setError("El campo contraseña no puede estar vacío");
                }
            }
        });
        IntentFilter filterGPS = new IntentFilter("gps_disable");
        filterGPS.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        LocalBroadcastManager.getInstance(this).registerReceiver(onGPS, filterGPS);
        pDialog = Tools.esperaDialog(LoginActivity.this,"Inicializando aplicación...");
        final Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                Tools.cancelarDialog(pDialog);
                if (!gps){
                    t.cancel();
                    Tools.notiLocal("gps_disable","showGPS",true,LoginActivity.this);
                }else{
                    t.cancel();
                    if (!Tools.checkNow(LoginActivity.this)){
                        DataSource dataSource = new DataSource(LoginActivity.this);
                        if (!dataSource.idEvent().equals("")){
                            Intent intent2 = new Intent(LoginActivity.this,PartidoActivity.class);
                            startActivity(intent2);
                            dataSource.closeDataBase();
                            finish();
                        }else {

                            dataSource.closeDataBase();
                        }
                    }
                }

            }
        },3000,1000);
    }

    private void login(){
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                try {
                    return new ConexionService(LoginActivity.this).login(txtPass.getText().toString().trim());
                } catch (JSONException e) {
                    e.printStackTrace();
                    return new ArrayList<Object>();
                }
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pDialog = Tools.esperaDialog(LoginActivity.this,"Verificando contraseña");
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
                            if((int)respuesta.get(3)!=3){
                                    DataSource dataSource =new DataSource(LoginActivity.this);
                                    dataSource.deleteEvent();
                                    dataSource.insertEvnto((String)respuesta.get(4),(int)respuesta.get(2));
                                    dataSource.closeDataBase();
                                    Intent intent2 = new Intent(LoginActivity.this,PartidoActivity.class);
                                    startActivity(intent2);
                                    finish();
                            }else{
                                uri = Tools.photo(LoginActivity.this,TAKE_PICTURE);
                                sendAlertInci((int)respuesta.get(2));
                            }
                        } else {
                            Tools.imprime((String)respuesta.get(2), LoginActivity.this);
                        }
                    } else if (res.equals(getString(R.string.response_server_error))) {
                        Tools.imprime(getString(R.string.str_error_server) + "\n (01:01:" + respuesta.get(1).toString() + ")", LoginActivity.this);
                    } else if (res.equals(getString(R.string.response_time_out))) {
                        Tools.imprime(getString(R.string.str_instente_mas_tarde) + "\n (01:01:" + respuesta.get(1) + ")", LoginActivity.this);
                    } else {
                        Tools.imprime(getString(R.string.str_list_error) + "\n (01:01:01)", LoginActivity.this);
                    }
                }
            }
        }.execute();
    }

    private void sendAlertInci(final int idCiudadano){
        Log.e("OKA","ddddd"+idCiudadano);
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!ServicesUbicacion.lat.equals("") && !ServicesUbicacion.lang.equals("")){
                    try {
                        new ConexionService(LoginActivity.this).incidencia(1,"insert",idCiudadano,ServicesUbicacion.lat,ServicesUbicacion.lang,1,"2","código de emergencia",new JSONArray(),null,null,null);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    timer.cancel();
                }
            }
        },100,2000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("CODE",""+resultCode);
        if (gps){
            if (!Tools.checkNow(LoginActivity.this)){
                DataSource dataSource = new DataSource(LoginActivity.this);
                if (!dataSource.idEvent().equals("")){
                    Intent intent2 = new Intent(LoginActivity.this,PartidoActivity.class);
                    startActivity(intent2);
                    dataSource.closeDataBase();
                    finish();
                }else {

                    dataSource.closeDataBase();
                }
            }
        }else{
            Tools.encenderGPS(LoginActivity.this);
        }
    }

    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)&& ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)&& ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) && ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {

            ActivityCompat.requestPermissions(LoginActivity.this,
                    new String[]{
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_CALENDAR);


        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_CALENDAR);

        }
    }

    private void verificar(){
        if ( ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            requestLocationPermission();

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == REQUEST_CALENDAR) {
            Log.e("TAG", "Received response for contact permissions request.");
            if (PermissionUtil.verifyPermissions(grantResults)) {
                verificar();
                Intent intent = new Intent(LoginActivity.this, ServicesUbicacion.class);
                intent.putExtra("BanderaPosicion",false);
                startService(intent);

            } else {
                Tools.imprimeFinish("Se denegaron los permisos para el acceso a la lectura de imagenes," +
                        " los cuales son " +
                        "necesarios para el uso de esta aplicación",LoginActivity.this,LoginActivity.this);
            }

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private BroadcastReceiver onGPS = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("datosGPS")) {
                try {
                    gps = intent.getBooleanExtra("datosGPS", false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(intent.hasExtra("showGPS")) {
                Tools.encenderGPS(LoginActivity.this);
            } else {
                Log.e("ELSE1", "Llega");
            }
        }
    };
}
