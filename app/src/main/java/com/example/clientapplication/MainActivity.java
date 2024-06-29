package com.example.clientapplication;

import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "ClientApp";

    private EditText numberEditText;
    private Button sendButton;
    private Button sendBackgroundButton;
    private TextView resultTextView;
    private Handler handler = new Handler(Looper.getMainLooper());
    //private static String SERVER_IP;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        numberEditText = findViewById(R.id.inputNumber);
        resultTextView = findViewById(R.id.resultText);
        sendButton = findViewById(R.id.sendButton);
        sendBackgroundButton = findViewById(R.id.sendBackgroundButton);

        sendButton.setOnClickListener(view -> sendNumber(true));
        sendBackgroundButton.setOnClickListener(view -> sendNumber(false));
    }

    private void sendNumber(boolean showUI) {
        String number = numberEditText.getText().toString();
        new Thread(() -> {
            try {
                // Fiziksel cihazda localhost kullanmak için 127.0.0.1 kullanılıyor
                Socket socket = new Socket("127.0.0.1", 5000);
                OutputStream out = socket.getOutputStream();
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                String message = number + ":" + showUI;
                Log.d(TAG, "Sending message: " + message);
                out.write((message + "\n").getBytes());
                out.flush();

                String response = in.readLine();
                int result = Integer.parseInt(response);

                handler.post(() -> resultTextView.setText("Sonuç: " + result));
                Log.d(TAG, "Received response: " + result);

                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "Error in sendNumber", e);
            }
        }).start();

        int number_actual = Integer.parseInt(number);
        Intent intent = new Intent("com.example.serverapplication.START_SERVER");
        intent.putExtra("number", number_actual);
        intent.putExtra("showUI", showUI);
        Log.d(TAG, "Sending broadcast with number: " + number + " and showUI: " + showUI);

        sendBroadcast(intent);

    }



    private void sendNumberrr(boolean showUI) {
        int number = Integer.parseInt(numberEditText.getText().toString());

        Intent intent = new Intent("com.example.serverapplication.START_SERVER");
        //intent.setAction("com.example.serverapplication.START_SERVER");
        intent.putExtra("number", number);
        intent.putExtra("showUI", showUI);
        Log.d(TAG, "Sending broadcast with number: " + number + " and showUI: " + showUI);

        sendBroadcast(intent);

        //new Thread(() -> tryConnect(number, showUI, 0)).start();

            /*
        new Thread(() -> {
            try {
                Socket socket = new Socket("127.0.0.1", 5000);
                OutputStream out = socket.getOutputStream();
                out.write((number + ":" + showUI + "\n").getBytes());
                out.flush();

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String response = in.readLine();
                runOnUiThread(() -> numberEditText.setText(response));
                //String message = number + ":" + showUI;
                //Log.d("TAG", "Sending message: " + message);

                //out.write((message + "\n").getBytes());
                //out.flush();

                //String response = in.readLine();
                //int result = Integer.parseInt(response);

                //handler.post(() -> resultTextView.setText("Sonuç: " + result));

                Log.d("TAG", "Received response: " + response);

                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("TAG", "Error in sendNumber", e);

            }
        }).start();


 */

    }


    private void tryConnect(int number, boolean showUI, int attempt) {
        if (attempt > 10) { // 10 denemeden sonra vazgeç
            runOnUiThread(() -> numberEditText.setText("Connection failed"));
            return;
        }
        try {
            // Yayın gönderildikten sonra server uygulamasının başlatılmasını bekle
            Thread.sleep(1000 * attempt); // Her denemede biraz daha uzun bekle

            // Socket bağlantısını başlat ve veriyi gönder
            Socket socket = new Socket("127.0.0.1", 5000);
            OutputStream out = socket.getOutputStream();
            out.write((number + ":" + showUI + "\n").getBytes());
            out.flush();

            // Cevabı oku
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String response = in.readLine();
            runOnUiThread(() -> numberEditText.setText(response));

            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
            // Bağlantı başarısız olduysa tekrar dene
            try {
                Thread.sleep(1000); // 1 saniye bekle
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
            tryConnect(number, showUI, attempt + 1);
        }
    }

}
