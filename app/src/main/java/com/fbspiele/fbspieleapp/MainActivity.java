package com.fbspiele.fbspieleapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LinearLayout rootLinearView = findViewById(R.id.activity_main_child_linear_view_id);

        setUpSettingsCard(rootLinearView);
    }

    void setUpSettingsCard(LinearLayout rootLinearView){
        View settingsCard = View.inflate(this,R.layout.card_layout,null);
        TextView settingsTitle = (TextView) settingsCard.findViewById(R.id.textViewTitle);
        settingsTitle.setText("settings");
        settingsCard.setOnClickListener(view -> startActivity(new Intent(this, SettingsActivity.class)));
        rootLinearView.addView(settingsCard,0);
    }
}