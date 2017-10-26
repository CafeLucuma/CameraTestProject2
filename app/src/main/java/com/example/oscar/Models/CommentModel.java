package com.example.oscar.Models;

import java.util.ArrayList;

/**
 * Created by Oscar on 03-10-2017.
 */

public class CommentModel
{
    //offset de donde comienza el caracter del texto comentado
    private int[] offsetWordsCommented;
    //comment realizado
    private String comment;
    //index absoluto de las palabras que fueron comentadas
    private ArrayList<Integer> indexWordsCommented;

    public ArrayList<Integer> getIndexWordsCommented() {
        return indexWordsCommented;
    }

    public void setIndexWordsCommented(ArrayList<Integer> indexWordsCommented) {
        this.indexWordsCommented = indexWordsCommented;
    }

    public int[] getOffsetWordsCommented() {
        return offsetWordsCommented;
    }

    public void setOffsetWordsCommented(int[] indexComentarios) {
        this.offsetWordsCommented = indexComentarios;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

}
