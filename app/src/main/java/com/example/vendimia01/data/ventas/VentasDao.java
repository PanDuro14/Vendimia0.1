package com.example.vendimia01.data.ventas;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface VentasDao {
    @Insert
    void insert(Ventas ventas);

    @Query("SELECT * FROM Ventas")
    List<Ventas> getAll();

    @Query("DELETE FROM Ventas WHERE id = :id")
    void deleteById(int id);

    @Query("SELECT * FROM Ventas WHERE id = :id LIMIT 1")
    Ventas getById(int id);

    @Update
    void update(Ventas ventas);
}
