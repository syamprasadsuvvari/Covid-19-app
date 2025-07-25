package com.example.covid_19;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment {

    private AutoCompleteTextView autoCompleteTextView;
    private TextView textConfirmed, textDeaths, textActive, textTests, textCountryName;
    private ImageView markerImage, mapImage;

    private final String[] countryList = Locale.getISOCountries();

    public SearchFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        autoCompleteTextView = view.findViewById(R.id.autoCompleteCountry);
        textConfirmed = view.findViewById(R.id.textConfirmed);
        textDeaths = view.findViewById(R.id.textDeaths);
        textActive = view.findViewById(R.id.textActive);
        textTests = view.findViewById(R.id.textTests);
        textCountryName = view.findViewById(R.id.textCountryName);
        markerImage = view.findViewById(R.id.marker);
        mapImage = view.findViewById(R.id.worldMap);
        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        // Convert country codes to display names
        List<String> countries = new ArrayList<>();
        for (String code : countryList) {
            Locale locale = new Locale("", code);
            countries.add(locale.getDisplayCountry());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_dropdown_item_1line, countries);
        autoCompleteTextView.setAdapter(adapter);
        autoCompleteTextView.setText(""); // Empty search bar

        // Restore from cache if available
        if (DataCache.searchCovidData != null && DataCache.searchCountry != null) {
            autoCompleteTextView.setText(DataCache.searchCountry);
            updateUI(DataCache.searchCovidData);
            updateMarkerPosition(
                    DataCache.searchCovidData.getCountryInfo().getLat(),
                    DataCache.searchCovidData.getCountryInfo().getLong()
            );
        } else {
            getUserCountryFromLocation(); // Load from location if no cache
        }
        getUserCountryFromLocation();

        // Handle search button (IME_ACTION_SEARCH)
        autoCompleteTextView.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String country = autoCompleteTextView.getText().toString().trim();
                if (!country.isEmpty()) {
                    fetchCovidData(country);
                    autoCompleteTextView.dismissDropDown();
                    InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(autoCompleteTextView.getWindowToken(), 0);
                }
                return true;
            }
            return false;
        });

        // On suggestion click
        autoCompleteTextView.setOnItemClickListener((parent, v, position, id) -> {
            String country = (String) parent.getItemAtPosition(position);
            fetchCovidData(country);
        });

        // Swipe to refresh
        swipeRefreshLayout.setOnRefreshListener(() -> {
            String country = autoCompleteTextView.getText().toString().trim();
            if (!country.isEmpty()) {
                DataCache.searchCovidData = null; // Invalidate cache
                fetchCovidData(country);
            }
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void fetchCovidData(String countryName) {
        String apiCompatibleName = getStandardizedCountryName(countryName);
        if (apiCompatibleName == null) {
            Toast.makeText(getContext(), "Invalid country: " + countryName, Toast.LENGTH_SHORT).show();
            return;
        }

        // Use cache if available
        if (DataCache.searchCovidData != null && countryName.equalsIgnoreCase(DataCache.searchCountry)) {
            updateUI(DataCache.searchCovidData);
            updateMarkerPosition(
                    DataCache.searchCovidData.getCountryInfo().getLat(),
                    DataCache.searchCovidData.getCountryInfo().getLong()
            );
            return;
        }

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<CovidData> call = apiService.getCountryData(apiCompatibleName);

        call.enqueue(new Callback<CovidData>() {
            @Override
            public void onResponse(@NonNull Call<CovidData> call, @NonNull Response<CovidData> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CovidData data = response.body();

                    // Save to cache
                    DataCache.searchCovidData = data;
                    DataCache.searchCountry = countryName;

                    updateUI(data);
                    updateMarkerPosition(data.getCountryInfo().getLat(), data.getCountryInfo().getLong());
                } else {
                    Toast.makeText(getContext(), "No data found for: " + apiCompatibleName, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<CovidData> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Error fetching data: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(CovidData data) {
        textCountryName.setText(data.getCountry());
        textConfirmed.setText(String.format(Locale.getDefault(), "%,d", data.getCases()));
        textDeaths.setText(String.format(Locale.getDefault(), "%,d", data.getDeaths()));
        textActive.setText(String.format(Locale.getDefault(), "%,d", data.getPopulation()));
        textTests.setText(String.format(Locale.getDefault(), "%,d", data.getTests()));
    }

    private void updateMarkerPosition(double lat, double lon) {
        mapImage.post(() -> {
            int width = mapImage.getWidth();
            int height = mapImage.getHeight();

            // Equirectangular projection
            float x = (float) ((lon + 180) / 360.0 * width);
            float y = (float) ((90 - lat) / 180.0 * height);

            // Center the marker
            x -= markerImage.getWidth() / 2f;
            y -= markerImage.getHeight() / 2f;

            markerImage.setX(x);
            markerImage.setY(y);
            markerImage.setVisibility(View.VISIBLE);
        });
    }




    private String getStandardizedCountryName(String inputName) {
        for (String code : Locale.getISOCountries()) {
            Locale locale = new Locale("", code);
            if (locale.getDisplayCountry().equalsIgnoreCase(inputName)) {
                return locale.getDisplayCountry();
            }
        }
        return null;
    }

    private void getUserCountryFromLocation() {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    if (!addresses.isEmpty()) {
                        String country = addresses.get(0).getCountryName();
                        autoCompleteTextView.setText(country);
                        fetchCovidData(country);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onResume()
    {
        super.onResume();
        autoCompleteTextView.setText("");
        markerImage.setVisibility(View.INVISIBLE);
        getUserCountryFromLocation();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        DataCache.searchCovidData=null;
        DataCache.searchCountry=null;
    }
}