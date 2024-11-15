package itstep.learning.andrpv221;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatActivity extends AppCompatActivity {

    private static final String fileName = "aut.hor";
    private final java.text.SimpleDateFormat sqlDateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final String chatUrl = "https://chat.momentfor.fun/";
    private String author = "";
    private TextView tvTitle;
    private LinearLayout chatContainer;
    private ScrollView scroller;
    private EditText etMessage, etAuthor;
    private View vBell;
    private final ExecutorService threadPool = Executors.newFixedThreadPool(3);
    private final Gson gson = new Gson();
    private boolean authorWasBlocked = false;

    private final List<ChatMessage> messages = new ArrayList<>();
    private Handler handler = new Handler();
    private Animation bellAnimation;

    @SuppressLint({"MissingInflatedId", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);
        // ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
        //     Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
        //     v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
        //     return insets;
        // });
        tvTitle       = findViewById( R.id.chat_tv_title     );
        chatContainer = findViewById( R.id.chat_ll_container );
        scroller      = findViewById( R.id.chat_scroller     );
        etAuthor      = findViewById( R.id.chat_et_author    );
        etMessage     = findViewById( R.id.chat_et_message   );
        vBell         = findViewById( R.id.chat_bell         );

        chatContainer.setOnTouchListener(( v, event ) -> {  // Сховати клавіатуру при кліку на контейнер
            if(getCurrentFocus() != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
            return false;
        });

        findViewById( R.id.chat_btn_send ).setOnClickListener( this::sendButtonClick );

        findViewById(R.id.change_Author).setOnClickListener( v -> {
            etAuthor.setEnabled(true);
            authorWasBlocked = false;
        });

        bellAnimation = AnimationUtils.loadAnimation(this, R.anim.bell_demo );

        author = loadAuthor();
        if (!author.isEmpty()) {
            setAuthor(author);
        }

        handler.post( this::periodic );
        scroller.addOnLayoutChangeListener( ( View v,
                                                  int left,    int top,    int right,    int bottom,
                                                  int leftWas, int topWas, int rightWas, int bottomWas) -> scroller.post(
                ()-> scroller.fullScroll( View.FOCUS_DOWN )
        ));
    }

    private void periodic() {
        loadChat();
        handler.postDelayed(this::periodic, 3000);
    }

    private void setAuthor(String author) {
        etAuthor.setText(author);
        blockEtAuthor();
    }

    private void sendButtonClick(View view) {
        author = etAuthor.getText().toString();
        String message = etMessage.getText().toString();

        if (author.isEmpty()) {
            Toast.makeText(this, "Заповніть автора", Toast.LENGTH_SHORT).show();
            return;
        }
        if (message.isEmpty()) {
            Toast.makeText(this, "Введіть текст повідомлення", Toast.LENGTH_SHORT).show();
            return;
        }

        CompletableFuture.runAsync(() ->
                        sendChatMessage(new ChatMessage()
                                .setAuthor(author)
                                .setText(message)
                                .setMoment(sqlDateFormat.format(new Date()))),
                threadPool
        );

        etMessage.setText("");

        if (!authorWasBlocked) {
            blockEtAuthor();
            saveAuthor();
        }
    }

    private void blockEtAuthor() {
        etAuthor.setEnabled(false);
        authorWasBlocked = true;
    }

    private void saveAuthor() {
        try (FileOutputStream fos = openFileOutput(fileName, MODE_PRIVATE)) {
            DataOutputStream writer = new DataOutputStream(fos);
            writer.writeUTF(author);
            writer.flush();
        } catch (IOException ex) {
            Log.e("ChatActivity::Error saving author",
                    ex.getMessage() != null ? ex.getMessage() : "Error writing File");
        }
    }

    private String loadAuthor() {
        try (
                FileInputStream fis = openFileInput(fileName);
                DataInputStream reader = new DataInputStream(fis)
        ) {
            return reader.readUTF();
        } catch (IOException ex) {
            Log.e("ChatActivity::Error loading author",
                    ex.getMessage() != null ? ex.getMessage() : "Error reading File");
            return "";
        }
    }

    private void sendChatMessage(ChatMessage chatMessage) {
        try {
            URL url = new URL(chatUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setChunkedStreamingMode(0);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Connection", "close");

            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(String.format("author=%s&msg=%s",
                            chatMessage.getAuthor(),
                            chatMessage.getText()).
                    getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            outputStream.close();

            int statusCode = connection.getResponseCode();
            if (statusCode >= 200 && statusCode < 300) {
                Log.i("ChatActivity::sendChatMessage", "Status: sent " + statusCode);
                loadChat();
            } else {
                Log.e("ChatActivity::sendChatMessage", "Status: Error " + statusCode);
            }
            connection.disconnect();

        } catch (Exception ex) {
            Log.e("ChatActivity::sendChatMessage",
                    ex.getMessage() == null ? ex.getClass().toString() : ex.getMessage());
        }
    }

    private void loadChat() {
        CompletableFuture
                .supplyAsync(this::getChatAsString, threadPool)
                .thenApply(this::processChatResponse)
                .thenAccept(m -> runOnUiThread( () -> displayChatMessages(m)) );
    }

    private String getChatAsString() {
        try (InputStream urlStream = new URL(chatUrl).openStream()) {
            return readString(urlStream);
        } catch (IOException ex) {
            Log.e("ChatActivity::loadChat", ex.getMessage() != null ? ex.getMessage() : "IOException");
        }
        return null;
    }

    private ChatMessage[] processChatResponse(String jsonString) {
        ChatResponse chatResponse = gson.fromJson(jsonString, ChatResponse.class);
        return chatResponse.data;
    }

    @SuppressLint({"ResourceAsColor", "SetTextI18n"})
    private void displayChatMessages(ChatMessage[] chatMessages) {
        boolean wasNew = false;
        for (ChatMessage cm : chatMessages) {
            if (messages.stream().noneMatch(m -> m.getId().equals(cm.getId()))) {
                messages.add(cm);
                wasNew = true;
            }
        }
        if (!wasNew) return;

        messages.sort( Comparator.comparing( ChatMessage::getMoment ) );

        for (ChatMessage cm : messages) {
            if (cm.getView() != null) continue;

            LinearLayout messageLayout = new LinearLayout(ChatActivity.this);
            messageLayout.setOrientation(LinearLayout.VERTICAL);

            // Встановлення гравітації для повідомлення та його фону в залежності від автора
            LinearLayout.LayoutParams layoutParamsMessage = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            if(cm.getAuthor().equals(author)){
                messageLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.chat_my_message_bkg));
                layoutParamsMessage.gravity = Gravity.END;
            }
            else{
                messageLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.chat_message_bkg));
                layoutParamsMessage.gravity = Gravity.START;
            }
            layoutParamsMessage.setMargins(10, 10, 10, 10);
            messageLayout.setLayoutParams(layoutParamsMessage);
            messageLayout.setPadding(15, 15, 15, 15);

            // Встановлюємо маргін знизу для автора
            LinearLayout.LayoutParams authorMargBottom = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            authorMargBottom.setMargins(0, 0, 0, 10);

            // Встановлюємо маргін зверху для дати
            LinearLayout.LayoutParams dateMatgTop = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            dateMatgTop.setMargins(0, 10, 0, 0);

            // Встановлення імені автора
            TextView tvAuthor = new TextView(ChatActivity.this);
            tvAuthor.setText(cm.getAuthor());
            tvAuthor.setTextSize(15);
            tvAuthor.setPadding(0, 0, 0, 0);
            tvAuthor.setTypeface(Typeface.DEFAULT_BOLD); // Використання жирного шрифта
            tvAuthor.setTextColor(ContextCompat.getColor(this, R.color.chat_text_author));
            tvAuthor.setLayoutParams(authorMargBottom);

            // Встановлення тексту повідомлення
            TextView tvMessageText = new TextView(ChatActivity.this);
            tvMessageText.setPadding(0, 0, 0, 0);
            tvMessageText.setText(cm.getText());
            tvMessageText.setTextColor(ContextCompat.getColor(this, R.color.chat_text_message)); // Встановлення кольору тексту

            // Встановлення дати повідомлення
            TextView tvMessageDate = new TextView(ChatActivity.this);
            tvMessageDate.setText(cm.getMoment());
            tvMessageDate.setPadding(0, 0, 0, 0);
            tvMessageDate.setTextSize(10);
            tvMessageDate.setTextColor(ContextCompat.getColor(this, R.color.chat_text_author));
            tvMessageDate.setLayoutParams(dateMatgTop);

            // Додавання елементів в layout
            messageLayout.addView(tvAuthor);
            messageLayout.addView(tvMessageText);
            messageLayout.addView(tvMessageDate);

            // Зберігання view для подальшого використання
            cm.setView(messageLayout);

            // Додавання в контейнер
            chatContainer.addView(messageLayout);
        }
        chatContainer.post( () -> {
            scroller.fullScroll( View.FOCUS_DOWN ) ;
            vBell.startAnimation( bellAnimation ) ;
        } ) ;
    }

    private String readString(InputStream stream) throws IOException {
        ByteArrayOutputStream byteBuilder = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int len;
        while ((len = stream.read(buffer)) != -1) {
            byteBuilder.write(buffer, 0, len);
        }
        return byteBuilder.toString(StandardCharsets.UTF_8.name());
    }

    @Override
    protected void onDestroy() {
        threadPool.shutdownNow();
        super.onDestroy();
    }

    // Вкладені класи
    class ChatResponse {
        private int status;
        private String msg;
        private ChatMessage[] data;

        public int getStatus() {
            return status;
        }

        public String getMsg() {
            return msg;
        }

        public ChatMessage[] getData() {
            return data;
        }
    }

    class ChatMessage {
        private String id;
        private String author;
        private String text;
        private String moment;
        private View view;

        public String getId() {
            return id;
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

        public View getView() {
            return view;
        }

        public ChatMessage setView(View view) {
            this.view = view;
            return this;
        }
    }
}

