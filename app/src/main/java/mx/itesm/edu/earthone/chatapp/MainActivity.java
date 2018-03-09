package mx.itesm.edu.earthone.chatapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends Activity {

    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = (EditText) findViewById(R.id.editText);
        sharedPreferences = getSharedPreferences("user", Context.MODE_PRIVATE);
        if(sharedPreferences.contains("name")){
            startActivity(new Intent(MainActivity.this, ChatActivity.class));
        }
    }

    private SharedPreferences sharedPreferences;

    public void enter(View view){
        String name = editText.getText().toString();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("name", name);
        editor.commit();
        startActivity(new Intent(MainActivity.this, ChatActivity.class));
    }
}
