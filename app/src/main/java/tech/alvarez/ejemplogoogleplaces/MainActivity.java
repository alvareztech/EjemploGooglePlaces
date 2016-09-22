package tech.alvarez.ejemplogoogleplaces;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private TextView infoTextView;
    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        infoTextView = (TextView) findViewById(R.id.infoTextView);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        googleApiClient.disconnect();
    }

    public void seleccionarLugar(View view) {

        PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
        try {

            startActivityForResult(intentBuilder.build(this), 777);

        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 777) {
            if (resultCode == RESULT_OK) {
                Place lugar = PlacePicker.getPlace(this, data);
                String nombre = lugar.getName().toString();
                String direccion = lugar.getAddress().toString();
                String id = lugar.getId();

                infoTextView.setText(nombre + "\n" + direccion + "\n" + id);
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public void mostrarLugaresCerca(View view) {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi.getCurrentPlace(googleApiClient, null);
        result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
            @Override
            public void onResult(@NonNull PlaceLikelihoodBuffer placeLikelihoods) {
                for (PlaceLikelihood placeLikelihood : placeLikelihoods) {
                    infoTextView.append(placeLikelihood.getPlace().getName()  + ":" + placeLikelihood.getLikelihood() + "\n");

//                    Toast.makeText(MainActivity.this, placeLikelihood.getPlace().getName() + ", " + placeLikelihood.getLikelihood(), Toast.LENGTH_SHORT).show();
                }
                placeLikelihoods.release();
            }
        });

    }

    public void buscarLugares(View view) {
        AutocompleteFilter autocompleteFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_CITIES)
                .build();

        PendingResult<AutocompletePredictionBuffer> result = Places.GeoDataApi.getAutocompletePredictions(googleApiClient, "La Paz", null, autocompleteFilter);
        result.setResultCallback(new ResultCallback<AutocompletePredictionBuffer>() {

            @Override
            public void onResult(@NonNull AutocompletePredictionBuffer autocompletePredictions) {
                for (AutocompletePrediction prediction : autocompletePredictions) {
                    Toast.makeText(MainActivity.this,
                            prediction.getPlaceId() + ": " + prediction.getFullText(null),
                            Toast.LENGTH_SHORT).show();
                }
                autocompletePredictions.release();
            }
        });
    }
}
