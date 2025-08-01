package com.example.vendimia01.data.cliente;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Cliente {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String nombre;
    public float cuenta;
    public String tipo; // Deudor o acredor;
}

