package com.example.vendimia01.ui.listDevices;

import androidx.annotation.RequiresPermission;
import androidx.lifecycle.ViewModelProvider;
import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.vendimia01.R;
import com.example.vendimia01.SharedViewModel;
import com.example.vendimia01.databinding.FragmentListDevicesBinding;

import java.util.HashMap;
import java.util.Map;

public class ListDevicesFragment extends Fragment {
    private FragmentListDevicesBinding binding;
    private ListDevicesViewModel mViewModel;
    private SharedViewModel sharedViewModel;
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    Integer currentFragment = 1;

    public static ListDevicesFragment newInstance() {
        return new ListDevicesFragment();
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentListDevicesBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        sharedViewModel.setCurrentFragment(currentFragment);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_list_item_1);
        Map<String, String> deviceMap = new HashMap<>();


        for (BluetoothDevice device: bluetoothAdapter.getBondedDevices()){
            String name = device.getName();
            String address = device.getAddress();

            adapter.add(name + "\n" + address);
            deviceMap.put(name, address);
        }
        binding.listItem.setAdapter(adapter);
        binding.listItem.setOnItemClickListener((((parent, view1, position, id) -> {
            String selectedDevice = (String) parent.getItemAtPosition(position);
            String deviceAddress = deviceMap.get(selectedDevice.split("\n")[0]);

            Toast.makeText(getContext(), "Dispositivo seleccionado: " + selectedDevice.split("\n")[0], Toast.LENGTH_SHORT).show();
            sharedViewModel.setDeviceAddress(deviceAddress);
        })));

        return view;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(ListDevicesViewModel.class);

    }
    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}