package com.example.covid_19;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class DialogGenderPicker extends DialogFragment {

    private static final String[] GENDERS = {"Select Gender", "Male", "Female", "Prefer not to Say"};

    public static DialogGenderPicker newInstance() {
        return new DialogGenderPicker();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog_edit_spinner, container, false);

        Spinner spinner = view.findViewById(R.id.spinnerOptions);
        Button saveButton = view.findViewById(R.id.buttonConfirm);
        Button cancelButton = view.findViewById(R.id.buttonCancel);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.spinner_item, GENDERS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        saveButton.setOnClickListener(v -> {
            int pos = spinner.getSelectedItemPosition();
            if (pos > 0) {
                sendResult(GENDERS[pos]);
                dismiss();
            } else {
                Toast.makeText(getContext(), "Please select a valid gender", Toast.LENGTH_SHORT).show();
            }
        });

        cancelButton.setOnClickListener(v -> dismiss());

        return view;
    }

    private void sendResult(String selectedGender) {
        Intent intent = new Intent();
        intent.putExtra("key", "gender");
        intent.putExtra("value", selectedGender);
        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
    }
}