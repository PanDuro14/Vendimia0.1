package com.example.vendimia01.ui.seguridad;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.vendimia01.MainActivity;
import com.example.vendimia01.R;
import com.example.vendimia01.SharedViewModel;
import com.example.vendimia01.databinding.FragmentSeguridadBinding;

public class Seguridad extends Fragment {

    private FragmentSeguridadBinding binding;
    private SharedViewModel sharedViewModel;
    private final int FRAGMENT_ID = 5;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSeguridadBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        initUI();
        setupObservers();
        setupButton();
        binding.btnTemperatura.setOnClickListener(v -> tempe());
    }

    private void initUI() {
        binding.btnSeguridad.setBackgroundColor(Color.GREEN);
        binding.btnSeguridad.setText("ACTIVAR SEGURIDAD");
        binding.TextoSeguridad.setText("Sistema inactivo");
        binding.TextoSeguridad.setTextColor(Color.RED);
    }

    private void setupObservers() {
        // Observador para datos entrantes
        sharedViewModel.getDataIn().observe(getViewLifecycleOwner(), data -> {
            if (data != null && sharedViewModel.getSeguridadActivada().getValue() != null
                    && sharedViewModel.getSeguridadActivada().getValue()) {
                binding.TextoSeguridad.setText(data);
            }
        });

        // Observador para movimiento detectado
        sharedViewModel.getMovimientoDetectado().observe(getViewLifecycleOwner(), movimiento -> {
            if (movimiento != null && movimiento) {
                showAlerta();
            } else {
                hideAlerta();
            }
        });

        // Observador para estado de conexión
        sharedViewModel.getIsConnected().observe(getViewLifecycleOwner(), isConnected -> {
            if (isConnected != null && !isConnected) {
                binding.TextoSeguridad.setText("Dispositivo desconectado");
                binding.TextoSeguridad.setTextColor(Color.GRAY);
            }
        });

        // Observador para estado de seguridad
        sharedViewModel.getSeguridadActivada().observe(getViewLifecycleOwner(), activada -> {
            if (activada != null) {
                binding.btnSeguridad.setBackgroundColor(activada ? Color.RED : Color.GREEN);
                binding.btnSeguridad.setText(activada ? "desactivar" : "activar");
                binding.TextoSeguridad.setText(activada ? "Modo seguridad ACTIVADO" : "Sistema inactivo");
            }
        });
    }


    int temp=1;
    private void tempe(){


        if(temp==1){
            binding.btnTemperatura.setBackgroundColor(Color.GREEN);
            ((MainActivity) requireActivity()).enviarDatosBT("5:1");
            temp=2;
        }else{
            binding.btnTemperatura.setBackgroundColor(Color.RED);
            ((MainActivity) requireActivity()).enviarDatosBT("5:2");
            temp=1;
        }

    }

    private void setupButton() {
        binding.btnSeguridad.setOnClickListener(v -> toggleSeguridad());
    }

    private void toggleSeguridad() {
        boolean nuevaEstado = !(sharedViewModel.getSeguridadActivada().getValue() != null &&
                sharedViewModel.getSeguridadActivada().getValue());

        sharedViewModel.setSeguridadActivada(nuevaEstado);
        ((MainActivity) requireActivity()).enviarDatosBT("4:" + (nuevaEstado ? "1" : "2") + "\n");

        if (!nuevaEstado) {
            hideAlerta();
        }
    }

    private void showAlerta() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                binding.TextoSeguridad.setTextColor(Color.RED);
                binding.TextoSeguridad.setTextSize(18);
                binding.TextoSeguridad.setText("¡ALERTA! Movimiento detectado");

                // Efecto visual
                binding.getRoot().setBackgroundColor(Color.argb(30, 255, 0, 0));
                binding.getRoot().postDelayed(() ->
                        binding.getRoot().setBackgroundColor(Color.TRANSPARENT), 500);

                Toast.makeText(getContext(), "¡Movimiento detectado!", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void hideAlerta() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                binding.TextoSeguridad.setTextColor(Color.BLACK);
                binding.TextoSeguridad.setTextSize(14);
                if (sharedViewModel.getSeguridadActivada().getValue() != null &&
                        sharedViewModel.getSeguridadActivada().getValue()) {
                    binding.TextoSeguridad.setText("Modo seguridad ACTIVADO");
                } else {
                    binding.TextoSeguridad.setText("Sistema inactivo");
                }
            });
        }
    }




    @Override
    public void onResume() {
        super.onResume();
        sharedViewModel.setCurrentFragment(FRAGMENT_ID);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (sharedViewModel.getSeguridadActivada().getValue() != null &&
                sharedViewModel.getSeguridadActivada().getValue()) {
            toggleSeguridad(); // Desactiva al salir
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}