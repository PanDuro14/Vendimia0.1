package com.example.vendimia01;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<String> deviceAddress = new MutableLiveData<>();
    private final MutableLiveData<String> dataIn = new MutableLiveData<>();
    private final MutableLiveData<String> dataOut = new MutableLiveData<>();
    private final MutableLiveData<Integer> currentFragment = new MutableLiveData<>();
    private final MutableLiveData<Integer> temp = new MutableLiveData<>();
    private final MutableLiveData<Integer> hum = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isConnected = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> movimientoDetectado = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> seguridadActivada = new MutableLiveData<>(false);

    public SharedViewModel() {
        deviceAddress.setValue("No hay dispositivo");
        dataIn.setValue(null);
        dataOut.setValue("No hay datos transmitidos");
        currentFragment.setValue(0);
        temp.setValue(0);
        hum.setValue(0);
    }

    // GETTERS
    public LiveData<String> getDeviceAddress() {
        return deviceAddress;
    }
    public LiveData<String> getDataIn() {
        return dataIn;
    }
    public LiveData<String> getDataOut() {
        return dataOut;
    }
    public LiveData<Integer> getTemp() {
        return temp;
    }
    public LiveData<Integer> getHum() {
        return hum;
    }
    public LiveData<Integer> getCurrentFragment() {
        return currentFragment;
    }
    public LiveData<Boolean> getIsConnected() {
        return isConnected;
    }
    public LiveData<Boolean> getMovimientoDetectado() {
        return movimientoDetectado;
    }
    public LiveData<Boolean> getSeguridadActivada() {
        return seguridadActivada;
    }

    // SETTERS
    public void setDeviceAddress(String address) {
        deviceAddress.setValue(address);
    }
    public void setTemp(int temperature) {
        temp.setValue(temperature);
    }
    public void setHum(int humidity) {
        hum.setValue(humidity);
    }
    public void setMovimientoDetectado(boolean estado) {
        movimientoDetectado.setValue(estado);
    }
    public void setSeguridadActivada(boolean estado) {
        seguridadActivada.setValue(estado);
    }

    public void setDataIn(String input) {
        if (input == null || input.isEmpty()) return;

        Log.d("BLUETOOTH_IN", "Dato recibido: " + input);

        // Procesar alerta de movimiento
        if (input.startsWith("ALERTA:")) {
            String tipoAlerta = input.substring(7).trim();
            if (tipoAlerta.equals("MOVIMIENTO") || tipoAlerta.contains("vendimia")) {
                dataIn.setValue("¡ALERTA! Movimiento detectado");
                movimientoDetectado.setValue(true);
                return;
            }
        }

        // Resetear estado de alerta
        if (input.contains("Modo Seguro:DESACTIVADO") || input.contains("DESACTIVADO")) {
            movimientoDetectado.setValue(false);
            dataIn.setValue("Modo seguridad desactivado");
        }

        // Procesamiento para humedad/temperatura
        if (input.contains("Humedad:") && input.contains("Temperatura:")) {
            procesarDatosAmbientales(input);
        } else {
            // Otros datos no relacionados
            dataIn.setValue(input);
        }
    }

    private void procesarDatosAmbientales(String data) {
        String humedad = extractValue(data, "Humedad:");
        String temperatura = extractValue(data, "Temperatura:");

        if (humedad != null && temperatura != null) {
            try {
                setHum((int)Float.parseFloat(humedad.replace("%", "")));
                setTemp((int)Float.parseFloat(temperatura.replace("C", "")));
                dataIn.setValue("H: " + humedad + "% T: " + temperatura + "°C");
            } catch (NumberFormatException e) {
                Log.e("SharedViewModel", "Error al procesar datos", e);
            }
        }
    }

    public void setDataOut(String output) {
        Log.d("BLUETOOTH_OUT", "Enviando dato: " + output);
        dataOut.setValue(output);
    }

    private String extractValue(String data, String label) {
        int startIndex = data.indexOf(label);
        if (startIndex == -1) return null;

        startIndex += label.length();
        while (startIndex < data.length() && Character.isWhitespace(data.charAt(startIndex))) {
            startIndex++;
        }

        int endIndex = data.indexOf(" ", startIndex);
        if (endIndex == -1) endIndex = data.length();

        return data.substring(startIndex, endIndex).trim();
    }

    public void setCurrentFragment(int currentFragmentValue) {
        this.currentFragment.setValue(currentFragmentValue);
    }

    public void setIsConnected(boolean status) {
        isConnected.setValue(status);
    }
}