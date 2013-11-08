package com.bradleege;

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import com.bradleege.tilesource.MapBoxTileSource;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.PathOverlay;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class BikeMapBoxActivity extends Activity
{
    private MapView mapView = null;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Setup MapBox As Provider
        MapBoxTileSource mapBoxTileSource = new MapBoxTileSource("mbtiles", ResourceProxy.string.base, 1, 20, 256, ".png", "bleege.map-3a5gfw2p", null);

        // Interact With the MapView
        mapView = (MapView)findViewById(R.id.mapview);
        mapView.setTileSource(mapBoxTileSource);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(14);
        mapView.getController().setCenter(new GeoPoint(43.05277119900874, -89.42244529724121));

        // Load The Route
        new LoadRouteOntoMapTask().execute();

        // Load UW Arboretum Marker
        ArrayList<OverlayItem> overlayItemArray = new ArrayList<OverlayItem>();
        OverlayItem arbMarker = new OverlayItem("UW Arboretum", "Fields, Trees, Abandoned City, etc", new GeoPoint(43.04277119900874, -89.42544529724121));
        overlayItemArray.add(arbMarker);
        DefaultResourceProxyImpl defaultResourceProxyImpl = new DefaultResourceProxyImpl(this);
        ItemizedIconOverlay<OverlayItem> myItemizedIconOverlay  = new ItemizedIconOverlay<OverlayItem>(overlayItemArray, null, defaultResourceProxyImpl);
        mapView.getOverlays().add(myItemizedIconOverlay);
    }

    private class LoadRouteOntoMapTask extends AsyncTask<Void, Void, ArrayList<GeoPoint>>
    {
        @Override
        protected ArrayList<GeoPoint> doInBackground(Void... params)
        {
            ArrayList<GeoPoint> points = new ArrayList<GeoPoint>();

            try
            {
                // Load Data From geojson file
                StringBuilder builder = new StringBuilder();

                InputStream is = getResources().getAssets().open("map.geojson");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                String line;
                while ((line = reader.readLine()) != null)
                {
                    builder.append(line);
                }

                JSONObject json = new JSONObject(builder.toString());
                JSONArray coordinates = json.getJSONArray("features").getJSONObject(0).getJSONObject("geometry").getJSONArray("coordinates");

                for (int lc = 0; lc < coordinates.length(); lc++)
                {
                    JSONArray c = coordinates.getJSONArray(lc);
                    GeoPoint gp = new GeoPoint(c.getDouble(1), c.getDouble(0));
                    points.add(gp);
                }
            }
            catch (IOException e)
            {
                Log.e(LoadRouteOntoMapTask.class.getName(), "Error Reading JSON Data", e);
            } catch (JSONException e)
            {
                Log.e(LoadRouteOntoMapTask.class.getName(), "Error Converting GeoJSON To GeoPoints", e);
            }

            return points;
        }

        @Override
        protected void onPostExecute(ArrayList<GeoPoint> geoPoints)
        {
            super.onPostExecute(geoPoints);

            // Build and Display the Path on the map
            PathOverlay myPath = new PathOverlay(Color.RED, getApplication().getApplicationContext());
            for (GeoPoint gp : geoPoints)
            {
                myPath.addPoint(gp);
            }
            mapView.getOverlays().add(0, myPath);
        }
    }
}
