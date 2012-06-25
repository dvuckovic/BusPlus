package com.dvuckovic.busplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

/**
 * Custom seek bar for use in application preferences in order to change the
 * shortcut icon hue if desired
 **/
public class SeekBarPreference extends Preference implements
		OnSeekBarChangeListener {

	public static int maximum = 360;
	public static int interval = 10;
	private float hueValue = 180;
	private int defValue = 180;
	ImageView icon;

	public SeekBarPreference(Context context) {
		super(context);
	}

	public SeekBarPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SeekBarPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected View onCreateView(ViewGroup parent) {
		// A few linear layouts parameters, to be used later
		LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		params1.gravity = Gravity.LEFT;

		LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		params2.gravity = Gravity.LEFT;

		LinearLayout.LayoutParams params3 = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		params3.gravity = Gravity.RIGHT;
		params3.weight = 1;

		// Our seekbar
		final SeekBar bar = new SeekBar(getContext());
		bar.setMax(maximum);
		bar.setProgress((int) this.hueValue);
		bar.setLayoutParams(params2);
		bar.setOnSeekBarChangeListener(this);

		// Small text underneath the seekbar
		TextView view = new TextView(getContext());
		view.setText(getTitle());
		view.setTextSize(14);
		view.setGravity(Gravity.LEFT);
		view.setLayoutParams(params1);

		// Icon for hue simulation
		Bitmap blankIcon = BitmapFactory.decodeResource(getContext()
				.getResources(), R.drawable.blank_icon);

		// Image view to host the icon and apply color filter
		icon = new ImageView(getContext());
		icon.setAdjustViewBounds(true);
		icon.setImageBitmap(blankIcon);
		icon.setColorFilter(ColorFilterGenerator
				.adjustHue((int) this.hueValue - 180));
		icon.setLayoutParams(params1);

		// When icon is clicked, reset the hue value
		icon.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				bar.setProgress(defValue);
				hueValue = defValue;
				updatePreference(defValue);
				notifyChanged();
			}
		});

		// Instantiate preference manager and set up preference listener
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getContext());
		prefs.registerOnSharedPreferenceChangeListener(onChange);

		// Set alpha on icon based on toggle check box above
		if (prefs.getBoolean("shortcut_custom_color", false))
			icon.setAlpha(255);
		else
			icon.setAlpha(127);

		// Vertical layout (contains seekbar and text underneath)
		LinearLayout vLayout = new LinearLayout(getContext());
		vLayout.setOrientation(LinearLayout.VERTICAL);
		vLayout.setPadding(15, 0, 0, 0);
		vLayout.setLayoutParams(params3);
		vLayout.addView(bar);
		vLayout.addView(view);

		// Horizontal layout (contains icon and vertical layout)
		LinearLayout layout = new LinearLayout(getContext());
		layout.setPadding(15, 15, 15, 15);
		layout.setOrientation(LinearLayout.HORIZONTAL);
		layout.addView(icon);
		layout.addView(vLayout);
		layout.setId(android.R.id.widget_frame);

		return layout;
	}

	/**
	 * Updates our hue value when seek bar is changed
	 * 
	 * @param seekBar
	 * @param progress
	 * @param fromUser
	 * **/
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		progress = Math.round(((float) progress) / interval) * interval;

		if (!callChangeListener(progress)) {
			seekBar.setProgress((int) this.hueValue);
			return;
		}

		seekBar.setProgress(progress);
		this.hueValue = progress;

		icon.setColorFilter(ColorFilterGenerator
				.adjustHue((int) progress - 180));

		updatePreference(progress);
	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
	}

	/**
	 * When tracking of the seek bar is stopped, update view (using it here
	 * renders seek bar more responsive
	 * 
	 * @param seekBar
	 **/
	@Override
	public void onStopTrackingTouch(SeekBar arg0) {
		notifyChanged();
	}

	/**
	 * Sets the default value
	 * 
	 * @param typedArray
	 * @param index
	 * **/
	@Override
	protected Object onGetDefaultValue(TypedArray ta, int index) {
		int dValue = (int) ta.getInt(index, defValue);

		return validateValue(dValue);
	}

	/**
	 * Persists our inital value
	 * 
	 * @param restoreValue
	 * @param defaultValue
	 */
	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
		int temp = restoreValue ? getPersistedInt(defValue)
				: (Integer) defaultValue;

		if (!restoreValue)
			persistInt(temp);

		this.hueValue = temp;
	}

	/**
	 * Validates set value
	 * 
	 * @param value
	 * @return
	 */
	private int validateValue(int value) {
		if (value > maximum)
			value = maximum;
		else if (value < 0)
			value = 0;
		else if (value % interval != 0)
			value = Math.round(((float) value) / interval) * interval;

		return value;
	}

	/**
	 * Stores preference in shared preferences
	 * 
	 * @param newValue
	 */
	private void updatePreference(int newValue) {
		SharedPreferences.Editor editor = getEditor();
		editor.putInt(getKey(), newValue);
		editor.commit();
	}

	/** Listens for changes of custom shortcut icon color checkbox **/
	SharedPreferences.OnSharedPreferenceChangeListener onChange = new SharedPreferences.OnSharedPreferenceChangeListener() {
		public void onSharedPreferenceChanged(SharedPreferences prefs,
				String key) {
			if (key.equals("shortcut_custom_color")) {
				// Set alpha on icon based on toggle check box above
				if (prefs.getBoolean("shortcut_custom_color", false))
					icon.setAlpha(255);
				else
					icon.setAlpha(127);
			}
		}
	};

}
