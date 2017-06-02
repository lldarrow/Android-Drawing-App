package luke.doodle;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private CanvasView canvasView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //set content view to main activity view
        setContentView(R.layout.activity_main);
        //set canvasView to canvas element
        canvasView = (CanvasView) findViewById(R.id.canvas);
    }

    //clear canvas function, clears the view and bitmap
    public void clearCanvas(View v){
        canvasView.clearCanvas();
    }

    //save canvas function, saves bitmap as png
    public void saveCanvas(View v){
        canvasView.saveCanvas();
    }
}
