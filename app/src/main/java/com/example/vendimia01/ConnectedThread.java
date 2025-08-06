package com.example.vendimia01;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ConnectedThread extends Thread {
    private final BluetoothSocket bluetoothSocket;
    private final InputStream inputStream;
    private final OutputStream outputStream;
    private final Handler handler;
    private final int handlerState;

    private final StringBuilder recDataString = new StringBuilder();
    private static final String TAG = "ConnectedThread";

    public ConnectedThread(BluetoothSocket socket, Handler handler, int handlerState) {
        this.bluetoothSocket = socket;
        this.handler = handler;
        this.handlerState = handlerState;

        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        try {
            tmpIn = bluetoothSocket.getInputStream();
            tmpOut = bluetoothSocket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error al obtener streams", e);
        }

        inputStream = tmpIn;
        outputStream = tmpOut;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[256];  // Buffer para leer datos
        int bytes;

        while (true) {
            try {
                // Lee bytes desde el InputStream
                bytes = inputStream.read(buffer);
                if (bytes > 0) {
                    String readMessage = new String(buffer, 0, bytes);
                    recDataString.append(readMessage);

                    // Busca salto de línea que indica fin de mensaje
                    int endOfLineIndex = recDataString.indexOf("\n");
                    while (endOfLineIndex > 0) {
                        // Extrae el mensaje completo hasta el salto de línea
                        String dataInPrint = recDataString.substring(0, endOfLineIndex);
                        // Envía mensaje al handler para UI
                        handler.obtainMessage(handlerState, dataInPrint).sendToTarget();
                        // Borra lo ya procesado del buffer
                        recDataString.delete(0, endOfLineIndex + 1);

                        // Busca si quedan más mensajes en el buffer
                        endOfLineIndex = recDataString.indexOf("\n");
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "Error de lectura del inputStream", e);
                break;  // Sale del ciclo si hay error
            }
        }
    }

    // Método para enviar datos al dispositivo Bluetooth
    public void write(String input) {
        try {
            if (bluetoothSocket != null && bluetoothSocket.isConnected()){
                outputStream.write(input.getBytes());
            }
        } catch (IOException e) {
            Log.e(TAG, "Error al enviar datos", e);
        }
    }

    // Cierra la conexión Bluetooth
    public void cancel() {
        try {
            if (bluetoothSocket != null && bluetoothSocket.isConnected()){
                bluetoothSocket.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error al cerrar el socket", e);
        }
    }
}
