package itstep.learning.andrpv221;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatActivity extends AppCompatActivity {

    private final java.text.SimpleDateFormat sqlDateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final String chatUrl = "https://chat.momentfor.fun/";
    private TextView tvTitle;
    private LinearLayout chatContainer;
    private ScrollView scroller;
    private EditText etMessage, etAuthor;

    private final ExecutorService threadPool = Executors.newFixedThreadPool( 3 );
    private final Gson gson = new Gson();

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

        tvTitle         = findViewById( R.id.chat_tv_title );
        chatContainer   = findViewById( R.id.chat_ll_container );
        scroller        = findViewById( R.id.chat_scroller );
        etMessage       = findViewById( R.id.chat_et_message );
        etAuthor        = findViewById( R.id.chat_et_author );

        findViewById( R.id.chat_btn_send ).setOnClickListener( this::sendButtonClick );
        loadChat();
    }

    private void sendButtonClick(View view) {
        String author = etAuthor.getText().toString();
        String message = etMessage.getText().toString();

        if (author.isEmpty()) {
            Toast.makeText(this, "Заповніть автора", Toast.LENGTH_SHORT).show();
            return;
        }
        if (message.isEmpty()) {
            Toast.makeText(this, "Введіть текст повідомлення", Toast.LENGTH_SHORT).show();
            return;
        }

        CompletableFuture.runAsync(() -> {
                    sendChatMessage(new ChatMessage()
                            .setAuthor( author )
                            .setText( message )
                            .setMoment( sqlDateFormat.format(String.valueOf(new Date())) )
                    );
                    runOnUiThread(() -> {
                        etAuthor.setText("");
                        etMessage.setText("");

                    });
                },
                threadPool
        );
    }



    private void sendChatMessage( ChatMessage chatMessage ) {
        try {
            URL url = new URL( chatUrl );
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput( true ); // Дозволяємо читання з потоку
            connection.setDoOutput( true ); // Дозволяємо запис в потік
            connection.setChunkedStreamingMode( 0 ); // Відправляємо одним блоком
            connection.setRequestMethod( "POST" );
            connection.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded" );// Тип відправлення
            connection.setRequestProperty( "Accept", "application/json" );
            connection.setRequestProperty( "Connection", "close" );

            // Відправляємо дані
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write( String.format( "author=%s&msg=%s",
                    chatMessage.getAuthor(),
                    chatMessage.getText() ).
                    getBytes( StandardCharsets.UTF_8 ) );
            outputStream.flush(); // Відправляємо дані
            outputStream.close();

            // Отримуємо відповідь
            int statusCode = connection.getResponseCode();
            if( statusCode >= 200 && statusCode < 300 ) {
                Log.i( "ChatActivity::sendChatMessage", "Status: sent " + statusCode );
                loadChat();
            }
            else {
                InputStream errorStream = connection.getErrorStream();

                Log.e( "ChatActivity::sendChatMessage", "Status: Error" + statusCode );
            }
            connection.disconnect();

        }
        catch( Exception ex ) {
            Log.e( "ChatActivity::sendChatMessage",
                    ex.getMessage() == null ? ex.getClass().toString() : ex.getMessage() );
        }
    }

    private void loadChat() {
        CompletableFuture
                .supplyAsync( this::getChatAsString, threadPool )
                .thenApply( this::processChatResponse )
                .thenAccept( this::displayChatMessages );
    }

    private String getChatAsString() {
        try( InputStream urlStream = new URL( chatUrl ).openStream() ) {
            return readString( urlStream );
        }
        catch( MalformedURLException ex ) {
            Log.e( "ChatActivity::loadChat",
                    ex.getMessage() == null ? "MalformedURLException" : ex.getMessage() );
        }
        catch( IOException ex ) {
            Log.e( "ChatActivity::loadChat",
                    ex.getMessage() == null ? "IOException" : ex.getMessage() );
        }
        return null;
    }

    private ChatMessage[] processChatResponse( String jsonString ) {
        ChatResponse chatResponse = gson.fromJson( jsonString, ChatResponse.class );
        return chatResponse.data;
    }

    @SuppressLint({"ResourceAsColor", "SetTextI18n"})
    private void displayChatMessages(ChatMessage[] chatMessages) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(10, 5, 10, 5);

        runOnUiThread(() -> {
            chatContainer.setBackgroundColor(ContextCompat.getColor(this, R.color.chat_bkg));
            chatContainer.setPadding(10, 5, 10, 5);
        });

        for (ChatMessage cm : chatMessages) {
            LinearLayout messageLayout = new LinearLayout(ChatActivity.this);
            messageLayout.setOrientation(LinearLayout.VERTICAL);
            messageLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.chat_message_bkg));
            messageLayout.setPadding(15, 15, 15, 15);

            LinearLayout.LayoutParams layoutParamsMessage = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            layoutParamsMessage.setMargins(10, 10, 10, 10);
            messageLayout.setLayoutParams(layoutParamsMessage);

            
            TextView tvAuthor = new TextView(ChatActivity.this);
            tvAuthor.setText(cm.getAuthor());
            tvAuthor.setPadding(10, 5, 10, 5);
            tvAuthor.setTextColor(ContextCompat.getColor(this, R.color.chat_text_author));
            tvAuthor.setTextSize(15);
            tvAuthor.setTypeface(null, Typeface.BOLD);


            TextView tvMessageText = new TextView(ChatActivity.this);
            tvMessageText.setText(cm.getText());
            tvMessageText.setPadding(10, 5, 10, 5);
            tvMessageText.setTextColor(ContextCompat.getColor(this, R.color.chat_text_message));


            messageLayout.addView(tvAuthor);
            messageLayout.addView(tvMessageText);

            runOnUiThread(() -> chatContainer.addView(messageLayout));
        }
    }


    private String readString( InputStream stream ) throws IOException {
        ByteArrayOutputStream byteBuilder = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int len;
        while( ( len = stream.read( buffer ) ) != -1 ) {
            byteBuilder.write( buffer, 0, len );
        }
        String res = byteBuilder.toString( StandardCharsets.UTF_8.name() ) ;
        byteBuilder.close();
        return res;
    }

    @Override
    protected void onDestroy() {
        threadPool.shutdownNow();
        super.onDestroy();
    }


    class ChatResponse {
        private int status;
        private ChatMessage[] data;

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public ChatMessage[] getData() {
            return data;
        }

        public void setData(ChatMessage[] data) {
            this.data = data;
        }
    }
    /*
    {
      "status": 1,
      "data": [ChatMessage]
     }
     */

    class ChatMessage {
        private String id;
        private String author;
        private String text;
        private String moment;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getAuthor() {
            return author;
        }

        public ChatMessage setAuthor(String author) {
            this.author = author;
            return this;
        }

        public String getText() {
            return text;
        }

        public ChatMessage setText(String text) {
            this.text = text;
            return this;
        }

        public String getMoment() {
            return moment;
        }

        public ChatMessage setMoment(String moment) {
            this.moment = moment;
            return this;
        }
    }

}
