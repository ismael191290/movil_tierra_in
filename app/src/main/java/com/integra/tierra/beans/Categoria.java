package com.integra.tierra.beans;

import android.support.annotation.NonNull;
import java.io.Serializable;

/**
 * Created by gruposantoro3 on 04/01/2018.
 */

public class Categoria implements Serializable,Comparable<Categoria> {

    private String categoria;
    private int idCategoria;
    private String img;

    public Categoria(String categoria, int idCategoria, String img) {
        this.categoria = categoria;
        this.idCategoria = idCategoria;
        this.img = img;
    }

    public Categoria(String categoria, int idCategoria) {
        this.categoria = categoria;
        this.idCategoria = idCategoria;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }


    @Override
    public int compareTo(@NonNull Categoria categoria) {
        return categoria.getCategoria().compareTo(this.getCategoria());
    }
}
