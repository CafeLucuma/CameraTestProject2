package com.example.oscar.cameratest;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;


//TODO hacer botton para menu, telefonos ya no tienen menu button

public class MainActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Log.i("CAMERATEST", "ONCREATE");
        final CharSequence[] items = {"Camara normal", "Camara doble (estereo)"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Seleccione tipo de cámara");
        builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {

                if (item == 0)
                {
                    //camara normal seleccionada
                    Log.i("CAMERATEST", "camera1 selected");
                    Log.i("selected RadioButton->", " " + items[item]);
                    //crea la actividad para escanear código QR
                    Intent intent = new Intent(MainActivity.this, MainActivityNormal.class);
                    startActivity(intent);
                    finish();
                }
                else
                {
                    //camara doble seleccionada
                    Log.i("CAMERATEST", "camera2 selected");
                    Log.e("selected RadioButton->"," " + items[item]);
                    Intent intent = new Intent(MainActivity.this, MainActivityEstereo.class);
                    startActivity(intent);
                    finish();
                }

                Toast.makeText(MainActivity.this, "Item seleccionado: " + items[item], Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();

        //this.camera = new Camera1(getContext());
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }
}