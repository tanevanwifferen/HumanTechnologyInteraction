package com.example.thermostatapp;

import java.net.ConnectException;
import java.util.Calendar;

import org.thermostatapp.util.CorruptWeekProgramException;
import org.thermostatapp.util.HeatingSystem;
import org.thermostatapp.util.WeekProgram;

import android.app.ActionBar;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.gson.Gson;

public class ManageWeekProgram extends ActionBarActivity{
    private final Gson gson = new Gson();
    private WeekProgram weekProgram;
    private WeekPagerAdapter adapter;
    private ViewPager pager;
    private TableLayout tableLayout;
    private String day;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_manage_weekprogram);

        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        weekProgram = gson.fromJson(mPrefs.getString("weekProgram", ""), WeekProgram.class);
        adapter = new WeekPagerAdapter(getSupportFragmentManager());
        pager = (ViewPager) findViewById(R.id.weekprogram_pager);
        pager.setAdapter(adapter);

        ActionBar bar = getActionBar();
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
//            	day = tab.getText().toString();
                System.out.println(tab.getPosition());
                pager.setCurrentItem(tab.getPosition(), true);
            }

            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
                // hide the given tab
            }

            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
                // probably ignore this event
            }
        };

        this.pager.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        // When swiping between pages, select the
                        // corresponding tab.
                        getActionBar().setSelectedNavigationItem(position);
                    }
                });

        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        for(String day : days)
        {
            ActionBar.Tab tab = bar.newTab();
            tab.setText(day);
            tab.setTabListener(tabListener);
            bar.addTab(tab);
        }
	}
	
	
	private void fillTable(){
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
					
					//PUT
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
//	    Toast.makeText(getApplicationContext(), day, Toast.LENGTH_SHORT).show();
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
    		
    		//PUT
		}
	}

    private class WeekPagerAdapter extends FragmentPagerAdapter{

        Fragment[] fragments = new Fragment[7];

        public WeekPagerAdapter(FragmentManager fm)
        {
            super(fm);
        }

        @Override
        public Fragment getItem(int i)
        {
        	//cache
//            Fragment f = this.fragments[i];
//            if(f == null) {
//                f = new ManageDayFragment();
//                this.fragments[i] = f;
//            }
        	
//        	Toast.makeText(ManageWeekProgram.this, i + "", Toast.LENGTH_SHORT).show();
        	Fragment f = new ManageDayFragment();
            Bundle data = new Bundle();
            data.putInt("dayIndex", i);
            f.setArguments(data);
            return f;
            
        }

        @Override
        public int getCount()
        {
            return 7;
        }
    }

    private class ManageDayFragment extends Fragment{
//    	public ManageDayFragment(int index){
//    		switch(index){
//    			case 0: 
//    				day = "Monday";
//    				break;
//    			case 1: 
//    				day = "Tuesday";
//    				break;
//    			case 2:
//    				day = "Wednesday";
//    				break;
//    			case 3:
//    				day = "Thursday";
//    				break;
//    			case 4: 
//    				day = "Friday";
//    				break;
//    			case 5:
//    				day = "Saturday";
//    				break;
//    			case 6:
//    				day = "Sunday";
//    				break;
//    			default:
//    				throw new RuntimeException("Unknown index (day)");
//    		}
//    	}
    	
    	
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance){
        	Bundle data = getArguments();
            int i = data.getInt("dayIndex", 0);
            day = getActionBar().getTabAt(i).getText().toString();
        	Toast.makeText(ManageWeekProgram.this, i + " " + day, Toast.LENGTH_SHORT).show();
        	
        	View rootView = inflater.inflate(R.layout.fragment_base, container, false);
        	
        	tableLayout = (TableLayout) rootView.findViewById(R.id.day_tab);
        	TextView textView = (TextView) rootView.findViewById(R.id.day_name);
        	textView.setText(day);
        	
        	tableLayout.removeAllViews();
        	fillTable();
    	    return rootView;
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
		    	//get new week program
		    	
		    	tableLayout.removeAllViews();
		    	new GetWeekProgram().execute();
		    default:
		    	break;
		}	
		return super.onOptionsItemSelected(item);
	}
	
	private class GetWeekProgram extends AsyncTask<String, Void, String> {
		ProgressDialog progressDialog;
		
        @Override
        protected String doInBackground(String... params) {
        	
        	//get methods!
			try {
				weekProgram = HeatingSystem.getWeekProgram();
			} catch (CorruptWeekProgramException e) {
				throw new RuntimeException("Corrupt week program!",e);
			} catch (ConnectException e) {
				throw new RuntimeException("Connect exception!",e);
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
        	progressDialog.setMessage("Getting information...");
        	progressDialog.setCancelable(false);
        	progressDialog.show();
        }
    }
}
