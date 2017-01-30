package org.literacyapp.visemes;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import org.rajawali3d.surface.IRajawaliSurface;
import org.rajawali3d.surface.RajawaliSurfaceView;

public class FaceActivity extends AppCompatActivity {

    private PoseRenderer poseRenderer;
    private RajawaliSurfaceView surface;

    private Button letterSound1Button;
    private Button letterSound2Button;
    private Button letterSound3Button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_face);

        letterSound1Button = (Button) findViewById(R.id.letterSound1);
        letterSound2Button = (Button) findViewById(R.id.letterSound2);
        letterSound3Button = (Button) findViewById(R.id.letterSound3);

        surface = (RajawaliSurfaceView) findViewById(R.id.rajwali_surface);
        surface.setFrameRate(15);
        surface.setRenderMode(IRajawaliSurface.RENDERMODE_WHEN_DIRTY);

        poseRenderer = new PoseRenderer(this);
        surface.setSurfaceRenderer(poseRenderer);
    }

    @Override
    protected void onStart() {
        super.onStart();

        letterSound1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                poseRenderer.renderViseme("e");
            }
        });

        letterSound2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                poseRenderer.renderViseme("t");
            }
        });

        letterSound3Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                poseRenderer.renderViseme("a");
            }
        });
    }
}
