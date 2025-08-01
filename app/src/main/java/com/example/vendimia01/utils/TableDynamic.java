package com.example.vendimia01.utils;

import android.content.Context;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.vendimia01.R;

import java.util.ArrayList;

public class TableDynamic {
    private TableLayout tableLayout;
    private Context context;
    private String[] header;
    private ArrayList<String[]> data;
    private TableActionListener listener;
    private boolean enableEdit;
    private boolean enableDelete;

    // Variables para construcción dinámica
    private TableRow tableRow;
    private TextView txtCell;
    private int indexC;
    private int indexR;

    public TableDynamic(TableLayout tableLayout, Context context, TableActionListener listener,
                        boolean enableEdit, boolean enableDelete) {
        this.tableLayout = tableLayout;
        this.context = context;
        this.listener = listener;
        this.enableEdit = enableEdit;
        this.enableDelete = enableDelete;
    }

    public void addHeader(String[] header) {
        this.header = header;
        createHeader();
    }

    public void addData(ArrayList<String[]> data) {
        this.data = data;
        clearTable(); // Evita duplicados al refrescar
        createHeader();
        createDataTable();
    }

    private void newRow() {
        tableRow = new TableRow(context);
    }

    private void newCell() {
        txtCell = new TextView(context);
        txtCell.setGravity(Gravity.CENTER);
        txtCell.setTextSize(16);
        txtCell.setPadding(10, 10, 10, 10);
    }

    private void createHeader() {
        indexC = 0;
        newRow();
        while (indexC < header.length) {
            newCell();
            txtCell.setText(header[indexC++]);
            tableRow.addView(txtCell, newTableRowParams());
        }

        if (enableEdit || enableDelete) {
            newCell();
            txtCell.setText("Acción");
            tableRow.addView(txtCell, newTableRowParams());
        }

        tableLayout.addView(tableRow);
    }

    private void createDataTable() {
        String info;
        for (indexR = 0; indexR < data.size(); indexR++) {
            newRow();
            String[] columns = data.get(indexR);

            for (indexC = 0; indexC < columns.length; indexC++) {
                newCell();
                info = (indexC < columns.length) ? columns[indexC] : "";
                txtCell.setText(info);
                tableRow.addView(txtCell, newTableRowParams());
            }

            if (enableEdit || enableDelete) {
                // Contenedor horizontal para botones
                LinearLayout actionLayout = new LinearLayout(context);
                actionLayout.setOrientation(LinearLayout.HORIZONTAL);
                actionLayout.setGravity(Gravity.CENTER);

                int id = Integer.parseInt(columns[0]); // ID siempre en la primera columna

                if (enableEdit) {
                    Button btnEdit = createButton("Editar", R.color.primary);
                    btnEdit.setOnClickListener(v -> {
                        if (listener != null) listener.onEdit(id);
                    });
                    actionLayout.addView(btnEdit);
                }

                if (enableDelete) {
                    Button btnDelete = createButton("Eliminar", R.color.danger);
                    btnDelete.setOnClickListener(v -> {
                        if (listener != null) listener.onDelete(id);
                    });
                    actionLayout.addView(btnDelete);
                }

                tableRow.addView(actionLayout, newTableRowParams());
            }

            tableLayout.addView(tableRow);
        }
    }

    private Button createButton(String text, int colorId) {
        Button btn = new Button(context);
        btn.setText(text);
        btn.setTextColor(ContextCompat.getColor(context, R.color.white));
        btn.setBackgroundColor(ContextCompat.getColor(context, colorId));
        return btn;
    }

    private TableRow.LayoutParams newTableRowParams() {
        TableRow.LayoutParams params = new TableRow.LayoutParams();
        params.setMargins(1, 1, 1, 1);
        params.weight = 1;
        return params;
    }

    private void clearTable() {
        tableLayout.removeAllViews();
    }
}
