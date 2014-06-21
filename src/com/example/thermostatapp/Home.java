package com.example.thermostatapp;

import org.thermostatapp.util.WeekProgram;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.text.style.ParagraphStyle;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.google.gson.Gson;

public class Home extends ActionBarActivity{

	public SeekBar targetTempSeekbar;
	public SeekBar highTempSeekbar;
	public SeekBar lowTempSeekbar;
	
	public TextView currentTempTextView;
	public TextView targetTempTextView;
	public TextView highTempTextView;
	public TextView lowTempTextView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home2);
		
		//GET INFORMATION
		SharedPreferences  mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		Gson gson = new Gson();
		String currentTemperature = mPrefs.getString("currentTemperature", null);
		
		targetTempSeekbar = (SeekBar)findViewById(R.id.target_temperature_seekbar);
		targetTempTextView = (TextView)findViewById(R.id.target_temperature_textview);
		targetTempSeekbar.setOnSeekBarChangeListener(new SliderListener(this, targetTempTextView));
		
		highTempSeekbar = (SeekBar)findViewById(R.id.high_temperature_seekbar);
		highTempTextView = (TextView)findViewById(R.id.high_temperature_textview);
		highTempSeekbar.setOnSeekBarChangeListener(new SliderListener(this, highTempTextView));
		
		lowTempSeekbar = (SeekBar)findViewById(R.id.low_temperature_seekbar);
		lowTempTextView = (TextView)findViewById(R.id.low_temperature_textview);
		lowTempSeekbar.setOnSeekBarChangeListener(new SliderListener(this, lowTempTextView));
		
		//String json = mPrefs.getString("weekProgram", "");
		//WeekProgram weekProgram = gson.fromJson(json, WeekProgram.class);
	}
	
	public void toManageWeekProgram(View view){
		Intent intent = new Intent(this, ManageWeekProgram.class);
		startActivity(intent);
	}
}

class SliderListener implements OnSeekBarChangeListener{
	Home parent;
	TextView toChange;

	public SliderListener(Home parent, TextView toChange){
		this.parent = parent;
		this.toChange = toChange;
	}
	
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		this.toChange.setText(progressToTemperature(seekBar.getProgress()));
		
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}
	
	public String progressToTemperature(int progress){
		return (progress + 50.0)/10.0 + "°C";
	}
	
	public int temperatureToProgress(String temp){
		int output;
		if(temp.endsWith("°C")){
			String tempNoSuffix = temp.substring(0, temp.length() - 2);
			output = (Integer.valueOf(tempNoSuffix) * 10 + 50);
		} else {
			output = (Integer.valueOf(temp) * 10) + 50;
		}
		return output;
	}
}


