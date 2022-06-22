package com.fbspiele.fbspieleapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    final static String tag = "mainActivity";
    CardView connectionCard;
    boolean connected = false;
    static Connectivity connectivity;

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

        sendNameUpdate(this);
        sendColorUpdate(this);
        sendTeamUpdate(this);
        sendRoleUpdate(this);
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


    final static String woLiegtWasFragmentTag = "woLiegtWasFragmentTag";
    void woLiegtWasAuflosungFromText(String text){
        if(WoLiegtWasMapFragment.mapView!=null){
            WoLiegtWasMapFragment.mapView.auflosungFromText(text);
        }
    }

    void woLiegtWasReset(){
        if(WoLiegtWasMapFragment.mapView!=null){
            WoLiegtWasMapFragment.mapView.reset();
        }
    }

    public static class WoLiegtWasOverviewFragment extends GameFragment {

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

        public WoLiegtWasOverviewFragment(MainActivity mainActivity, FragmentManager fragmentManager) {
            super(mainActivity, fragmentManager, R.layout.layout_woliegtwas_overview);
            this.mainActivity = mainActivity;
            this.fragmentManager = fragmentManager;
        }


        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            LinearLayout linearLayout = (LinearLayout) view;

            View mapCardWorld = getStandardCard(context, linearLayout, getString(R.string.mapcards_mapWorld_titleText), getResources().getColor(R.color.light_blue_200));
            WoLiegtWasMapWorldFragment woLiegtWasMapWorldFragment = new WoLiegtWasMapWorldFragment(mainActivity, fragmentManager);
            mapCardWorld.setOnClickListener(view1 -> fragmentManager.beginTransaction().replace(container.getId(), woLiegtWasMapWorldFragment, woLiegtWasFragmentTag).addToBackStack(null).commit());

            View mapCardDeutschland = getStandardCard(context, linearLayout, getString(R.string.mapcards_mapDeutschland_titleText), getResources().getColor(R.color.green_200));
            WoLiegtWasMapDeutschlandFragment woLiegtWasMapDeutschlandFragment = new WoLiegtWasMapDeutschlandFragment(mainActivity, fragmentManager);
            mapCardDeutschland.setOnClickListener(view1 -> fragmentManager.beginTransaction().replace(container.getId(), woLiegtWasMapDeutschlandFragment, woLiegtWasFragmentTag).addToBackStack(null).commit());

            View mapCardHamburg = getStandardCard(context, linearLayout, getString(R.string.mapcards_mapHamburg_titleText), getResources().getColor(R.color.deep_orange_200));
            WoLiegtWasMapHamburgFragment woLiegtWasMapHamburgFragment = new WoLiegtWasMapHamburgFragment(mainActivity, fragmentManager);
            mapCardHamburg.setOnClickListener(view1 -> fragmentManager.beginTransaction().replace(container.getId(), woLiegtWasMapHamburgFragment, woLiegtWasFragmentTag).addToBackStack(null).commit());
        }
    }


    public static class WoLiegtWasMapFragment extends GameFragment {

        double refPunkt1X;
        double refPunkt1Y;
        double refPunkt1Phi;
        double refPunkt1Theta;

        double refPunkt2X;
        double refPunkt2Y;
        double refPunkt2Phi;
        double refPunkt2Theta;

        double refPicIntrinsicDimensionsX;
        double refPicIntrinsicDimensionsY;

        static MyMapView mapView;

        public void updateRefPunkt1(double refPunkt1X, double refPunkt1Y, double refPunkt1Phi, double refPunkt1Theta){
            this.refPunkt1X = refPunkt1X;
            this.refPunkt1Y = refPunkt1Y;
            this.refPunkt1Phi = refPunkt1Phi;
            this.refPunkt1Theta = refPunkt1Theta;
        }

        public void updateRefPunkt2(double refPunkt2X, double refPunkt2Y, double refPunkt2Phi, double refPunkt2Theta){
            this.refPunkt2X = refPunkt2X;
            this.refPunkt2Y = refPunkt2Y;
            this.refPunkt2Phi = refPunkt2Phi;
            this.refPunkt2Theta = refPunkt2Theta;
        }

        public void updateIntrinsicDimensions(double refPicIntrinsicDimensionsX, double refPicIntrinsicDimensionsY){
            this.refPicIntrinsicDimensionsX = refPicIntrinsicDimensionsX;
            this.refPicIntrinsicDimensionsY = refPicIntrinsicDimensionsY;
        }

        public WoLiegtWasMapFragment(MainActivity mainActivity, FragmentManager fragmentManager, int LayoutId) {
            super(mainActivity, fragmentManager, LayoutId);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = super.onCreateView(inflater, container, savedInstanceState);
            assert view != null;
            mapView = view.findViewById(R.id.mapImage);
            mapView.updateRefPunkt1(refPunkt1X, refPunkt1Y, refPunkt1Phi, refPunkt1Theta);
            mapView.updateRefPunkt2(refPunkt2X, refPunkt2Y, refPunkt2Phi, refPunkt2Theta);
            mapView.updateIntrinsicDimensions(refPicIntrinsicDimensionsX, refPicIntrinsicDimensionsY);

            Button reset = view.findViewById(R.id.reset);
            reset.setOnClickListener(view1 -> mapView.reset());

            Button confirm = view.findViewById(R.id.confirm);
            confirm.setOnClickListener(view1 -> {
                mapView.initializeMarkerList();
                if(mapView.myMarker==null){
                    Toast.makeText(context, "place a marker first", Toast.LENGTH_SHORT).show();
                    return;
                }
                mapView.mySendMarker = mapView.myMarker;
                mapView.mySendMarker.setColor(Color.parseColor("#B0BEC5"));
                mapView.mySendMarker.markerId = 1;
                mapView.markerList.add(mapView.mySendMarker);
                mainActivity.sendText(getSendMyCoordsText(mapView.getMyMarkerKugelCoords()));
                mapView.invalidate();
            });

            Button cancel = view.findViewById(R.id.cancel);
            cancel.setOnClickListener(view1 -> {
                if(mapView.mySendMarker==null){
                    Toast.makeText(context, "nothing to cancel here", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(mapView.mySendMarker!=mapView.myMarker){
                    mapView.markerList.remove(mapView.myMarker);
                }
                mapView.myMarker.kugelCoords = mapView.mySendMarker.getKugelCoords();
                mapView.invalidate();
            });

            return view;
        }

        String getSendMyCoordsText(double[] coords){
            return "woLiegtWas" +
                    "MyCoordsPhiABC" + coords[0] + "DEF" +
                    "myCoordsThetaGHI" + coords[1] + "JKL";
        }



    }

    public static class WoLiegtWasMapWorldFragment extends WoLiegtWasMapFragment {
        public WoLiegtWasMapWorldFragment(MainActivity mainActivity, FragmentManager fragmentManager) {
            super(mainActivity, fragmentManager, R.layout.layout_mapview_world);
            super.updateIntrinsicDimensions(17658.544921875, 12288.0);
            super.updateRefPunkt1(3213.715731672285, 655.465400201688, -74.312326, 83.083284);
            super.updateRefPunkt2(7411.006265461198, 9238.037382160866, 48.726532, -66.793219);
        }
    }

    public static class WoLiegtWasMapDeutschlandFragment extends WoLiegtWasMapFragment {
        public WoLiegtWasMapDeutschlandFragment(MainActivity mainActivity, FragmentManager fragmentManager) {
            super(mainActivity, fragmentManager, R.layout.layout_mapview_deutschland);
            super.updateIntrinsicDimensions(6252.0, 7626.545654296875);
            super.updateRefPunkt1(1289.9882542101989, 1492.4257898263718, 7.101430, 53.681416);
            super.updateRefPunkt2(5386.46252088853, 6938.988192356576, 14.598883, 47.353683);
        }
    }

    public static class WoLiegtWasMapHamburgFragment extends WoLiegtWasMapFragment {
        public WoLiegtWasMapHamburgFragment(MainActivity mainActivity, FragmentManager fragmentManager) {
            super(mainActivity, fragmentManager, R.layout.layout_mapview_hamburg);
            super.updateIntrinsicDimensions(7136.7275390625, 7664.7275390625);
            super.updateRefPunkt1(898.1525844016661, 627.4686514970464, 9.829585, 53.635658);
            super.updateRefPunkt2(5777.7116473597425, 6885.483791873342, 10.108871, 53.422679);
        }
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
            fragmentManager.beginTransaction().replace(container.getId(),new WoLiegtWasOverviewFragment(mainActivity, fragmentManager)).addToBackStack(null).commit();
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

    }



    static View getStandardCard(Context context, ViewGroup viewGroup, String titleText, int backgroundColor){
        View inflated = LayoutInflater.from(context).inflate(R.layout.card_layout, viewGroup, false);
        CardView card = inflated.findViewById(R.id.card_view);
        card.setCardBackgroundColor(backgroundColor);
        TextView title = card.findViewById(R.id.textViewTitle);
        title.setText(titleText);
        viewGroup.addView(inflated);        // add inflated but return card!
        return card;
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



    static void sendNameUpdate(Context context){
        String name = PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.settings_key_name),"");
        sendText(context.getString(R.string.sendText_newNameAnfangString)+name+context.getString(R.string.sendText_newNameEndString));
    }

    static void sendColorUpdate(Context context){
        String colorHex = PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.settings_key_color_hex),"");
        sendText(context.getString(R.string.sendText_newColorAnfangString)+colorHex+context.getString(R.string.sendText_newColorEndString));
    }


    static void sendTeamUpdate(Context context){
        int team = PreferenceManager.getDefaultSharedPreferences(context).getInt(context.getString(R.string.settings_key_team_int),0);
        sendText(context.getString(R.string.sendText_newTeamAnfangString)+team+context.getString(R.string.sendText_newTeamEndString));
    }


    static void sendRoleUpdate(Context context){
        String role = PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.settings_key_role),"");
        sendText(context.getString(R.string.sendText_newRoleAnfangString)+role+context.getString(R.string.sendText_newRoleEndString));
    }


    static void sendText(String message){
        // just connectivity.fbsendtosocket(message); leads to android.os.NetworkOnMainThreadException so detour over new thread even though it seems to work in win10control
        new Thread(() -> connectivity.fbsendtosocket(message)).start();
    }
}