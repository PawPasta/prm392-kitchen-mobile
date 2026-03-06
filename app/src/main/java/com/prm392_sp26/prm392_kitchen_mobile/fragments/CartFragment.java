package com.prm392_sp26.prm392_kitchen_mobile.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.prm392_sp26.prm392_kitchen_mobile.R;

public class CartFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        Button btnCreateCustomOrder = view.findViewById(R.id.btnCreateCustomOrder);
        btnCreateCustomOrder.setOnClickListener(v -> {
            // Navigate to CreateCustomOrderFragment
            CreateCustomOrderFragment fragment = new CreateCustomOrderFragment();
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }
}
