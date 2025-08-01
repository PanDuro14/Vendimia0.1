package com.example.vendimia01.ui.clientes;

import androidx.lifecycle.ViewModelProvider;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.room.Room;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.example.vendimia01.MainActivity;
import com.example.vendimia01.R;
import com.example.vendimia01.SharedViewModel;
import com.example.vendimia01.data.AppDataBase;
import com.example.vendimia01.data.cliente.Cliente;
import com.example.vendimia01.data.cliente.ClienteDao;
import com.example.vendimia01.databinding.FragmentClientesBinding;
import com.example.vendimia01.utils.TableActionListener;
import com.example.vendimia01.utils.TableDynamic;
import com.example.vendimia01.databinding.DialogClientFormBinding;

import java.util.ArrayList;

public class clientesFragment extends Fragment implements TableActionListener {
    private FragmentClientesBinding binding;
    private DialogClientFormBinding dialogBinding;

    // Instanciar el header
    private String[] header = {"ID", "Cliente", "Cuenta", "Tipo"};
    private AppDataBase db;
    private ClienteDao clienteDao;
    private TableDynamic tableDynamic;
    private SharedViewModel sharedViewModel;

    // Funciones para el esp32
    Integer currentFragment = 2;
    private boolean isMessageSent = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentClientesBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        db = Room.databaseBuilder(requireContext(), AppDataBase.class, "vendimia-db")
                .allowMainThreadQueries()
                .build();
        clienteDao = db.clienteDao();

        tableDynamic = new TableDynamic(binding.table, requireContext(), this, true, true);
        tableDynamic.addHeader(header);
        loadTableData(tableDynamic);

        binding.addIndex.setOnClickListener(v -> showAddDialog());

        // Inicializar el viewModel
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        sharedViewModel.setCurrentFragment(currentFragment);

        sharedViewModel.getDataIn().observe(getViewLifecycleOwner(), data -> {
            Boolean isConnected = sharedViewModel.getIsConnected().getValue();

            // Si está conectado a un dispositivo
            if (Boolean.TRUE.equals(isConnected)) {
                // Enviar el fragmento al ESP32
                if (!isMessageSent) {
                    enviarMensaje(sharedViewModel.getCurrentFragment().getValue().toString());
                    isMessageSent = true;
                }
            } else {
                isMessageSent = false;
            }
        });
        return view;
    }

    private void enviarMensaje(String mensaje){
        mensaje = mensaje.trim();
        if(!mensaje.isEmpty()){
            ((MainActivity) requireActivity()).enviarDatosBT(mensaje);
        }
    }

    @Override
    public void onEdit(int id) {
        showEditDialog(id);
    }

    @Override
    public void onDelete(int id) {
        clienteDao.deleteById(id);
        loadTableData(tableDynamic);
    }

    private void loadTableData(TableDynamic tableDynamic){
        ArrayList<String[]> data = new ArrayList<>();
        for (Cliente cliente: clienteDao.getAll()) {
            data.add(new String[] {
                String.valueOf(cliente.id),
                cliente.nombre,
                String.valueOf(cliente.cuenta),
                cliente.tipo
            });
        }
        tableDynamic.addData(data);
    }

    private void setTipoOptions(AutoCompleteTextView autoCompleteTextView) {
        dialogBinding.autoTipo.setDropDownWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        String[] opcionesTipo = {"Acreedor", "Deudor"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, opcionesTipo);
        autoCompleteTextView.setAdapter(adapter);
    }


    public void showAddDialog(){
        dialogBinding = DialogClientFormBinding.inflate(LayoutInflater.from(requireContext()));
        // Set opciones en AutoCompleteTextView
        setTipoOptions(dialogBinding.autoTipo);

        dialogBinding.txtCliente.setText("");
        dialogBinding.txtCuenta.setText("");
        dialogBinding.autoTipo.setText("", false);

        new AlertDialog.Builder(requireContext())
                .setTitle("Agregar Cliente")
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String nombre = dialogBinding.txtCliente.getText().toString().trim();
                    String cuentaStr = dialogBinding.txtCuenta.getText().toString().trim();
                    String tipo = dialogBinding.autoTipo.getText().toString().trim();

                    if (!nombre.isEmpty() && !cuentaStr.isEmpty() && !tipo.isEmpty()) {
                        try {
                            float cuenta = Float.parseFloat(cuentaStr);

                            Cliente nuevoCliente = new Cliente();
                            nuevoCliente.nombre = nombre;
                            nuevoCliente.cuenta = cuenta;
                            nuevoCliente.tipo = tipo;

                            clienteDao.insert(nuevoCliente);
                            loadTableData(tableDynamic);
                        } catch (NumberFormatException e) {
                            new AlertDialog.Builder(requireContext())
                                    .setTitle("Error")
                                    .setMessage("Cuenta debe ser un número válido.")
                                    .setPositiveButton("OK", null)
                                    .show();
                        }
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showEditDialog(int id){
        Cliente cliente = clienteDao.getById(id);
        if (cliente == null) return;

        dialogBinding = DialogClientFormBinding.inflate(LayoutInflater.from(requireContext()));

        setTipoOptions(dialogBinding.autoTipo);
        dialogBinding.txtCliente.setText(cliente.nombre);
        dialogBinding.txtCuenta.setText(String.valueOf(cliente.cuenta));
        dialogBinding.autoTipo.setText(cliente.tipo, false);

        new AlertDialog.Builder(requireContext())
                .setTitle("Editar Cliente")
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String nuevoNombre = dialogBinding.txtCliente.getText().toString().trim();
                    String nuevaCuentaStr = dialogBinding.txtCuenta.getText().toString().trim();
                    String nuevoTipo = dialogBinding.autoTipo.getText().toString().trim();

                    if (!nuevoNombre.isEmpty() && !nuevaCuentaStr.isEmpty() && !nuevoTipo.isEmpty()) {
                        try {
                            float nuevaCuenta = Float.parseFloat(nuevaCuentaStr); // String -> float

                            cliente.nombre = nuevoNombre;
                            cliente.cuenta = nuevaCuenta;
                            cliente.tipo = nuevoTipo;

                            clienteDao.update(cliente);
                            loadTableData(tableDynamic);
                        } catch (NumberFormatException e) {
                            // Manejar error: el usuario no ingresó un número válido
                            new AlertDialog.Builder(requireContext())
                                    .setTitle("Error")
                                    .setMessage("Cuenta debe ser un número válido.")
                                    .setPositiveButton("OK", null)
                                    .show();
                        }
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onPause() {
        super.onPause();
        enviarMensaje(sharedViewModel.getCurrentFragment().getValue().toString());
        sharedViewModel.setCurrentFragment(0);
        isMessageSent = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTableData(tableDynamic);
        sharedViewModel.setCurrentFragment(currentFragment);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        enviarMensaje(sharedViewModel.getCurrentFragment().getValue().toString());
        sharedViewModel.setCurrentFragment(0);
        isMessageSent = false;
        binding = null;

    }
}