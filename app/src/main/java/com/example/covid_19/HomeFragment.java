package com.example.covid_19;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private TextView confirmedText, deathsText, activeText, testsText;
    private ImageView flagImage;
    private ViewPager2 viewPager;
    private SwipeRefreshLayout swipeRefreshLayout;

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable autoScrollRunnable;
    private List<SlideCard> cardList = new ArrayList<>();
    private int currentPage = 0;

    public HomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // Bind Views
        confirmedText = view.findViewById(R.id.textConfirmed);
        deathsText = view.findViewById(R.id.textDeaths);
        activeText = view.findViewById(R.id.textActive);
        testsText = view.findViewById(R.id.textTests);
        flagImage = view.findViewById(R.id.country);
        viewPager = view.findViewById(R.id.cardSlider);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        TextView nameTextView = view.findViewById(R.id.name);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String uid = user.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance()
                    .getReference("users").child(uid);

            userRef.child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String name = snapshot.getValue(String.class);
                        nameTextView.setText("Hi, " + name);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Failed to load name", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Setup ViewPager
        setupViewPager();
        startAutoScroll();

        // Load cached or fresh data
        if (DataCache.homeCovidData == null) {
            checkLocationPermissionAndFetch();
        } else {
            updateUI(DataCache.homeCovidData);
        }

        // Setup pull-to-refresh
        swipeRefreshLayout.setOnRefreshListener(() -> {
            DataCache.homeCovidData = null;
            checkLocationPermissionAndFetch();
        });
    }

    private void setupViewPager() {
        cardList.add(new SlideCard("Find COVID-19 Testing Labs", ActionType.WEBSITE,
                "https://covid19cc.nic.in/icmr/Citizen/LabList.aspx", R.drawable.lab_icon));
        cardList.add(new SlideCard("Call Health Ministry", ActionType.PHONE,
                "1075", R.drawable.phone_icon));
        cardList.add(new SlideCard("Book Vaccination Slot", ActionType.WEBSITE,
                "https://www.cowin.gov.in/", R.drawable.vaccine_icon));

        CardSliderAdapter adapter = new CardSliderAdapter(cardList, getContext(), card -> {
            if (card.actionType == ActionType.WEBSITE) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(card.actionData)));
            } else if (card.actionType == ActionType.PHONE) {
                startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + card.actionData)));
            }
        });

        viewPager.setAdapter(adapter);
    }

    private void startAutoScroll() {
        autoScrollRunnable = new Runnable() {
            @Override
            public void run() {
                if (viewPager.getAdapter() != null && cardList.size() > 0) {
                    currentPage = (currentPage + 1) % cardList.size();
                    viewPager.setCurrentItem(currentPage, true);
                    handler.postDelayed(this, 3000);
                }
            }
        };
        handler.postDelayed(autoScrollRunnable, 3000);
    }

    private void checkLocationPermissionAndFetch() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            fetchLocationAndData();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
                grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchLocationAndData();
        } else {
            Toast.makeText(requireContext(), "Location permission required to fetch COVID data.", Toast.LENGTH_LONG).show();
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    @SuppressLint("MissingPermission")
    private void fetchLocationAndData() {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
        fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
            if (location != null) {
                getCountryNameFromLocation(location);
            } else {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(requireContext(), "Unable to get location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getCountryNameFromLocation(Location location) {
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (!addresses.isEmpty()) {
                String countryName = addresses.get(0).getCountryName();
                fetchCovidDataForCountry(countryName);
            } else {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(requireContext(), "Unable to determine country", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(requireContext(), "Geocoder failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchCovidDataForCountry(String countryName) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<CovidData> call = apiService.getCountryData(countryName);

        call.enqueue(new Callback<CovidData>() {
            @Override
            public void onResponse(Call<CovidData> call, Response<CovidData> response) {
                swipeRefreshLayout.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    DataCache.homeCovidData = response.body();  // ✅ Cache data
                    updateUI(DataCache.homeCovidData);
                } else {
                    Toast.makeText(requireContext(), "No data found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CovidData> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(requireContext(), "Failed to fetch data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(CovidData data) {
        confirmedText.setText(String.format(Locale.getDefault(), "%,d", data.getCases()));
        deathsText.setText(String.format(Locale.getDefault(), "%,d", data.getDeaths()));
        activeText.setText(String.format(Locale.getDefault(), "%,d", data.getPopulation()));
        testsText.setText(String.format(Locale.getDefault(), "%,d", data.getTests()));
        Glide.with(this).load(data.getCountryInfo().getFlag()).into(flagImage);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(autoScrollRunnable);
    }
}