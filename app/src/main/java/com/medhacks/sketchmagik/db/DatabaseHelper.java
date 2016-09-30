package com.medhacks.sketchmagik.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.medhacks.sketchmagik.models.Row;
import com.medhacks.sketchmagik.utils.Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by Shreya on 25/09/16.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private String TAG = "DatabaseHelper";
    private static String DB_NAME = "sketchMagik.sqlite";
    private static int DB_VERSION = 1;
    private static String DB_PATH;
    private static DatabaseHelper mInstance;
    private SharedPreferences sharedpreferences;
    Context context;

    // Table Names
    private static String TAB_APP_DATA = "user_data";

    private static String COL_APP_DATE = "date";
    private static String COL_APP_SKETCH_NUM = "sketch_number";
    private static String COL_APP_ATTEMPT_NUM = "attempt_number";
    private static String COL_APP_TIME_TAKEN = "time_taken";
    private static String COL_APP_DEVIATION = "deviation";
    private static String COL_APP_STATUS = "status";
    private static String COL_APP_IMAGE_DATA = "imageData";

    // Table Creation Commands
    private static final String COM_CREATE_USERS_DATA = "CREATE TABLE "
            + TAB_APP_DATA + "(" + COL_APP_DATE + " TEXT, " + COL_APP_SKETCH_NUM
            + " INTEGER, " + COL_APP_ATTEMPT_NUM + " INTEGER, " + COL_APP_TIME_TAKEN + " REAL, " + COL_APP_DEVIATION+" REAL, "
            + COL_APP_STATUS +" INTEGER, "+COL_APP_IMAGE_DATA+" TEXT)";

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (mInstance == null) {
            Log.e("DbHelper", "inside null instance of Dbhelper!");
            mInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return mInstance;
    }

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
        DB_PATH = context.getApplicationInfo().dataDir+"/databases/";
       // DB_PATH = this.context.getDatabasePath(DB_NAME).getAbsolutePath();
        sharedpreferences = context.getSharedPreferences(Constants.MyPREFERENCES, Context.MODE_PRIVATE);
    }

    /**
     * Creates a empty database on the system and rewrites it with your own database.
     * */
    public void createDataBase() throws IOException {

        boolean dbExist = checkDataBase();

        if(dbExist){
            //do nothing - database already exist
            Log.e("DbHelper", "DB already exists!");

        }else{

            //By calling this method and empty database will be created into the default system path
            //of your application so we are gonna be able to overwrite that database with our database.
            this.getReadableDatabase();

            try {

                copyDataBase();

            } catch (IOException e) {

                throw new Error("Error copying database");

            }
        }

    }

    private boolean checkDataBase(){
        File dbFile = new File(DB_PATH+DB_NAME);
        return dbFile.exists();
    }

    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     * */
    private void copyDataBase() throws IOException{

        //Open your local db as the input stream
        InputStream myInput = context.getAssets().open(DB_NAME);

        // Path to the just created empty db
        String outFileName = DB_PATH + DB_NAME;

        //Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);

        //transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer))>0){
            myOutput.write(buffer, 0, length);
        }

        //Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();

    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void createNewRow(Row row) {
        String date = row.getDate();
        int sketchNumber = row.getSketchNumber();
        int attemptNumber = row.getAttemptNumber();
        double timeTaken = row.getTimeTaken();
        double deviation = row.getDeviation();
        int status = row.getStatus();
        String imageData = row.getImageData();
        createAssetTableIfNotExists();
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_APP_DATE, date);
        cv.put(COL_APP_SKETCH_NUM, sketchNumber);
        cv.put(COL_APP_ATTEMPT_NUM, attemptNumber);
        cv.put(COL_APP_TIME_TAKEN, timeTaken);
        cv.put(COL_APP_DEVIATION, deviation);
        cv.put(COL_APP_STATUS, status);
        cv.put(COL_APP_IMAGE_DATA, imageData);
        db.insert(TAB_APP_DATA, null, cv);
    }

    public void createAssetTableIfNotExists(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TAB_APP_DATA
                + "(" + COL_APP_DATE + " TEXT," + COL_APP_SKETCH_NUM + " INTEGER," + COL_APP_ATTEMPT_NUM + " INTEGER,"
                + COL_APP_TIME_TAKEN + " REAL," + COL_APP_DEVIATION + " REAL," + COL_APP_STATUS +" INTEGER,"+ COL_APP_IMAGE_DATA + " TEXT)");
    }

    public int getLatestAttemptNumber(String date, int sketchNum) {
        SQLiteDatabase db = this.getReadableDatabase();
        SQLiteStatement s = db.compileStatement("select count(*) from user_data where date = '" + date + "' and sketch_number = " + sketchNum);
        int count = (int) s.simpleQueryForLong();
        return count;
    }

    public int getNumberOfPendingRows() {
        int n = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        n = (int) DatabaseUtils.longForQuery(db, "SELECT COUNT(*) FROM " + TAB_APP_DATA+" WHERE status != 1", null);
        return n;
    }

    public void updateStatus(String date, int sketchNumber, int attempt, int status) {
//        String snStr = ""+sketchNumber;
//        String atStr = ""+attempt;
//        SQLiteDatabase db = this.getReadableDatabase();
//        ContentValues cv = new ContentValues();
//        cv.put(COL_APP_STATUS, status);
//        String where = "date=? AND sketch_number=? AND attempt_number=?";
//        String[] values = {date, snStr, atStr};
//        db.update(TAB_APP_DATA, cv, where, values);
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values=new ContentValues();
        values.put("status", status);
        db.update(TAB_APP_DATA, values, "date='"+date+"' and sketch_number='"+sketchNumber+"' and attempt_number='"+attempt+"'",null);
    }

    // get all pending rows
    public ArrayList<Row> getPendingRows(){
        ArrayList<Row> al = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TAB_APP_DATA +
                " WHERE " + COL_APP_STATUS + " != 1", null);
        int date = cursor.getColumnIndex(COL_APP_DATE);
        int sketchNumber = cursor.getColumnIndex(COL_APP_SKETCH_NUM);
        int attemptNumber = cursor.getColumnIndex(COL_APP_ATTEMPT_NUM);
        int timeTaken = cursor.getColumnIndex(COL_APP_TIME_TAKEN);
        int deviation = cursor.getColumnIndex(COL_APP_DEVIATION);
        int status = cursor.getColumnIndex(COL_APP_STATUS);
        int imageData = cursor.getColumnIndex(COL_APP_IMAGE_DATA);
        cursor.moveToFirst();
        while(!cursor.isAfterLast()){
            Row myRow = new Row();
            myRow.setDate(cursor.getString(date));
            myRow.setSketchNumber(cursor.getInt(sketchNumber));
            myRow.setAttemptNumber(cursor.getInt(attemptNumber));
            myRow.setTimeTaken(cursor.getDouble(timeTaken));
            myRow.setDeviation(cursor.getDouble(deviation));
            myRow.setStatus(cursor.getInt(status));
            myRow.setImage(cursor.getString(imageData));
            al.add(myRow);
            cursor.moveToNext();
        }
        cursor.close();
        return al;
    }


}
