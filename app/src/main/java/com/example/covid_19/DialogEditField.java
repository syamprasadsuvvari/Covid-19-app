package com.example.covid_19;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DialogEditField extends DialogFragment {

    private EditText editText;
    private String fieldKey;

    public static DialogEditField newInstance(String fieldKey) {
        DialogEditField dialog = new DialogEditField();
        Bundle args = new Bundle();
        args.putString("fieldKey", fieldKey);
        dialog.setArguments(args);
        return dialog;
    }

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_edit_field, container, false);

        fieldKey = getArguments().getString("fieldKey");
        editText = view.findViewById(R.id.editTextField);
        TextView title = view.findViewById(R.id.textDialogTitle);
        title.setText("Edit " + fieldKey.substring(0, 1).toUpperCase() + fieldKey.substring(1));

        view.findViewById(R.id.buttonSave).setOnClickListener(v -> saveChange());
        view.findViewById(R.id.buttonDiscard).setOnClickListener(v -> dismiss());

        return view;
    }

    private void saveChange() {
        String value = editText.getText().toString().trim();

        if (value.isEmpty()) {
            editText.setError("Required");
            return;
        }

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(FirebaseAuth.getInstance().getCurrentUser ().getUid());

        ref.child(fieldKey).setValue(value)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Updated", Toast.LENGTH_SHORT).show();
                    // Set result to indicate success
                    Intent intent = new Intent();
                    intent.putExtra("key", fieldKey);
                    intent.putExtra("value", value);
                    getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
                    dismiss();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed", Toast.LENGTH_SHORT).show());
    }




    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90); // 90% of screen width
            getDialog().getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

}