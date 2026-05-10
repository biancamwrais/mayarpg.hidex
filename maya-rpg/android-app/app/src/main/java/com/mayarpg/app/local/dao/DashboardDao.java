package com.mayarpg.app.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mayarpg.app.local.entities.DashboardCache;

@Dao
public interface DashboardDao {


    @Query("SELECT * FROM dashboard_cache WHERE pacienteId = :pacienteId LIMIT 1")
    DashboardCache buscar(int pacienteId);


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void salvar(DashboardCache cache);


    @Query("DELETE FROM dashboard_cache")
    void limpar();
}
