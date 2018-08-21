package com.integra.tierra.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.integra.tierra.R;
import com.integra.tierra.beans.Categoria;

import java.util.List;

/**
 * Created by gruposantoro3 on 04/01/2018.
 */

public class AdapterCategoria extends ArrayAdapter<Categoria> {

    private Context c;
    private List<Categoria> data;

    public AdapterCategoria(@NonNull Context context, int resource, @NonNull List<Categoria> objects) {
        super(context, resource, objects);
        this.data=objects;
        this.c=context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView==null){
            convertView = ((LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.adapter_categoria,null);
        }
        TextView lblCatego = (TextView) convertView.findViewById(R.id.lblCatego);
        ImageView img = (ImageView) convertView.findViewById(R.id.img);
        lblCatego.setText(data.get(position).getCategoria());
        //img.setIm//se pone la imagen con glide
        return convertView;
    }
}
