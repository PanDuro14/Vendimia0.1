package com.example.vendimia01.ui.venta;

import androidx.lifecycle.ViewModelProvider;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.room.Room;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.vendimia01.R;
import com.example.vendimia01.SharedViewModel;
import com.example.vendimia01.data.AppDataBase;
import com.example.vendimia01.data.inventario.Inventario;
import com.example.vendimia01.data.inventario.InventarioDao;
import com.example.vendimia01.data.ventas.Converters;
import com.example.vendimia01.data.ventas.Ventas;
import com.example.vendimia01.data.ventas.VentasDao;
import com.example.vendimia01.utils.TableActionListener;

import com.example.vendimia01.databinding.FragmentVentasBinding;
import com.example.vendimia01.databinding.DialogVentasFormBinding;
import com.example.vendimia01.utils.TableDynamic;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class VentasFragment extends Fragment implements TableActionListener {

    private FragmentVentasBinding binding;
    private  DialogVentasFormBinding dialogBinding;
    private AppDataBase db;
    private InventarioDao inventarioDao;
    private VentasDao ventasDao;
    private TableDynamic tableDynamic;
    private SharedViewModel sharedViewModel;

    private  String[] header = {"ID", "Producto", "Cantidad", "Total" , "Fecha", "Metodo de pago"};
    private Integer currentFragment = 3;
    private boolean isMessageSent = false;
    private BarChart grafica;
    private PieChart graficaVentas;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentVentasBinding.inflate(inflater,container,false);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        View view = binding.getRoot();

        db = Room.databaseBuilder(requireContext(), AppDataBase.class, "vendimia-db")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();

        ventasDao =db.ventasDao();
        inventarioDao =db.inventarioDao();
        tableDynamic = new TableDynamic(binding.tableVentas, requireContext(),this,false,true);
        tableDynamic.addHeader(header);


        grafica = binding.inventarioChart;
        graficaVentas = binding.ventasPieChart;
        loadTableData();
        setupInventarioChart();
        setupVentasChart();

        binding.btnVenta.setOnClickListener(v -> {
            int conteo = inventarioDao.totalprd();
            if(conteo > 0){
                showVentaDialog();
            }else {
                Toast.makeText(requireContext(),"No hay productos que vender, agrege uno al inventario", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }


    private void showVentaDialog(){
        dialogBinding = DialogVentasFormBinding.inflate(LayoutInflater.from(requireContext()));

        List<Inventario> inventarioList = inventarioDao.getAll();
        List<String> productos = new ArrayList<>();
        for (Inventario item : inventarioList){
            productos.add(item.producto);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, productos);
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        dialogBinding.spinnerProducto.setAdapter(adapter);
        dialogBinding.spinnerProducto.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                Inventario productoSel = inventarioList.get(position);
                dialogBinding.txtPrecioTotal.setText(String.format(Locale.US, "%.2f", productoSel.precio));

            }

            public void onNothingSelected(AdapterView<?> parent){}
        });

        dialogBinding.txtCantidad.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                try{
                    int cantidad = Integer.parseInt(charSequence.toString());
                    int posicion = dialogBinding.spinnerProducto.getSelectedItemPosition();
                    Inventario productoSel = inventarioList.get(posicion);
                    float precioTotal = productoSel.precio *cantidad;
                    dialogBinding.txtPrecioTotal.setText(String.format(Locale.US, "%.2f", precioTotal));

                }catch (NumberFormatException e){
                    dialogBinding.txtPrecioTotal.setText("0.00");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        new AlertDialog.Builder(requireContext())
                .setTitle("Registrar Venta")
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Guardar",(dialog, which) ->{
                    int posicion = dialogBinding.spinnerProducto.getSelectedItemPosition();
                    String cantidadStr = dialogBinding.txtCantidad.getText().toString().trim();
                    String metodoPago = dialogBinding.spinnerMetodoPago.getSelectedItem().toString();

                    if(cantidadStr.isEmpty()){
                        Toast.makeText(requireContext(),"Cantidad invalida", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try{
                        int catidad = Integer.parseInt(cantidadStr);
                        Inventario prodVendido = inventarioList.get(posicion);
                        if(catidad > prodVendido.cantidad){
                            Toast.makeText(requireContext(),"Insuficiente stock", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        //Venta
                        Ventas nuevaVenta = new Ventas();
                        nuevaVenta.productos = Collections.singletonList(prodVendido.producto);

                        // ... (El resto de la asignación de datos a nuevaVenta)
                        nuevaVenta.cantidad = String.valueOf(catidad);
                        nuevaVenta.precioTotal = prodVendido.precio * catidad;
                        nuevaVenta.fecha = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                        nuevaVenta.metodoPago = metodoPago;

                        ventasDao.insert(nuevaVenta);

                        //Actualizar imventario
                        prodVendido.cantidad -= catidad;
                        inventarioDao.update(prodVendido);



                        Toast.makeText(requireContext(),"Venta realizada",Toast.LENGTH_SHORT).show();
                        loadTableData();
                       setupInventarioChart();
                        setupVentasChart();
                    }catch (NumberFormatException e){
                        Toast.makeText(requireContext(),"Cantidad invalida", Toast.LENGTH_SHORT).show();
                    }
                }).setNegativeButton("Cancelar",null)
                .show();
    }

   private void setupInventarioChart() {
        List<BarEntry> entries = new ArrayList<>();
        List<Inventario> inventarioList = inventarioDao.getAll();
        ArrayList<String> labels = new ArrayList<>();

        for (int i = 0; i < inventarioList.size(); i++) {
            Inventario item = inventarioList.get(i);
            entries.add(new BarEntry(i, item.cantidad));
            labels.add(item.producto);
        }

        BarDataSet dataSet = new BarDataSet(entries, "Inventario Actual");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        grafica.setData(barData);

        // -----------------------------------------------------------
        // --- Configuracion de la grafica ---
        // -----------------------------------------------------------
        XAxis xAxis = grafica.getXAxis();

        // poner las etiquetas al fondo
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        // 2. Establecer el formateador de valores para usar los nombres de los productos
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setDrawLabels(true);

        // tamaño del texto para las etiquetas
        xAxis.setTextSize(10f);

        //  color del texto para las etiquetas
        xAxis.setTextColor(Color.WHITE);

        // rotacion de los labels de las barras
        xAxis.setLabelRotationAngle(-45);

        // 7. Configurar la granularidad y el conteo de etiquetas para evitar superposiciones
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setLabelCount(labels.size());

        // -----------------------------------------------------------
        // --- Otras personalizaciones de la gráfica ---
        // -----------------------------------------------------------

        grafica.getAxisRight().setEnabled(false); // Deshabilita el eje Y derecho
        grafica.getAxisLeft().setTextColor(Color.WHITE); // Cambia el color del texto del eje Y izquierdo

        grafica.getLegend().setEnabled(true);
        grafica.getLegend().setTextColor(Color.WHITE);

        // actualizar la gráfica para aplicar todos los cambios
        grafica.invalidate();
    }


    private void setupVentasChart() {
        List<PieEntry> entries = new ArrayList<>();
        List<VentasDao.VentaTotalPorProducto> ventasTotales = ventasDao.getVentasTotalesPorProducto();

        for (VentasDao.VentaTotalPorProducto item : ventasTotales) {

            entries.add(new PieEntry(item.totalCantidad, item.nombreProducto));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Ventas Totales por Producto");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(15f);

        PieData pieData = new PieData(dataSet);
        graficaVentas.setData(pieData);


        //Leyenda
        Legend legend = graficaVentas.getLegend();
        legend.setEnabled(true);
        legend.setTextColor(Color.WHITE);

        // Personalizar la gráfica
        graficaVentas.getDescription().setText("Ventas totales por producto");
        graficaVentas.getDescription().setTextColor(Color.WHITE);
        graficaVentas.animateY(1000);
        graficaVentas.invalidate(); // Refrescar gráfica
    }


    private void loadTableData() {
        ArrayList<String[]> data = new ArrayList<>();
        for (Ventas item : ventasDao.getAll()) {
             data.add(new String[]{
                    String.valueOf(item.id),
                    String.valueOf(item.productos),
                    String.valueOf(item.cantidad),
                    String.valueOf(item.precioTotal),
                    item.fecha,
                    item.metodoPago
            });
        }
        tableDynamic.addData(data);
    }
    @Override
    public void onResume() {
        super.onResume();
        loadTableData();
        setupInventarioChart(); // Refrescar gráfica en onResume
        setupVentasChart();
        sharedViewModel.setCurrentFragment(currentFragment);
    }


    @Override
    public void onEdit(int id) {

    }

    @Override
    public void onDelete(int id) {
        ventasDao.deleteById(id);
        loadTableData();
        setupVentasChart();

    }
}