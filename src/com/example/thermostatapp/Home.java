package com.example.thermostatapp;

import java.net.ConnectException;

import org.thermostatapp.util.HeatingSystem;
import org.thermostatapp.util.InvalidInputValueException;

import android.content.Context;
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
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Button;
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
	Button week_program_button;
	Switch weekProgramSwitch;
	SharedPreferences mPrefs;
	Editor prefsEditor;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		
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
		week_program_button = (Button) findViewById(R.id.weekprogram_button);
		weekProgramSwitch = (Switch) findViewById(R.id.weekProgramSwitch);
	}
	
	private void getInformation(){
		String targetTemperature = mPrefs.getString("currentTemperature", "");
		String dayTemperature = mPrefs.getString("dayTemperature", "");
		String nightTemperature = mPrefs.getString("nightTemperature", "");
		
		target_temperature_seekbar.setProgress(temperatureToProgress(targetTemperature));
		high_temperature_seekbar.setProgress(temperatureToProgress(dayTemperature));
		low_temperature_seekbar.setProgress(temperatureToProgress(nightTemperature));
		
		target_temperature_textview.setText(targetTemperature + "°C");
		high_temperature_textview.setText(dayTemperature + "°C");
		low_temperature_textview.setText(nightTemperature + "°C");
		
		new GetTemperature().execute();
	}
	
	
	
	private void setWeekProgramSwitchChangeListener(){
		weekProgramSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					prefsEditor.putString("weekProgramState", "on");
				} else {
					prefsEditor.putString("weekProgramState", "off");
				}
				high_temperature_seekbar.setEnabled(isChecked);
				low_temperature_seekbar.setEnabled(isChecked);
				week_program_button.setEnabled(isChecked);
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
			week_program_button.setEnabled(false);
			high_temperature_seekbar.setEnabled(false);
			low_temperature_seekbar.setEnabled(false);
		} else {
			throw new IllegalArgumentException("Illegal argument!");
		}
	}
	
	private void setSeekBarOnChangeListener(){
		target_temperature_seekbar.setOnSeekBarChangeListener(new SeekbarChangeListener(target_temperature_textview, this, "currentTemperature"));
		high_temperature_seekbar.setOnSeekBarChangeListener(new SeekbarChangeListener(high_temperature_textview, this, "dayTemperature"));
		low_temperature_seekbar.setOnSeekBarChangeListener(new SeekbarChangeListener(low_temperature_textview, this, "nightTemperature"));
	}
	
	 public int temperatureToProgress(String temp){
		 	int output;
		 	if(temp.endsWith("°C")){
		 	 String tempNoSuffix = temp.substring(0, temp.length() - 2);
		 	 output = (int)(Double.parseDouble(tempNoSuffix) * 10) - 50;
		 	} else {
		 	 output = (int)(Double.parseDouble(temp) * 10) - 50;
		 	}
		 	return output;
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
	
	@Override
	public void onBackPressed() {
	    Intent startMain = new Intent(Intent.ACTION_MAIN);      
	        startMain.addCategory(Intent.CATEGORY_HOME);                        
	        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);          
	        startActivity(startMain); 
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
        	current_Temperature_textView.setText(currentTemperature + "°C");
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
	
	public void toManageWeekProgram(View view){
		Intent intent = new Intent(this, ManageWeekProgram.class);
		startActivity(intent);
	}
}

class SeekbarChangeListener implements OnSeekBarChangeListener {
	Context context;
	TextView textview;
	SharedPreferences mPrefs;
	SharedPreferences.Editor prefsEditor;
	String preference;
	
	public SeekbarChangeListener(TextView textview, Context context, String preference){
		this.context = context;
		this.textview = textview;
		
		this.preference = preference;
		mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		prefsEditor = mPrefs.edit();
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		textview.setText(progressToTemperature(progress));
		if(fromUser){ //if not refreshed
            prefsEditor.putString(preference, (progress+50)/10.0 + "");
            prefsEditor.commit();
		}
		
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		new PutTemperature().execute();		
	}
	
	public String progressToTemperature(int progress){
	 	return (progress + 50.0)/10.0 + "°C";
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
}