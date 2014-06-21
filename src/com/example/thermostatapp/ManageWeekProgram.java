package com.example.thermostatapp;

import java.net.ConnectException;
import java.util.ArrayList;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.*;
import org.thermostatapp.util.HeatingSystem;
import org.thermostatapp.util.InvalidInputValueException;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

public class ManageWeekProgram extends ActionBarActivity{
	private ToggleButton vacation_mode_togglebutton;
	private SeekBar vacation_mode_seekBar;
	private TextView vacation_mode_temperature;
	private boolean refresh = false;
	private TextView vacation_mode_textView;

    private WeekPagerAdapter adapter;

    private ViewPager pager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_manage_weekprogram);
		
		vacation_mode_togglebutton = (ToggleButton) findViewById(R.id.vacation_mode_togglebutton);
		vacation_mode_seekBar = (SeekBar) findViewById(R.id.vacation_mode_seekBar);
		vacation_mode_temperature = (TextView) findViewById(R.id.vacation_mode_temperature);
		vacation_mode_textView = (TextView) findViewById(R.id.vacation_mode_textView);
				
		vacation_mode_togglebutton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					vacation_mode_seekBar.setVisibility(View.VISIBLE);
					vacation_mode_temperature.setVisibility(View.VISIBLE);
					vacation_mode_textView.setVisibility(View.VISIBLE);
					new GetCurrentTemperature().execute();
				} else {
					vacation_mode_seekBar.setVisibility(View.GONE);
					vacation_mode_temperature.setVisibility(View.GONE);
					vacation_mode_textView.setVisibility(View.GONE);
					new PutTemperature("","off").execute();
				}
			}
		});
		
		
		vacation_mode_seekBar.setMax(25);
		vacation_mode_seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				vacation_mode_temperature.setText(progress+5 +"");
				if(fromUser){ //if not refreshed
					//then the seekbar has been changed by the user, so set values
					new PutTemperature(progress+5 + "", "on").execute(); 
				} else {
				
				}
			}
		});

        this.adapter = new WeekPagerAdapter(getSupportFragmentManager());
        this.pager = (ViewPager) findViewById(R.id.weekprogram_pager);
        this.pager.setAdapter(this.adapter);

        ActionBar bar = getActionBar();
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                ManageWeekProgram.this.pager.setCurrentItem(tab.getPosition(), true);
            }

            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
                // hide the given tab
            }

            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
                // probably ignore this event
            }
        };

        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        for(String day : days)
        {
            ActionBar.Tab tab = bar.newTab();
            tab.setText(day);
            tab.setTabListener(tabListener);
            bar.addTab(tab);
        }

		new GetWeekProgramState().execute();
	}

    class WeekPagerAdapter extends FragmentPagerAdapter
    {

        Fragment[] fragments = new Fragment[7];

        public WeekPagerAdapter(FragmentManager fm)
        {
            super(fm);
        }

        @Override
        public Fragment getItem(int i)
        {
            Fragment f = this.fragments[i];
            if(f == null) {
                f = new ManageDayFragment();
                this.fragments[i] = f;
            }
            Bundle data = new Bundle();
            // Fill data
            f.setArguments(data);
            return f;
        }

        @Override
        public int getCount()
        {
            return 7;
        }
    }

    class ManageDayFragment extends Fragment
    {

        // Add some switches

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
        {
            // The last two arguments ensure LayoutParams are inflated
            // properly.
            View rootView = inflater.inflate(R.layout.fragment_base, container, false);
            Bundle args = getArguments();
            // Instantiate the fields
            return rootView;
        }
    }
	
	public void toOverview(View view){
		Intent intent = new Intent(this, Overview.class);
		switch(view.getId()) {
			case R.id.monday_button:
				intent.putExtra("day","Monday");
				break;
			case R.id.tuesday_button:
				intent.putExtra("day","Tuesday");
				break;
			case R.id.wednesday_button:
				intent.putExtra("day","Wednesday");
				break;
			case R.id.thursday_button:
				intent.putExtra("day","Thursday");
				break;
			case R.id.friday_button:
				intent.putExtra("day","Friday");
				break;
			case R.id.saturday_button:
				intent.putExtra("day","Saturday");
				break;
			case R.id.sunday_button:
				intent.putExtra("day","Sunday");
				break;
			default:
				throw new RuntimeException("Unknown day");
		}
		startActivity(intent);
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
		    	refresh = true;
		    	new GetWeekProgramState().execute();
		    default:
		    	break;
		}	
		return super.onOptionsItemSelected(item);
	}
	
	private class GetWeekProgramState extends AsyncTask<String, Void, String> {
		ProgressDialog progressDialog;
		
        @Override
        protected String doInBackground(String... params) {
        	String weekProgramState;
        	
        	//get methods!
			try {
				weekProgramState = HeatingSystem.get("weekProgramState");
			} catch (ConnectException e) {
				throw new RuntimeException("Connect exception!",e);
			}
			
			return weekProgramState;
        }

        @Override
        protected void onPostExecute(String result) {
        	progressDialog.dismiss();
        	
        	if (result.equals("on")){
        		vacation_mode_seekBar.setVisibility(View.VISIBLE);
				vacation_mode_temperature.setVisibility(View.VISIBLE);
				vacation_mode_textView.setVisibility(View.VISIBLE);
				vacation_mode_togglebutton.setChecked(true);
				new GetCurrentTemperature().execute();
        	} else if (result.equals("off")){
        		vacation_mode_seekBar.setVisibility(View.GONE);
				vacation_mode_temperature.setVisibility(View.GONE);
				vacation_mode_textView.setVisibility(View.GONE);
				vacation_mode_togglebutton.setChecked(false);
        	} else {
        		throw new RuntimeException("Not valid result");
        	}
        }

        @Override
        protected void onPreExecute() {
        	progressDialog = new ProgressDialog(ManageWeekProgram.this);
        	progressDialog.setMessage("Getting information...");
        	progressDialog.setCancelable(false);
        	progressDialog.show();
        }
    }
	
	private class GetCurrentTemperature extends AsyncTask<String, Void, String> {
		ProgressDialog progressDialog;
		
        @Override
        protected String doInBackground(String... params) {
        	String currentTemperature;
        	
        	//get methods!
			try {
				currentTemperature = HeatingSystem.get("currentTemperature");
			} catch (ConnectException e) {
				throw new RuntimeException("Connect exception!",e);
			}
			
			return currentTemperature;
        }

        @Override
        protected void onPostExecute(String result) {
        	progressDialog.dismiss();
        	
        	vacation_mode_temperature.setText(result);
        	vacation_mode_seekBar.setProgress((int)Double.parseDouble(result) - 5);
        	
        	if(!refresh){
        		//then put to server that you're actually in the vacationmode
        		new PutTemperature(result, "on").execute();
        	} else {
        		refresh = false;
        	}
        }

        @Override
        protected void onPreExecute() {
        	progressDialog = new ProgressDialog(ManageWeekProgram.this);
        	progressDialog.setMessage("Getting information...");
        	progressDialog.setCancelable(false);
        	progressDialog.show();
        }
    }
	
	
	private class PutTemperature extends AsyncTask<String, Void, String> {
		ProgressDialog progressDialog;
		String currentTemperature;
		String weekProgramState;
		
		private PutTemperature(String currentTemperature, String weekProgramState){
			this.currentTemperature = currentTemperature;
			this.weekProgramState = weekProgramState;
		}
		
        @Override
        protected String doInBackground(String... params) {
			try {
				if(weekProgramState.equals("off")){
					HeatingSystem.put("weekProgramState", "off");
				} else {
					HeatingSystem.put("weekProgramState", "on");
					HeatingSystem.put("currentTemperature", currentTemperature);
				} 
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
        	progressDialog = new ProgressDialog(ManageWeekProgram.this);
        	progressDialog.setMessage("Saving information...");
        	progressDialog.setCancelable(false);
        	progressDialog.show();
        }
    }	
}
