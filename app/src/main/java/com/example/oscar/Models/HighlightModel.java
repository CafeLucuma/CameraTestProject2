package com.example.oscar.Models;

import java.util.ArrayList;

/**
 * Created by Oscar on 27-08-2017.
 */

//modelo de los highlight generados por el documento digital

public class HighlightModel {

    public ArrayList<ArrayList<String>> getWords() {
        return words;
    }

    public void setWords(ArrayList<ArrayList<String>> words) {
        this.words = words;
    }

    public ArrayList<ArrayList<Integer>> getWordsIndex() {
        return wordsIndex;
    }

    public void setWordsIndex(ArrayList<ArrayList<Integer>> wordsIndex) {
        this.wordsIndex = wordsIndex;
    }

    public ArrayList<int[]> getWordsAbsoluteIndex() {
        return wordsAbsoluteIndex;
    }

    public void setWordsAbsoluteIndex(ArrayList<int[]> wordsAbsoluteIndex) {
        this.wordsAbsoluteIndex = wordsAbsoluteIndex;
    }

    //words highlight por linea
    public ArrayList<ArrayList<String>> words;
    //offset de palabras por linea
    public ArrayList<ArrayList<Integer>> wordsIndex;
    //index de las palabras NO contando por linea
    private ArrayList<int[]> wordsAbsoluteIndex;

    public HighlightModel(int numLines)
    {
        this.wordsAbsoluteIndex = new ArrayList<>();
        this.words = new ArrayList<>(numLines);
        this.wordsIndex = new ArrayList<>(numLines);
        for(int i = 0; i < numLines; i++)
        {
            ArrayList<String> s = new ArrayList<>();
            this.words.add(s);
            ArrayList<Integer> offset = new ArrayList<>();
            this.wordsIndex.add(offset);
        }
    }
}
