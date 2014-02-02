package com.dvuckovic.busplus;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

/** Map view tab activity **/
public class LocationMap extends MapActivity implements LocationListener,
		OnGestureListener, OnDoubleTapListener {

	private final static int CENTER = 0;
	private final static int CENTER_BOTTOM = 1;
	private final static int UPDATE_LOCATION = 2;
	public final static String SHOW_LOCATIONS = "com.dvuckovic.busplus.SHOW_LOCATIONS";
	private SharedPreferences prefs;
	private MyMapView mapView;
	private LocationManager mLocationManager;
	private double mLongitude;
	private double mLatitude;
	private Thread mThread;
	private MapOverlay locationOverlay;
	private Cursor c;
	private DataBaseHelper helper;
	private String tapCode;
	private String tapName;
	private boolean noProvider;
	private static final String fields[] = { "name", "desc" };
	private boolean all;
	private boolean isScrolling;
	private boolean centered = false;
	private String lineSummary = "";
	private String status = "";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Instantiate preference manager
		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		// Instantiate Application class and call the setLanguage method
		BusPlus bp = (BusPlus) getApplicationContext();
		bp.setLanguage(prefs.getString("language", "sr"));

		// Sets the view for this activity
		setContentView(R.layout.map);

		// Instantiate helper object for work on database
		helper = new DataBaseHelper(this);

		// Set up some map view parameters
		mapView = (MyMapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
		mapView.getController().setZoom(BusPlus.mapZoom);
		mapView.setOnZoomListener(new MyMapView.OnZoomListener() {
			@Override
			public void onZoom() {
				BusPlus.mapZoom = mapView.getZoomLevel();
				if (all)
					drawViewMarkers();
			}
		});

		// Set Satellite view on our map, if the preference has been set
		mapView.setSatellite(prefs.getBoolean("map_satellite", false));

		Object retained = getLastNonConfigurationInstance();
		if (retained != null) {
			this.status = (String) retained;
		}

		if (!BusPlus.showStationId.equals("")) {
			BusPlus.showStationIdVal = BusPlus.showStationId;
			BusPlus.showStationId = "";
			drawStationMarker(BusPlus.showStationIdVal);
		} else if (status.equals("showStation"))
			drawStationMarker(BusPlus.showStationIdVal);
		else if (status.equals("showMapCenter")) {
			centerLocation(noProvider);
			centered = true;
		} else if (status.equals("showMapNearby")) {
			lineSummary = "";
			drawNearbyMarkers();
		} else if (status.equals("showMapAll")) {
			all = true;
			lineSummary = "";
			drawViewMarkers();
		} else if (status.equals("showLineA"))
			if (!BusPlus.showLineId.equals(""))
				drawLineMarkers(BusPlus.showLineId, "A");
			else
				centerMap(44.818061, 20.456524);
		else if (status.equals("showLineB"))
			if (!BusPlus.showLineId.equals(""))
				drawLineMarkers(BusPlus.showLineId, "B");
			else
				centerMap(44.818061, 20.456524);
		else
			centerMap(44.818061, 20.456524);

		Button centerBtn = (Button) findViewById(R.id.centerButton);
		centerBtn.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				// Center location
				status = "showMapCenter";
				centerLocation(noProvider);
				centered = true;
			}
		});

		Button nearbyBtn = (Button) findViewById(R.id.nearbyButton);
		nearbyBtn.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				// Draw nearby stations
				status = "showMapNearby";
				lineSummary = "";
				drawNearbyMarkers();
			}
		});

		Button lineBtn = (Button) findViewById(R.id.lineButton);
		lineBtn.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				// Show lines dialog
				showLines();
			}
		});

		Button viewBtn = (Button) findViewById(R.id.viewButton);
		viewBtn.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				// Draw all stations in view
				status = "showMapAll";
				all = true;
				lineSummary = "";
				drawViewMarkers();
			}
		});

		// Get the best location provider and start location thread
		initializeLocationAndStartGpsThread();
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		return status;
	}

	@Override
	protected boolean isLocationDisplayed() {
		if (noProvider)
			return false;
		else
			return true;
	}

	@Override
	protected void onPause() {
		// Stop monitoring location updates
		mLocationManager.removeUpdates(this);

		super.onPause();
	}

	@Override
	protected void onResume() {
		// Start monitoring location updates
		setCurrentGpsLocation(null);

		if (!BusPlus.showStationId.equals("")) {
			BusPlus.showStationIdVal = BusPlus.showStationId;
			BusPlus.showStationId = "";
			drawStationMarker(BusPlus.showStationIdVal);
		}

		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		helper.close();

		// Interrupt background location thread
		mThread.interrupt();
	}

	/**
	 * Sets our location to the last known location and start a separate thread
	 * to update GPS location.
	 */
	private void initializeLocationAndStartGpsThread() {
		setCurrentGpsLocation(null);
		mThread = new Thread(new MyThreadRunner());
		mThread.start();
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	// Listeners for location events

	public void onLocationChanged(Location arg0) {
		setCurrentGpsLocation(arg0);
	}

	public void onProviderDisabled(String arg0) {
		setCurrentGpsLocation(null);
	}

	public void onProviderEnabled(String arg0) {
		setCurrentGpsLocation(null);
	}

	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		setCurrentGpsLocation(null);
	}

	/**
	 * Sends a message to the update handler with either the current location or
	 * the last known location.
	 * 
	 * @param location
	 *            is either null or the current location
	 */
	private void setCurrentGpsLocation(Location location) {

		// Find the best location provider
		if (location == null) {
			mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

			// Start with GPS
			if (mLocationManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
				noProvider = false;
				mLocationManager.requestLocationUpdates(
						LocationManager.GPS_PROVIDER, 5000, 0, this);

				// Then try network provider
			} else if (mLocationManager
					.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
				noProvider = false;
				mLocationManager.requestLocationUpdates(
						LocationManager.NETWORK_PROVIDER, 5000, 0, this);

				// Set no provider
			} else {
				noProvider = true;
			}
		}

		// Broadcast location update
		try {
			mLatitude = location.getLatitude();
			mLongitude = location.getLongitude();
			Message msg = Message.obtain();
			msg.what = UPDATE_LOCATION;
			LocationMap.this.updateHandler.sendMessage(msg);

			// Reset no provider
			noProvider = false;
		} catch (NullPointerException e) {

		}
	}

	/**
	 * Handles GPS updates. Source: Android tutorials
	 * 
	 * @see http://www.androidph.com/2009/02/app-10-beer-radar.html
	 */
	@SuppressLint("HandlerLeak")
	Handler updateHandler = new Handler() {

		/** Gets called on every message that is received */
		// @Override
		public void handleMessage(Message msg) {
			switch (msg.what) {

			// On location update move location marker on map and center
			// location if it has been said so
			case UPDATE_LOCATION: {
				moveLocationMarker();
				if (centered)
					centerLocation(noProvider);
				break;
			}

			}

			super.handleMessage(msg);
		}

	};

	/** Move location overlay by removing old and adding new one **/
	private void moveLocationMarker() {
		try {
			mapView.getOverlays().remove(locationOverlay);
		} catch (Exception e) {
		}

		List<Overlay> mapOverlays = mapView.getOverlays();
		Drawable marker = getResources().getDrawable(
				R.drawable.ic_maps_indicator_current_position);
		marker.setBounds(0, 0, marker.getIntrinsicWidth(),
				marker.getIntrinsicHeight());
		GeoPoint currentPosition = new GeoPoint((int) (mLatitude * 1E6),
				(int) (mLongitude * 1E6));
		locationOverlay = new MapOverlay(marker, currentPosition, null,
				getString(R.string.current_location), CENTER);
		mapOverlays.add(locationOverlay);

		// Refresh map view
		mapView.invalidate();
	}

	/** Draw nearby stations as overlays **/
	private void drawNearbyMarkers() {

		// Get nearby stations first
		c = helper.getNearbyStations(mLatitude, mLongitude);
		startManagingCursor(c);

		// Clear all overlays
		List<Overlay> mapOverlays = mapView.getOverlays();
		mapOverlays.clear();

		// Add stations
		if (c.getCount() != 0) {
			c.moveToFirst();
			while (c.isAfterLast() == false) {
				Drawable marker = getResources().getDrawable(R.drawable.pin);
				marker.setBounds(0, 0, marker.getIntrinsicWidth(),
						marker.getIntrinsicHeight());
				String id = c.getString(c.getColumnIndex("_id"));
				String name = c.getString(c.getColumnIndex("name"));
				Double lat = c.getDouble(c.getColumnIndex("lat"));
				Double lon = c.getDouble(c.getColumnIndex("lon"));
				if (lat != null && lat != 0 && lon != null && lon != 0) {
					GeoPoint markerPosition = new GeoPoint((int) (lat * 1E6),
							(int) (lon * 1E6));
					MapOverlay markerOverlay = new MapOverlay(marker,
							markerPosition, id, name, CENTER_BOTTOM);
					mapOverlays.add(markerOverlay);
				}

				c.moveToNext();
			}

			all = false;
		}
		stopManagingCursor(c);
		c.close();

		centerLocation(noProvider);

		// Create location overlay if location provider is available, otherwise
		// refresh map view
		if (!noProvider)
			moveLocationMarker();
		else
			mapView.invalidate();
	}

	/** Show lines in a list dialog **/
	private void showLines() {
		LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = li.inflate(R.layout.list, null, false);

		TextView titleText = (TextView) v.findViewById(R.id.title);
		titleText.setVisibility(View.GONE);
		TextView helpText = (TextView) v.findViewById(R.id.help);
		helpText.setVisibility(View.GONE);

		final Dialog listDialog = new Dialog(this);
		listDialog.setTitle(getString(R.string.choose_line));
		listDialog.setContentView(v);
		listDialog.setCancelable(true);

		// Get all stations in the cursor
		final Cursor l = helper.getLinesByName("");
		startManagingCursor(l);

		// Set up a list adapter with two fields
		final CursorAdapter lAdapter = new SimpleCursorAdapter(this,
				R.layout.row_alt, l, fields, new int[] { R.id.itemId,
						R.id.itemName });

		AbsListView list = (AbsListView) listDialog
				.findViewById(android.R.id.list);
		list.setAdapter(lAdapter);
		list.setFastScrollEnabled(true);

		list.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				//
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				int childCount = view.getChildCount();

				for (int i = 0; i < childCount; i++) {
					View v = view.getChildAt(i);
					TextView lineNumber = (TextView) v
							.findViewById(R.id.itemId);
					lineNumber.setBackgroundColor(BusPlus
							.getBackgroundColor(lineNumber.getText().toString()));
					lineNumber.setShadowLayer(3, 0, 0, Color.rgb(0, 0, 0));
				}
			}
		});

		// Listen for clicks on items in the list
		list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

				// Get id of line clicked
				Cursor cursor = (Cursor) lAdapter.getItem(arg2);
				final String lineId = cursor.getString(cursor
						.getColumnIndex("_id"));
				final String lineName = cursor.getString(cursor
						.getColumnIndex("name"));
				final String lineDescA = cursor.getString(cursor
						.getColumnIndex("desc_a"));
				final String lineDescB = cursor.getString(cursor
						.getColumnIndex("desc_b"));

				// Build and show direction dialog
				AlertDialog.Builder builder = new AlertDialog.Builder(arg1
						.getContext());
				builder.setTitle(
						getString(R.string.choose_direction) + " " + lineName)
						.setMessage(
								"(A) " + lineDescA + "\n\n" + "(B) "
										+ lineDescB)
						.setNegativeButton("A",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();
										listDialog.dismiss();
										stopManagingCursor(l);
										l.close();

										status = "showLineA";
										BusPlus.showLineId = lineId;
										lineSummary = lineDescA;

										// Draw line markers in direction "A"
										drawLineMarkers(lineId, "A");
									}
								})
						.setPositiveButton("B",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();
										listDialog.dismiss();
										stopManagingCursor(l);
										l.close();

										status = "showLineB";
										BusPlus.showLineId = lineId;
										lineSummary = lineDescB;

										// Draw line markers in direction "B"
										drawLineMarkers(lineId, "B");
									}
								});
				AlertDialog alert = builder.create();
				alert.show();
			}
		});

		listDialog.show();
	}

	/** Draw stations from line as overlays on map **/
	private void drawLineMarkers(String lineId, String dir) {

		// Get all stations from the supplied line id
		c = helper.getStationsByLine(lineId, dir);
		startManagingCursor(c);

		// Clear all overlays
		List<Overlay> mapOverlays = mapView.getOverlays();
		mapOverlays.clear();

		// Draw stations
		if (c.getCount() != 0) {
			c.moveToFirst();

			// Save coordinates for the first station
			Double cLat = c.getDouble(c.getColumnIndex("lat"));
			Double cLon = c.getDouble(c.getColumnIndex("lon"));

			while (c.isAfterLast() == false) {
				Drawable marker = getResources().getDrawable(R.drawable.pin);
				marker.setBounds(0, 0, marker.getIntrinsicWidth(),
						marker.getIntrinsicHeight());
				String id = c.getString(c.getColumnIndex("_id"));
				String name = c.getString(c.getColumnIndex("name"));
				Double lat = c.getDouble(c.getColumnIndex("lat"));
				Double lon = c.getDouble(c.getColumnIndex("lon"));
				if (lat != null && lat != 0 && lon != null && lon != 0) {
					GeoPoint markerPosition = new GeoPoint((int) (lat * 1E6),
							(int) (lon * 1E6));
					MapOverlay markerOverlay = new MapOverlay(marker,
							markerPosition, id, name, CENTER_BOTTOM);
					mapOverlays.add(markerOverlay);
				}
				c.moveToNext();
			}

			// Center map on that first station
			if (cLat != null && cLat != 0 && cLon != null && cLon != 0)
				centerMap(cLat, cLon);

			all = false;
			centered = false;
		}
		stopManagingCursor(c);
		c.close();

		// Create location overlay if location provider is available, otherwise
		// refresh map view
		if (!noProvider)
			moveLocationMarker();
		else
			mapView.invalidate();
	}

	/** Draw station overlays visible on map **/
	private void drawViewMarkers() {

		// Get map center
		GeoPoint mapCenter = mapView.getMapCenter();

		// Get stations in view
		c = helper.getStationsInView(mapCenter.getLatitudeE6() / 1E6,
				mapCenter.getLongitudeE6() / 1E6, mapView.getLatitudeSpan(),
				mapView.getLongitudeSpan());
		startManagingCursor(c);

		// Clear all overlays
		List<Overlay> mapOverlays = mapView.getOverlays();
		mapOverlays.clear();

		if (c.getCount() != 0) {
			c.moveToFirst();
			while (c.isAfterLast() == false) {
				Drawable marker = getResources().getDrawable(R.drawable.pin);
				marker.setBounds(0, 0, marker.getIntrinsicWidth(),
						marker.getIntrinsicHeight());
				String id = c.getString(c.getColumnIndex("_id"));
				String name = c.getString(c.getColumnIndex("name"));
				Double lat = c.getDouble(c.getColumnIndex("lat"));
				Double lon = c.getDouble(c.getColumnIndex("lon"));
				if (lat != null && lat != 0 && lon != null && lon != 0) {
					GeoPoint markerPosition = new GeoPoint((int) (lat * 1E6),
							(int) (lon * 1E6));
					MapOverlay markerOverlay = new MapOverlay(marker,
							markerPosition, id, name, CENTER_BOTTOM);
					mapOverlays.add(markerOverlay);
				}
				c.moveToNext();
			}
		}
		stopManagingCursor(c);
		c.close();

		// Create location overlay if location provider is available, otherwise
		// refresh map view
		if (!noProvider)
			moveLocationMarker();
		else
			mapView.invalidate();
	}

	private void drawStationMarker(String stationId) {

		status = "showStation";

		// Get station by it's ID
		c = helper.getStationById(stationId);
		startManagingCursor(c);

		// Clear all overlays
		List<Overlay> mapOverlays = mapView.getOverlays();
		mapOverlays.clear();

		// Draw station marker and center map on it
		if (c.getCount() != 0) {
			c.moveToFirst();
			Drawable marker = getResources().getDrawable(R.drawable.pin);
			marker.setBounds(0, 0, marker.getIntrinsicWidth(),
					marker.getIntrinsicHeight());
			String id = c.getString(c.getColumnIndex("_id"));
			String name = c.getString(c.getColumnIndex("name"));
			Double lat = c.getDouble(c.getColumnIndex("lat"));
			Double lon = c.getDouble(c.getColumnIndex("lon"));
			if (lat != null && lat != 0 && lon != null && lon != 0) {
				GeoPoint markerPosition = new GeoPoint((int) (lat * 1E6),
						(int) (lon * 1E6));
				MapOverlay markerOverlay = new MapOverlay(marker,
						markerPosition, id, name, CENTER_BOTTOM);
				mapOverlays.add(markerOverlay);
				centerMap(lat, lon);
				all = false;
				centered = false;
			} else
				Toast.makeText(getApplicationContext(),
						R.string.no_station_location, Toast.LENGTH_SHORT)
						.show();
		} else
			Toast.makeText(getApplicationContext(),
					R.string.no_station_location, Toast.LENGTH_SHORT).show();
		stopManagingCursor(c);
		c.close();

		// Create location overlay if location provider is available, otherwise
		// refresh map view
		if (!noProvider)
			moveLocationMarker();
		else
			mapView.invalidate();

	}

	/** Center map on supplied coordinates **/
	public void centerMap(Double lat, Double lon) {
		GeoPoint centerGeoPoint = new GeoPoint((int) (lat * 1E6),
				(int) (lon * 1E6));
		mapView.getController().setCenter(centerGeoPoint);
	}

	/** Center map on current coordinates **/
	public void centerLocation(boolean showDialog) {

		// Show a dialog if there is no provider
		if (noProvider & showDialog) {
			showMsgDialog(getString(R.string.no_provider),
					getString(R.string.turn_on_location));
		} else {
			if (mLatitude != 0 && mLongitude != 0) {
				// Center on current coordinates
				GeoPoint centerGeoPoint = new GeoPoint((int) (mLatitude * 1E6),
						(int) (mLongitude * 1E6));
				mapView.getController().setCenter(centerGeoPoint);

				// Draw all markers again if they were visible
				if (all)
					drawViewMarkers();
			} else {
				BusPlus bp = (BusPlus) getApplicationContext();
				bp.showToastMessage(getString(R.string.location_waiting));
			}
		}

	}

	/**
	 * Handles location updates in the background.
	 */
	class MyThreadRunner implements Runnable {
		public void run() {
			while (!Thread.currentThread().isInterrupted()) {
				Message m = Message.obtain();
				m.what = 0;
				LocationMap.this.updateHandler.sendMessage(m);
				try {
					Thread.sleep(5);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}
	}

	/** Class for map overlays **/
	private class MapOverlay extends ItemizedOverlay<OverlayItem> {
		private OverlayItem item = null;

		public MapOverlay(Drawable marker, GeoPoint point, String code,
				String name, int pos) {
			super(marker);

			switch (pos) {
			case CENTER_BOTTOM:
				// Align markers center bottom
				boundCenterBottom(marker);
				break;
			case CENTER:
			default:
				// Align marker center center
				boundCenter(marker);
			}

			item = new OverlayItem(point, name, code);
			populate();
		}

		@Override
		protected OverlayItem createItem(int i) {
			return (item);
		}

		@Override
		public int size() {
			return (1);
		}

		@Override
		protected boolean onTap(int i) {
			if (item.getSnippet() == null) {
				// Show toast message with current location string
				Toast.makeText(LocationMap.this, item.getTitle(),
						Toast.LENGTH_SHORT).show();
			} else {
				// Show dialog with options on tap
				AlertDialog.Builder builder = new AlertDialog.Builder(
						LocationMap.this);
				tapCode = item.getSnippet();
				tapName = item.getTitle();
				
				c = helper.getLinesByStation(tapCode);
				startManagingCursor(c);

				String[] lines = new String[c.getCount()];
				int k = 0;
				
				if (c.getCount() != 0) {
					c.moveToFirst();
					while (c.isAfterLast() == false) {
						lines[k] = c.getString(c.getColumnIndex("name"));
						k++;
						c.moveToNext();
					}
				}
				
				stopManagingCursor(c);
				c.close();
				
				LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View v = li.inflate(R.layout.message, null, false);

				TextView stationName = (TextView) v.findViewById(R.id.stationName);
				stationName.setTextAppearance(LocationMap.this,
						android.R.style.TextAppearance_Medium);	
				stationName.setText("[" + tapCode + "] " + tapName);

				FlowLayout fl = (FlowLayout) v.findViewById(R.id.flowLayout);
				
				for (String line : lines) {
					
					TextView lineTextView = new TextView(LocationMap.this);
					lineTextView.setTextAppearance(LocationMap.this,
							android.R.style.TextAppearance_Medium);
					lineTextView
							.setBackgroundColor(BusPlus.getBackgroundColor(line));
					lineTextView.setShadowLayer(3, 0, 0, Color.rgb(0, 0, 0));
					lineTextView.setText(" " + line + " ");
					
					fl.addView(lineTextView);
				}

				builder.setTitle(getString(R.string.station))
						.setView(v)
						.setNegativeButton(getString(R.string.query),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										// USSD query
										Intent i = new Intent(LocationMap.this,
												FavoritesActivity.class);
										i.putExtra(FavoritesActivity.EXTRA_ID,
												tapCode);
										i.setAction(FavoritesActivity.INTENT_NAME);
										startActivity(i);
										dialog.dismiss();
									}
								})
						.setNeutralButton(getString(R.string.favorites),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										String suffix = "";
										if (!lineSummary.equals(""))
											suffix = " ("
													+ lineSummary.replaceAll(
															"\\s+/.*?/", "")
													+ ")";

										// Add favorite and toast a message
										helper.insertFavorite(
												Integer.parseInt(tapCode),
												tapName + suffix);
										BusPlus bp = (BusPlus) getApplicationContext();
										bp.showToastMessage(getString(R.string.favorite_added_to_list));
									}
								})
						.setPositiveButton(getString(R.string.shortcut),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										String suffix = "";
										if (!lineSummary.equals(""))
											suffix = " ("
													+ lineSummary.replaceAll(
															"\\s+/.*?/", "")
													+ ")";

										// Add shortcut
										BusPlus bp = (BusPlus) getApplicationContext();
										bp.setupShortcut(tapCode, tapName
												+ suffix);
									}
								});
				AlertDialog alert = builder.create();
				alert.show();
			}
			return (true);
		}

		public void draw(android.graphics.Canvas canvas, MapView mapView,
				boolean shadow) {
			super.draw(canvas, mapView, false);

			// Disable unzooming map more than level 14, there will be to many
			// stations in map view
			if (mapView.getZoomLevel() < 14)
				mapView.getController().setZoom(14);
		}
	}

	/**
	 * On double tap on blank area on map, zoom in one level
	 * 
	 * @param MotionEvent
	 * **/
	@Override
	public boolean onDoubleTap(MotionEvent e) {
		mapView.getController().zoomIn();

		return false;
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {

		// If all markers are displayed on map, monitor scrolling when it starts
		if (all)
			isScrolling = true;

		// If location was centered it, then do not center any more
		if (centered)
			centered = false;

		return false;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {

		// If there was a scroll in progress when the touch ended, draw all
		// visible markers, only if they are being displayed on map
		if (ev.getAction() == MotionEvent.ACTION_UP) {
			if (isScrolling) {
				isScrolling = false;
				if (all)
					drawViewMarkers();
			}
		}

		return super.dispatchTouchEvent(ev);
	}

	@Override
	public void onShowPress(MotionEvent e) {
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	/**
	 * Show neutral OK dialog to user
	 * 
	 * @param title
	 * @param message
	 **/
	public void showMsgDialog(String title, String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title).setMessage(message)
				.setNeutralButton("OK", new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}

}
