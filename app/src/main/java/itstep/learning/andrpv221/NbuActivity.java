package itstep.learning.andrpv221;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

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
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NbuActivity extends AppCompatActivity {

    private final java.text.SimpleDateFormat sqlDateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final String kursNbuUrl = "https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?json";

    private TextView tvTitle;
    private LinearLayout chatContainer;
    private ScrollView scroller;

    private final ExecutorService threadPool = Executors.newFixedThreadPool( 3 );
    private final Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_nbu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvTitle         = findViewById( R.id.chat_tv_title );
        chatContainer   = findViewById( R.id.chat_ll_container );
        scroller        = findViewById( R.id.chat_scroller );

        loadKurs();
    }


    private void loadKurs() {
        CompletableFuture
                .supplyAsync( this::getChatAsString, threadPool )
                .thenApply( this::processChatResponse )
                .thenAccept( this::displayChatMessages );
    }

    private String getChatAsString() {
        try( InputStream urlStream = new URL( kursNbuUrl ).openStream() ) {
            return readString( urlStream );
        }
        catch( MalformedURLException ex ) {
            Log.e( "NbuActivity::loadKurs",
                    ex.getMessage() == null ? "MalformedURLException" : ex.getMessage() );
        }
        catch( IOException ex ) {
            Log.e( "NbuActivity::loadKurs",
                    ex.getMessage() == null ? "IOException" : ex.getMessage() );
        }
        return null;
    }

    private KursMessage[] processChatResponse(String jsonString ) {
        return gson.fromJson( jsonString, KursMessage[].class );
    }

    @SuppressLint({"ResourceAsColor", "SetTextI18n"})
    private void displayChatMessages(KursMessage[] kursMessages) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(10, 5, 10, 5);

        runOnUiThread(() -> {
            chatContainer.setBackgroundColor(ContextCompat.getColor(this, R.color.chat_bkg));
            chatContainer.setPadding(10, 5, 10, 5);
        });

        for (KursMessage km : kursMessages) {
            LinearLayout messageLayout = new LinearLayout(NbuActivity.this);
            messageLayout.setOrientation(LinearLayout.VERTICAL);
            messageLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.chat_message_bkg));
            messageLayout.setPadding(15, 15, 15, 15);

            LinearLayout.LayoutParams layoutParamsMessage = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            layoutParamsMessage.setMargins(10, 10, 10, 10);
            messageLayout.setLayoutParams(layoutParamsMessage);


            TextView tvName = new TextView(NbuActivity.this);
            tvName.setText(km.getTxt());
            tvName.setPadding(10, 5, 10, 5);
            tvName.setTextColor(ContextCompat.getColor(this, R.color.chat_text_author));
            tvName.setTextSize(15);
            tvName.setTypeface(null, Typeface.BOLD);

            TextView tvRateDate = new TextView(NbuActivity.this);

            SpannableString spannable = new SpannableString(km.getRate() + " на " + km.getExchangedate());
            spannable.setSpan(new StyleSpan(Typeface.BOLD), 0, km.getRate().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new UnderlineSpan(), 0, km.getRate().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new ForegroundColorSpan(Color.GREEN), 0, km.getRate().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new RelativeSizeSpan(1.2f), 0, km.getRate().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tvRateDate.setText(spannable);

            tvRateDate.setPadding(10, 5, 10, 5);
            tvRateDate.setTextColor(ContextCompat.getColor(this, R.color.chat_text_message));




            messageLayout.addView(tvName);
            messageLayout.addView(tvRateDate);

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

    class KursMessage {
        private String r030;
        private String txt;
        private String rate;
        private String cc;
        private String exchangedate;

        public String getR030() {
            return r030;
        }

        public void setR030(String r030) {
            this.r030 = r030;
        }

        public String getTxt() {
            return txt;
        }

        public void setTxt(String txt) {
            this.txt = txt;
        }

        public String getRate() {
            return rate;
        }

        public void setRate(String rate) {
            this.rate = rate;
        }

        public String getCc() {
            return cc;
        }

        public void setCc(String cc) {
            this.cc = cc;
        }

        public String getExchangedate() {
            return exchangedate;
        }

        public void setExchangedate(String exchangedate) {
            this.exchangedate = exchangedate;
        }
    }

}
