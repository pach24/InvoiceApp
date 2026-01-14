package com.nexosolar.android.ui.smartsolar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.nexosolar.android.databinding.FragmentEnergyBinding;

public class EnergyFragment extends Fragment {

    private FragmentEnergyBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEnergyBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}
