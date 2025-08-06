package com.example.vendimia01;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.vendimia01.databinding.ActivityMainBinding;

import java.io.IOException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;
    private BluetoothSocket bluetoothSocket;
    private boolean pauseDueToBluetooth = false;

    // SharedViewModel y Handler estáticos para acceso desde el Handler
    private static SharedViewModel sharedViewModel;
    private static Handler bluetoothIn;
    private static int handlerState = 0;

    private ConnectedThread MyConnectionBT = null;

    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Setter para que el Handler pueda acceder al SharedViewModel
    public static void setSharedViewModel(SharedViewModel svm){
        sharedViewModel = svm;
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);
        setSharedViewModel(sharedViewModel);  // importante para el Handler

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null){
            Toast.makeText(this, "Bluetooth no disponible", Toast.LENGTH_SHORT).show();
            finish();
        }

        if(!bluetoothAdapter.isEnabled()){
            requestBluetoothEnable();
        }

        registerReceiver(btReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);
        binding.appBarMain.fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .setAnchorView(R.id.fab).show());
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_list_devices, R.id.nav_clientes, R.id.nav_inventario, R.id.nav_ventas,R.id.nav_seguridad)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Handler estático que recibe datos Bluetooth
        bluetoothIn = new Handler(){
            public void handleMessage(@NonNull android.os.Message msg){
                if(msg.what == handlerState){
                    String recivedData = (String) msg.obj;
                    Log.d("Datos", "handleMessage: " + recivedData);
                    if(sharedViewModel != null){
                        sharedViewModel.setDataIn(recivedData);
                    }
                }
            }
        };

        // Manejar la conexión con el dispositivo
        sharedViewModel.getDeviceAddress().observe(this, new Observer<String>() {
            @SuppressLint("MissingPermission")
            @Override
            public void onChanged(String address) {
                if(address == null || address.equals("No hay dispositivo") || address.isEmpty()){
                    Toast.makeText(getBaseContext(), "No se ha seleccionado un dispositivo", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (bluetoothSocket != null && bluetoothSocket.isConnected()){
                    if (bluetoothDevice != null && bluetoothDevice.getAddress().equals(address)){
                        Toast.makeText(getBaseContext(), "Ya estás conectado a este dispositivo", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        bluetoothSocket.close();
                        sharedViewModel.setIsConnected(false);
                        Toast.makeText(getBaseContext(), "Cambiando de dispositivo...", Toast.LENGTH_SHORT).show();
                    } catch (IOException e){
                        Toast.makeText(getBaseContext(), "Error al cerrar la conexión previa", Toast.LENGTH_SHORT).show();
                    }
                }

                bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
                try {
                    bluetoothSocket = createBluetoothSocket(bluetoothDevice);
                    bluetoothSocket.connect();
                    MyConnectionBT = new ConnectedThread(bluetoothSocket, bluetoothIn, handlerState);
                    MyConnectionBT.start();
                    sharedViewModel.setIsConnected(true);
                } catch (IOException e){
                    Toast.makeText(getBaseContext(), "Error al conectar el socket", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private void requestBluetoothEnable(){
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivity(intent);
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private BluetoothSocket createBluetoothSocket(@NonNull BluetoothDevice device) throws IOException{
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    private final BroadcastReceiver btReceiver = new BroadcastReceiver() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
            if (state == BluetoothAdapter.STATE_OFF ){
                pauseDueToBluetooth = true;
                onPause();
                requestBluetoothEnable();
            } else if (state == BluetoothAdapter.STATE_ON && pauseDueToBluetooth){
                pauseDueToBluetooth = false;
                onResume();
            }
        }
    };

    private void closeBluetootConnection(){
        try {
            if(bluetoothSocket != null && bluetoothSocket.isConnected()){
                bluetoothSocket.close();
                sharedViewModel.setIsConnected(false);
                Toast.makeText(this, "Conexión con bluetooth cerrada", Toast.LENGTH_LONG).show();
            }
        } catch (IOException e){
            Toast.makeText(this, "Error al cerrar la conexión con Bluetooth", Toast.LENGTH_SHORT).show();
        }
    }

    // Enviar información al fragmento
    public void enviarDatosBT(String mensaje){
        if (MyConnectionBT != null && bluetoothSocket != null && bluetoothSocket.isConnected()){
            MyConnectionBT.write(mensaje);
            sharedViewModel.setDataOut(mensaje);
        } else {
            Toast.makeText(this, "Bluetooth no conectado ", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Toast.makeText(this, "Bluetooth Apagado", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Toast.makeText(this, "Bluetooth Reactivado", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeBluetootConnection();
        unregisterReceiver(btReceiver);
    }

}
