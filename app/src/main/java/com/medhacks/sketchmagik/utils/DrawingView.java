package com.medhacks.sketchmagik.utils;

import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.PathMeasure;
import android.media.ThumbnailUtils;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.EventLog;
import android.util.Log;
import android.view.View;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.widget.ImageView;
import com.medhacks.sketchmagik.R;
import com.medhacks.sketchmagik.db.DatabaseHelper;
import com.medhacks.sketchmagik.models.Row;

import java.io.ByteArrayOutputStream;
import java.io.Console;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;


/**
 * Created by Shreya on 24/09/16.
 */
public class DrawingView extends View{



    private Row myRow;
    DatabaseHelper myDbHelp;
    Paint trace;
    public int width;
    public  int height;
    private Bitmap  mBitmap;
    private Canvas  mCanvas;
    private Path    mPath;
    private Paint   mBitmapPaint;
    Context context;
    private Paint circlePaint;
    private Path circlePath;
    private Paint mPaint;
    double pX, pY;
    int radius = 600;
    Paint fillPoint;
    float centerX;
    float centerY;
    int numberOfPoints = 4;

    FloatPoint[] pathPointsArray;
    int numberOfCirclePoints;
    FloatPoint[] circleArray;
    float pathLength=0;
    int arrayLength=0;
    double[] distances;

    double[] degreeArray;
    double averageOfDistances = 0;

    long startTime = 0;
    long totalTime = 0;
    double totalTimeSecs = 0.0;

    public DrawingView(Context context, AttributeSet attrs){
        super(context, attrs);
        myDbHelp = DatabaseHelper.getInstance(context);
        try {
            myDbHelp.createDataBase();
        } catch (IOException e) {
            // TODO Auto-generated catch block;
            e.printStackTrace();
        }
        trace = new Paint();
        trace.setColor(ContextCompat.getColor(context, R.color.greyBg));
        DashPathEffect dashPath = new DashPathEffect(new float[]{25,25}, (float)0.5);
        trace.setPathEffect(dashPath);
        trace.setStrokeWidth(10);
        trace.setStyle(Paint.Style.STROKE);
        fillPoint = new Paint();
        fillPoint.setColor(ContextCompat.getColor(context, R.color.primaryColorDark));
        fillPoint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        centerX = getWidth() / 2f;
        centerY = getHeight() / 2f;
        canvas.drawCircle(centerX, centerY, radius, trace);
        for(int i = 1; i <= numberOfPoints; i++) {
            pX = (centerX) + (radius * Math.cos(Math.toRadians(90*i)));
            pY = (centerY) + (radius * Math.sin(Math.toRadians(270*i)));
            canvas.drawCircle((float)pX, (float)pY, 35, fillPoint);
        }
        if(Constants.drawing || Constants.up) {
            canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
            canvas.drawPath(mPath, mPaint);
            canvas.drawPath(circlePath, circlePaint);
            pathPointsArray = getPointsFromPath();
        }
    }

    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

    private void touch_start(float x, float y) {
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
        invalidate();
        startTime=System.currentTimeMillis();
    }

    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
            mX = x;
            mY = y;

