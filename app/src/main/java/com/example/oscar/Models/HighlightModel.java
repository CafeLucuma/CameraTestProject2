package com.example.oscar.Models;

import java.util.ArrayList;

/**
 * Created by Oscar on 27-08-2017.
 */

public class HighlightModel {

    //palabras highlight
    public ArrayList<ArrayList<String>> palabras;
    public ArrayList<ArrayList<Integer>> wordOffset;

    public HighlightModel(int numLines)
    {
        this.palabras = new ArrayList<>(numLines);
        this.wordOffset = new ArrayList<>(numLines);
        for(int i = 0; i < numLines; i++)
        {
            ArrayList<String> s = new ArrayList<>();
            this.palabras.add(s);
            ArrayList<Integer> offset = new ArrayList<>();
            this.wordOffset.add(offset);
        }
    }

}
