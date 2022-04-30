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
import android.widget.Toast;

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

        fragmentManager.beginTransaction().add(R.id.activity_main_frame_layout, new AllGamesFragment(this, fragmentManager)).commit();

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
    void firstBuzzered(){
        BuzzerFragment buzzerFragment = (BuzzerFragment) fragmentManager.findFragmentByTag(getString(R.string.buzzerFragmentTag));
        assert buzzerFragment != null;
        buzzerFragment.glow();
    }

    void buzzeredToSlow(){
        runOnUiThread(() -> Toast.makeText(getBaseContext(),getString(R.string.buzzer_toSlow_Toastmessage),Toast.LENGTH_SHORT).show());
    }

    public static class BuzzerFragment extends GameFragment {
        public BuzzerFragment(MainActivity mainActivity, FragmentManager fragmentManager) {
            super(mainActivity, fragmentManager, R.layout.fragment_buzzer);
        }
        Handler handler;

        View view;
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            view = super.onCreateView(inflater, container, savedInstanceState);
            assert view != null;
            final View buzzerTop = view.findViewById(R.id.buzzerImageTop);
            float buzzerTopInitialYpos = buzzerTop.getY();
            view.findViewById(R.id.buzzerImageBot).setOnClickListener(view1 -> {
                Log.v("test","hi");
                mainActivity.sendText(getString(R.string.buzzer_message));
                long buzzerAnimationDuration = 100;
                buzzerTop.animate().y(buzzerTopInitialYpos+50).setDuration(buzzerAnimationDuration).start();
                Handler handler = new Handler();
                Runnable resetBuzzer = () -> buzzerTop.animate().y(buzzerTopInitialYpos).setDuration(buzzerAnimationDuration).start();
                handler.postDelayed(resetBuzzer, buzzerAnimationDuration);
            });
            handler = new Handler();
            return view;
        }

        void glow(){
            long glowAnimationDuration = 500;
            View buzzerGlowView = view.findViewById(R.id.buzzerImageGlow);
            Runnable glowAn = () -> buzzerGlowView.animate().alpha(1).setDuration(glowAnimationDuration).start();
            Runnable glowAus = () -> buzzerGlowView.animate().alpha(0).setDuration(glowAnimationDuration).start();
            for(int i = 0; i<5; i++){
                handler.postDelayed(glowAn, i*1000);
                handler.postDelayed(glowAus, i*1000+glowAnimationDuration);
            }
        }
    }

    public static class GameFragment extends Fragment {
        MainActivity mainActivity;
        FragmentManager fragmentManager;
        int fragmentLayout;
        public GameFragment(MainActivity mainActivity, FragmentManager fragmentManager, int fragmentLayout){
            this.mainActivity = mainActivity;
            this.fragmentManager = fragmentManager;
            this.fragmentLayout = fragmentLayout;
        }

        View fragmentView;
        Context context;
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            fragmentView = inflater.inflate(fragmentLayout, container, false);
            context = getContext();
            return fragmentView;
        }
    }

    public static class AllGamesFragment extends Fragment {
        MainActivity mainActivity;
        FragmentManager fragmentManager;

        Fragment buzzerFragment;


        public AllGamesFragment(MainActivity mainActivity, FragmentManager fragmentManager){
            this.mainActivity = mainActivity;
            this.fragmentManager = fragmentManager;
        }
        View fragmentView;
        Context context;
        ViewGroup container;
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            fragmentView = inflater.inflate(R.layout.fragment_games, container, false);
            context = getContext();
            this.container = container;
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
            //fragmentManager.beginTransaction().replace(R.id.fragment_games,new BuzzerFragment(fragmentManager)).remove(this).addToBackStack(null).commit();
        }

        void onWoLiegtWasCardClick(){
            //todo
        }


        void onBuzzerCardClick(){
            //todo
            fragmentManager.beginTransaction().replace(container.getId(),new BuzzerFragment(mainActivity, fragmentManager), getString(R.string.buzzerFragmentTag)).addToBackStack(null).commit();
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