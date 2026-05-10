package com.mayarpg.app.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mayarpg.app.local.entities.DashboardCache;

/**
 * DAO (Data Access Object) do cache de dashboard.
 * O Room gera automaticamente a implementacao dessas funcoes em tempo de build.
 */
@Dao
public interface DashboardDao {

    /** Busca o cache do paciente. Retorna null se ainda nao existe. */
    @Query("SELECT * FROM dashboard_cache WHERE pacienteId = :pacienteId LIMIT 1")
    DashboardCache buscar(int pacienteId);

    /** Insere ou substitui o cache (REPLACE = upsert). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void salvar(DashboardCache cache);

    /** Limpa todo o cache (usado quando o usuario faz logout). */
    @Query("DELETE FROM dashboard_cache")
    void limpar();
}
