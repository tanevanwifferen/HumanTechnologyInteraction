package com.example.thermostatapp;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;

public class AddSwitchesToScrollViewAdapter extends BaseAdapter {
	
	Context context;
	ArrayList<org.thermostatapp.util.Switch> data;

	private static LayoutInflater inflater = null;

	public AddSwitchesToScrollViewAdapter(Context context, ArrayList<org.thermostatapp.util.Switch> data) {
	    this.context = context;
	    this.data = data;
	    inflater = (LayoutInflater) context
	            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Object getItem(int arg0) {
		return data.get(arg0);	
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View vi = convertView;
		final org.thermostatapp.util.Switch switcher = data.get(position);
		
		if(vi == null){
			vi = inflater.inflate(R.layout.switch_per_day, null);
		}
		
		
		final ImageView image = (ImageView)vi.findViewById(R.id.DayNightImage);
	    final Bitmap imageToShow;

	    // set time
	    final EditText timeIndicator = (EditText)vi.findViewById(R.id.timerIndicator);

	    // set bitmap image
	    if(data.get(position).type.equals("day")){
	    	imageToShow = BitmapFactory.decodeResource(context.getResources(), R.drawable.sun);
	    } else {
	    	imageToShow = BitmapFactory.decodeResource(context.getResources(), R.drawable.moon);
	    }
	    image.setImageBitmap(imageToShow);

	    //init switch
	    Switch statusSwitch = (Switch)vi.findViewById(R.id.switchSwitch);
	    statusSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Bitmap imageReplacement;
				if(isChecked){
					if(switcher.type.equals("day")){
						imageReplacement = BitmapFactory.decodeResource(context.getResources(), R.drawable.sun);
					} else {
						imageReplacement = BitmapFactory.decodeResource(context.getResources(), R.drawable.moon);
					}
				} else {
					imageReplacement = BitmapFactory.decodeResource(context.getResources(), R.drawable.item_disabled);
				}

				image.setImageBitmap(imageReplacement);	
				timeIndicator.setEnabled(isChecked);				
			}
		});
	    return vi;
	}

}
