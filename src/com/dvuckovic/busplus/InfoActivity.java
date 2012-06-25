package com.dvuckovic.busplus;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

/** Provides text list with several options (About menu) **/
public class InfoActivity extends Activity {

	private String currentView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Show list
		showInfo();
	}

	/** Populate list with options **/
	private void showInfo() {
		ListView lView = new ListView(this);
		String[] values = new String[] { getString(R.string.help),
				getString(R.string.about) };
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, android.R.id.text1, values);
		lView.setAdapter(adapter);

		// Set click listener and switch content accordingly
		lView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				switch (arg2) {
				case 0:
					showHelp();
					break;
				case 1:
					showAbout();
					break;
				}
			}
		});

		setContentView(lView);
		setTitle(R.string.app_name);
		currentView = "info";
	}

	/** Show help text **/
	private void showHelp() {
		ScrollView sView = new ScrollView(this);
		LinearLayout lLayout = new LinearLayout(this);

		lLayout.setOrientation(LinearLayout.VERTICAL);
		lLayout.setPadding(9, 9, 9, 9);
		lLayout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));

		TextView headingWidget = makeHeading(R.string.help_widget);
		TextView textWidget = makeText(R.string.help_widget_text);

		TextView headingIcon = makeHeading(R.string.help_shortcut_color);
		TextView textIcon = makeText(R.string.help_shortcut_color_text);

		sView.addView(lLayout);

		lLayout.addView(headingWidget);
		lLayout.addView(textWidget);

		lLayout.addView(headingIcon);
		lLayout.addView(textIcon);

		setContentView(sView);
		setTitle(getString(R.string.app_name) + " | "
				+ getString(R.string.help));
		currentView = "help";
	}

	/** Show about text **/
	private void showAbout() {
		ScrollView sView = new ScrollView(this);
		LinearLayout lLayout = new LinearLayout(this);

		lLayout.setOrientation(LinearLayout.VERTICAL);
		lLayout.setPadding(9, 9, 9, 9);
		lLayout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));

		TextView headingAbout = makeHeading(R.string.about);
		TextView textAbout = makeText(R.string.about_text);
		TextView subHeadingDonations = makeSubHeading(R.string.about_donations);
		TextView textDonations = makeText(R.string.about_donations_text);

		sView.addView(lLayout);

		lLayout.addView(headingAbout);
		lLayout.addView(textAbout);
		lLayout.addView(subHeadingDonations);
		lLayout.addView(textDonations);

		setContentView(sView);
		setTitle(getString(R.string.app_name) + " | "
				+ getString(R.string.about));
		currentView = "about";
	}

	/**
	 * Return heading text
	 * 
	 * @param text_resource
	 * @return text_view
	 **/
	private TextView makeHeading(int text) {
		TextView tView = new TextView(this);
		tView.setText(text);
		tView.setTextAppearance(this, R.style.headingText);
		tView.setPadding(0, 17, 0, 15);

		return tView;
	}

	/**
	 * Return sub heading text
	 * 
	 * @param text_resource
	 * @return text_view
	 **/
	private TextView makeSubHeading(int text) {
		TextView tView = new TextView(this);
		tView.setText(text);
		tView.setTextAppearance(this, R.style.subHeading);
		tView.setMovementMethod(LinkMovementMethod.getInstance());
		tView.setPadding(0, 15, 0, 15);

		return tView;
	}

	/**
	 * Return body text
	 * 
	 * @param text_resource
	 * @return text_view
	 **/
	private TextView makeText(int text) {
		return makeText(text, 0);
	}

	/**
	 * Return body text
	 * 
	 * @param text_resource
	 * @param padding
	 * @return text_view
	 **/
	private TextView makeText(int text, int padding) {
		TextView tView = new TextView(this);
		tView.setText(text);
		tView.setTextAppearance(this, R.style.bodyText);
		tView.setMovementMethod(LinkMovementMethod.getInstance());
		tView.setPadding(padding, 0, 0, 0);

		return tView;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// Check if back button was pressed
		if (keyCode == KeyEvent.KEYCODE_BACK) {

			if (currentView == "help" || currentView == "about") {

				// Show first screen if the current is deeper
				showInfo();
				return true;
			} else
				// Otherwise raise the super
				return super.onKeyDown(keyCode, event);
		}
		return super.onKeyDown(keyCode, event);
	}
}
