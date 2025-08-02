package com.example.vendimia01.data.inventario;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface InventarioDao {

    @Query("SELECT * FROM inventario")
    List<Inventario> getAll();

    @Query("SELECT * FROM inventario WHERE id = :id LIMIT 1")
    Inventario getById(int id);

    @Insert
    void insert(Inventario inventario);

    @Update
    void update(Inventario inventario);

    @Query("DELETE FROM inventario WHERE id = :id")
    void deleteById(int id);
}
