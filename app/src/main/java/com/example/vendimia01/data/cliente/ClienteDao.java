package com.example.vendimia01.data.cliente;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface ClienteDao {
    @Insert
    void insert(Cliente cliente);

    @Query("SELECT * FROM Cliente")
    List<Cliente> getAll();

    @Query("DELETE FROM Cliente WHERE id = :id")
    void deleteById(int id);

    @Query("SELECT * FROM Cliente WHERE id = :id LIMIT 1")
    Cliente getById(int id);

    @Update
    void update(Cliente cliente);
}
