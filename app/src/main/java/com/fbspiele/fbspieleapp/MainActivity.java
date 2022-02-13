package com.fbspiele.fbspieleapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    final static String tag = "mainActivity";
    CardView connectionCard;
    boolean connected = false;
    Connectivity connectivity;

    Handler handler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(tag,"onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new Handler();

        LinearLayout rootLinearView = findViewById(R.id.activity_main_child_linear_view_id);

        connectivity = new Connectivity(this);
        connectionCard = setUpConnectionCard(rootLinearView);
        setUpSettingsCard(rootLinearView);
        setUpBuzzerCard(rootLinearView);
        setUpWoLiegtWasCard(rootLinearView);
        setUpBierathlonCard(rootLinearView);
        setUpSchaetztnCard(rootLinearView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        connectivity.mainActivityOnResume();
    }

    void onConnect(){
        connected = true;
        resetConnectionStatus();
    }

    void onDisconnect(){
        connected = false;
        resetConnectionStatus();
    }

    void setConnectionStatusText(String statusMessage){
        TextView connectionStatus = connectionCard.findViewById(R.id.textViewConnectionStatus);
        connectionStatus.setText(statusMessage);
    }

    CardView setUpCardStandardCard(LinearLayout rootLinearView, String titleText, int backgroundColor){
        View cardLayout = View.inflate(this,R.layout.card_layout,null);
        CardView card = cardLayout.findViewById(R.id.card_view);
        card.setCardBackgroundColor(backgroundColor);
        TextView title = card.findViewById(R.id.textViewTitle);
        title.setText(titleText);
        rootLinearView.addView(cardLayout);
        return card;
    }

    void setUpSchaetztnCard(LinearLayout rootLinearView){
        CardView card = setUpCardStandardCard(rootLinearView,
                getString(R.string.maincards_schaetztn_title_text),
                getResources().getColor(R.color.blue_100));
        card.setOnClickListener(view -> onSchaetztnCardClick());
    }

    void onSchaetztnCardClick(){
        //todo
    }


    void setUpBierathlonCard(LinearLayout rootLinearView){
        CardView card = setUpCardStandardCard(rootLinearView,
                getString(R.string.maincards_bierathlon_title_text),
                getResources().getColor(R.color.amber_200));
        card.setOnClickListener(view -> onBierathlonCardClick());
    }

    void onBierathlonCardClick(){
        //todo
    }

    void setUpWoLiegtWasCard(LinearLayout rootLinearView){
        CardView card = setUpCardStandardCard(rootLinearView,
                getString(R.string.maincards_woLiegtWas_title_text),
                getResources().getColor(R.color.teal_200));
        card.setOnClickListener(view -> onWoLiegtWasCardClick());
    }

    void onWoLiegtWasCardClick(){
        //todo
    }

    void setUpBuzzerCard(LinearLayout rootLinearView){
        CardView card = setUpCardStandardCard(rootLinearView,
                getString(R.string.maincards_buzzer_title_text),
                getResources().getColor(R.color.deep_orange_200));
        card.setOnClickListener(view -> onBuzzerCardClick());
    }

    void onBuzzerCardClick(){
        //todo
    }

    CardView setUpConnectionCard(LinearLayout rootLinearView){
        View cardLayout = View.inflate(this,R.layout.card_layout,null);
        LinearLayout innerLinearLayout = cardLayout.findViewById(R.id.card_view_inner_linear_layout);
        CardView connectionCard = cardLayout.findViewById(R.id.card_view);
        connectionCard.setCardBackgroundColor(getResources().getColor(R.color.deep_purple_200));
        TextView settingsTitle = connectionCard.findViewById(R.id.textViewTitle);
        settingsTitle.setText(getString(R.string.maincards_connection_title_text));
        connectionCard.setOnClickListener(view -> onConnectionCardClick());
        rootLinearView.addView(cardLayout);

        TextView statusTextView = new TextView(this);
        statusTextView.setText(getString(R.string.maincards_connection_disconnected_text));
        statusTextView.setId(R.id.textViewConnectionStatus);
        statusTextView.setGravity(Gravity.CENTER);
        innerLinearLayout.addView(statusTextView);

        return connectionCard;
    }

    void onConnectionCardClick(){
        if(connected){
            pingServer();
        }
        else {
            tryReconnectToServer();
        }
    }

    long pingStartTime;
    void pingServer() {
        pingStartTime = System.currentTimeMillis();
        TextView connectionStatus = connectionCard.findViewById(R.id.textViewConnectionStatus);
        connectionStatus.setText(getString(R.string.maincards_connection_pinging_text));
        String pingMessage = getString(R.string.ping_message);
        sendText(pingMessage);
        handler.removeCallbacks(resetConnectionStatusRunnable);
        handler.postDelayed(resetConnectionStatusRunnable, 2500);
    }

    void onPong(){
        long pongTime = System.currentTimeMillis()-pingStartTime;
        setConnectionStatusText("pong in "+pongTime+"ms");
        handler.removeCallbacks(resetConnectionStatusRunnable);
        handler.postDelayed(resetConnectionStatusRunnable, 1000);
    }

    Runnable resetConnectionStatusRunnable = this::resetConnectionStatus;

    void resetConnectionStatus(){
        if(connected){
            connectionCard.setCardBackgroundColor(getResources().getColor(R.color.teal_700));
            setConnectionStatusText(getString(R.string.maincards_connection_connected_text));
        }
        else {
            connectionCard.setCardBackgroundColor(getResources().getColor(R.color.red_300));
            setConnectionStatusText(getString(R.string.maincards_connection_disconnected_text));
        }
    }

    void tryReconnectToServer(){
        TextView connectionStatus = connectionCard.findViewById(R.id.textViewConnectionStatus);
        connectionStatus.setText(getString(R.string.maincards_connection_tryingToReconnect_text));
        connectivity.fbconnecttoserver();
    }

    void sendText(String message){
        // just connectivity.fbsendtosocket(message); leads to android.os.NetworkOnMainThreadException so detour over new thread even though it seems to work in win10control
        new Thread(() -> connectivity.fbsendtosocket(message)).start();
    }




    void setUpSettingsCard(LinearLayout rootLinearView){
        CardView card = setUpCardStandardCard(rootLinearView,
                getString(R.string.maincards_settings_title_text),
                getResources().getColor(R.color.gray_300));
        card.setOnClickListener(view -> startActivity(new Intent(this, SettingsActivity.class)));
    }

}