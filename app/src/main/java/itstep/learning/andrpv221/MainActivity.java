package itstep.learning.andrpv221;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById( R.id.button ).setOnClickListener( this::onCalcButtonClick );
        findViewById( R.id.button2 ).setOnClickListener( this::onGameButtonClick );
        findViewById( R.id.button3 ).setOnClickListener( this::onAnimButtonClick );
    }

    private void onCalcButtonClick ( View view ) {
        Intent intent = new Intent(this, CalcActivity.class);
        startActivity( intent );
    }

    private void onGameButtonClick ( View view ) {
        Intent intent = new Intent(this, GameActivity.class);
        startActivity( intent );
    }

    private void onAnimButtonClick( View view ) {
        Intent intent = new Intent( MainActivity.this, AnimActivity.class );
        startActivity( intent );
    }
}