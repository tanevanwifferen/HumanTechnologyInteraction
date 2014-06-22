package com.example.thermostatapp;

import java.net.ConnectException;

import android.app.AlertDialog;
import android.content.DialogInterface;
import org.thermostatapp.util.HeatingSystem;
import org.thermostatapp.util.InvalidInputValueException;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
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
	private Menu optionsMenu;
	private boolean firstTime = false;

    private AlertDialog putConnectionFailed;
    private AlertDialog getConnectionFailed;

	public Menu getOptionsMenu(){
		return optionsMenu;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

        this.putConnectionFailed = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_DARK)
                .setTitle("Connection failed")
                .setMessage("Failed to send data to the thermostat server. An internet connection is needed to properly use this app.\n\n Please verify that you are connected to the internet before attempting to retry.")
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
                        new PutWeekProgramState().execute();
                    }
                })
                .create();

        this.getConnectionFailed = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_DARK)
                .setTitle("Connection failed")
                .setMessage("Failed to retrieve data from the thermostat server. An internet connection is needed to properly use this app.\n\n (Please verify that you are connected to the internet before attempting to retry.)")
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
                        new GetTemperature().execute();
                    }
                })
                .create();

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
				Bitmap sunBitmap;
				Bitmap moonBitmap;
				if(isChecked){
					prefsEditor.putString("weekProgramState", "on");
					sunBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sun);
					moonBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.moon);
				} else {
					prefsEditor.putString("weekProgramState", "off");
					sunBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sun_unsaturated);
					moonBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.moon_unsaturated);
				}
				
				ImageView sunView = (ImageView)findViewById(R.id.home_imageview_sun);
				ImageView moonView = (ImageView)findViewById(R.id.home_imageview_moon);
				
				sunView.setImageBitmap(sunBitmap);
				moonView.setImageBitmap(moonBitmap);
				
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
		target_temperature_seekbar.setOnSeekBarChangeListener(new SeekbarChangeListener(target_temperature_textview, this, "currentTemperature", this.putConnectionFailed));
		high_temperature_seekbar.setOnSeekBarChangeListener(new SeekbarChangeListener(high_temperature_textview, this, "dayTemperature", this.putConnectionFailed));
		low_temperature_seekbar.setOnSeekBarChangeListener(new SeekbarChangeListener(low_temperature_textview, this, "nightTemperature", this.putConnectionFailed));
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
		optionsMenu = menu;
	    getMenuInflater().inflate(R.menu.main, menu);
	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		    case android.R.id.home:
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
		String targetTemperature;
		private MenuItem refreshItem = optionsMenu.findItem(R.id.action_refresh);
		
        @Override
        protected String doInBackground(String... params) {
        	HeatingSystem.setActivity(Home.this);
            try {
				currentTemperature = HeatingSystem.get("currentTemperature");
				targetTemperature = HeatingSystem.get("targetTemperature");
			} catch (ConnectException e) {
                Home.this.getConnectionFailed.show();
				this.cancel(true);
			} finally {
                HeatingSystem.unsetActivity();
            }
        	return null;
        }

        @Override
        protected void onPostExecute(String result) {
        	current_Temperature_textView.setText(currentTemperature + "°C");
        	target_temperature_seekbar.setProgress(temperatureToProgress(targetTemperature));
        	refreshItem.setActionView(null);
        }

        @Override
        protected void onPreExecute() {
        	refreshItem.setActionView(R.layout.actionbar_indeterminate_progress);
        }
    }
	
	/*private class GetTemperature2 extends AsyncTask<String, Void, String> {
		String currentTemperature;
		String targetTemperature;
		
        @Override
        protected String doInBackground(String... params) {
            HeatingSystem.setActivity(Home.this);
            try {
				currentTemperature = HeatingSystem.get("currentTemperature");
				targetTemperature = HeatingSystem.get("targetTemperature");
			} catch (ConnectException e) {
				throw new RuntimeException("Connect exception!",e);
			} finally {
                HeatingSystem.unsetActivity();
            }
        	return null;
        }
        
        @Override
        protected void onPostExecute(String result) {
        	current_Temperature_textView.setText(currentTemperature + "°C");
        	target_temperature_seekbar.setProgress(temperatureToProgress(targetTemperature));
        }
    }*/
	
	private class PutWeekProgramState extends AsyncTask<String, Void, String> {
		String weekProgramState = mPrefs.getString("weekProgramState", "");
		private MenuItem refreshItem = optionsMenu.findItem(R.id.action_refresh);
				
        @Override
        protected String doInBackground(String... params) {
			try{
                HeatingSystem.put("weekProgramState", weekProgramState);
            } catch (ConnectException e) {
                Home.this.putConnectionFailed.show();
                this.cancel(true);
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException("Not valid argument",e);
			} catch (InvalidInputValueException e) {
				throw new RuntimeException("Not valid input",e);
			}
			return null;
        }

        @Override
        protected void onPostExecute(String result) {
        	refreshItem.setActionView(null);
        }

        @Override
        protected void onPreExecute() {
        	refreshItem.setActionView(R.layout.actionbar_indeterminate_progress);
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

    private final AlertDialog putConnectionFailed;
	
	public SeekbarChangeListener(TextView textview, Context context, String preference, AlertDialog putConnectionFailed){
		this.context = context;
		this.textview = textview;
		
		this.preference = preference;
		mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		prefsEditor = mPrefs.edit();

        this.putConnectionFailed = putConnectionFailed;
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
			private MenuItem refreshItem = ((Home) context).getOptionsMenu().findItem(R.id.action_refresh);
					
	        @Override
	        protected String doInBackground(String... params) {
				try {
					HeatingSystem.put("currentTemperature", currentTemperature);
					HeatingSystem.put("dayTemperature", dayTemperature);
					HeatingSystem.put("nightTemperature", nightTemperature);
				} catch(ConnectException e) {
                    SeekbarChangeListener.this.putConnectionFailed.show();
                    this.cancel(true);
                } catch (IllegalArgumentException e) {
					throw new IllegalArgumentException("Not valid argument",e);
				} catch (InvalidInputValueException e) {
					throw new RuntimeException("Not valid input",e);
				}
				return null;
	        }

	        @Override
	        protected void onPostExecute(String result) {
	        	refreshItem.setActionView(null);
	        }

	        @Override
	        protected void onPreExecute() {
	        	refreshItem.setActionView(R.layout.actionbar_indeterminate_progress);
	        }
	    }
}