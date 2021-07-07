package com.example.navermapapi;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PointF;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraAnimation;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.overlay.PolygonOverlay;
import com.naver.maps.map.util.FusedLocationSource;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private Spinner spinner;
    private NaverMap mNaverMap;
    private ArrayList<Marker> markerArrayList;
    private ArrayList<LatLng> latLngArrayList;
    private PolygonOverlay polygonOverlay;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource locationSource;
    private EditText editTextAddress;
    private Button btnInputAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        markerArrayList = new ArrayList<>();
        latLngArrayList = new ArrayList<LatLng>();
        polygonOverlay = new PolygonOverlay();

//        지도 객체 생성
        FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment) fm.findFragmentById(R.id.map);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            fm.beginTransaction().add(R.id.map, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);

        //스피너 생성
        spinner = (Spinner) findViewById(R.id.spinner);
        String[] list1 = new String[2];
        list1[0] = "일반지도";
        list1[1] = "위성지도";
        ArrayAdapter spinnerAdapter;
        spinnerAdapter = new ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, list1);
        spinner.setAdapter(spinnerAdapter);

        //체크박스 생성과 지적도 추가/제거
        CheckBox checkBox = (CheckBox) findViewById(R.id.check1);
        checkBox.setOnClickListener(new CheckBox.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {
                    mNaverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_CADASTRAL, true);
                } else {
                    mNaverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_CADASTRAL, false);
                }

            }
        });

//        현재 기기 위치 표시
        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);


        editTextAddress = findViewById(R.id.editTextAddres);
        btnInputAddress = findViewById(R.id.btnInputAddress);

        btnInputAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String targetAddress;
                targetAddress = editTextAddress.getText().toString();

                NaverMapGeocoding naverMapGeocoding = new NaverMapGeocoding();
                naverMapGeocoding.execute(targetAddress);

            }
        });


    }

    @Override
    public void onMapReady(@NonNull @org.jetbrains.annotations.NotNull NaverMap naverMap) {

        //스피너 선택시 지도 타입 변경과 스피너 텍스트 색 변환
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(Color.BLACK);

                if (position == 0) {
                    naverMap.setMapType(NaverMap.MapType.Basic);

                } else if (position == 1) {
                    naverMap.setMapType(NaverMap.MapType.Satellite);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        this.mNaverMap = naverMap;

        //실행시 군산대학교로 위치 표시
        CameraPosition cameraPosition = new CameraPosition(
                new LatLng(35.946150123265014, 126.68213361186021),
                10
        );
        naverMap.setCameraPosition(cameraPosition);

//      군산대,군산시청,군산항 마커 표시
        Marker marker1 = new Marker();
        marker1.setPosition(new LatLng(35.94614143753062, 126.68210142526509));
        marker1.setMap(mNaverMap);
        Marker marker2 = new Marker();
        marker2.setPosition(new LatLng(35.96857326118372, 126.73801265702906));
        marker2.setMap(mNaverMap);
        Marker marker3 = new Marker();
        marker3.setPosition(new LatLng(35.97073411872367, 126.6163550872227));
        marker3.setMap(mNaverMap);

        PolygonOverlay polygon = new PolygonOverlay();
        polygon.setCoords(Arrays.asList(
                new LatLng(35.94614143753062, 126.68210142526509),
                new LatLng(35.96857326118372, 126.73801265702906),
                new LatLng(35.97073411872367, 126.6163550872227)
        ));
        polygon.setMap(mNaverMap);
        polygon.setColor(0x80ff0000);


//        지도 클릭시 마커 및 다각형 생성
        naverMap.setOnMapClickListener(new NaverMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull @NotNull PointF pointF, @NonNull @NotNull LatLng latLng) {
                Marker marker = new Marker();
                marker.setPosition(latLng);
                marker.setMap(mNaverMap);
                markerArrayList.add(marker);
                latLngArrayList.add(marker.getPosition());

                if (latLngArrayList.size() >= 3) {
                    polygonOverlay.setCoords(latLngArrayList);
                    polygonOverlay.setMap(mNaverMap);
                    polygonOverlay.setColor(0x80ff0000);
                }
            }
        });


        //오버레이 초기화 버튼
        Button button = (Button) findViewById(R.id.btn_del);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < markerArrayList.size(); i++) {
                    markerArrayList.get(i).setMap(null);
                }
                markerArrayList.clear();
                latLngArrayList.clear();
                polygonOverlay.setMap(null);
                marker1.setMap(null);
                marker2.setMap(null);
                marker3.setMap(null);
                polygon.setMap(null);
                naverMap.setLocationTrackingMode(LocationTrackingMode.None);
            }
        });


