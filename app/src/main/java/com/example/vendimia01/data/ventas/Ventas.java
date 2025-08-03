package com.example.vendimia01.data.ventas;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Ventas {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String[] productos;
    public float precioTotal;
    public String fecha;
}
