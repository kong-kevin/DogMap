package com.kong.app.dogmap;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeAddress;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeResult;


public class MapActivity extends Activity implements GeocodeSearch.OnGeocodeSearchListener{


    public MapView mapView;
    public AMap aMap;
    public LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        init();
        RadioButton rb = (RadioButton) findViewById(R.id.gps);
        Button btn = (Button) findViewById(R.id.btn);
        final TextView addrTv = (TextView) findViewById(R.id.address);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String addr = addrTv.getText().toString();
                if (addr.equals("")){
                    Toast.makeText(MapActivity.this,"请输入有效的地址",Toast.LENGTH_SHORT).show();

                }else {
                    GeocodeSearch search = new GeocodeSearch(MapActivity.this);
                    search.setOnGeocodeSearchListener(MapActivity.this);
                    GeocodeQuery query = new GeocodeQuery(addr,"中国");
                    search.getFromLocationNameAsyn(query);
                }
            }
        });
        rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 300, 8, new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            updatePosition(location);

                        }

                        @Override
                        public void onStatusChanged(String provider, int status, Bundle extras) {

                        }

                        @Override
                        public void onProviderEnabled(String provider) {
                            if (ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                // TODO: Consider calling
                                //    ActivityCompat#requestPermissions
                                // here to request the missing permissions, and then overriding
                                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                //                                          int[] grantResults)
                                // to handle the case where the user grants the permission. See the documentation
                                // for ActivityCompat#requestPermissions for more details.
                                return;
                            }
                            updatePosition(locationManager.getLastKnownLocation(provider));

                        }

                        @Override
                        public void onProviderDisabled(String provider) {

                        }
                    });
                }
            }
        });
        Button bn = (Button) findViewById(R.id.loc);
        final TextView latTv = (TextView) findViewById(R.id.lat);
        final TextView lngTv = (TextView) findViewById(R.id.lng);
        ToggleButton tb = (ToggleButton) findViewById(R.id.bt);
        bn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String lng = lngTv.getEditableText().toString().trim();
                String lat = latTv.getEditableText().toString().trim();
                if (lng.equals("")||lat.equals("")){
                    Toast.makeText(MapActivity.this,"请输入有效的经度和纬度",Toast.LENGTH_SHORT).show();
                }else {
                    ((RadioButton)findViewById(R.id.manual)).setChecked(true);
                    double dLng = Double.parseDouble(lng);
                    double dLat = Double.parseDouble(lat);
                    LatLng pos = new LatLng(dLat,dLng);
                    CameraUpdate cu = CameraUpdateFactory.changeLatLng(pos);
                    aMap.moveCamera(cu);
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(pos);
                    markerOptions.title("通过经纬度");
                    markerOptions.snippet("搜索的位置");
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                    markerOptions.draggable(true);
                    Marker marker = aMap.addMarker(markerOptions);
                    marker.showInfoWindow();


                }
            }
        });
        tb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //设置使用卫星视图
                    aMap.setMapType(AMap.MAP_TYPE_SATELLITE);
                } else {
                    //设置使用普通视图
                    aMap.setMapType(AMap.MAP_TYPE_NORMAL);
                }
            }
        });

    }

    private void updatePosition(Location location) {
        LatLng pos = new LatLng(location.getLatitude(),location.getLongitude());
        CameraUpdate cu = CameraUpdateFactory.changeLatLng(pos);
        aMap.moveCamera(cu);
        aMap.clear();
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(pos);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher));
        markerOptions.draggable(true);
        Marker marker = aMap.addMarker(markerOptions);

    }

    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
            CameraUpdate cu = CameraUpdateFactory.zoomBy(15);
            aMap.moveCamera(cu);
            CameraUpdate tiltUpdate = CameraUpdateFactory.changeTilt(30);
            aMap.moveCamera(tiltUpdate);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {

    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {
        GeocodeAddress geo = geocodeResult.getGeocodeAddressList().get(0);
        LatLonPoint pos = geo.getLatLonPoint();
        LatLng targetPos = new LatLng(pos.getLatitude(),pos.getLongitude());
        CameraUpdate cu = CameraUpdateFactory.changeLatLng(targetPos);
        aMap.moveCamera(cu);
        GroundOverlayOptions options = new GroundOverlayOptions()/*.image(BitmapDescriptorFactory
                .fromResource(R.mipmap.ic_launcher))*/.position(targetPos, 64);
        aMap.addGroundOverlay(options);
        CircleOptions coptions = new CircleOptions().center(targetPos)//.fillColor(0x80ffff00)
                .radius(80).strokeWidth(1).strokeColor(Color.RED);
        aMap.addCircle(coptions);

    }

}



