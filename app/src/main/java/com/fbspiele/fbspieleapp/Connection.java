package com.fbspiele.fbspieleapp;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;

class Connection implements Runnable {
    final static String tag = "Connection";

    MainActivity mainActivity;
    Crypto crypto;
    Socket socket;
    Connectivity connectivity;

    String save_eintrage_trenner;

    Connection(MainActivity mainActivity, Connectivity connectivity, Crypto crypto){
        this.mainActivity = mainActivity;
        this.connectivity = connectivity;
        this.crypto = crypto;
        save_eintrage_trenner = getString(R.string.settings_key_eintrageTrenner);
    }

    @Override
    public void run() {
        try {
            String str = getServerIpString();
            int port = getPort();
            try {
                socket = new Socket(str,port);
            }
            catch (SocketException e){
                e.printStackTrace();
                return;
            }
            connectivity.setSocket(socket);

            BufferedReader socketbufferedreader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            final int BUFFER_SIZE = 2048;
            try {
                String message;
                int charsRead;
                char[] buffer = new char[BUFFER_SIZE];
                while ((charsRead = socketbufferedreader.read(buffer)) != -1) {
                    message = new String(buffer).substring(0, charsRead);
                    for (String line : message.split("\n")){
                        if(crypto==null){
                            Log.v(tag,"crypto == null vor socketdataverarbeiten");
                        }
                        else{
                            String decrypted;
                            try {
                                decrypted = crypto.decryptHex(line);
                                Log.v(tag,"Receiving\n"+line +"\n"+decrypted);
                            } catch (Exception e) {
                                e.printStackTrace();
                                decrypted = "";
                                Log.v(tag,"receiving exception for message\n"+line);
                            }
                            if(decrypted.length()>0){
                                fbsocketdataverarbeiten(decrypted);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.v(tag,"disconnected");
            connectivity.connectionDisconnected();


            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mainActivity.getApplicationContext());
            boolean autoReconnect = sharedPref.getBoolean(getString(R.string.settings_key_autoReconnect),true);

            if(autoReconnect){
                connectivity.handler.postDelayed(connectivity.connectToServer,1000);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    String getServerIpString(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mainActivity.getBaseContext());
        return sharedPref.getString(getString(R.string.settings_key_ipAddress),getString(R.string.settings_default_ipAddress));
    }
    int getPort(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mainActivity.getBaseContext());
        String portString = sharedPref.getString(getString(R.string.settings_key_port),getString(R.string.settings_default_port));
        try{
            assert portString != null;
            return Integer.parseInt(portString);
        }
        catch (NumberFormatException e){
            e.printStackTrace();
            Log.e("port","port = 0");
            return 0;
        }
    }

    private void fbsocketdataverarbeiten(String message){
        if(message.contains(save_eintrage_trenner)){
            String[] parts = message.split(save_eintrage_trenner,2);
            fbsocketdataverarbeiten(parts[0]);
            fbsocketdataverarbeiten(parts[1]);
            return;
        }
        Log.v(tag,message);
        if(message.contains("connected")){
            connectivity.connected();
        }
        if(message.contains("pong")){
            mainActivity.onPong();
        }
    }

    String getString(int id){
        return connectivity.getString(id);
    }

}

