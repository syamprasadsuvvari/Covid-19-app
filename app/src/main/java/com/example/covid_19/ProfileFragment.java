package com.example.covid_19;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.*;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    private ImageView imageProfile;
    private TextView textName, textAge, textEmail, textGender, textCountry;
    private Button buttonLogout;

    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private DatabaseReference userRef;

    private static final int EDIT_FIELD_REQUEST = 100;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        imageProfile = view.findViewById(R.id.imageProfile);
        textName = view.findViewById(R.id.textName);
        textAge = view.findViewById(R.id.textAge);
        textEmail = view.findViewById(R.id.textEmail);
        textGender = view.findViewById(R.id.textGender);
        textCountry = view.findViewById(R.id.textCountry);
        buttonLogout = view.findViewById(R.id.buttonLogout);

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();

        if (firebaseUser == null) {
            startActivity(new Intent(getActivity(), AuthActivity.class));
            requireActivity().finish();
            return view;
        }

        userRef = FirebaseDatabase.getInstance().getReference("users").child(firebaseUser.getUid());

        loadUserProfile();
        setupListeners(view);

        view.findViewById(R.id.buttonChangePassword).setOnClickListener(v -> {
            String email = firebaseUser.getEmail();
            if (email != null) {
                auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> Toast.makeText(getContext(),
                                task.isSuccessful() ? "Reset email sent to " + email : "Failed to send reset email",
                                Toast.LENGTH_LONG).show());
            }
        });

        return view;
    }

    private void loadUserProfile() {
        if (DataCache.cachedUser != null) {
            updateUI(DataCache.cachedUser);
            return;
        }

        userRef.get().addOnSuccessListener(snapshot -> {
            User user = snapshot.getValue(User.class);
            if (user != null) {
                DataCache.cachedUser = user;
                updateUI(user);
            }
        }).addOnFailureListener(e ->
                Toast.makeText(getContext(), "Failed to load user data", Toast.LENGTH_SHORT).show()
        );
    }

    private void updateUI(User user) {
        textName.setText(user.getName());
        textAge.setText(user.getAge());
        textEmail.setText(user.getEmail());
        textGender.setText(user.getGender());
        textCountry.setText(getUserCountry());

        imageProfile.setImageResource(R.drawable.userprofile);
    }

    private void setupListeners(View view) {
        setupEditClick(view, R.id.editName, "name");
        setupEditClick(view, R.id.editAge, "age");

        // Use spinner dialog for gender field
        view.findViewById(R.id.editGender).setOnClickListener(v -> {
            DialogGenderPicker dialog = DialogGenderPicker.newInstance();
            dialog.setTargetFragment(ProfileFragment.this, EDIT_FIELD_REQUEST);
            dialog.show(getParentFragmentManager(), "DialogGenderPicker");
        });

        buttonLogout.setOnClickListener(v -> {
            auth.signOut();
            DataCache.cachedUser = null;
            startActivity(new Intent(getActivity(), AuthActivity.class));
            requireActivity().finish();
        });
    }

    private void setupEditClick(View view, int viewId, String key) {
        View editView = view.findViewById(viewId);
        if (editView != null) {
            editView.setOnClickListener(v -> {
                DialogEditField dialog = DialogEditField.newInstance(key);
                dialog.setTargetFragment(ProfileFragment.this, EDIT_FIELD_REQUEST);
                dialog.show(getParentFragmentManager(), "DialogEditField");
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EDIT_FIELD_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            String key = data.getStringExtra("key");
            String value = data.getStringExtra("value");

            if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
                userRef.child(key).setValue(value);
                if (DataCache.cachedUser != null) {
                    switch (key) {
                        case "name":   DataCache.cachedUser.setName(value);   textName.setText(value);   break;
                        case "age":    DataCache.cachedUser.setAge(value);    textAge.setText(value);    break;
                        case "gender": DataCache.cachedUser.setGender(value); textGender.setText(value); break;
                    }
                }
            }
        }
    }

    private String getUserCountry() {
        try {
            LocationManager lm = (LocationManager) requireActivity().getSystemService(Activity.LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return "Unknown";
            }

            Location location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location != null) {
                Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                if (!addresses.isEmpty()) {
                    return addresses.get(0).getCountryName();
                }
            }
        } catch (IOException | SecurityException ignored) {}
        return "Unknown";
    }
}


