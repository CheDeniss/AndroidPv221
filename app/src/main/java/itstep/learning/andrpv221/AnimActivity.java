package itstep.learning.andrpv221;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AnimActivity extends AppCompatActivity {

    private Animation alphaDemo;
    private Animation scaleDemo;
    private Animation rotateDemo;
    private Animation translateDemo;
    private AnimationSet combiDemo;
    private Animation bellDemo;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_anim);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        alphaDemo = AnimationUtils.loadAnimation(this, R.anim.alpha_demo );
        scaleDemo = AnimationUtils.loadAnimation(this, R.anim.scale_demo );
        rotateDemo = AnimationUtils.loadAnimation(this, R.anim.rotate_demo );
        translateDemo = AnimationUtils.loadAnimation(this, R.anim.translate_demo );

        combiDemo = new AnimationSet( true );
        combiDemo.addAnimation( rotateDemo );
        combiDemo.addAnimation( scaleDemo );

        bellDemo = AnimationUtils.loadAnimation(this, R.anim.bell_demo );

        findViewById( R.id.anim_alpha ).setOnClickListener( this::alphaClick );
        findViewById( R.id.anim_scale ).setOnClickListener( this::scaleClick );
        findViewById( R.id.anim_rotate ).setOnClickListener( this::rotateClick );
        findViewById( R.id.anim_translate ).setOnClickListener( this::translateClick );
        findViewById( R.id.anim_combo ).setOnClickListener( this::combiClick );
        findViewById( R.id.anim_bell ).setOnClickListener( this::bellClick );
    }

    private void bellClick(View view) {
        view.startAnimation( bellDemo );
    }

    private void combiClick(View view) {
        view.startAnimation( combiDemo );
    }

    private void translateClick(View view) {
        view.startAnimation( translateDemo );
    }

    private void rotateClick(View view) {
        view.startAnimation( rotateDemo );
    }

    private void alphaClick( View view ) {
        view.startAnimation( alphaDemo );
    }

    private void scaleClick( View view ) {
        view.startAnimation( scaleDemo );
    }
}