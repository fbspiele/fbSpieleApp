package com.fbspiele.fbspieleapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    final static String tag = "mainActivity";
    CardView connectionCard;
    boolean connected = false;
    Connectivity connectivity;

    Handler handler;

    FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(tag,"onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fragmentManager = getSupportFragmentManager();

        fragmentManager.beginTransaction().add(R.id.activity_main_frame_layout, new allGamesFragment(fragmentManager)).commit();

        handler = new Handler();

        LinearLayout rootLinearViewAll = findViewById(R.id.activity_main_linear_view_all);
        //LinearLayout rootLinearViewGames = findViewById(R.id.fragment_games_linear_layout);

        connectivity = new Connectivity(this);
        connectionCard = setUpConnectionCard(rootLinearViewAll);
        //setUpBuzzerCard(rootLinearViewGames);
        //setUpWoLiegtWasCard(rootLinearViewGames);
        //setUpBierathlonCard(rootLinearViewGames);
        //setUpSchaetztnCard(rootLinearViewGames);
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

/*
    }

*/
    public static class allGamesFragment extends Fragment {
        FragmentManager fragmentManager;

        public allGamesFragment(FragmentManager fragmentManager){
            this.fragmentManager = fragmentManager;
        }
        View fragmentView;
        Context context;
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            fragmentView = inflater.inflate(R.layout.fragment_games, container, false);
            context = getContext();
            return fragmentView;
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            LinearLayout linearLayout = (LinearLayout) view;

            View cardSettings = getStandardCard(context, linearLayout, getString(R.string.maincards_settings_title_text), getResources().getColor(R.color.gray_300));
            cardSettings.findViewById(R.id.card_view).setOnClickListener(view1 -> onSettingsCardClick());

            View cardBuzzer = getStandardCard(context, linearLayout, getString(R.string.maincards_buzzer_title_text), getResources().getColor(R.color.deep_orange_200));
            cardBuzzer.setOnClickListener(view1 -> onBuzzerCardClick());

            View cardBierathlon = getStandardCard(context, linearLayout, getString(R.string.maincards_bierathlon_title_text), getResources().getColor(R.color.amber_200));
            cardBierathlon.setOnClickListener(view1 -> onBierathlonCardClick());

            View cardWoLiegtWas = getStandardCard(context, linearLayout,getString(R.string.maincards_woLiegtWas_title_text),getResources().getColor(R.color.teal_200));
            cardWoLiegtWas.setOnClickListener(view1 -> onWoLiegtWasCardClick());


            View getSchaetztnCard = getStandardCard(context, linearLayout,getString(R.string.maincards_schaetztn_title_text),getResources().getColor(R.color.blue_100));
            getSchaetztnCard.setOnClickListener(view1 -> onSchaetztnCardClick());
        }

        void onBierathlonCardClick(){
            //todo
            fragmentManager.beginTransaction().remove(this).commit();
        }

        void onWoLiegtWasCardClick(){
            //todo
        }


        void onBuzzerCardClick(){
            //todo
            //findViewById(R.id.activity_main_linear_view_games).setBackgroundColor(getResources().getColor(R.color.teal_200));
        }

        void onSettingsCardClick(){
            startActivity(new Intent(context, SettingsActivity.class));
        }

        void onSchaetztnCardClick(){
            //todo
        }

        View getStandardCard(Context context, ViewGroup viewGroup, String titleText, int backgroundColor){
            View inflated = LayoutInflater.from(context).inflate(R.layout.card_layout, viewGroup, false);
            CardView card = inflated.findViewById(R.id.card_view);
            card.setCardBackgroundColor(backgroundColor);
            TextView title = card.findViewById(R.id.textViewTitle);
            title.setText(titleText);
            viewGroup.addView(inflated);        // add inflated but return card!
            return card;
        }
    }


    CardView setUpConnectionCard(LinearLayout rootLinearView){
        View cardLayout = View.inflate(this,R.layout.card_layout,null);
        LinearLayout innerLinearLayout = cardLayout.findViewById(R.id.card_view_inner_linear_layout);
        CardView connectionCard = cardLayout.findViewById(R.id.card_view);
        connectionCard.setCardBackgroundColor(getResources().getColor(R.color.deep_purple_200));
        TextView settingsTitle = connectionCard.findViewById(R.id.textViewTitle);
        settingsTitle.setText(getString(R.string.maincards_connection_title_text));
        connectionCard.setOnClickListener(view -> onConnectionCardClick());
        rootLinearView.addView(cardLayout,0);

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
}