//        현재위치 표시
        this.mNaverMap = naverMap;
        naverMap.setLocationSource(locationSource);
        naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);

        // 화면 전환 버튼
        Button button1 = (Button) findViewById(R.id.btn_move);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraUpdate cameraUpdate = CameraUpdate.scrollTo(new LatLng(35.94615012326476, 126.68210142526509)).animate(CameraAnimation.Easing);
                naverMap.moveCamera(cameraUpdate);
            }
        });

        //    롱클릭 이벤트
        mNaverMap.setOnMapLongClickListener(new NaverMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(@NonNull @NotNull PointF pointF, @NonNull @NotNull LatLng latLng) {
                NaverAddrApi naverAddrApi = new NaverAddrApi();
                naverAddrApi.execute(latLng);

            }
        });

    }

//    Reverse Geocoding
    public class NaverAddrApi extends AsyncTask<LatLng, String, String> {
        private StringBuilder urlBuilder;
        private URL url;
        private HttpURLConnection conn;
        double markerla,markerlo;


        @Override
        protected String doInBackground(LatLng... latLngs) {
            String strCoord = String.valueOf(latLngs[0].longitude) + "," + String.valueOf(latLngs[0].latitude);
            StringBuilder sb = new StringBuilder();
            markerla = latLngs[0].latitude;
            markerlo = latLngs[0].longitude;

            urlBuilder = new StringBuilder("https://naveropenapi.apigw.ntruss.com/map-reversegeocode/v2/gc?request=coordsToaddr&coords=" +strCoord+ "&sourcecrs=epsg:4326&output=json&orders=addr"); /* URL */
            try {
                url = new URL(urlBuilder.toString());
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-type", "application/json");
                conn.setRequestProperty("X-NCP-APIGW-API-KEY-ID","mzze7x0i3t");
                conn.setRequestProperty("X-NCP-APIGW-API-KEY","mO3TGGj34sBrSzBIdj85mqslW0XfYTaS9Qcdpt5L");

                BufferedReader rd;
                if(conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
                    rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                } else {
                    rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                }
                String line;
                while ((line = rd.readLine()) != null) {
                    sb.append(line);
                }
                rd.close();
                conn.disconnect();

            } catch (Exception e) {
                 return null;
            }
            return sb.toString();
        }

        @Override
        protected void onPostExecute(String jsonStr) {
            super.onPostExecute(jsonStr);
            String pnu = getPnu(jsonStr);

            Marker marker4 = new Marker();
            marker4.setPosition(new LatLng(markerla, markerlo));
            marker4.setMap(mNaverMap);
            InfoWindow infoWindow = new InfoWindow();
            infoWindow.open(marker4);
            infoWindow.setAdapter(new InfoWindow.DefaultTextAdapter(MainActivity.this) {
                @NonNull
                @NotNull
                @Override
                public CharSequence getText(@NonNull @NotNull InfoWindow infoWindow) {
                    return pnu;
                }
            });

        }

        private String getPnu(String jsonStr) {
            JsonParser jsonParser = new JsonParser();

            JsonObject jsonObj = (JsonObject) jsonParser.parse(jsonStr);
            JsonArray jsonArray = (JsonArray) jsonObj.get("results");
            jsonObj = (JsonObject) jsonArray.get(0);
            jsonObj = (JsonObject) jsonObj.get("region");
            jsonObj = (JsonObject) jsonObj.get("area1");
            String pnu = jsonObj.get("name").getAsString();

            jsonObj = (JsonObject) jsonArray.get(0);
            jsonObj = (JsonObject) jsonObj.get("region");
            jsonObj = (JsonObject) jsonObj.get("area2");
            pnu = pnu + " " + jsonObj.get("name").getAsString();

            jsonObj = (JsonObject) jsonArray.get(0);
            jsonObj = (JsonObject) jsonObj.get("region");
            jsonObj = (JsonObject) jsonObj.get("area3");
            pnu = pnu + " " + jsonObj.get("name").getAsString();

            jsonObj = (JsonObject) jsonArray.get(0);
            jsonObj = (JsonObject) jsonObj.get("region");
            jsonObj = (JsonObject) jsonObj.get("area4");
            pnu = pnu + " " + jsonObj.get("name").getAsString();

            jsonObj = (JsonObject) jsonParser.parse(jsonStr);
            jsonArray = (JsonArray) jsonObj.get("results");
            jsonObj = (JsonObject) jsonArray.get(0);
            jsonObj = (JsonObject) jsonObj.get("land");
            String number1 = jsonObj.get("number1").getAsString();
            String number2 = jsonObj.get("number2").getAsString();
            pnu = pnu + " " + number1 + "-" + number2;
            return pnu;
        }

//        private String makeStringNum(String number) {
//            String strNum="";
//            for (int i=0; i<4-number.length(); i++) {
//                strNum = strNum + "0";
//            }
//            strNum=strNum+number;
//            return strNum;
//        }
    }

