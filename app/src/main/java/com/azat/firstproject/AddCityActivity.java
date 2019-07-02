package com.azat.firstproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class AddCityActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_city);
    }

    public void addToFile(View view) {
        EditText editText =  findViewById(R.id.edit_message);
        String cityName = editText.getText().toString();
        FileOutputStream fout = null;
        try {
            fout = openFileOutput("content.txt",MODE_APPEND);
            String text = " " + cityName;
            fout.write(text.getBytes());
            Toast.makeText(this,"Город добавлен",Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        finally{
            try{
                if(fout!=null)
                    fout.close();
            }
            catch(IOException ex){

                Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void backToCityList(View view) {
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }
}
