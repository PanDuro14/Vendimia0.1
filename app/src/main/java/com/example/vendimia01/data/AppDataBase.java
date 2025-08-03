package com.example.vendimia01.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.vendimia01.data.cliente.Cliente;
import com.example.vendimia01.data.cliente.ClienteDao;
import com.example.vendimia01.data.inventario.Inventario;
import com.example.vendimia01.data.inventario.InventarioDao;
import com.example.vendimia01.data.ventas.Converters;
import com.example.vendimia01.data.ventas.Ventas;
import com.example.vendimia01.data.ventas.VentasDao;

@Database(entities = {Cliente.class, Inventario.class, Ventas.class}, version = 3)
@TypeConverters(Converters.class)
public abstract class AppDataBase extends RoomDatabase {
    public abstract ClienteDao clienteDao();
    public abstract InventarioDao inventarioDao();
    public abstract VentasDao ventasDao();
}
