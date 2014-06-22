package com.example.thermostatapp;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

import org.thermostatapp.util.HeatingSystem;
import org.thermostatapp.util.InvalidInputValueException;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SeekBar;
import android.widget.TextView;

public class ManageThermostat extends ActionBarActivity{
	private TextView current_temperature_textView;
	private TextView day_temperature_textView;
	private TextView night_temperature_textView;
	private SeekBar day_temperature_seekBar;
	private SeekBar night_temperature_seekBar;
	private SeekBar current_temperature_seekBar;
	private boolean current_temperature_changed;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_manage_thermostat);
		
		current_temperature_textView = (TextView) findViewById(R.id.current_temperature_textView);
		day_temperature_textView = (TextView) findViewById(R.id.day_temperature_textView);
		night_temperature_textView = (TextView) findViewById(R.id.night_temperature_textView);
		current_temperature_seekBar = (SeekBar) findViewById(R.id.current_temperature_seekBar);
		day_temperature_seekBar = (SeekBar) findViewById(R.id.day_temperature_seekBar);
		night_temperature_seekBar = (SeekBar) findViewById(R.id.night_temperature_seekBar);
		
		current_temperature_seekBar.setMax(25);
		current_temperature_seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				current_temperature_textView.setText(progress+5 +"");
				
				if(fromUser){ //if not refreshed
					current_temperature_changed = true; 
					new PutTemperature(current_temperature_textView.getText().toString(), 
							day_temperature_textView.getText().toString(),
							night_temperature_textView.getText().toString()).execute();
				}
			}
		});
		
		day_temperature_seekBar.setMax(25);
		day_temperature_seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				day_temperature_textView.setText(progress+5 +"");
				
				if(fromUser){ //if not refreshed
					new PutTemperature(current_temperature_textView.getText().toString(), 
							day_temperature_textView.getText().toString(),
							night_temperature_textView.getText().toString()).execute();
				}
			}
		});
		
		night_temperature_seekBar.setMax(25);
		night_temperature_seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				night_temperature_textView.setText(progress+5 +"");
				if(fromUser){ //if not refreshed
					new PutTemperature(current_temperature_textView.getText().toString(), 
							day_temperature_textView.getText().toString(),
							night_temperature_textView.getText().toString()).execute();
				}
				
			}
		});
			
		new GetTemperature().execute();
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
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	    	
	    }
	    return super.onKeyDown(keyCode, event);
	} 
	
	private class GetTemperature extends AsyncTask<String, Void, List<String>> {
		ProgressDialog progressDialog;
		
        @Override
        protected List<String> doInBackground(String... params) {
        	String currentTemperature;
        	String dayTemperature;
        	String nightTemperature;
        	
        	//get methods!
			try {
				currentTemperature = HeatingSystem.get("currentTemperature");
				dayTemperature = HeatingSystem.get("dayTemperature");
				nightTemperature = HeatingSystem.get("nightTemperature");
			} catch (ConnectException e) {
				throw new RuntimeException("Connect exception!",e);
			}
			
			List<String> temperatures = new ArrayList<String>();
			temperatures.add(currentTemperature);
			temperatures.add(dayTemperature);
			temperatures.add(nightTemperature);
			
			return temperatures;
        }

        @Override
        protected void onPostExecute(List<String> result) {
        	progressDialog.dismiss();
        	
        	current_temperature_textView.setText(result.get(0));
        	day_temperature_textView.setText(result.get(1));
        	night_temperature_textView.setText(result.get(2));
        	
        	current_temperature_seekBar.setProgress((int)Double.parseDouble(result.get(0)) - 5);
    		day_temperature_seekBar.setProgress((int)Double.parseDouble(result.get(1)) - 5);
    		night_temperature_seekBar.setProgress((int)Double.parseDouble(result.get(2)) - 5);
        }

        @Override
        protected void onPreExecute() {
        	progressDialog = new ProgressDialog(ManageThermostat.this);
        	progressDialog.setMessage("Getting information...");
        	progressDialog.setCancelable(false);
        	progressDialog.show();
        }
    }
	
	private class PutTemperature extends AsyncTask<String, Void, String> {
		ProgressDialog progressDialog;
		String dayTemperature;
		String nightTemperature;
		String currentTemperature;
		
		private PutTemperature(String currentTemperature, String dayTemperature, String nightTemperature){
			this.currentTemperature = currentTemperature;
			this.dayTemperature = dayTemperature;
			this.nightTemperature = nightTemperature;
		}
		
        @Override
        protected String doInBackground(String... params) {
			try {
				if (current_temperature_changed){
					HeatingSystem.put("currentTemperature", currentTemperature);
					current_temperature_changed = false;
				}
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
        	progressDialog.dismiss();
        }

        @Override
        protected void onPreExecute() {
        	progressDialog = new ProgressDialog(ManageThermostat.this);
        	progressDialog.setMessage("Saving information...");
        	progressDialog.setCancelable(false);
        	progressDialog.show();
        }
    }	
}