            circlePath.reset();
            circlePath.addCircle(mX, mY, 30, Path.Direction.CW);
        }
        invalidate();
    }

    private void touch_up() {
        mPath.lineTo(mX, mY);
        circlePath.reset();
        // commit the path to our offscreen
        mCanvas.drawPath(mPath, mPaint);
        if(circleArray == null && arrayLength != 0.0) {
            degreeArray = new double[arrayLength];
            for(int i=0; i<arrayLength; i++){
                degreeArray[i]=getDegreeFromCoordinate(pathPointsArray[i].getX(),pathPointsArray[i].getY());
            }
            circleArray = getPointsFromTrace();
            distances = assignDistances();
            for(int i = 0; i<distances.length; i++){
                averageOfDistances+=distances[i];
            }
            averageOfDistances/=(distances.length);
            myRow.setDeviation(averageOfDistances);
            Log.e("myDeviation", ""+myRow.getDeviation());

            totalTime = System.currentTimeMillis()-startTime;
            totalTimeSecs=(double)(totalTime/1000.00);
            Log.e("Total Time Taken: Secs" , "" + totalTimeSecs);
            myRow.setTimeTaken(totalTimeSecs);

            if(Constants.success == true) {
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String addedDate = dateFormat.format(cal.getTime());
                myRow.setDate(addedDate);
                Log.e("ADDED DATE", addedDate);
                // get number of sketches for the date from db and add one to it. For now, we only have one sketch
                myRow.setSketchNumber(1);
                int attempt = myDbHelp.getLatestAttemptNumber(addedDate, 1) + 1;
                myRow.setAttemptNumber(attempt);
                // Gotta calculate average deviation
                myRow.setStatus(0);
                myDbHelp.createNewRow(myRow);
                //if internet available
                if (isNetworkAvailable()) {
                    if (myDbHelp.getNumberOfPendingRows() > 0) {
                        ArrayList<Row> rowList = myDbHelp.getPendingRows();
                        new SendToServer(context);
                    }
                }
                //takeScreenshot();
            }

        }





        // kill this so we don't double draw
        mPath.reset();
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Constants.up = false;
                if(Constants.drawing) {
                    touch_start(x, y);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if(Constants.drawing) {
                    touch_move(x, y);
                }
                break;
            case MotionEvent.ACTION_UP:
                if(Constants.drawing) {
                    touch_up();
                    Constants.drawing = false;
                    Constants.up = true;
                   // saveDrawing();
                }
                break;
        }
        return true;
    }

    public void startNew(Context c, Paint p) {
        circleArray = null;
        clearDrawing();
        context = c;
        myRow = new Row();
        setDrawingCacheEnabled(true);
        mPath = new Path();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        circlePaint = new Paint();
        circlePath = new Path();
        circlePaint.setAntiAlias(true);
        circlePaint.setColor(Color.BLUE);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeJoin(Paint.Join.MITER);
        circlePaint.setStrokeWidth(4f);
        mPaint = p;
    }

    public void clearDrawing() {
        setDrawingCacheEnabled(false);
        onSizeChanged(width, height, width, height);
        invalidate();
    }

    private FloatPoint[] getPointsFromPath() {
        PathMeasure pm = new PathMeasure(mPath, false);
        pathLength = pm.getLength();
        arrayLength=(int)(pathLength/30);
        float distance = 0f;
        FloatPoint[] pointArray = new FloatPoint[arrayLength];
        float speed = 30 ;
        int counter = 0;
        float[] aCoordinates = new float[2];

        while ((distance < pathLength) && (counter < pointArray.length)) {
            // get point from the path
            pm.getPosTan(distance, aCoordinates, null);
            pointArray[counter] = new FloatPoint(aCoordinates[0],
                    aCoordinates[1]);
            counter++;
            distance = distance + speed;
        }

        return pointArray;
    }

    private double getDegreeFromCoordinate(float x, float y){
        double degree=0;
        double tempVal = (centerY-y)/(x-centerX);
        double arcTan= Math.toDegrees(Math.atan(tempVal));
        if(x<centerX) arcTan+=180;

        return arcTan;
    }

    private FloatPoint getPointFromDegreeForCircle(double degrees, int radius ){
        float xCoord= (float)(centerX + radius * Math.cos(Math.toRadians(degrees)));
        float yCoord= (float)(centerY - radius * Math.sin(Math.toRadians(degrees)));
        FloatPoint myPt= new FloatPoint(xCoord,yCoord);
        return myPt;
    }

    private FloatPoint[] getPointsFromTrace() {
        FloatPoint[] circlePoint = new FloatPoint[arrayLength];
        for(int i = 0; i < arrayLength; i++) {
            FloatPoint tempPt = getPointFromDegreeForCircle(degreeArray[i],radius);
            double x = tempPt.getX();
            double y = tempPt.getY();
            float[] aCoordinates = new float[2];
            aCoordinates[0] = (float) x;
            aCoordinates[1] = (float) y;
            circlePoint[i] = new FloatPoint(aCoordinates[0],
                    aCoordinates[1]);
        }
        return circlePoint;
    }



    public void saveDrawing() {
        Bitmap whatTheUserDrewBitmap = getDrawingCache();
        // don't forget to clear it (see above) or you just get duplicates

        // almost always you will want to reduce res from the very high screen res
        whatTheUserDrewBitmap = ThumbnailUtils.extractThumbnail(whatTheUserDrewBitmap, 256, 256);
        // NOTE that's an incredibly useful trick for cropping/resizing squares
        // while handling all memory problems etc
        // http://stackoverflow.com/a/17733530/294884

        // these days you often need a "byte array". for example,
        // to save to parse.com or other cloud services
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        whatTheUserDrewBitmap.compress(Bitmap.CompressFormat.PNG, 0, baos);
        byte[] yourByteArray;
        yourByteArray = baos.toByteArray();
    }

    private double[] assignDistances(){
        double[] distanceArray = new double[arrayLength];
        for(int i=0; i < arrayLength; i++){
            double distancey = Math.pow((pathPointsArray[i].getY()-circleArray[i].getY()),2);
            double distancex = Math.pow((pathPointsArray[i].getX()-circleArray[i].getX()),2);
            double distance = Math.pow((distancex+distancey),0.5);
            distanceArray[i] = distance;
        }
        return distanceArray;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void takeScreenshot() {
        Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd hh:mm:ss", now);

        try {
            // image naming and path  to include sd card  appending name you choose for file
            String mPath = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg";

            // create bitmap screen capture
            //View v1 = getWindow().getDecorView().getRootView();
            View v1 = this;
            v1.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);

            File imageFile = new File(mPath);

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();

            //openScreenshot(imageFile);
        } catch (Throwable e) {
            // Several error may come out with file handling or OOM
            e.printStackTrace();

        }
    }
}
