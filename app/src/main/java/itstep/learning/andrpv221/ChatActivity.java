package itstep.learning.andrpv221;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ChatActivity extends AppCompatActivity {

    private final String chatUrl = "https://chat.momentfor.fun/";
    private TextView tvTitle;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        tvTitle = findViewById(R.id.chat_tv_title);
        new Thread( this::loadChat ).start();
    }

    private void loadChat() {
        try(InputStream inputStream = new URL( chatUrl ).openStream()) {
            String jsonStr = readString( inputStream );
            runOnUiThread( () -> tvTitle.setText( jsonStr ) );
        }
        catch (MalformedURLException e) {
            Log.e("ChatActivity::loadChat",
                    e.getMessage() == null ?
                            "MalformedURLException" : e.getMessage());
        }
        catch (IOException e) {
            Log.e("ChatActivity::loadChat",
                    e.getMessage() == null ?
                            "IOException" : e.getMessage());
        }
    }

    private String readString(InputStream stream) throws IOException {
        ByteArrayOutputStream byteBuilder = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int len;

        while ((len = stream.read( buffer )) != -1) {
            byteBuilder.write(buffer, 0, len);
        }
        String result = byteBuilder.toString( StandardCharsets.UTF_8.name() );
        byteBuilder.close();
        return result;
    }

}