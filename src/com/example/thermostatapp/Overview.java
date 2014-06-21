package com.example.thermostatapp;

import java.net.ConnectException;
import java.util.Calendar;

import org.thermostatapp.util.CorruptWeekProgramException;
import org.thermostatapp.util.HeatingSystem;
import org.thermostatapp.util.WeekProgram;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

public class Overview extends ActionBarActivity{
	private TableLayout tableLayout;
	private WeekProgram weekProgram;
	private String day;
	private TextView textView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_overview);
		
		day = getIntent().getStringExtra("day");
	    textView = (TextView) findViewById(R.id.overview_textView);
		textView.setText("Program of " + day.toLowerCase());
		
		new GetWeekProgram().execute();
	}
	
	private void fillTable(){
		tableLayout = (TableLayout) findViewById(R.id.overview_tableLayout);
		tableLayout.setStretchAllColumns(true);
	    tableLayout.bringToFront();
	    
	    TableRow tr1 =  new TableRow(this);
        TextView c11 = new TextView(this);
        c11.setText("Time");
        TextView c21 = new TextView(this);
        c21.setText("Type");
        TextView c31 = new TextView(this);
        c31.setText("State");
        c11.setGravity(Gravity.CENTER);
        c21.setGravity(Gravity.CENTER);
        c31.setGravity(Gravity.CENTER);
        tr1.addView(c11);
        tr1.addView(c21);
        tr1.addView(c31);
        tableLayout.addView(tr1);
        
	    for(int i = 0; i < 10; i++){
			
	        TableRow tr =  new TableRow(this);
	        Button time = new Button(this);
	        final int switchNumber = i;
	        time.setOnClickListener(new OnClickListener(){
	        	@Override
	        	public void onClick(View view){
	        		showTimePickerDialog(view, switchNumber);
	        	}
	        });
	    
	        time.setText(weekProgram.getData().get(day).get(i).getTime());
	        TextView type = new TextView(this);
	        type.setText(weekProgram.getData().get(day).get(i).getType());
	        ToggleButton state = new ToggleButton(this);
	        state.setTextOn("on");
	        state.setTextOff("off");
	        boolean weekProgramState = weekProgram.getData().get(day).get(i).getState();
	        if(weekProgramState){
	        	state.setChecked(true);
	        } else {
	        	state.setChecked(false);
	        }
	        
	        state.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					weekProgram.getData().get(day).get(switchNumber).setState(isChecked);
					
					new PutWeekProgram().execute();
				}
			});
	        
	        
	        time.setGravity(Gravity.CENTER);
	        type.setGravity(Gravity.CENTER);
	        state.setGravity(Gravity.CENTER);
	        
	        tr.addView(time);
	        tr.addView(type);
	        tr.addView(state);
	        tableLayout.addView(tr);
	    }
	}
	
	public void showTimePickerDialog(View v, int switchNumber) {
	    DialogFragment newFragment = new TimePickerFragment(switchNumber);
	    newFragment.show(getSupportFragmentManager(), "timePicker");
	}
	
	private class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
		private int switchNumber;
		
		private TimePickerFragment(int switchNumber){
			this.switchNumber = switchNumber;
		}
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current time as the default values for the picker
			final Calendar c = Calendar.getInstance();
			int hour = c.get(Calendar.HOUR_OF_DAY);
			int minute = c.get(Calendar.MINUTE);
		
			// Create a new instance of TimePickerDialog and return it
			return new TimePickerDialog(getActivity(), this, hour, minute,
								DateFormat.is24HourFormat(getActivity()));
		}
		
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			String hour = "" + hourOfDay;
			String minuteString = "" + minute;
			if(hourOfDay < 10){
				hour = "0" + hour;
			}
			
			if(minute < 10){
				minuteString = "0" + minuteString;
			}
			
			String time = hour + ":" + minuteString;
			weekProgram.getData().get(day).get(switchNumber).setTime(time);
			
    		tableLayout.removeAllViews();
    		new PutWeekProgram().execute();
		}
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
		    	tableLayout.removeAllViews();
		    	new GetWeekProgram().execute();
		    	break;
		    default:
		    	throw new RuntimeException("Not possible menu item");
		}	
		return super.onOptionsItemSelected(item);
	}
	
	private class GetWeekProgram extends AsyncTask<String, Void, WeekProgram> {
		ProgressDialog progressDialog;
		
        @Override
        protected WeekProgram doInBackground(String... params) {   	
			try {
				weekProgram = HeatingSystem.getWeekProgram();
			} catch (ConnectException e) {
				e.printStackTrace();
			} catch (CorruptWeekProgramException e) {
				e.printStackTrace();
			}
			
			return weekProgram;
        }

        @Override
        protected void onPostExecute(WeekProgram result) {
        	progressDialog.dismiss();
        	
        	fillTable();
        }

        @Override
        protected void onPreExecute() {
        	progressDialog = new ProgressDialog(Overview.this);
        	progressDialog.setMessage("Getting information...");
        	progressDialog.setCancelable(false);
        	progressDialog.show();
        }
    }
	
	private class PutWeekProgram extends AsyncTask<String, Void, String> {
		ProgressDialog progressDialog;
		
        @Override
        protected String doInBackground(String... params) {
			try {
				HeatingSystem.setWeekProgram(weekProgram);
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException("Not valid argument",e);
			}
			return null;
        }

        @Override
        protected void onPostExecute(String result) {
        	progressDialog.dismiss();
        	
        	new GetWeekProgram().execute();
        }

        @Override
        protected void onPreExecute() {
        	progressDialog = new ProgressDialog(Overview.this);
        	progressDialog.setMessage("Saving information...");
        	progressDialog.setCancelable(false);
        	progressDialog.show();
        }
    }
}
