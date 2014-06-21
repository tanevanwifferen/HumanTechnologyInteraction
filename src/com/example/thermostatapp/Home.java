package com.example.thermostatapp;

import java.net.ConnectException;

import org.thermostatapp.util.HeatingSystem;
import org.thermostatapp.util.InvalidInputValueException;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

public class Home extends ActionBarActivity{
	TextView target_temperature_textview;
	TextView current_Temperature_textView;	
	TextView high_temperature_textview;
	TextView low_temperature_textview;
	SeekBar target_temperature_seekbar;
	SeekBar high_temperature_seekbar;
	SeekBar low_temperature_seekbar;
	Switch weekProgramSwitch;
	SharedPreferences mPrefs;
	Editor prefsEditor;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home2);
		
		initializeVariables();
		setInitialSwitchState();
		setSeekBarOnChangeListener();
		setWeekProgramSwitchChangeListener();
		getInformation();
	}
	
	private void initializeVariables(){
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefsEditor = mPrefs.edit();
		current_Temperature_textView = (TextView) findViewById(R.id.current_Temperature_textView);
		target_temperature_textview = (TextView) findViewById(R.id.target_temperature_textview);
		high_temperature_textview = (TextView) findViewById(R.id.high_temperature_textview);
		low_temperature_textview = (TextView) findViewById(R.id.low_temperature_textview);
		target_temperature_seekbar = (SeekBar) findViewById(R.id.target_temperature_seekbar);
		high_temperature_seekbar = (SeekBar) findViewById(R.id.high_temperature_seekbar);
		low_temperature_seekbar = (SeekBar) findViewById(R.id.low_temperature_seekbar);
		weekProgramSwitch = (Switch) findViewById(R.id.weekProgramSwitch);
		
		target_temperature_seekbar.setMax(25);
		high_temperature_seekbar.setMax(25);
		low_temperature_seekbar.setMax(25);
	}
	
	private void getInformation(){
		String targetTemperature = mPrefs.getString("currentTemperature", "");
		String dayTemperature = mPrefs.getString("dayTemperature", "");
		String nightTemperature = mPrefs.getString("nightTemperature", "");
		
		target_temperature_seekbar.setProgress((int) Double.parseDouble(targetTemperature));
		high_temperature_seekbar.setProgress((int) Double.parseDouble(dayTemperature));
		low_temperature_seekbar.setProgress((int) Double.parseDouble(nightTemperature));
		
		target_temperature_textview.setText(targetTemperature);
		high_temperature_textview.setText(dayTemperature);
		low_temperature_textview.setText(nightTemperature);
		
		new GetTemperature().execute();
	}
	
	private void setWeekProgramSwitchChangeListener(){
		weekProgramSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					prefsEditor.putString("weekProgramState", "on");
					high_temperature_seekbar.setEnabled(true);
					low_temperature_seekbar.setEnabled(true);
				} else {
					prefsEditor.putString("weekProgramState", "off");
					high_temperature_seekbar.setEnabled(false);
					low_temperature_seekbar.setEnabled(false);
				}
				
				prefsEditor.commit();
				new PutWeekProgramState().execute();
			}
		});
	}
	
	private void setInitialSwitchState(){
		String weekProgramState = mPrefs.getString("weekProgramState", "");
		if(weekProgramState.equals("on")){
			weekProgramSwitch.setChecked(true);
		} else if (weekProgramState.equals("off")){
			weekProgramSwitch.setChecked(false);
			high_temperature_seekbar.setEnabled(false);
			low_temperature_seekbar.setEnabled(false);
		} else {
			throw new IllegalArgumentException("Illegal argument!");
		}
	}
	
	private void setSeekBarOnChangeListener(){
		target_temperature_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				target_temperature_textview.setText(progress+5 +"");
				
				if(fromUser){
		            prefsEditor.putString("currentTemperature", progress+5 + "");
		            prefsEditor.commit();
		            new PutTemperature().execute();
				}
			}
		});
		
		
		high_temperature_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				high_temperature_textview.setText(progress+5 +"");
				
				if(fromUser){ //if not refreshed
		            prefsEditor.putString("dayTemperature", progress+5 + "");
		            prefsEditor.commit();
		            new PutTemperature().execute();
				}
			}
		});
		
		low_temperature_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				low_temperature_textview.setText(progress+5 +"");
				if(fromUser){ //if not refreshed
		            prefsEditor.putString("nightTemperature", progress+5 + "");
		            prefsEditor.commit();
		            new PutTemperature().execute();
				}
			}
		});
	}
	
	public void toWeekProgram(View view){
		Intent intent = new Intent(this, ManageWeekProgram.class);
		startActivity(intent);
	}
	
	public void weekProgramStateChange(){
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    getMenuInflater().inflate(R.menu.main, menu);
	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		    case android.R.id.home:
		    	break;
		    case R.id.action_settings:
		    	break;
		    case R.id.action_refresh:
		    	new GetTemperature().execute();
		    	break;
		    default:
		    	break;
		}	
		return super.onOptionsItemSelected(item);
	}
	
	private class GetTemperature extends AsyncTask<String, Void, String> {
		String currentTemperature;
		
        @Override
        protected String doInBackground(String... params) {
        	try {
				currentTemperature = HeatingSystem.get("currentTemperature");
			} catch (ConnectException e) {
				throw new RuntimeException("Connect exception!",e);
			}
        	return null;
        }

        @Override
        protected void onPostExecute(String result) {
        	current_Temperature_textView.setText(currentTemperature);
        }

        @Override
        protected void onPreExecute() {
        }
    }
	
	private class PutTemperature extends AsyncTask<String, Void, String> {
		String currentTemperature = mPrefs.getString("currentTemperature", "");
		String dayTemperature = mPrefs.getString("dayTemperature", "");
		String nightTemperature = mPrefs.getString("nightTemperature", "");
				
        @Override
        protected String doInBackground(String... params) {
			try {
				HeatingSystem.put("currentTemperature", currentTemperature);
				HeatingSystem.put("dayTemperature", dayTemperature);
				HeatingSystem.put("nightTemperature", nightTemperature);
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException("Not valid argument",e);
			} catch (InvalidInputValueException e) {
				throw new RuntimeException("Not valid input",e);
			}
			return null;
        }

        @Override
        protected void onPostExecute(String result) {
        }

        @Override
        protected void onPreExecute() {
        }
    }
	
	private class PutWeekProgramState extends AsyncTask<String, Void, String> {
		String weekProgramState = mPrefs.getString("weekProgramState", "");
				
        @Override
        protected String doInBackground(String... params) {
			try {
				HeatingSystem.put("weekProgramState", weekProgramState);
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException("Not valid argument",e);
			} catch (InvalidInputValueException e) {
				throw new RuntimeException("Not valid input",e);
			}
			return null;
        }

        @Override
        protected void onPostExecute(String result) {
        }

        @Override
        protected void onPreExecute() {
        }
    }
	
	public void toManageThermostat(View view){
		Intent intent = new Intent(this, ManageThermostat.class);
		startActivity(intent);
	}
	
	public void toManageWeekProgram(View view){
		Intent intent = new Intent(this, ManageWeekProgram.class);
		startActivity(intent);
	}
}