package com.mayarpg.app.local.dao;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mayarpg.app.local.entities.CacheJson;

@Dao
public interface CacheJsonDao {

    @Query("SELECT * FROM cache_json WHERE chave = :chave LIMIT 1")
    CacheJson buscar(@NonNull String chave);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void salvar(CacheJson cache);

    @Query("DELETE FROM cache_json WHERE chave = :chave")
    void apagar(@NonNull String chave);

    @Query("DELETE FROM cache_json")
    void limparTudo();
}
