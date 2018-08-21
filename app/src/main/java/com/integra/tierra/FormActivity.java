package com.integra.tierra;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Intent;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.integra.tierra.db.DataSource;
import com.integra.tierra.service.ServicesUbicacion;
import com.integra.tierra.service.ServicioSend;
import com.integra.tierra.tools.ConexionService;
import com.integra.tierra.tools.Tools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class FormActivity extends AppCompatActivity {

    private LinearLayout contenedor;
    private ArrayList<Map<String, Object>> listMapView;
    FormActivity activiti = FormActivity.this;
    private ProgressDialog pDialog;
    private int subCatego;
    private TextView lblEncabeza;
    private int partido;
    private String subC = "";
    private String cat = "";
    private ArrayList<String> img;
    private final static int TAKE_PICTURE = 599;
    private final static int GALLERY = 499;
    private String imagePath = "";
    private Uri uri;
    private DataSource dataSource;
    private Timer timer;
    private JSONArray array;
    private String idEvento="2";
    private boolean internet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        contenedor = (LinearLayout) findViewById(R.id.contenedor);
        listMapView = new ArrayList<Map<String, Object>>();
        img = new ArrayList<>();
        if (getIntent().hasExtra("idEvento")){
            idEvento=getIntent().getStringExtra("idEvento");
        }
        subCatego = getIntent().getIntExtra("cate", 0);
        partido = getIntent().getIntExtra("partido", 0);
        lblEncabeza = (TextView) findViewById(R.id.lblEncabezado);
        cat = getIntent().getStringExtra("cat");
        subC = getIntent().getStringExtra("nameCate");
        lblEncabeza.setText(subC);
        Log.e("ID", subCatego + "");
        vista(subCatego);
    }

    public RadioGroup radios(String radio1, String radio2, int size, int id, int id2) {
        RadioGroup rg = new RadioGroup(activiti);
        RadioButton rb = new RadioButton(activiti);
        rb.setText(radio1);
        //rb.setId((Integer) id);
        rb.setTextSize(size);
        rg.addView(rb);
        RadioButton rb1 = new RadioButton(activiti);
        rb1.setText(radio2);
        //rb1.setId((Integer) id2);
        rb1.setTextSize(size);
        rg.addView(rb1);
        return rg;

    }

    public EditText typeNumber(String titulo, String tipoData) {

        EditText editText = new EditText(activiti);
        editText.setHint(titulo + ": ");
        if (tipoData.equals("numero")) {
            editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
        }
        return editText;
    }

    public LinearLayout countEditext(Map<String, Object> map, LinearLayout.LayoutParams lp, int cantidad, String tipoNumero, String texto1, String texto2, String texto3, int id1, int id2, int id3) {

        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setOrientation(LinearLayout.HORIZONTAL);

        if (cantidad == 1) {
            TextInputLayout textInputLayout = new TextInputLayout(activiti);
            EditText cantidad1 = typeNumber(texto1, tipoNumero);
            textInputLayout.addView(cantidad1);
            map.put("id", id1);
            map.put("valor", cantidad1);
            listMapView.add(map);
            map = new HashMap<>();
            layout.addView(textInputLayout, lp);
        }

        if (cantidad == 2) {
            TextInputLayout textInputLayout = new TextInputLayout(activiti);
            EditText cantidad1 = typeNumber(texto1, tipoNumero);
            textInputLayout.addView(cantidad1);
            map.put("id", id1);
            map.put("valor", cantidad1);
            listMapView.add(map);
            map = new HashMap<>();
            layout.addView(textInputLayout, lp);

            TextInputLayout textInputLayout1 = new TextInputLayout(activiti);
            EditText alValue = typeNumber(texto2, tipoNumero);
            textInputLayout1.addView(alValue);
            map.put("id", id2);
            map.put("valor", alValue);
            listMapView.add(map);
            map = new HashMap<>();
            layout.addView(textInputLayout1, lp);
        }

        if (cantidad == 3) {

            TextInputLayout textInputLayout = new TextInputLayout(activiti);
            EditText cantidad1 = typeNumber(texto1, tipoNumero);
            textInputLayout.addView(cantidad1);
            map.put("id", id1);
            map.put("valor", cantidad1);
            listMapView.add(map);
            map = new HashMap<>();
            layout.addView(textInputLayout, lp);

            TextInputLayout textInputLayout1 = new TextInputLayout(activiti);
            EditText alValue = typeNumber(texto2, tipoNumero);
            textInputLayout1.addView(alValue);
            map.put("id", id2);
            map.put("valor", alValue);
            listMapView.add(map);
            map = new HashMap<>();
            layout.addView(textInputLayout1, lp);

            TextInputLayout textInputLayout2 = new TextInputLayout(activiti);
            EditText larValue = typeNumber(texto3, tipoNumero);
            textInputLayout2.addView(larValue);
            map.put(texto3, larValue);
            map.put("id", id3);
            map.put("valor", larValue);
            listMapView.add(map);
            map = new HashMap<>();
            layout.addView(textInputLayout2, lp);
        }

        return layout;
    }

    public void basicView(Map<String, Object> map, LinearLayout.LayoutParams lp) {
        contenedor.addView(countEditext(map, lp, 3, "numero", "Cantidad", "Ancho (Metros)", "Largo (Metros)", 1, 5, 3));
        map = new HashMap<>();
    }

    public void vista(int option) {
        LinearLayout.LayoutParams valores = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        valores.weight = 1;
        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setOrientation(LinearLayout.HORIZONTAL);
        Map<String, Object> map = new HashMap<>();
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lp.weight = 1;
        lp.setMargins(0, 8, 0, 0);
        switch (option) {
            case 17:
                contenedor.addView(textoName("Cantidad", "numero", valores, map, 1));
                map = new HashMap<>();
                contenedor.addView(doubleLayout24(10, "Medallon", "Rotulado", 13, 32, 23, lp, map, "Camion", "Taxi", "Combi", "Otro", 37, 33, 2, 43, "Tipo", "Tipo vehiculo"));
                map = new HashMap<>();
                contenedor.addView(textoName("Placas", "", valores, map, 43));
                map = new HashMap<>();
                check(map);
                map = new HashMap<>();
                contenedor.addView(textoName("Comentarios", "", lp, map, 44));
                break;
            case 18:
                basicView(map, lp);
                map = new HashMap<>();
                contenedor.addView(simpleLayout13(10, lp, map, "Pantalla", "Cartel", "Lona", 34, 234, 43, "Tipo"));
                map = new HashMap<>();
                contenedor.addView(textoName("Placas", "", valores, map, 43));
                map = new HashMap<>();
                check(map);
                map = new HashMap<>();
                contenedor.addView(textoName("Comentarios", "", lp, map, 44));
                break;
            case 19:
                contenedor.addView(textoName("Cantidad", "numero", valores, map, 1));
                map = new HashMap<>();
                //contenedor.addView(doubleLayout22(4, "Si", "No", 10, 1, 2, valores, map, "Medallon", "Rotulado", 3, 4, "Publicidad", "Tipo"));
                contenedor.addView(simpleLayout13(10, lp, map, "Medallon", "Rotulado", "Otro", 65, 656, 7, "Tipo"));
                map = new HashMap<>();
                contenedor.addView(textoName("Placas", "", valores, map, 43));
                map = new HashMap<>();
                check(map);
                map = new HashMap<>();
                contenedor.addView(textoName("Comentarios", "", lp, map, 44));
                break;
            case 21:
                basicView(map, lp);
                map = new HashMap<>();
                contenedor.addView(simpleLayout13(10, lp, map, "Vagon", "Pasillo", "Otro", 54, 343, 32, "Tipo"));
                map = new HashMap<>();
                check(map);
                map = new HashMap<>();
                contenedor.addView(textoName("Comentarios", "", lp, map, 44));
                break;
            case 22:
                contenedor.addView(countEditext(map, lp, 1, "numero", "Cantidad", "", "", 0, 0, 0));
                //map = new HashMap<>();
                //contenedor.addView(textoName("Placas", "", valores, map, 43));
                map = new HashMap<>();
                check(map);
                map = new HashMap<>();
                contenedor.addView(textoName("Comentarios", "", lp, map, 44));
                break;
            case 23:
                basicView(map, lp);
                //map = new HashMap<>();
                //contenedor.addView(textoName("Placas", "", valores, map, 43));
                map = new HashMap<>();
                check(map);
                map = new HashMap<>();
                contenedor.addView(textoName("Comentarios", "", lp, map, 44));
                break;
            case 24:
                contenedor.addView(textoName("Cantidad", "numero", valores, map, 1));
                map = new HashMap<>();
                check(map);
                map = new HashMap<>();
                contenedor.addView(textoName("Comentarios", "", lp, map, 44));
                break;
            case 5:
                basicView(map, lp);
                map = new HashMap<>();
                check(map);
                map = new HashMap<>();
                contenedor.addView(textoName("Comentarios", "", lp, map, 44));
                break;
            case 6:
                basicView(map, lp);
                map = new HashMap<>();
                check(map);
                map = new HashMap<>();
                contenedor.addView(textoName("Comentarios", "", lp, map, 44));
                break;
            case 7:
                contenedor.addView(countEditext(map, lp, 3, "numero", "Cantidad", "Alto (Metros)", "Largo (Metros)", 1, 2, 3));
                map = new HashMap<>();
                check(map);
                map = new HashMap<>();
                contenedor.addView(textoName("Comentarios", "", lp, map, 44));
                break;
            case 8:
                basicView(map, lp);
                map = new HashMap<>();
                contenedor.addView(doubleLayout24(10, "Peatonal", "Vehicular", 4, 1, 2, valores, map, "Pintado", "Cartel", "Lona", "Otro", 3, 4, 5, 6, "Tipo", "Tipo publicidad"));
                map = new HashMap<>();
                check(map);
                map = new HashMap<>();
                contenedor.addView(textoName("Comentarios", "", lp, map, 44));
                break;
            case 9:
                basicView(map, lp);
                map = new HashMap<>();
                check(map);
                map = new HashMap<>();
                contenedor.addView(textoName("Comentarios", "", lp, map, 44));
                break;
            case 10:
                basicView(map, lp);
                map = new HashMap<>();
                contenedor.addView(simpleLayout13(10, lp, map, "Stand", "Kiosko", "Otro", 423, 4234, 324234, "Tipo"));
                map = new HashMap<>();
                check(map);
                map = new HashMap<>();
                contenedor.addView(textoName("Comentarios", "", lp, map, 44));
                break;
            case 11:
                contenedor.addView(countEditext(map, lp, 3, "numero", "Cantidad", "Alto (Metros)", "Largo (Metros)", 1, 2, 3));
                map = new HashMap<>();
                check(map);
                map = new HashMap<>();
                contenedor.addView(textoName("Comentarios", "", lp, map, 44));
                break;
            case 12:
                basicView(map, lp);
                map = new HashMap<>();
                check(map);
                map = new HashMap<>();
                contenedor.addView(textoName("Comentarios", "", lp, map, 44));
                break;
            /*case 13:
                basicView(map, lp);
                map = new HashMap<>();
                check(map);
                map = new HashMap<>();
                contenedor.addView(textoName("Comentarios","",lp,map,44));
                break;*/
            case 14:
                contenedor.addView(countEditext(map, lp, 1, "numero", "Cantidad", "", "", 1, 0, 0));
                map = new HashMap<>();
                contenedor.addView(simpleLayout14(10, lp, map, "Triptico", "Diptico", "Volante", "Otros", 2, 3, 4, 5, "Tipo"));
                map = new HashMap<>();
                check(map);
                map = new HashMap<>();
                contenedor.addView(textoName("Comentarios", "", lp, map, 44));
                break;
            /*case 15:
                contenedor.addView(countEditext(map, lp, 3, "numero", "Cantidad", "Alto (Metros)", "Largo (Metros)", 1, 2, 3));
                map = new HashMap<>();
                contenedor.addView(doubleLayout22(4, "Si", "No", 5, 1, 2, valores, map, "Gruesa", "Delgada", 3, 4, "Publicidad", "Ancho"));
                map = new HashMap<>();
                check(map);
                map = new HashMap<>();
                contenedor.addView(textoName("Comentarios","",lp,map,44));
                break;*/
            case 16:
                basicView(map, lp);
                map = new HashMap<>();
                check(map);
                map = new HashMap<>();
                contenedor.addView(textoName("Comentarios", "", lp, map, 44));
                break;
            /*case 130:
                basicView(map, lp);
                map = new HashMap<>();
                check(map);
                map = new HashMap<>();
                contenedor.addView(textoName("Comentarios","",lp,map,44));
                break;*/
            case 131:
                contenedor.addView(countEditext(map, lp, 2, "numero", "Cantidad", "Pulgadas", "", 1, 16, 0));
                map = new HashMap<>();
                contenedor.addView(simpleLayout14(47, lp, map, "Camion", "Metro", "Tren", "Otros", 343, 545, 65, 5, "Ubicación"));
                map = new HashMap<>();
                check(map);
                map = new HashMap<>();
                contenedor.addView(textoName("Comentarios", "", lp, map, 44));
                break;
            case 132:
                basicView(map, lp);
                map = new HashMap<>();
                check(map);
                map = new HashMap<>();
                contenedor.addView(textoName("Comentarios", "", lp, map, 44));
                break;
            case 133:
                basicView(map, lp);
                map = new HashMap<>();
                check(map);
                map = new HashMap<>();
                contenedor.addView(textoName("Comentarios", "", lp, map, 44));
                break;
            case 134:
                basicView(map, lp);
                map = new HashMap<>();
                check(map);
                map = new HashMap<>();
                contenedor.addView(textoName("Comentarios", "", lp, map, 44));
                break;
            case 135:
                basicView(map, lp);
                map = new HashMap<>();
                check(map);
                map = new HashMap<>();
                contenedor.addView(textoName("Comentarios", "", lp, map, 44));
                break;
            case 136:
                basicView(map, lp);
                map = new HashMap<>();
                contenedor.addView(simpleLayout14(10, lp, map, "Banca", "Jardineria", "Muppi", "Otros", 5, 5, 6, 7, "Tipo"));
                map = new HashMap<>();
                check(map);
                map = new HashMap<>();
                contenedor.addView(textoName("Comentarios", "", lp, map, 44));
                break;
            case 137:
                basicView(map, lp);
                map = new HashMap<>();
                check(map);
                map = new HashMap<>();
                contenedor.addView(textoName("Comentarios", "", lp, map, 44));
                break;
            case 139:
                basicView(map, lp);
                map = new HashMap<>();
                check(map);
                map = new HashMap<>();
                contenedor.addView(textoName("Comentarios", "", lp, map, 44));
                break;
            case 140:
                basicView(map, lp);
                map = new HashMap<>();
                check(map);
                map = new HashMap<>();
                contenedor.addView(textoName("Comentarios", "", lp, map, 44));
                break;
            case 160:
                LinearLayout l334 = countEditext(map, lp, 1, "numero", "Cantidad", "", "", 1, 0, 0);
                map = new HashMap<>();
                contenedor.addView(l334);
                contenedor.addView(textoName("Capacidad", "numero", lp, map, 37));
                map = new HashMap<>();
                check(map);
                map = new HashMap<>();
                contenedor.addView(textoName("Comentarios", "", lp, map, 44));
                break;
            case 161:
                contenedor.addView(countEditext(map, lp, 1, "numero", "Cantidad", "", "", 1, 0, 0));
                map = new HashMap<>();
                contenedor.addView(simpleLayout14(27,lp,map,"Chico","Mediano","Grande","Otro",321,456,432,44,"Tamaño"));
                map = new HashMap<>();
                check(map);
                map = new HashMap<>();
                contenedor.addView(textoName("Comentarios", "", lp, map, 44));
                break;
            case 162:
                contenedor.addView(countEditext(map, lp, 2, "numero", "Cantidad", "Cantidad integrantes", "", 1, 30, 0));
                map = new HashMap<>();
                check(map);
                map = new HashMap<>();
                contenedor.addView(textoName("Comentarios", "", lp, map, 44));
                break;
            case 163:
                contenedor.addView(countEditext(map, lp, 1, "numero", "Cantidad", "", "", 1, 0, 0));
                map = new HashMap<>();
                contenedor.addView(simpleLayout12(48,lp,map,"Si", "No", "Microfono"));
                map = new HashMap<>();
                check(map);
                map = new HashMap<>();
                contenedor.addView(textoName("Comentarios", "", lp, map, 44));
                break;
            case 164:
                contenedor.addView(countEditext(map, lp, 1, "numero", "Cantidad", "", "", 1, 0, 0));
                map = new HashMap<>();
                check(map);
                map = new HashMap<>();
                contenedor.addView(textoName("Comentarios", "", lp, map, 44));
                break;
            case 165:
                contenedor.addView(countEditext(map, lp, 1, "numero", "Cantidad", "", "", 1, 0, 0));
                map = new HashMap<>();
                check(map);
                map = new HashMap<>();
                contenedor.addView(textoName("Comentarios", "", lp, map, 44));
                break;
            case 166:
                basicView(map,lp);
                map = new HashMap<>();
                check(map);
                map = new HashMap<>();
                contenedor.addView(textoName("Comentarios", "", lp, map, 44));
                break;
            case 167:
                basicView(map,lp);
                map = new HashMap<>();
                check(map);
                map = new HashMap<>();
                contenedor.addView(textoName("Comentarios", "", lp, map, 44));
                break;
            case 168:
                contenedor.addView(countEditext(map, lp, 1, "numero", "Cantidad", "", "", 1, 0, 0));
                map = new HashMap<>();
                contenedor.addView(simpleLayout12(10,lp,map,"Bordada", "Estampado", "Tipo"));
                map = new HashMap<>();
                check(map);
                map = new HashMap<>();
                contenedor.addView(textoName("Comentarios", "", lp, map, 44));
                break;
            case 169:
                contenedor.addView(countEditext(map, lp, 1, "numero", "Cantidad", "", "", 1, 0, 0));
                map = new HashMap<>();
                check(map);
                map = new HashMap<>();
                contenedor.addView(textoName("Comentarios", "", lp, map, 44));
                break;
            case 170:
                contenedor.addView(countEditext(map, lp, 1, "numero", "Cantidad", "", "", 1, 0, 0));
                map = new HashMap<>();
                check(map);
                map = new HashMap<>();
                contenedor.addView(textoName("Comentarios", "", lp, map, 44));
                break;
            case 171:
                contenedor.addView(countEditext(map, lp, 1, "numero", "Cantidad", "", "", 1, 0, 0));
                map = new HashMap<>();
                check(map);
                map = new HashMap<>();
                contenedor.addView(textoName("Comentarios", "", lp, map, 44));
                break;
            case 172:
                contenedor.addView(countEditext(map, lp, 1, "numero", "Cantidad", "", "", 1, 0, 0));
                map = new HashMap<>();
                check(map);
                map = new HashMap<>();
                contenedor.addView(textoName("Comentarios", "", lp, map, 44));
                break;
            case 173:
                contenedor.addView(countEditext(map, lp, 1, "numero", "Cantidad", "", "", 1, 0, 0));
                map = new HashMap<>();
                contenedor.addView(simpleLayout13(10,lp,map,"Sobrero","Vicera","Otro",15,531,31,"Tipo"));
                map = new HashMap<>();
                check(map);
                map = new HashMap<>();
                contenedor.addView(textoName("Comentarios", "", lp, map, 44));
                break;
            case 174:
                basicView(map,lp);
                map = new HashMap<>();
                check(map);
                map = new HashMap<>();
                contenedor.addView(textoName("Comentarios", "", lp, map, 44));
                break;
            case 175:
                contenedor.addView(countEditext(map, lp, 1, "numero", "Cantidad", "", "", 1, 0, 0));
                map = new HashMap<>();
                contenedor.addView(simpleLayout13(10,lp,map,"Solo sandwich","Completo","Otro",15,531,31,"Tipo"));
                map = new HashMap<>();
                check(map);
                map = new HashMap<>();
                contenedor.addView(textoName("Comentarios", "", lp, map, 44));
                break;
            case 176:
                contenedor.addView(countEditext(map, lp, 1, "numero", "Cantidad", "", "", 1, 0, 0));
                map = new HashMap<>();
                check(map);
                map = new HashMap<>();
                contenedor.addView(textoName("Comentarios", "", lp, map, 44));
                break;
            case 177:
                contenedor.addView(countEditext(map, lp, 1, "numero", "Cantidad", "", "", 1, 0, 0));
                map = new HashMap<>();
                contenedor.addView(simpleLayout14(10,lp,map,"Redonda", "Tablon", "Cuadrada", "Otro",45,7757,557,89,"Tipo"));
                map = new HashMap<>();
                check(map);
                map = new HashMap<>();
                contenedor.addView(textoName("Comentarios", "", lp, map, 44));
                break;
            case 178:
                contenedor.addView(countEditext(map, lp, 1, "numero", "Cantidad", "", "", 1, 0, 0));
                map = new HashMap<>();
                check(map);
                map = new HashMap<>();
                contenedor.addView(textoName("Comentarios", "", lp, map, 44));
                break;
            case 179:
                basicView(map,lp);
                map = new HashMap<>();
                check(map);
                map = new HashMap<>();
                contenedor.addView(textoName("Comentarios", "", lp, map, 44));
                break;
            case 180:
                basicView(map,lp);
                map = new HashMap<>();
                check(map);
                map = new HashMap<>();
                contenedor.addView(textoName("Comentarios", "", lp, map, 44));
                break;
            case 181:
                contenedor.addView(countEditext(map, lp, 1, "numero", "Cantidad", "", "", 1, 0, 0));
                map = new HashMap<>();
                contenedor.addView(simpleLayout13(10,lp,map,"Tela","Plastico","Otro",15,531,31,"Tipo"));
                map = new HashMap<>();
                check(map);
                map = new HashMap<>();
                contenedor.addView(textoName("Comentarios", "", lp, map, 44));
                break;
            case 182:
                contenedor.addView(countEditext(map, lp, 1, "numero", "Cantidad", "", "", 1, 0, 0));
                map = new HashMap<>();
                contenedor.addView(simpleLayout12(10,lp,map,"Polo", "T-shirt", "Tipo"));
                map = new HashMap<>();
                check(map);
                map = new HashMap<>();
                contenedor.addView(textoName("Comentarios", "", lp, map, 44));
                break;
            case 183:
                contenedor.addView(countEditext(map, lp, 1, "numero", "Cantidad", "", "", 1, 0, 0));
                map = new HashMap<>();
                check(map);
                map = new HashMap<>();
                contenedor.addView(textoName("Comentarios", "", lp, map, 44));
                break;
            case 184:
                basicView(map,lp);
                map = new HashMap<>();
                check(map);
                map = new HashMap<>();
                contenedor.addView(textoName("Comentarios", "", lp, map, 44));
                break;
            case 185:
                contenedor.addView(countEditext(map, lp, 1, "numero", "Cantidad", "", "", 1, 0, 0));
                map = new HashMap<>();
                check(map);
                map = new HashMap<>();
                contenedor.addView(textoName("Comentarios", "", lp, map, 44));
                break;
            case 186:
                contenedor.addView(countEditext(map, lp, 1, "numero", "Cantidad", "", "", 1, 0, 0));
                map = new HashMap<>();
                check(map);
                map = new HashMap<>();
                contenedor.addView(textoName("Comentarios", "", lp, map, 44));
                break;
            case 187:
                contenedor.addView(countEditext(map, lp, 1, "numero", "Cantidad", "", "", 1, 0, 0));
                map = new HashMap<>();
                contenedor.addView(simpleLayout12(10,lp,map,"Cilindro", "Thermo", "Tipo"));
                map = new HashMap<>();
                check(map);
                map = new HashMap<>();
                contenedor.addView(textoName("Comentarios", "", lp, map, 44));
                break;
            case 188:
                contenedor.addView(countEditext(map, lp, 1, "numero", "Cantidad", "", "", 1, 0, 0));
                map = new HashMap<>();
                contenedor.addView(simpleLayout13(10,lp,map,"Dipticos","Tripticos","Otro",15,531,31,"Tipo"));
                map = new HashMap<>();
                check(map);
                map = new HashMap<>();
                contenedor.addView(textoName("Comentarios", "", lp, map, 44));
                break;
            case 189:
                basicView(map,lp);
                map = new HashMap<>();
                check(map);
                map = new HashMap<>();
                contenedor.addView(textoName("Comentarios", "", lp, map, 44));
                break;
            default:
                break;
        }
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(2, 20, 2, 0);
        Button btnFoto = new Button(FormActivity.this);
        btnFoto.setBackground(getDrawable(R.drawable.btn_oval));
        btnFoto.setText("Tomar fotos");
        btnFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ServicesUbicacion.myLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    if (img.size() < 3) {
                        uri = Tools.photo(FormActivity.this, TAKE_PICTURE);
                    } else {
                        Tools.imprime("Las imagenes ya han sido tomadas, favor de presionar el botón de capturar datos", FormActivity.this);
                    }
                } else {
                    Tools.encenderGPS(FormActivity.this);
                }
            }
        });
        ////
        Button btnGale = new Button(FormActivity.this);
        btnGale.setBackground(getDrawable(R.drawable.btn_oval));
        btnGale.setText("Galería");
        btnGale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (img.size() < 3) {
                    if (ServicesUbicacion.myLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        carrete();
                    } else {
                        Tools.encenderGPS(FormActivity.this);
                    }
                } else {
                    Tools.imprime("Las imagenes ya han sido tomadas, favor de presionar el botón de capturar datos", FormActivity.this);
                }
            }
        });
        ////
        LinearLayout l = new LinearLayout(FormActivity.this);
        params.weight = 1;
        l.addView(btnFoto, params);
        l.addView(btnGale, params);
        Button btn = new Button(FormActivity.this);
        btn.setBackground(getDrawable(R.drawable.btn_oval));
        btn.setText("Capturar datos");
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (img.size() >= 3) {
                    new AsyncTask() {
                        @Override
                        protected Object doInBackground(Object[] objects) {
                            internet = Tools.isOnlineNet();
                            return null;
                        }

                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();
                            pDialog = Tools.esperaDialog(FormActivity.this, "Comprobando internet");
                        }

                        @Override
                        protected void onPostExecute(Object o) {
                            super.onPostExecute(o);
                            Tools.cancelarDialog(pDialog);
                            outData();
                        }
                    }.execute();
                } else {
                    Tools.imprime("Las imagenes son obligatorias", FormActivity.this);
                }
            }
        });
        contenedor.addView(l, params);
        contenedor.addView(btn, params);
    }

    public LinearLayout doubleLayout22(int id1, String radtext1, String radtext2, int id2, int nu11, int nu12, LinearLayout.LayoutParams lp, Map<String, Object> map, String text21, String text22, int nu1, int nu2, String text, String text2) {

        LinearLayout contLaout3 = new LinearLayout(activiti);
        contLaout3.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout linearLayout14 = new LinearLayout(activiti);
        linearLayout14.setOrientation(LinearLayout.VERTICAL);

        TextView nameRadio8 = new TextView(activiti);
        nameRadio8.setTextSize(22);
        nameRadio8.setGravity(Gravity.CENTER);
        nameRadio8.setText(text);
        linearLayout14.addView(nameRadio8);

        RadioGroup rg9 = radios(radtext1, radtext2, 18, nu11, nu12);
        map.put("id", id1);
        map.put("valor", rg9);
        listMapView.add(map);
        map = new HashMap<>();
        linearLayout14.addView(rg9);
        LinearLayout linearLayout15 = new LinearLayout(activiti);
        linearLayout15.setOrientation(LinearLayout.VERTICAL);
        TextView nameRadio9 = new TextView(activiti);
        nameRadio9.setTextSize(22);
        nameRadio9.setGravity(Gravity.CENTER);
        nameRadio9.setText(text2);
        linearLayout15.addView(nameRadio9);

        RadioGroup rg10 = radios(text21, text22, 18, nu1, nu2);
        map.put("id", id2);
        map.put("valor", rg10);
        listMapView.add(map);
        map = new HashMap<>();
        linearLayout15.addView(rg10);


        contLaout3.addView(linearLayout15, lp);
        contLaout3.addView(linearLayout14, lp);
        return contLaout3;
    }

    public LinearLayout textoName(String titulo, String numero, LinearLayout.LayoutParams lp, Map<String, Object> map, int idAtributo) {
        LinearLayout layout19 = new LinearLayout(getApplicationContext());
        layout19.setOrientation(LinearLayout.VERTICAL);

        TextInputLayout textInputLayout5 = new TextInputLayout(activiti);
        EditText alValues1 = typeNumber(titulo, numero);

        textInputLayout5.addView(alValues1);
        map.put("id", idAtributo);
        map.put("valor", alValues1);
        listMapView.add(map);
        map = new HashMap<>();
        layout19.addView(textInputLayout5, lp);
        return layout19;
    }

    public LinearLayout doubleLayout24(int id1, String radtext1, String radtext2, int id2, int nu11, int nu12, LinearLayout.LayoutParams lp, Map<String, Object> map, String text21, String text22, String text23, String text24, int nu1, int nu2, int nu3, int nu4, String text, String text2) {

        LinearLayout contLaout3 = new LinearLayout(activiti);
        contLaout3.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout linearLayout14 = new LinearLayout(activiti);
        linearLayout14.setOrientation(LinearLayout.VERTICAL);

        TextView nameRadio8 = new TextView(activiti);
        nameRadio8.setTextSize(22);
        nameRadio8.setGravity(Gravity.CENTER);
        nameRadio8.setText(text);
        linearLayout14.addView(nameRadio8);

        RadioGroup rg9 = radios(radtext1, radtext2, 18, nu11, nu12);
        map.put("id", id1);
        map.put("valor", rg9);
        listMapView.add(map);
        map = new HashMap<>();
        linearLayout14.addView(rg9);
        LinearLayout linearLayout15 = new LinearLayout(activiti);
        linearLayout15.setOrientation(LinearLayout.VERTICAL);

        TextView nameRadio9 = new TextView(activiti);
        nameRadio9.setTextSize(22);
        nameRadio9.setGravity(Gravity.CENTER);
        nameRadio9.setText(text2);
        linearLayout15.addView(nameRadio9);
        RadioGroup rg10 = radios4(text21, text22, text23, text24, 18, nu1, nu2, nu3, nu4);
        map.put("id", id2);
        map.put("valor", rg10);
        listMapView.add(map);
        map = new HashMap<>();
        linearLayout15.addView(rg10);
        contLaout3.addView(linearLayout15, lp);
        contLaout3.addView(linearLayout14, lp);
        return contLaout3;
    }

    public RadioGroup radios4(String radio1, String radio2, String radio3, String radio4, int size, int id, int id2, int id3, int id4) {

        RadioGroup rg = new RadioGroup(activiti);
        RadioButton rb = new RadioButton(activiti);
        rb.setText(radio1);
        //rb.setId((Integer) id);
        rb.setTextSize(size);
        rg.addView(rb);


        RadioButton rb1 = new RadioButton(activiti);
        rb1.setText(radio2);
        //rb1.setId((Integer) id2);
        rb1.setTextSize(size);
        rg.addView(rb1);

        RadioButton rb2 = new RadioButton(activiti);
        rb2.setText(radio3);
        //rb2.setId((Integer) id3);
        rb2.setTextSize(size);
        rg.addView(rb2);

        RadioButton rb3 = new RadioButton(activiti);
        rb3.setText(radio4);
        //rb3.setId((Integer) id4);
        rb3.setTextSize(size);
        rg.addView(rb3);

        return rg;

    }

    public RadioGroup radios3(String radio1, String radio2, String radio3, int size, int id, int id2, int id3) {

        RadioGroup rg = new RadioGroup(activiti);
        RadioButton rb = new RadioButton(activiti);
        rb.setText(radio1);
        //rb.setId((Integer) id);
        rb.setTextSize(size);
        rg.addView(rb);
        RadioButton rb1 = new RadioButton(activiti);
        rb1.setText(radio2);
        //rb1.setId((Integer) id2);
        rb1.setTextSize(size);
        rg.addView(rb1);
        RadioButton rb2 = new RadioButton(activiti);
        rb2.setText(radio3);
        //rb2.setId((Integer) id3);
        rb2.setTextSize(size);
        rg.addView(rb2);
        return rg;
    }

    public RadioGroup radios2(String radio1, String radio2, int size) {

        RadioGroup rg = new RadioGroup(activiti);
        RadioButton rb = new RadioButton(activiti);
        rb.setText(radio1);
        //rb.setId((Integer) id);
        rb.setTextSize(size);
        rg.addView(rb);
        RadioButton rb1 = new RadioButton(activiti);
        rb1.setText(radio2);
        //rb1.setId((Integer) id2);
        rb1.setTextSize(size);
        rg.addView(rb1);
        return rg;
    }

    public void outData() {
        array = new JSONArray();
        try {
            for (Map<String, Object> m : listMapView) {
                int id = (int) m.get("id");
                Log.e("ID", "++++++" + id);
                String valor = "";
                Object o = m.get("valor");
                JSONObject json = new JSONObject();
                if (o instanceof EditText) {
                    EditText e = (EditText) o;
                    if (Tools.validarVacios(e.getText().toString())) {
                        valor = e.getText().toString();
                        Log.e("val", valor);
                    } else {
                        array = new JSONArray();
                        e.setError("Este campo es obligatorio");
                        return;
                    }

                } else if (o instanceof RadioGroup) {
                    RadioGroup rg = (RadioGroup) o;
                    int aux = rg.getChildCount();
                    for (int in = 0; in < aux; in++) {
                        RadioButton rb = (RadioButton) rg.getChildAt(in);
                        if (rb.isChecked())
                            valor = rb.getText().toString();
                    }
                    if (valor.equals("")) {
                        array = new JSONArray();
                        Tools.imprime("selecciona una opción", FormActivity.this);
                        return;
                    }
                } else {
                    //Log.e("LINEAR","Entro");
                    LinearLayout l = (LinearLayout) o;
                    int xyz = l.getChildCount();
                    JSONArray jsonArray = new JSONArray();
                    // Log.e("TAMA",""+xyz);
                    for (int i = 0; i < xyz; i++) {
                        View view = l.getChildAt(i);
                        if (view instanceof CheckBox) {
                            CheckBox cb = (CheckBox) view;
                            if (cb.isChecked()) {
                                //Log.e("ENTRO","OKA");
                                jsonArray.put(cb.getText().toString());
                            }
                        } else {
                            if (!(view instanceof TextView)) {
                                RadioGroup rg = (RadioGroup) view;
                                int aux = rg.getChildCount();
                                for (int in = 0; in < aux; in++) {
                                    RadioButton rb = (RadioButton) rg.getChildAt(in);
                                    if (rb.isChecked())
                                        valor = rb.getText().toString();
                                }
                                if (valor.equals("")) {
                                    array = new JSONArray();
                                    Tools.imprime("selecciona una opción", FormActivity.this);
                                    return;
                                }
                            }
                        }
                    }
                    if (jsonArray.length() > 0) {
                        json.put("id", id);
                        json.put("valor", jsonArray);
                    } else if (jsonArray.length() == 0 && !valor.equals("")) {

                    } else {
                        Tools.imprime("El campo de publicidad compartida es obligatorio", FormActivity.this);
                        return;
                    }
                }
                if (!json.has("id")) {
                    json.put("id", id);
                    json.put("valor", valor);
                }
                array.put(json);
                valor = "";
                id = 0;
            }
            Log.e("JSON_ATR", array.toString());
            pDialog = Tools.esperaDialog(FormActivity.this, "Enviando Datos");
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (!ServicesUbicacion.lat.equals("") && !ServicesUbicacion.lang.equals("")) {
                        timer.cancel();
                        sendData(array);
                    }
                }
            }, 100, 1000);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendData(final JSONArray data) {
        if (img.size() > 2) {
            if (Tools.checkNow(FormActivity.this) && internet) {
                new AsyncTask() {
                    @Override
                    protected Object doInBackground(Object[] objects) {
                        DataSource dataSource = new DataSource(FormActivity.this);
                        int idPartido = partido;
                        int idUser = dataSource.idUser();
                        dataSource.closeDataBase();
                        ArrayList<String> im = (ArrayList<String>) objects[1];
                        JSONArray array = new JSONArray();
                        Tools tools = new Tools();
                        for (String s : im) {
                            array.put(tools.getImg64(FormActivity.this, s));
                            Tools.deleteImage(s, FormActivity.this);
                        }
                        try {
                            if (cat.equals("Brigada")){
                                return new ConexionService(FormActivity.this).sendActivacion("info", idUser, ServicesUbicacion.lat, ServicesUbicacion.lang, idPartido, idEvento, "Activación", array, subCatego, (JSONArray) objects[0], cat.toLowerCase(), subC.toLowerCase(), null);
                            }else{
                                return new ConexionService(FormActivity.this).sendForm("insert", idUser, ServicesUbicacion.lat, ServicesUbicacion.lang, idPartido, idEvento, "Tierra", array, subCatego, (JSONArray) objects[0], cat.toLowerCase(), subC.toLowerCase(), null);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            return new ArrayList<>();
                        }
                    }

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
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
                                    for (String s : img) {
                                        Tools.deleteImage(s, FormActivity.this);
                                    }
                                    Tools.imprimeFinish("Información enviada", FormActivity.this, FormActivity.this);
                                } else {
                                    Tools.imprimeFinish((String) respuesta.get(2), FormActivity.this, FormActivity.this);
                                }
                            } else if (res.equals(getString(R.string.response_server_error))) {
                                Tools.imprimeFinish(getString(R.string.str_error_server) + "\n (05:07:" + respuesta.get(1).toString() + "), la información sera enviada posteriormente", FormActivity.this, FormActivity.this);
                            } else if (res.equals(getString(R.string.response_time_out))) {
                                //aqui ira si no hay internet
                                Tools.imprimeFinish(getString(R.string.str_instente_mas_tarde) + "\n (05:07:" + respuesta.get(1) + "), la información sera enviada posteriormente", FormActivity.this, FormActivity.this);
                            } else {
                                Tools.imprime(getString(R.string.str_list_error) + "\n (05:07:01)", FormActivity.this);
                            }
                        }
                    }
                }.execute(data, img);
            } else {
                new AsyncTask() {
                    @Override
                    protected Object doInBackground(Object[] objects) {
                        dataSource = new DataSource(FormActivity.this);
                        int idCiudadano = dataSource.idUser();
                        dataSource.closeDataBase();
                        JSONArray array = new JSONArray();
                        Tools tools = new Tools();
                        for (String s : img) {
                            array.put(tools.getImg64(FormActivity.this, s));
                        }
                        try {
                            JSONObject jason = new JSONObject();
                            jason.put("ciudadano", idCiudadano);
                            jason.put("latitud", ServicesUbicacion.lat);
                            jason.put("longitud", ServicesUbicacion.lang);
                            jason.put("tipo", subCatego);
                            jason.put("habitacion", "");
                            jason.put("partido", partido);
                            jason.put("idEvento", idEvento);
                            jason.put("option", "insert");
                            jason.put("data", data);
                            jason.put("descripcion", "Tierra");
                            jason.put("cate", cat);
                            jason.put("subC", subC);
                            jason.put("imagen", array);
                            dataSource = new DataSource(FormActivity.this);
                            dataSource.insertImg(jason.toString());
                            dataSource.closeDataBase();
                            Intent intent = new Intent(FormActivity.this, ServicioSend.class);
                            FormActivity.this.startService(intent);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        // pDialog = Tools.esperaDialog(FormActivity.this, "Enviando Datos");
                    }

                    @Override
                    protected void onPostExecute(Object o) {
                        super.onPostExecute(o);
                        Tools.cancelarDialog(pDialog);
                        Tools.imprimeFinish("No cuenta con conexión a internet, las imagenes seran enviadas automaticamente más tarde", FormActivity.this, FormActivity.this);
                    }
                }.execute();
            }
        } else {
            Tools.imprime("Las imagenes son necesarias para enviar la información", FormActivity.this);
        }
    }

    public void manageImage(final int requestCode, final int resultCode, final @Nullable ArrayList<String> imgs) {
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                if ((requestCode == TAKE_PICTURE && resultCode == RESULT_OK) || (requestCode == GALLERY && resultCode == RESULT_OK)) {
                    if (imgs == null) {
                        imagePath = getRealPathFromURI(uri);
                        img.add(imagePath);
                    } else {
                        img = imgs;
                        for (String s : imgs){
                            if (s==null){
                                img=null;
                                break;
                            }
                        }
                    }

                }
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                if (img!=null){
                    Toast.makeText(getApplicationContext(), "Fotos: " + img.size() + "/3", Toast.LENGTH_SHORT).show();
                    if (img.size() == 3) {
                        Tools.imprime("Se han capturado las imagenes presione Capturar datos para enviar la información", FormActivity.this);
                    } else {
                        uri = Tools.photo(FormActivity.this, TAKE_PICTURE);
                    }
                }else{
                    Tools.imprime("Ocurrio un error al seleccionar las imagenes de la galería, favor de seleccionarlas nuevamente, en caso de persistir el error reiniciar el celular", FormActivity.this);
                }
            }
        }.execute();
    }

    public void check(Map<String, Object> map) {
        LinearLayout l3 = new LinearLayout(FormActivity.this);
        TextView lbl = new TextView(FormActivity.this);
        lbl.setTextSize(20);
        lbl.setText("Publicidad compartida");
        l3.setOrientation(LinearLayout.VERTICAL);
        l3.addView(lbl);
        CheckBox cb1 = new CheckBox(FormActivity.this);
        cb1.setText("Presidente");
        cb1.setTextSize(18);
        l3.addView(cb1);
        CheckBox cb2 = new CheckBox(FormActivity.this);
        cb2.setText("Gobernador");
        cb2.setTextSize(18);
        l3.addView(cb2);
        CheckBox cb3 = new CheckBox(FormActivity.this);
        cb3.setText("Senador");
        cb3.setTextSize(18);
        l3.addView(cb3);
        CheckBox cb4 = new CheckBox(FormActivity.this);
        cb4.setText("Diputado federal");
        cb4.setTextSize(18);
        l3.addView(cb4);
        CheckBox cb7 = new CheckBox(FormActivity.this);
        cb7.setText("Diputado local");
        cb7.setTextSize(18);
        l3.addView(cb7);
        CheckBox cb5 = new CheckBox(FormActivity.this);
        cb5.setText("Alcalde");
        cb5.setTextSize(18);
        l3.addView(cb5);
        CheckBox cb6 = new CheckBox(FormActivity.this);
        cb6.setText("Partido");
        cb6.setTextSize(18);
        l3.addView(cb6);
        map.put("id", 42);
        map.put("valor", l3);
        listMapView.add(map);
        contenedor.addView(l3);
    }

    public LinearLayout simpleLayout14(int id2, LinearLayout.LayoutParams lp, Map<String, Object> map, String text21, String text22, String text23, String text24, int nu1, int nu2, int nu3, int nu4, String text2) {

        LinearLayout contLaout3 = new LinearLayout(activiti);
        contLaout3.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout linearLayout15 = new LinearLayout(activiti);
        linearLayout15.setOrientation(LinearLayout.VERTICAL);

        TextView nameRadio9 = new TextView(activiti);
        nameRadio9.setTextSize(22);
        nameRadio9.setGravity(Gravity.CENTER);
        nameRadio9.setText(text2);
        linearLayout15.addView(nameRadio9);
        RadioGroup rg10 = radios4(text21, text22, text23, text24, 18, nu1, nu2, nu3, nu4);
        map.put("id", id2);
        map.put("valor", rg10);
        listMapView.add(map);
        map = new HashMap<>();
        linearLayout15.addView(rg10);
        contLaout3.addView(linearLayout15, lp);
        return contLaout3;
    }

    public LinearLayout simpleLayout13(int id2, LinearLayout.LayoutParams lp, Map<String, Object> map, String text21, String text22, String text23, int nu1, int nu2, int nu3, String text2) {

        LinearLayout contLaout3 = new LinearLayout(activiti);
        contLaout3.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout linearLayout15 = new LinearLayout(activiti);
        linearLayout15.setOrientation(LinearLayout.VERTICAL);

        TextView nameRadio9 = new TextView(activiti);
        nameRadio9.setTextSize(22);
        nameRadio9.setGravity(Gravity.CENTER);
        nameRadio9.setText(text2);
        linearLayout15.addView(nameRadio9);

        RadioGroup rg10 = radios3(text21, text22, text23, 18, 23, 54, 58);
        map.put("id", id2);
        map.put("valor", rg10);
        listMapView.add(map);
        map = new HashMap<>();
        linearLayout15.addView(rg10);
        contLaout3.addView(linearLayout15, lp);
        return contLaout3;
    }

    public LinearLayout simpleLayout12(int id2, LinearLayout.LayoutParams lp, Map<String, Object> map, String text21, String text22, String text2) {

        LinearLayout contLaout3 = new LinearLayout(activiti);
        contLaout3.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout linearLayout15 = new LinearLayout(activiti);
        linearLayout15.setOrientation(LinearLayout.VERTICAL);
        TextView nameRadio9 = new TextView(activiti);
        nameRadio9.setTextSize(22);
        nameRadio9.setGravity(Gravity.CENTER);
        nameRadio9.setText(text2);
        linearLayout15.addView(nameRadio9);

        RadioGroup rg10 = radios2(text21, text22, 18);
        map.put("id", id2);
        map.put("valor", rg10);
        listMapView.add(map);
        map = new HashMap<>();
        linearLayout15.addView(rg10);
        contLaout3.addView(linearLayout15, lp);
        return contLaout3;
    }

    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(column_index);
        return path;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 899) {
            if (ServicesUbicacion.myLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                uri = Tools.photo(FormActivity.this, TAKE_PICTURE);
            } else {
                Tools.encenderGPS(FormActivity.this);
            }
        } else if (resultCode == RESULT_OK && requestCode == GALLERY) {
            Log.e("ENTRO", "ELSEIF");
            ArrayList<String> temp = manageGallery(data);
            if (temp != null) {
                manageImage(requestCode, resultCode, temp);
            }
        } else {
            Log.e("ENTRO", "ELSE");
            manageImage(requestCode, resultCode, null);
        }
    }

    private void carrete() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, GALLERY);
    }

    private ArrayList<String> manageGallery(Intent intent) {
        ArrayList<String> photos = new ArrayList<>();
        //Log.e("PHOTO","otro"+intent.getData());
        if (intent.getData() == null) {
            ClipData clipData = intent.getClipData();
            if (clipData.getItemCount() == 3) {
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    ClipData.Item item = clipData.getItemAt(i);
                    Uri u = item.getUri();
                    String imageEncoded;
                    String wholeID = DocumentsContract.getDocumentId(u);
                    String id = wholeID.split(":")[1];
                    Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            new String[]{MediaStore.Images.Media.DATA}, MediaStore.Images.Media._ID + "=?", new String[]{id}, null);
                    if (cursor.moveToFirst()) {
                        int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                        imageEncoded = cursor.getString(columnIndex);
                        photos.add(imageEncoded);
                    }
                    cursor.close();
                }
            } else {
                Tools.imprime("Favor de seleccionar 3 imagenes", FormActivity.this);
                return null;
            }
        } else {
            Tools.imprime("Favor de seleccionar 3 imagenes", FormActivity.this);
            return null;
        }
        return photos;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.simple_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
