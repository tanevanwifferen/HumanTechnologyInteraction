package com.example.thermostatapp;

import java.net.ConnectException;

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
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
       
        new PrefetchData().execute();
    }
    
    private class PrefetchData extends AsyncTask<Void, Void, Void> {
 
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
 
        @Override
        protected Void doInBackground(Void... arg0) {
        	try{
				currentTemperature = HeatingSystem.get("currentTemperature");
				dayTemperature = HeatingSystem.get("dayTemperature");
				nightTemperature = HeatingSystem.get("nightTemperature");
				weekProgramState = HeatingSystem.get("weekProgramState");
				weekProgram = HeatingSystem.getWeekProgram();
			} catch (ConnectException e) {
				throw new RuntimeException("Connect exception!",e);
			} catch (CorruptWeekProgramException e){
				throw new RuntimeException("Corrupt week program!", e);
			}
        	
        	return null;
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
