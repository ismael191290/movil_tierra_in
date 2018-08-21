package com.integra.tierra;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

public class PartidoActivity extends AppCompatActivity {

    private ImageView imgPRI,imgPRD,imgMorena;
    private Intent intent;
    private int action;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_partido);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        intent = new Intent(PartidoActivity.this, CategoriaActivity.class);
        action=getIntent().getIntExtra("action",0);
        imgPRI = findViewById(R.id.imgPRI);
        imgPRD = findViewById(R.id.imgPRD);
        imgMorena = findViewById(R.id.imgMorena);
        imgPRI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.putExtra("partido",1);
                intent.putExtra("action",action);
                intent.putExtra("partStr","PRI");
                startActivity(intent);
            }
        });
        imgPRD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.putExtra("partido",2);
                intent.putExtra("action",action);
                intent.putExtra("partStr","PRD");
                startActivity(intent);
            }
        });
        imgMorena.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.putExtra("partido",4);
                intent.putExtra("action",action);
                intent.putExtra("partStr","MOR");
                startActivity(intent);
            }
        });
    }

}
