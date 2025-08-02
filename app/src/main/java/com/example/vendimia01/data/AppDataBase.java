package com.example.vendimia01.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.vendimia01.data.cliente.Cliente;
import com.example.vendimia01.data.cliente.ClienteDao;
import com.example.vendimia01.data.inventario.Inventario;
import com.example.vendimia01.data.inventario.InventarioDao;

@Database(entities = {Cliente.class, Inventario.class}, version = 3)
public abstract class AppDataBase extends RoomDatabase {
    public abstract ClienteDao clienteDao();
    public abstract InventarioDao inventarioDao();
}
