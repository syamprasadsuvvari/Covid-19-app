package com.example.covid_19;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class DetailsFragment extends Fragment {

    private static final String TAG = "DetailsFragment";

    private EditText editTextName, editTextAge;
    private Spinner spinnerGender;
    private Button btnSubmit;
    private String email, password;
    private FirebaseAuth mAuth;

    public DetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        if (getArguments() != null) {
            email = getArguments().getString("email");
            password = getArguments().getString("password");
        }

        editTextName = view.findViewById(R.id.editTextName);
        editTextAge = view.findViewById(R.id.editTextAge);
        spinnerGender = view.findViewById(R.id.spinnerGender);
        btnSubmit = view.findViewById(R.id.btnSubmitDetails);

        // Setup gender spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.gender_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);

        btnSubmit.setOnClickListener(v -> validateAndRegisterUser());
    }

    private void validateAndRegisterUser() {
        String name = editTextName.getText().toString().trim();
        String age = editTextAge.getText().toString().trim();
        String gender = spinnerGender.getSelectedItem().toString();

        if (name.isEmpty() || age.isEmpty() || gender.equals("Select Gender")) {
            Toast.makeText(getContext(), "Please fill in all the details.", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveUserDetails(user.getUid(), name, age, gender);
                        }
                    } else {
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() : "Unknown error";
                        Toast.makeText(getContext(), "Signup failed: " + errorMessage,
                                Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Registration failed: " + errorMessage);
                    }
                });
    }

    private void saveUserDetails(String uid, String name, String age, String gender) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("users");

        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("name", name);
        userMap.put("age", age);
        userMap.put("gender", gender);
        userMap.put("email", email);

        dbRef.child(uid).setValue(userMap)
                .addOnSuccessListener(unused -> {
                    navigateToAuthActivity();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to save user data: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Failed to save user data", e);
                });
    }

    private void navigateToAuthActivity() {
        Toast.makeText(getContext(), "Account created successfully!", Toast.LENGTH_SHORT).show();
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), AuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }
}