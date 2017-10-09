package com.example.oscar.Models;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by Oscar on 26-08-2017.
 */

public class HOCRModel {

    //numero maximo de lineas del texto
    public int numLines;

    public ArrayList<int[]> lineTopBottomPixels;

    //palabras por linea (index = numero de linea)
    public ArrayList<String[]> wordsLine;

    //boundingboxes de palabras segun index (index 0 = palabra[0])
    public ArrayList<int[]> bboxes;

    //contiene las lineas con sus bboxes
    public LinkedList bboxesLine;


    public HOCRModel()
    {
        this.numLines = 0;
        this.wordsLine = new ArrayList<>();
        this.bboxesLine = new LinkedList();
        this.lineTopBottomPixels = new ArrayList<>();
    }

    public void setWordsLine(ArrayList<String[]> wordsLine) {
        this.wordsLine = wordsLine;
    }

    public ArrayList<String[]> getWordsLine() {
        return wordsLine;
    }

}
