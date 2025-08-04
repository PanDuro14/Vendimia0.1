package com.example.vendimia01.data.ventas;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.List;

@Entity
public class Ventas {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public List<String> productos;
    public float precioTotal;
    public String fecha;
    public String metodoPago;
    public String cantidad;
}
