package com.example.vendimia01.data.inventario;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "inventario")
public class Inventario {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "producto")
    public String producto;

    @ColumnInfo(name = "cantidad")
    public int cantidad;

    @ColumnInfo(name = "precio")
    public float precio;
}
