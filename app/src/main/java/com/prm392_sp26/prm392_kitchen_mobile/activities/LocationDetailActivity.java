package com.prm392_sp26.prm392_kitchen_mobile.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.prm392_sp26.prm392_kitchen_mobile.R;
import com.prm392_sp26.prm392_kitchen_mobile.util.Constants;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class LocationDetailActivity extends AppCompatActivity {

    private MapView detailMapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_detail);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        detailMapView = findViewById(R.id.detailMapView);
        setupMap();
    }

    private void setupMap() {
        if (detailMapView == null) {
            return;
        }

        GeoPoint storePoint = new GeoPoint(Constants.STORE_LATITUDE, Constants.STORE_LONGITUDE);
        detailMapView.setTileSource(TileSourceFactory.MAPNIK);
        detailMapView.getController().setZoom(17.0);
        detailMapView.getController().setCenter(storePoint);
        detailMapView.setMultiTouchControls(true);
        detailMapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);

        Marker marker = new Marker(detailMapView);
        marker.setPosition(storePoint);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle(Constants.STORE_NAME);
        detailMapView.getOverlays().add(marker);
        marker.showInfoWindow();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (detailMapView != null) {
            detailMapView.onResume();
        }
    }

    @Override
    protected void onPause() {
        if (detailMapView != null) {
            detailMapView.onPause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (detailMapView != null) {
            detailMapView.onDetach();
        }
        super.onDestroy();
    }
}
