package com.fbspiele.fbspieleapp;

import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.PrintStream;
import java.net.Socket;

public class Connectivity {
    final static private String tag = "Connection";

    MainActivity mainActivity;
    Crypto crypto;
    final Handler handler = new Handler();


    //socket
    private Socket socket;
    boolean stillconnected = false;
    boolean firstconnection = true;


    Connectivity(MainActivity mainActivity){
        this.mainActivity = mainActivity;
    }

    void mainActivityOnResume(){
        if(firstconnection){
            stillconnected = false;
            fbconnecttoserver();
            firstconnection = false;
        }
        else {
            if(socket==null){
                fbconnecttoserver();
            }
            else if(socket.isClosed()){
                fbconnecttoserver();
            }
            else if (!stillconnected){
                fbconnecttoserver();
            }
        }
    }

    String getString(int resId){
        return mainActivity.getString(resId);
    }

    //socket
    public void fbconnecttoserver() {
        Crypto settingsEncryptionCrypto = new Crypto(getString(R.string.settings_settingsEncryption_password),getString(R.string.settings_settingsEncryption_salt));
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mainActivity.getBaseContext());
        String encryptedPassword = sharedPreferences.getString(getString(R.string.settings_encryptedPassword_pref_key),"");
        if(encryptedPassword.length()==0){
            Log.w(tag, "encrypted password length == 0");
            return;
        }
        String encryptedSalt = sharedPreferences.getString(getString(R.string.settings_encryptedSalt_pref_key),"");
        if(encryptedSalt.length()==0){
            Log.w(tag, "encrypted salt length == 0");
            return;
        }
        crypto = new Crypto(settingsEncryptionCrypto.decryptHex(encryptedPassword),settingsEncryptionCrypto.decryptHex(encryptedSalt));
        Connection connection = new Connection(mainActivity,this, crypto);
        Thread connectionThread = new Thread(connection);
        connectionThread.setName("listeningThread");
        connectionThread.start();
    }

    void setSocket(Socket socket){
        this.socket = socket;
    }

    void connectionDisconnected(){
        stillconnected = false;
        mainActivity.onDisconnect();
    }


    void connected(){
        stillconnected = true;
        mainActivity.onConnect();
        handler.removeCallbacks(connectToServer);
    }

    Runnable connectToServer = new Runnable() {
        @Override
        public void run() {
            handler.removeCallbacks(connectToServer);
            fbconnecttoserver();
        }
    };

    //send
    public void fbsendtosocket(String text){
        try {
            if(socket!=null){
                PrintStream ps = new PrintStream(socket.getOutputStream());
                String[] umlauteToReplace = {"ä","ae","ö","oe","ü","ue","ß","sz","Ä","AE","Ö","OE","Ü","UE","ẞ","SZ"};      //muss offensichtlich 2*N_0 sein
                for (int i = 0; i<umlauteToReplace.length;i=i+2){
                    text = text.replace(umlauteToReplace[i],umlauteToReplace[i+1]);
                }
                text = "xxx"+text+"xxx";
                if (crypto!=null){
                    String encrypted = crypto.encryptHex(text);
                    ps.println(encrypted);
                    Log.v("sending", "\n"+text+"\n"+encrypted);
                }
                else {
                    Log.v(tag, "crypto == null (no password/salt set?)");
                }

            } else{
                Log.v("sendingtosocket", "socket ist null");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
