package com.medhacks.sketchmagik;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import com.medhacks.sketchmagik.utils.Constants;
import com.medhacks.sketchmagik.utils.DrawingView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private DrawingView drawView;
    private Paint mPaint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        drawView = (DrawingView) findViewById(R.id.canvasArea);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // setup canvas and load appropriate points for the design

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // start drawing on canvas on mouse down
                Constants.drawing = true;
                mPaint = new Paint();
                mPaint.setAntiAlias(true);
                mPaint.setDither(true);
                mPaint.setColor(Color.GREEN);
                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setStrokeJoin(Paint.Join.ROUND);
                mPaint.setStrokeCap(Paint.Cap.ROUND);
                mPaint.setStrokeWidth(12);
                drawView.startNew(getApplicationContext(), mPaint);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
