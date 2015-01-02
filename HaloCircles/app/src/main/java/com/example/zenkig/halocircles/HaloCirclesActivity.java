package com.example.zenkig.halocircles;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import android.location.Location;
import android.widget.Toast;

import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

import static android.location.Location.distanceBetween;
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;


public class HaloCirclesActivity extends ActionBarActivity implements
        GoogleMap.OnMarkerDragListener, GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMyLocationChangeListener, GoogleMap.OnInfoWindowClickListener{

    // Global setting variables announced
    final int RQS_GooglePlayServices = 1;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    boolean markerClicked; // default status

    Circle myCircleCenter;    // Circle for user location
    Circle mockMarkersCircle; // Circle for markers location
    TextView tvLocInfo;    // Text on the icon marker
    private Location currentLocation;
    private Marker mockMarker;    // markers location Marker object
    private FragmentManager supportFragmentManager;
    //private List<CircleOptions> circleDynamics = new ArrayList<>();
    private List<Circle> mockMarkersCircles = new ArrayList<Circle>();
    private List<Marker> mockMarkers = new ArrayList<>();
	// a range of parameters to give color encdoing on distance level
	final float rangeMeters1 = 100;
	final float rangeMeters2 = 300;
	final float rangeMeters3 = 500;
	final float rangeMeters4 = 1000;
    private float padding = 10;
	//final float rangeMeters5 = 1500;

    // Info Window Class
    class MyInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private final View myContentsView;

        MyInfoWindowAdapter() {
            myContentsView = getLayoutInflater().inflate(R.layout.custom_info_contents, null);
        }

        @Override
        public View getInfoContents(Marker marker) {
            TextView tvTitle = ((TextView) myContentsView.findViewById(R.id.title));
            tvTitle.setText(marker.getTitle());
            TextView tvSnippet = ((TextView) myContentsView.findViewById(R.id.snippet));
            tvSnippet.setText(marker.getSnippet());

            return myContentsView;
        }

        @Override
        public View getInfoWindow(Marker marker) {
            // TODO Auto-generated method stub
            return null;
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_halo_circles);
        setUpMapIfNeeded();

        tvLocInfo = (TextView) findViewById(R.id.locinfo);   // location axis shown text

        mMap.setMyLocationEnabled(true); // location layer does not provide data
        currentLocation = mMap.getMyLocation(); // current Location type get, best way from google
		
		//// **The location provider info is not precise in identify of current location on map, obselete **
//        // Getting LocationManager object from System Service LOCATION_SERVICE
//        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
//
//        // Creating a criteria object to retrieve provider
//        Criteria criteria = new Criteria();
//
//        // Getting the name of the best provider
//        String provider = locationManager.getBestProvider(criteria, true);
//
//        // Getting Current GeoLocation
//        currentLocation = locationManager.getLastKnownLocation(provider);  //

        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);

        mMap.setOnMarkerDragListener(this);
        mMap.setOnMyLocationChangeListener(this);  // listener for location change added
        mMap.setInfoWindowAdapter(new MyInfoWindowAdapter()); // Info window listener adaptor added
        mMap.setOnInfoWindowClickListener(this);

        markerClicked = false;

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();

        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());

        if (resultCode == ConnectionResult.SUCCESS) {
            Toast.makeText(getApplicationContext(),
                    "isGooglePlayServicesAvailable SUCCESS",
                    Toast.LENGTH_LONG).show();
        } else {
            GooglePlayServicesUtil.getErrorDialog(resultCode, this, RQS_GooglePlayServices);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_halo_circles, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.menu_legalnotices:
                String LicenseInfo = GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(
                        getApplicationContext());
                AlertDialog.Builder LicenseDialog = new AlertDialog.Builder(HaloCirclesActivity.this);
                LicenseDialog.setTitle("Legal Notices");
                LicenseDialog.setMessage(LicenseInfo);
                LicenseDialog.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.

            FragmentManager myFragmentManager = getFragmentManager();
            MapFragment myMapFragment
                    = (MapFragment) myFragmentManager.findFragmentById(R.id.map);
            mMap = myMapFragment.getMap();

            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {

        //Marker paris = mMap.addMarker(new MarkerOptions().position(new LatLng(48.7, 2.338)).title("Paris Area"));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(18f)); // default map zoom level
    }


    @Override
    public void onMapClick(LatLng point) {

        int disRadius = 500;  // default circle size
        float radiusRatio = (float) 0.600;  // the ratio to show the circle on map

        // ArrayList<Markers> markers = new ArrayList<Markers>();
        tvLocInfo.setText("New marker with Circle added @" + point.toString());

        //To alter the shape of the circle after it has been added,
        //can call Circle.setRadius() or Circle.setCenter() and provide new values.

        // add Marker upon circle center and show the position
        mockMarker = mMap.addMarker(new MarkerOptions()
                .position(point)
                .title(point.toString()));
        float[] distancePoints = new float[3]; // circle size according to distance on the map
        LatLng newPostions = mockMarker.getPosition();      // markers pointed location
        System.out.println(newPostions);  // marker position output

        currentLocation = mMap.getMyLocation(); // current Location get using google service
		// calculate the distance of Markers position<--> current position
        Location.distanceBetween(newPostions.latitude, newPostions.longitude,
                currentLocation.getLatitude(), currentLocation.getLongitude(), distancePoints); 

        // adjust circle according to distance between a marker and the current position
        //To alter the shape of the circle after it has been added,
        //can call Circle.setRadius() or Circle.setCenter() and provide new values.
        System.out.println("current distance between  :" + radiusRatio * distancePoints[0]);  // marker position output
        CircleOptions circleDynamic = new CircleOptions()
           		 								.center(point)   //set center
         										.radius(radiusRatio * distancePoints[0])   //set radius in meters
       											.fillColor(0x40ff0000)  //semi-transparent
     											.strokeColor(Color.RED)
            									.strokeWidth(5);
        mockMarkersCircle = mMap.addCircle(circleDynamic);
        mockMarkers.add(mockMarker);
        mockMarkersCircles.add(mockMarkersCircle); // adding new circles in the list

        markerClicked = false; // set as false 

    }

    @Override
    public void onMapLongClick(LatLng point) {

        tvLocInfo.setText(point.toString()); // Clicked Location
        mMap.animateCamera(CameraUpdateFactory.newLatLng(point));
        int colorIndex;
        float[] distanceMtoC = new float[3]; //get distance on the map marker/current location
        int distanceLevel;

        // add Marker upon Circle center
        mMap.addMarker(new MarkerOptions()
                .position(point)
                .title(point.toString()));

        Location myLocation = mMap.getMyLocation();
        if(myLocation == null){
            Toast.makeText(getApplicationContext(),
                    "My location not available",
                    Toast.LENGTH_LONG).show();
        }else{
            PolylineOptions polylineOptions = new PolylineOptions();
            polylineOptions.add(point);
            polylineOptions.add(
                    new LatLng(myLocation.getLatitude(), myLocation.getLongitude()));
            Location.distanceBetween(point.latitude, point.longitude,
                    myLocation.getLatitude(), myLocation.getLongitude(),
                    distanceMtoC);

            if ((distanceMtoC[0] > 0) && (distanceMtoC[0] <= rangeMeters1)) {
                distanceLevel = 1;
                System.out.println("colorIndex1");  // colorIndex position output
                colorIndex = 0x80C0FF3E;

            }
            else if ((distanceMtoC[0] < rangeMeters1) && (distanceMtoC[0] <= rangeMeters2)) {
                distanceLevel = 2;
                System.out.println("colorIndex2");  // colorIndex position output
                colorIndex = 0x80FFFF00;
            }
            else if ((distanceMtoC[0] < rangeMeters2) && (distanceMtoC[0] <= rangeMeters3)) {
                distanceLevel = 3;
                System.out.println("colorIndex3");  // colorIndex position output
                colorIndex = 0x80FFA500;
            }
            else if ((distanceMtoC[0] < rangeMeters3) && (distanceMtoC[0] <= rangeMeters4)) {
                distanceLevel = 4;
                System.out.println("colorIndex4");  // colorIndex position output
                colorIndex = 0x80FF82AB;
            }
            else{
                distanceLevel = 5;
                System.out.println("colorIndex5");  // colorIndex position output
                colorIndex = 0x8097FFFF;
            }

            Toast.makeText(getBaseContext(),
                    "Distance Level to my location  =" + distanceLevel
                            + "Distance to my location  " + distanceMtoC[0],
                    Toast.LENGTH_SHORT).show();
            // set line-color according to relative distance level: current <--> target locaitons
            mMap.addPolyline(polylineOptions.width(16)
                                            .color(colorIndex)); 
        }

        markerClicked = false;
    }


    @Override
    public void onMarkerDrag(Marker marker) {
        tvLocInfo.setText("Marker " + marker.getId() + " Drag@" + marker.getPosition());
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        tvLocInfo.setText("Marker " + marker.getId() + " DragEnd");
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        tvLocInfo.setText("Marker " + marker.getId() + " DragStart");

    }

    @Override
    public void onMyLocationChange(Location location) {

        int zoomFactor = 20;
        float rRatio = (float) 0.600;
        float searchDistance = 1000;

        tvLocInfo.setText("New circle added@" + location.toString());  // text info on current location

        // Getting latitude, longitude of the current location
        double clatitude = location.getLatitude();
        double clongitude = location.getLongitude();

        LatLng locLatLng = new LatLng(clatitude, clongitude);

        double accuracy = location.getAccuracy();
        accuracy = accuracy * zoomFactor;

        if (myCircleCenter == null) {
            CircleOptions circleOptions = new CircleOptions()
                    .center(locLatLng)   //set center
                    .radius(accuracy)   //set radius in meters
                    .fillColor(0x30000000) // my location circle color
                    .strokeColor(Color.BLUE)
                    .strokeWidth(5);

            myCircleCenter = mMap.addCircle(circleOptions);
        } else {
            myCircleCenter.setCenter(locLatLng);
            myCircleCenter.setRadius(accuracy);
        }

        //TextView tvLocation = (TextView) findViewById(R.id.tv_location); // for current location info
        // Showing the current location in Google Map        // Zoom in the Google Map
        mMap.moveCamera(CameraUpdateFactory.newLatLng(locLatLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(18f));
        mMap.animateCamera(CameraUpdateFactory.newLatLng(locLatLng));

        // Draw circle of current search range
        final Circle currentCircle = mMap.addCircle(new CircleOptions()
                .center(new LatLng(clatitude, clongitude))
                .strokeColor(Color.BLUE).radius(searchDistance));

        float[] distancePoints = new float[3]; //get distance on the map marker/current location
        for(Circle mockMarkersCircle: mockMarkersCircles) {  //loop over circles on map

            LatLng circleDynCenter = mockMarkersCircle.getCenter(); // center lat lang for markers

            Location.distanceBetween(clatitude, clongitude,
                    circleDynCenter.latitude, circleDynCenter.longitude,
                    distancePoints);
			
			// improved code 2014.12.19
            Projection projection = mMap.getProjection();
            Point placeLocationPixels = projection.toScreenLocation(circleDynCenter);
			Point myLocationPixels =  projection.toScreenLocation(locLatLng);  // PROBLEM !
			float dx = abs(placeLocationPixels.x - myLocationPixels.x);
            float dy = abs(placeLocationPixels.y - myLocationPixels.y);

            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int screenSizeX = size.x;
            int screenSizeY = size.y;

            float ox = dx - ((screenSizeX / 2) - padding);
            float oy = dy - ((screenSizeY / 2) - padding);

			if (ox < 0) ox = 0;
			if (oy < 0) oy = 0;

			double radiusFinal = sqrt((ox*ox) + (oy*oy));  // final radius

//            if(mockMarkersCircle!=null){
//                mockMarkersCircle.remove();
//            } // if have previous circles, remove first then change the circle styles.

            if (distancePoints[0] > 0 && distancePoints[0] <= rangeMeters1) {
                System.out.println("Circle Dynamic NEAREST 1");  // Circle

                mockMarkersCircle.setStrokeWidth(10);
                mockMarkersCircle.setRadius(radiusFinal); // change final radius on screen
                mockMarkersCircle.setFillColor(0x6097FFFF);
                mockMarkersCircle.setStrokeColor(0x6097FFFF);
                //mockMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

//                circleDynamic
//                        .center(circleDynCenter)   //set center
//                        .radius(rRatio * distancePoints[0])   //set radius in meters
//                        .fillColor(0x40C0FF3E)  //semi-transparent
//                        .strokeColor(Color.BLUE)
//                        .strokeWidth(10);
//                mMap.addCircle(circleDynamic);
            }
            else if ((distancePoints[0] < rangeMeters1) && (distancePoints[0] <= rangeMeters2)) {
                System.out.println("Circle Dynamic CLOSE 2");  // Circle

                mockMarkersCircle.setStrokeWidth(8);
                mockMarkersCircle.setRadius(radiusFinal); // change final radius on screen
                mockMarkersCircle.setFillColor(0x60C0FF3E);
                mockMarkersCircle.setStrokeColor(0x60C0FF3E);
                //mockMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));


//                circleDynamic
//                        .center(circleDynCenter)   //set center
//                        .radius(rRatio * distancePoints[0])   //set radius in meters
//                        .fillColor(0x40FFFF00)  //semi-transparent
//                        .strokeColor(Color.CYAN)
//                        .strokeWidth(8);
//                mMap.addCircle(circleDynamic);
            }
            else if ((distancePoints[0] < rangeMeters2) && (distancePoints[0] <= rangeMeters3)) {
                System.out.println("Circle Dynamic NOT FAR 3");  // Circle

                mockMarkersCircle.setStrokeWidth(6);
                mockMarkersCircle.setRadius(radiusFinal); // change final radius on screen
                mockMarkersCircle.setFillColor(0x60FFFF00);
                mockMarkersCircle.setStrokeColor(0x60FFFF00);
                //mockMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
//
//                circleDynamic
//                        .center(circleDynCenter)   //set center
//                        .radius(rRatio * distancePoints[0])   //set radius in meters
//                        .fillColor(0x40FFA500)  //semi-transparent
//                        .strokeColor(Color.YELLOW)
//                        .strokeWidth(6);
//                mMap.addCircle(circleDynamic);
            }
            else if((distancePoints[0] < rangeMeters3) && (distancePoints[0] <= rangeMeters4)) {
                System.out.println("Circle Dynamic MIDDLE 4");  // Circle

                mockMarkersCircle.setStrokeWidth(5);
                mockMarkersCircle.setRadius(radiusFinal); // change final radius on screen
                mockMarkersCircle.setFillColor(0x60FFA500);
                mockMarkersCircle.setStrokeColor(0x60FFA500);
                //mockMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));

//                circleDynamic
//                        .center(circleDynCenter)   //set center
//                        .radius(rRatio * distancePoints[0])   //set radius in meters
//                        .fillColor(0x40FF82AB)  //semi-transparent
//                        .strokeColor(Color.RED)
//                        .strokeWidth(5);
//                mMap.addCircle(circleDynamic);
            }
            else{
                System.out.println("Circle Dynamic FAR 5");  // Circle

                mockMarkersCircle.setStrokeWidth(4);
                mockMarkersCircle.setRadius(radiusFinal); // change final radius on screen
                mockMarkersCircle.setFillColor(0x60FF82AB);
                mockMarkersCircle.setStrokeColor(0x60FF82AB);
                //mockMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));

//                circleDynamic
//                        .center(circleDynCenter)   //set center
//                        .radius(rRatio * distancePoints[0])   //set radius in meters
//                        .fillColor(0x4097FFFF)  //semi-transparent
//                        .strokeColor(Color.MAGENTA)
//                        .strokeWidth(4);
//                mMap.addCircle(circleDynamic);
            }
            //mockMarkersCircle.setRadius(rRatio * distancePoints[0]); // orignally code, absolute radius on screen
           // mockMarkersCircle.setRadius(radiusFinal); // change final radius on screen
						
        } // end of for loop

        for(Marker mockMarker: mockMarkers) {  //loop over circles on map

            LatLng markerCenter = mockMarker.getPosition(); // center lat lang for markers

            Location.distanceBetween(clatitude, clongitude,
                    markerCenter.latitude, markerCenter.longitude,
                    distancePoints);

            if (distancePoints[0] > 0 && distancePoints[0] <= rangeMeters1) {
                mockMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

            }else if ((distancePoints[0] < rangeMeters1) && (distancePoints[0] <= rangeMeters2)){
                mockMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

            }else if ((distancePoints[0] < rangeMeters2) && (distancePoints[0] <= rangeMeters3)){
                mockMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));

            }else if ((distancePoints[0] < rangeMeters4) && (distancePoints[0] <= rangeMeters4)){
                mockMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));

            }else{
                mockMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));

            }

            } // end of for circle loop

        } // end of onMyLocationChange

    @Override
    public void onInfoWindowClick(Marker marker) {

        // Getting LocationManager object from System Service LOCATION_SERVICE
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Creating a criteria object to retrieve provider
        Criteria criteria = new Criteria();

        // Getting the name of the best provider
        String provider = locationManager.getBestProvider(criteria, true);

        // Getting Current GeoLocation
        currentLocation = locationManager.getLastKnownLocation(provider);

        float[] distancePoints = new float[3]; //get distance on the map marker/current location
        LatLng currentMarkerPosi = marker.getPosition();
        Location.distanceBetween(currentMarkerPosi.latitude, currentMarkerPosi.longitude,
                currentLocation.getLatitude(), currentLocation.getLongitude(), distancePoints);

        float DistanceToMarker = distancePoints[0];

        Toast.makeText(getBaseContext(),
                "See Info Window @ Marker ID =" + marker.getId()
                +"Distance to my location  " + DistanceToMarker,
                Toast.LENGTH_SHORT).show();

        marker.isDraggable();
        marker.setTitle("Distance to my location" + DistanceToMarker);

        markerClicked = false;
       }   // end of onInfoWindowClick

}  //end of HaloCirclesActivity

