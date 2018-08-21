package com.integra.tierra;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.integra.tierra.adapters.AdapterCategoria;
import com.integra.tierra.beans.Categoria;
import com.integra.tierra.db.DataSource;
import com.integra.tierra.service.ServicesUbicacion;
import com.integra.tierra.tools.ConexionService;
import com.integra.tierra.tools.Tools;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;

public class SubCategoriaActivity extends AppCompatActivity {

    private AdapterCategoria adapter;
    private ListView lv;
    private ArrayList<Categoria> data;
    private TextView lblEncabeza,lblCerrar;
    private int idCatego;
    private String categoria;
    private int subCatego;
    View v;
    private int partido;
    private ImageView imgOrder;
    private boolean ban=true;
    private DataSource dataSource;
    private ProgressDialog pDialog;
    private TextInputLayout inCant;
    private EditText txtCantP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_categoria);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        v = getWindow().getDecorView();
        idCatego = getIntent().getIntExtra("idCatego", 0);
        partido = getIntent().getIntExtra("partido", 0);
        categoria = getIntent().getStringExtra("catego");
        lblEncabeza = (TextView) findViewById(R.id.lblEncabezado);
        lblEncabeza.setText(categoria);
        imgOrder = (ImageView) findViewById(R.id.imgOrder);
        data = new ArrayList<Categoria>();
        choseData();
        Collections.sort(data);
        ArrayList<Categoria> orderData = new ArrayList<>();
        imgOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ban){
                    Collections.sort(data);
                    adapter.notifyDataSetChanged();
                    ban=false;
                }else{
                    //Collections.sort(data);
                    ArrayList<Categoria> dat = new ArrayList<>();
                    for (int i=data.size()-1;i>-1;i--){
                        dat.add(data.get(i));
                    }
                    data=dat;
                    adapter=new AdapterCategoria(SubCategoriaActivity.this, R.layout.adapter_categoria, data);
                    lv.setAdapter(adapter);
                    ban=true;
                }
            }
        });
        for (int i=data.size()-1;i>-1;i--){
            orderData.add(data.get(i));
        }
        data=orderData;
        adapter = new AdapterCategoria(SubCategoriaActivity.this, R.layout.adapter_categoria, data);
        lv = (ListView) findViewById(R.id.lv);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                DataSource dataSource = new DataSource(SubCategoriaActivity.this);
                String idEv = dataSource.idActiva();
                dataSource.closeDataBase();
                subCatego = data.get(i).getIdCategoria();
                Intent intent = new Intent(SubCategoriaActivity.this, FormActivity.class);
                //Log.e("PARTI",partido+"");
                if (idCatego==9){
                    intent.putExtra("idEvento",idEv);
                }
                intent.putExtra("cate", subCatego);
                intent.putExtra("cat", categoria);
                intent.putExtra("partido", partido);
                intent.putExtra("nameCate", data.get(i).getCategoria());
                startActivity(intent);

            }
        });
        lblCerrar = (TextView) findViewById(R.id.lblCerrar);
        if (idCatego==9){
            cerrarAll();
        }else{
            lblCerrar.setVisibility(View.GONE);
        }
    }

    private void cerrarAll (){
        lblCerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View v = ((LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_close_evento, null);
                Button btnClose = (Button) v.findViewById(R.id.btnCloseEven);
                inCant = (TextInputLayout) v.findViewById(R.id.inCant);
                txtCantP = (EditText) v.findViewById(R.id.txtCant);
                AlertDialog.Builder builder = new AlertDialog.Builder(SubCategoriaActivity.this);
                builder.setView(v);
                final AlertDialog dialog = builder.create();
                btnClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        JSONArray array = new JSONArray();
                        if (Tools.checkNow(SubCategoriaActivity.this)) {
                            if (!Tools.validarVacios(txtCantP.getText().toString())) {
                                inCant.setError("El campo de cantidad es obligatorio");
                            } else {
                                dialog.dismiss();
                                if (ServicesUbicacion.myLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                                    closeEven(txtCantP.getText().toString(), "0", array.put("Presidente"), "0");
                                } else {
                                    Tools.encenderGPS(SubCategoriaActivity.this);
                                }
                            }
                        } else {
                            dialog.dismiss();
                            Tools.imprime("No cuenta con una conexión a internet", SubCategoriaActivity.this);
                        }
                    }
                });
                dialog.show();
            }
        });
    }

    private void closeEven(final String cantP, final String cantC, final JSONArray typeC, final String dura) {
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                DataSource dataSource = new DataSource(SubCategoriaActivity.this);
                String eventTemp = dataSource.idActiva();
                dataSource.closeDataBase();
                try {
                    return new ConexionService(SubCategoriaActivity.this).closeEvent(cantP, cantC, typeC, dura, eventTemp);
                } catch (JSONException e) {
                    e.printStackTrace();
                    return new ArrayList<>();
                }
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pDialog = Tools.esperaDialog(SubCategoriaActivity.this, "Cerrando evento");
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                Tools.cancelarDialog(pDialog);
                ArrayList respuesta = (ArrayList) o;
                if (respuesta.size() > 0) {
                    String res = respuesta.get(0).toString();
                    if (res.equals(getString(R.string.response_true))) {
                        if ((boolean) respuesta.get(1)) {
                            Tools.notiLocal("close", "close", true, SubCategoriaActivity.this);
                            dataSource = new DataSource(SubCategoriaActivity.this);
                            Intent intent = new Intent(SubCategoriaActivity.this, PartidoActivity.class);
                            intent.putExtra("idUser", dataSource.idUser());
                            dataSource.deleteActiva();
                            dataSource.closeDataBase();
                            startActivity(intent);
                            finish();
                        } else {
                            Tools.imprime((String) respuesta.get(2), SubCategoriaActivity.this);
                        }
                    } else if (res.equals(getString(R.string.response_server_error))) {
                        Tools.imprime(getString(R.string.str_error_server) + "\n (03:03:" + respuesta.get(1).toString() + "-br)", SubCategoriaActivity.this);
                    } else if (res.equals(getString(R.string.response_time_out))) {
                        Tools.imprime(getString(R.string.str_instente_mas_tarde) + "\n (03:03:" + respuesta.get(1) + "-br)", SubCategoriaActivity.this);
                    } else {
                        Tools.imprime(getString(R.string.str_list_error) + "\n (03:03:01-br)", SubCategoriaActivity.this);
                    }
                }
            }
        }.execute();
    }

    private void choseData() {
        String[] subCate = {};
        String[] idSub = {};
        switch (idCatego) {
            case 1:
                subCate = getResources().getStringArray(R.array.array_tierra);
                idSub = getResources().getStringArray(R.array.array_tierra_id);
                break;
            case 2:
                subCate = getResources().getStringArray(R.array.array_movil);
                idSub = getResources().getStringArray(R.array.array_movil_id);
                break;
            case 9:
                subCate = getResources().getStringArray(R.array.array_briga);
                idSub = getResources().getStringArray(R.array.array_briga_id);
                break;
        }
        for (int i = 0; i < idSub.length; i++) {
            data.add(new Categoria(subCate[i], Integer.parseInt(idSub[i])));
        }
    }
}
