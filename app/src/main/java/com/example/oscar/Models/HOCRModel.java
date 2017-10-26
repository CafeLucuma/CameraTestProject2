package com.example.oscar.Models;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by Oscar on 26-08-2017.
 */

//modelo del HOCR generado por tesseract

public class HOCRModel
{
    //numero maximo de lineas del texto
    public int numLines;

    //boundingbox del bloque de texto
    public int[] blockBoundingBox;

    //contiene las coordenadas de la parte superior e inferior de las líneas de texto
    public ArrayList<int[]> lineTopBottomPixels;

    //words por linea (index = numero de linea)
    public ArrayList<String[]> wordsPerLine;

    //boundingboxes de words segun index absoluto(index 0 = palabra[0])
    public ArrayList<int[]> bboxes;

    //contiene las lineas con sus bboxes
    public LinkedList bboxesLine;

    public HOCRModel()
    {
        this.numLines = 0;
        this.wordsPerLine = new ArrayList<>();
        this.bboxesLine = new LinkedList();
        this.lineTopBottomPixels = new ArrayList<>();
    }

    public void setWordsPerLine(ArrayList<String[]> wordsPerLine) {
        this.wordsPerLine = wordsPerLine;
    }

    public ArrayList<String[]> getWordsPerLine() {
        return wordsPerLine;
    }


    public int getNumLines() {
        return numLines;
    }

    public void setNumLines(int numLines) {
        this.numLines = numLines;
    }

    public int[] getBlockBoundingBox() {
        return blockBoundingBox;
    }

    public void setBlockBoundingBox(int[] blockBoundingBox) {
        this.blockBoundingBox = blockBoundingBox;
    }

    public ArrayList<int[]> getLineTopBottomPixels() {
        return lineTopBottomPixels;
    }

    public void setLineTopBottomPixels(ArrayList<int[]> lineTopBottomPixels) {
        this.lineTopBottomPixels = lineTopBottomPixels;
    }

    public ArrayList<int[]> getBboxes() {
        return bboxes;
    }

    public void setBboxes(ArrayList<int[]> bboxes) {
        this.bboxes = bboxes;
    }

    public LinkedList getBboxesLine() {
        return bboxesLine;
    }

    public void setBboxesLine(LinkedList bboxesLine) {
        this.bboxesLine = bboxesLine;
    }

}
