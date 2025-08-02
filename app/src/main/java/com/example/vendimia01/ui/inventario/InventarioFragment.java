package com.example.vendimia01.ui.inventario;

import android.app.AlertDialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.room.Room;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.vendimia01.MainActivity;
import com.example.vendimia01.R;
import com.example.vendimia01.SharedViewModel;
import com.example.vendimia01.data.AppDataBase;
import com.example.vendimia01.data.inventario.Inventario;
import com.example.vendimia01.data.inventario.InventarioDao;
import com.example.vendimia01.databinding.DialogInventarioFormBinding;
import com.example.vendimia01.databinding.FragmentInventarioBinding;
import com.example.vendimia01.utils.TableActionListener;
import com.example.vendimia01.utils.TableDynamic;

import java.util.ArrayList;

public class InventarioFragment extends Fragment implements TableActionListener {

    private FragmentInventarioBinding binding;
    private DialogInventarioFormBinding dialogBinding;
    private String[] header = {"ID", "Producto", "Cantidad", "Precio"};

    private AppDataBase db;
    private InventarioDao inventarioDao;
    private TableDynamic tableDynamic;
    private SharedViewModel sharedViewModel;

    private Integer currentFragment = 3;
    private boolean isMessageSent = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentInventarioBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        db = Room.databaseBuilder(requireContext(), AppDataBase.class, "vendimia-db")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();

        inventarioDao = db.inventarioDao();

        tableDynamic = new TableDynamic(binding.tableInventario, requireContext(), this, true, true);
        tableDynamic.addHeader(header);
        loadTableData();

        binding.addInventario.setOnClickListener(v -> showAddDialog());

        // Inicializar ViewModel para ESP32
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        sharedViewModel.setCurrentFragment(currentFragment);

        sharedViewModel.getDataIn().observe(getViewLifecycleOwner(), data -> {
            Boolean isConnected = sharedViewModel.getIsConnected().getValue();
            if (Boolean.TRUE.equals(isConnected) && !isMessageSent) {
                enviarMensaje(sharedViewModel.getCurrentFragment().getValue().toString());
                isMessageSent = true;
            } else {
                isMessageSent = false;
            }
        });

        return view;
    }

    private void enviarMensaje(String mensaje) {
        mensaje = mensaje.trim();
        if (!mensaje.isEmpty()) {
            ((MainActivity) requireActivity()).enviarDatosBT(mensaje);
        }
    }

    private void loadTableData() {
        ArrayList<String[]> data = new ArrayList<>();
        for (Inventario item : inventarioDao.getAll()) {
            data.add(new String[]{
                    String.valueOf(item.id),
                    item.producto,
                    String.valueOf(item.cantidad),
                    String.valueOf(item.precio)
            });
        }
        tableDynamic.addData(data);
    }

    public void showAddDialog() {
        dialogBinding = DialogInventarioFormBinding.inflate(LayoutInflater.from(requireContext()));

        dialogBinding.txtProducto.setText("");
        dialogBinding.txtCantidad.setText("");
        dialogBinding.txtPrecio.setText("");

        new AlertDialog.Builder(requireContext())
                .setTitle("Agregar Producto")
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String producto = dialogBinding.txtProducto.getText().toString().trim();
                    String cantidadStr = dialogBinding.txtCantidad.getText().toString().trim();
                    String precioStr = dialogBinding.txtPrecio.getText().toString().trim();

                    if (!producto.isEmpty() && !cantidadStr.isEmpty() && !precioStr.isEmpty()) {
                        try {
                            int cantidad = Integer.parseInt(cantidadStr);
                            float precio = Float.parseFloat(precioStr);

                            Inventario nuevo = new Inventario();
                            nuevo.producto = producto;
                            nuevo.cantidad = cantidad;
                            nuevo.precio = precio;

                            inventarioDao.insert(nuevo);
                            loadTableData();
                        } catch (NumberFormatException e) {
                            Toast.makeText(requireContext(), "Cantidad y Precio deben ser números válidos.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(requireContext(), "Completa todos los campos.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showEditDialog(int id) {
        Inventario inventario = inventarioDao.getById(id);
        if (inventario == null) return;

        dialogBinding = DialogInventarioFormBinding.inflate(LayoutInflater.from(requireContext()));

        dialogBinding.txtProducto.setText(inventario.producto);
        dialogBinding.txtCantidad.setText(String.valueOf(inventario.cantidad));
        dialogBinding.txtPrecio.setText(String.valueOf(inventario.precio));

        new AlertDialog.Builder(requireContext())
                .setTitle("Editar Producto")
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String producto = dialogBinding.txtProducto.getText().toString().trim();
                    String cantidadStr = dialogBinding.txtCantidad.getText().toString().trim();
                    String precioStr = dialogBinding.txtPrecio.getText().toString().trim();

                    if (!producto.isEmpty() && !cantidadStr.isEmpty() && !precioStr.isEmpty()) {
                        try {
                            int cantidad = Integer.parseInt(cantidadStr);
                            float precio = Float.parseFloat(precioStr);

                            inventario.producto = producto;
                            inventario.cantidad = cantidad;
                            inventario.precio = precio;

                            inventarioDao.update(inventario);
                            loadTableData();
                        } catch (NumberFormatException e) {
                            Toast.makeText(requireContext(), "Cantidad y Precio deben ser números válidos.", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onEdit(int id) {
        showEditDialog(id);
    }

    @Override
    public void onDelete(int id) {
        inventarioDao.deleteById(id);
        loadTableData();
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
        loadTableData();
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
