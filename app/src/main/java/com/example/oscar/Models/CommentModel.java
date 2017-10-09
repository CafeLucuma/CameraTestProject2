package com.example.oscar.Models;

import java.util.ArrayList;

/**
 * Created by Oscar on 03-10-2017.
 */

public class CommentModel
{
    private int[] offsetComentarios;
    private String comentario;
    private ArrayList<Integer> indexPalabrasSeleccionadas;

    public ArrayList<Integer> getIndexPalabrasSeleccionadas() {
        return indexPalabrasSeleccionadas;
    }

    public void setIndexPalabrasSeleccionadas(ArrayList<Integer> indexPalabrasSeleccionadas) {
        this.indexPalabrasSeleccionadas = indexPalabrasSeleccionadas;
    }

    public int[] getOffsetComentarios() {
        return offsetComentarios;
    }

    public void setOffsetComentarios(int[] indexComentarios) {
        this.offsetComentarios = indexComentarios;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

}
