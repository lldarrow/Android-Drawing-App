package luke.doodle;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by Luke on 5/25/2017.
 */

public class CanvasView extends View{

    private Path mPath;
    private Paint mPaint;
    private float mX, mY;
    private static final float TOLERANCE = 5;
    Context context;


    public CanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        //enable drawing cache and set background color to white for saving later
        //bitmap will be taken from drawing cache for saving
        this.setDrawingCacheEnabled(true);
        this.setDrawingCacheBackgroundColor(Color.WHITE);

        mPath = new Path();

        //paint attributes and initialization
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(20f);
    }

    //called when the view of this size is changed, it shouldn't call this since I set it to portrait though
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawPath(mPath, mPaint);
    }

    private void onStartTouch(float x, float y) {
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void moveTouch(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if(dx >= TOLERANCE || dy >= TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    public void clearCanvas() {
        this.destroyDrawingCache();
        mPath.reset();
        invalidate();
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    /* scan file function for updating androids gallery */
    private void scanFile(String path) {
        MediaScannerConnection.scanFile(context,
                new String[] { path }, null,
                new MediaScannerConnection.OnScanCompletedListener() {

                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("TAG", "Finished scanning " + path);
                    }
                });
    }

    /* Saves canvas bitmap to PNG in external storage directory */
    public void saveCanvas() {
        //check permissions
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e("ERROR", "NO PERMISSIONS");
        }

        if(!isExternalStorageWritable() && !isExternalStorageReadable())
            return;

        //get directory for the user's public pictures directory
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "DoodlePictures");

        //try to make directories, error if bad return value
        if(!file.exists()) {
            if(!file.mkdirs()){
                Log.e("ERROR", "Directory " + file.toString() + " not created");
                AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                alert.setTitle("Whoops!");
                alert.setMessage("ERROR: Failed to create directory!");
                alert.setPositiveButton("OK", null);
                alert.show();
                return;
            }
        }

        //create a new file with name based on the time
        File saveFile = new File(file, Long.toString(System.currentTimeMillis())+".png");

        try {
            //open stream
            FileOutputStream fos = new FileOutputStream(saveFile);

            //compress bitmap to stream
            this.getDrawingCache().compress(Bitmap.CompressFormat.PNG, 100, fos);
            //close file output stream
            fos.close();

        } catch (Exception e) {
            //exception, error message popup
            e.printStackTrace();
            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
            alert.setTitle("Whoops!");
            alert.setMessage("ERROR: Image not saved! Do you have enough space?");
            alert.setPositiveButton("OK", null);
            alert.show();
        } finally {
            //popup message confirming save location
            Toast.makeText(context, saveFile.toString() + " saved!", Toast.LENGTH_LONG).show();
        }

        //time for mediascanner to update the gallery on android since it is too dumb to do it itself
        scanFile(saveFile.toString());

    }

    private void upTouch() {
        mPath.lineTo(mX, mY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        float x = event.getX();
        float y = event.getY();

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                onStartTouch(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                moveTouch(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                upTouch();
                invalidate();
                break;
        }
        return true;
    }
}
