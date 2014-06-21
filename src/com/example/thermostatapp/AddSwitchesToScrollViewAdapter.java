package com.example.thermostatapp;

import java.util.ArrayList;
import java.util.Calendar;

import org.thermostatapp.util.WeekProgram;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.webkit.WebView.FindListener;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TimePicker;
/* Tane@Mark: Het is heel makkelijk deze class te gebruiken, om data in een
 * listview te zetten. Per dag (tab) wil je deze regels code toevoegen, in de oncreate()
 * dan zou hij vanzelf al moeten werken. Succes!!
 * 
 *  	ArrayList<org.thermostatapp.util.Switch> switchesToDisplay = weekProgram.getData().get(today);
 *  	ListView listview = (ListView) findViewById(R.id.listViewToday);	
 *		listview.setAdapter(new AddSwitchesToScrollViewAdapter(this, switchesToDisplay));
 * 
 */
		
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

		if (vi == null) {
			vi = inflater.inflate(R.layout.switch_per_day, null);
		}

		final ImageView image = (ImageView) vi.findViewById(R.id.DayNightImage);
		final Bitmap imageToShow;
		
		// set bitmap image
		if (data.get(position).type.equals("day")) {
			imageToShow = BitmapFactory.decodeResource(context.getResources(),
					R.drawable.sun);
		} else {
			imageToShow = BitmapFactory.decodeResource(context.getResources(),
					R.drawable.moon);
		}
		image.setImageBitmap(imageToShow);

		// init switch
		Switch statusSwitch = (Switch) vi.findViewById(R.id.switchSwitch);
		statusSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				Bitmap imageReplacement;
				if (isChecked) {
					if (switcher.type.equals("day")) {
						imageReplacement = BitmapFactory.decodeResource(
								context.getResources(), R.drawable.sun);
					} else {
						imageReplacement = BitmapFactory.decodeResource(
								context.getResources(), R.drawable.moon);
					}
				} else {
					imageReplacement = BitmapFactory.decodeResource(
							context.getResources(), R.drawable.item_disabled);
				}

				image.setImageBitmap(imageReplacement);			}
		});
		return vi;
	}
}