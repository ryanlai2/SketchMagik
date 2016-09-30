package com.medhacks.sketchmagik;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.medhacks.sketchmagik.utils.Constants;

public class LoginActivity extends AppCompatActivity {

    private SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedpreferences = getSharedPreferences(Constants.MyPREFERENCES, Context.MODE_PRIVATE);

        if(sharedpreferences.getString(Constants.usernamePref, "none").equals("sm_admin@gmail.com") &&
                sharedpreferences.getString(Constants.passwordPref, "none").equals("test")) {
            Intent in = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(in);
            finish();
        }
        setContentView(R.layout.activity_login);
        final EditText userName = (EditText) findViewById(R.id.username);
        final EditText password = (EditText) findViewById(R.id.password);
        Button login = (Button) findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String givenUn = userName.getText().toString();
                String givenPw = password.getText().toString();

                if(givenUn.equals("sm_admin@gmail.com") && givenPw.equals("test")){
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putString(Constants.usernamePref, givenUn);
                    editor.putString(Constants.passwordPref, givenPw);
                    editor.commit();
                    Intent in = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(in);
                    finish();
                } else{
                    Snackbar.make(v, "Incorrect Username or Password!", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });
    }
}
