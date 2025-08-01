package com.example.vendimia01.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.vendimia01.data.cliente.Cliente;
import com.example.vendimia01.data.cliente.ClienteDao;


// Inicializar las bases de datos existentes
// Ejemplo para agregar una clase
// @Database(entities = {DBName.class}, version = 1)
@Database(entities = {Cliente.class}, version = 1)
public abstract class AppDataBase extends RoomDatabase {
    // Ejemplo para instanciar una clase (db)
    //public abstract DBNameDao dbNameDao();
    public abstract ClienteDao clienteDao();
}
