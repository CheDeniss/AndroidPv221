package itstep.learning.andrpv221;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameActivity extends AppCompatActivity {
    private static final int N = 4;
    private final int[][] cells = new int[N][N];
    private final TextView[][] tvCells = new TextView[N][N];
    private final Random random = new Random();

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.game_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        LinearLayout gameField = findViewById(R.id.game_ll_field);

        gameField.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            int width = gameField.getWidth();
            gameField.getLayoutParams().height = width;
            gameField.requestLayout();
        });

        gameField.setOnTouchListener( new OnSwipeListener( GameActivity.this ) {
            @Override
            public void onSwipeLeft() {
                System.out.println("Left");
                Toast.makeText(GameActivity.this, "Left", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSwipeRight() {
                System.out.println("Right");
                Toast.makeText(GameActivity.this, "Right", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSwipeTop() {
                System.out.println("Top");
                Toast.makeText(GameActivity.this, "Top", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSwipeBottom() {
                System.out.println("Bottom");
                Toast.makeText(GameActivity.this, "Bottom", Toast.LENGTH_SHORT).show();
            }
            });

        initField();
        spawnField();
        showField();
        }

        private boolean spawnField() {
            List<Coordinates> emptyCells = new ArrayList<>();
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    if (cells[i][j] == 0) {
                        emptyCells.add(new Coordinates(i,j));
                    }
                }
            }
            if (emptyCells.isEmpty()) {
                return false;
            }
            Coordinates randomCoordinate = emptyCells.get( random.nextInt(emptyCells.size()) );
            cells[randomCoordinate.x][randomCoordinate.y] =
                    random.nextInt(10) == 0 ? 4 : 2;

            return true;
        }

        class Coordinates {
            int x, y;

            public Coordinates(int i, int j) {
                x = i;
                y = j;
            }
        }

        private void initField() {
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    cells[i][j] = (int) Math.pow(2, i + j + 1);
                    tvCells[i][j] = findViewById(getResources().getIdentifier("game_cell_" + i + j, "id", getPackageName()));
                }
            }
        }

    private void showField() {
        for (int i = 1; i < N; i++) {
            for (int j = 0; j < N; j++) {
                tvCells[i][j].setText( String.valueOf( cells[i][j] ) ) ;
                tvCells[i][j].setBackgroundColor( getResources().getColor(
                        getResources().getIdentifier(
                                cells[i][j] <= 2048
                                        ? "game_tile_" + cells[i][j]
                                        : "game_tile_other",
                                "color",
                                getPackageName()
                        ),
                        getTheme()
                ) ) ;
                tvCells[i][j].setTextColor( getResources().getColor(
                        getResources().getIdentifier(
                                cells[i][j] <= 2048
                                        ? "game_text_" + cells[i][j]
                                        : "game_text_other",
                                "color",
                                getPackageName()
                        ),
                        getTheme()
                ) ) ;
            }
        }
    }

}