//    Geocoding
    public class NaverMapGeocoding extends AsyncTask<String, String, String> {
        private StringBuilder urlBuilder;
        private URL url;
        private HttpURLConnection conn;

        @Override
        protected String doInBackground(String... str) {
            StringBuilder sb = new StringBuilder();



            urlBuilder = new StringBuilder("https://naveropenapi.apigw.ntruss.com/map-geocode/v2/geocode?query=" + editTextAddress.getText().toString()); /* URL */
            try {
                url = new URL(urlBuilder.toString());
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-type", "application/json");
                conn.setRequestProperty("X-NCP-APIGW-API-KEY-ID","mzze7x0i3t");
                conn.setRequestProperty("X-NCP-APIGW-API-KEY","mO3TGGj34sBrSzBIdj85mqslW0XfYTaS9Qcdpt5L");

                BufferedReader rd;
                if(conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
                    rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                } else {
                    rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                }
                String line;
                while ((line = rd.readLine()) != null) {
                    sb.append(line);
                }
                rd.close();
                conn.disconnect();

            } catch (Exception e) {
                return null;
            }
            return sb.toString();
        }

        @Override
        protected void onPostExecute(String jsonStr) {
            JsonParser jsonParser = new JsonParser();

            JsonObject jsonObj = (JsonObject) jsonParser.parse(jsonStr);
            JsonArray jsonArray = (JsonArray) jsonObj.get("addresses");
            jsonObj = (JsonObject) jsonArray.get(0);
            Double a =jsonObj.get("x").getAsDouble();

            jsonObj = (JsonObject) jsonParser.parse(jsonStr);
            jsonArray = (JsonArray) jsonObj.get("addresses");
            jsonObj = (JsonObject) jsonArray.get(0);
            Double b =jsonObj.get("y").getAsDouble();

            Marker marker5 = new Marker();
            marker5.setPosition(new LatLng(b, a));
            marker5.setMap(mNaverMap);

        }
/*
        private String getPnu(String jsonStr) {
            JsonParser jsonParser = new JsonParser();

            JsonObject jsonObj = (JsonObject) jsonParser.parse(jsonStr);
            JsonArray jsonArray = (JsonArray) jsonObj.get("addresses");
            jsonObj = (JsonObject) jsonArray.get(0);
            String pnu = "x : " + jsonObj.get("x").getAsString();

            jsonObj = (JsonObject) jsonParser.parse(jsonStr);
            jsonArray = (JsonArray) jsonObj.get("addresses");
            jsonObj = (JsonObject) jsonArray.get(0);
            pnu = pnu + ", y :" + jsonObj.get("y").getAsString();
            return pnu;
        }

 */


    }

}