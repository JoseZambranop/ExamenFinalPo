package com.example.examenfinalpo;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import WebServices.Asynchtask;
import WebServices.WebService;

public class Informacion extends AppCompatActivity implements Asynchtask {

    private TextView tvpais;
    private TextView tvcapital;
    private TextView tvcodigo;

    private String pais5, west1,east1, north1,south1;
    private TextView west,east;
    private TextView north;
    private TextView south;
    private TextView codigoISO;
    private double lat, log;
    public LatLng posicionMap;

    private GoogleMap mapa;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_informacion);

        tvpais = findViewById(R.id.tvPais);
        tvcapital = findViewById(R.id.tvCapital);
        tvcodigo = findViewById(R.id.tvCodigoISO);
        west = findViewById(R.id.txtoeste);
        east = findViewById(R.id.txteste);
        north = findViewById(R.id.txtnorte);
        south = findViewById(R.id.txtSur);

        Bundle bundle = this.getIntent().getExtras();
        pais5=bundle.getString("pd");

        tvpais.setText(bundle.getString("pd"));
        Map<String, String> datos = new HashMap<String, String>();
        WebService ws= new WebService("http://www.geognos.com/api/en/countries/info/all.json", datos,
                Informacion.this, Informacion.this  );
        ws.execute();
    }


    public void InicializarMp(){
        SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mapa = googleMap;
                posicionMap = new LatLng(lat,log);
                CameraUpdate camUpd1 = CameraUpdateFactory.newLatLngZoom(posicionMap, 5);
                mapa.moveCamera(camUpd1);
                DrawaRectangulo();
            }
        });
    }

    public void DrawaRectangulo(){
        PolylineOptions rectangulo = new PolylineOptions()
                .add(new LatLng(Double.parseDouble(north1), Double.parseDouble(west1)))
                .add(new LatLng(Double.parseDouble(north1), Double.parseDouble(east1)))
                .add(new LatLng(Double.parseDouble(south1), Double.parseDouble(east1)))
                .add(new LatLng(Double.parseDouble(south1), Double.parseDouble(west1)))
                .add(new LatLng(Double.parseDouble(north1), Double.parseDouble(west1)));

        rectangulo.width(10);
        rectangulo.color(Color.BLUE);
        mapa.addPolyline(rectangulo);
    }

    @Override
    public void processFinish(String result) throws JSONException {
        List<InfPais> lpa = new ArrayList<>();
        JSONObject jsonObject = new JSONObject(result);
        JSONObject jresults = jsonObject.getJSONObject("Results");
        InfPais pais = new InfPais();
        Iterator<?> iterator = jresults.keys();
        while (iterator.hasNext()){
            String key =(String)iterator.next();
            JSONObject jpais = jresults.getJSONObject(key);
            if(jpais.getString("Name").equals(pais5)) {
                pais.setNombres(jpais.getString("Name"));
                JSONObject jCountryCodes = jpais.getJSONObject("Capital");
                pais.setCapital(jCountryCodes.getString("Name"));
                JSONObject jGeoRectangle = jresults.getJSONObject("GeoRectangle");
                pais.setWest(jGeoRectangle.getString("West"));
                pais.setEast(jGeoRectangle.getString("East"));
                pais.setNorth(jGeoRectangle.getString("North"));
                pais.setSourth(jGeoRectangle.getString("South"));
                JSONArray jGeoPt = jresults.getJSONArray("GeoPt");
                pais.setLat(String.valueOf(jGeoPt.getDouble(0)));
                pais.setLog(String.valueOf(jGeoPt.getDouble(1)));
                JSONObject jCountryCode = jresults.getJSONObject("CountryCodes");
                pais.setCodIso(jCountryCodes.getString("iso2"));
                lpa.add(pais);
            }else {}
        }
        lat = Double.valueOf(pais.getLat());
        log = Double.valueOf(pais.getLog());
        tvcodigo.setText(pais.getCodIso());
        west.setText(pais.getWest());
        east.setText(pais.getEast());
        north.setText(pais.getNorth());
        south.setText(pais.getSourth());
        west1=pais.getWest();
        east1=(pais.getEast());
        north1=(pais.getNorth());
        south1=(pais.getSourth());
        InicializarMp();
    }
}
