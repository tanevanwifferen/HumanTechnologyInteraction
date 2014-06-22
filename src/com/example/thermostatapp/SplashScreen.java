package com.example.thermostatapp;

import java.net.ConnectException;

import android.app.AlertDialog;
import android.content.DialogInterface;
import org.thermostatapp.util.CorruptWeekProgramException;
import org.thermostatapp.util.HeatingSystem;
import org.thermostatapp.util.WeekProgram;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.google.gson.Gson;

public class SplashScreen extends Activity {
    String currentTemperature;
	String dayTemperature;
	String nightTemperature;
	String weekProgramState;
	WeekProgram weekProgram;

    AlertDialog connectionFailed;
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        this.connectionFailed = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_DARK)
                .setTitle("Connection failed")
                .setMessage("Failed to connect to the thermostat server. An internet connection is needed to properly use this app.\n" +
                        "\n" +
                        " Please verify that you are connected to the internet before attempting to retry.")
                .setCancelable(true)
                .setPositiveButton("Close App", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.cancel();
                        Intent startMain = new Intent(Intent.ACTION_MAIN);
                        startMain.addCategory(Intent.CATEGORY_HOME);
                        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(startMain);
                    }
                })
                .setNegativeButton("Retry", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.cancel();
                        new PrefetchData().execute();
                    }
                })
                .create();

        new PrefetchData().execute();
    }
    
    private class PrefetchData extends AsyncTask<Void, Void, Void> {
 
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
 
        @Override
        protected Void doInBackground(Void... args) {
            HeatingSystem.setActivity(SplashScreen.this);
            try{
				currentTemperature = HeatingSystem.get("currentTemperature");
				dayTemperature = HeatingSystem.get("dayTemperature");
				nightTemperature = HeatingSystem.get("nightTemperature");
				weekProgramState = HeatingSystem.get("weekProgramState");
				weekProgram = HeatingSystem.getWeekProgram();
			} catch (ConnectException e) {
                this.cancel(true);
			} catch (CorruptWeekProgramException e){
                // Programmer's fault
				throw new RuntimeException("Corrupt week program!", e);
			} finally {
                HeatingSystem.unsetActivity();
            }
        	
        	return null;
        }

        @Override
        protected void onCancelled(Void result) {
            SplashScreen.this.connectionFailed.show();
        }
 
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            
            //SAVE INFORMATION
            SharedPreferences  mPrefs = PreferenceManager.getDefaultSharedPreferences(SplashScreen.this);
            Editor prefsEditor = mPrefs.edit();
            prefsEditor.putString("currentTemperature", currentTemperature);
            prefsEditor.putString("dayTemperature", dayTemperature);
            prefsEditor.putString("nightTemperature", nightTemperature);
            prefsEditor.putString("weekProgramState", weekProgramState);
            
            Gson gson = new Gson();
            String json = gson.toJson(weekProgram);
            prefsEditor.putString("weekProgram", json);
            prefsEditor.commit();
            
//            Toast.makeText(getApplicationContext(), "SAVED" + currentTemperature, Toast.LENGTH_SHORT).show();
            
            Intent intent = new Intent(SplashScreen.this, Home.class);
            startActivity(intent);
        }
 
    }
 
}
