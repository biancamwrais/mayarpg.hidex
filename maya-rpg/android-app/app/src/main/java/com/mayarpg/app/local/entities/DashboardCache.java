package com.mayarpg.app.local.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Cache local do dashboard do paciente.
 *
 * Usamos uma unica linha por paciente (id = paciente_id), entao quando os dados
 * mudam a gente sobrescreve essa linha em vez de criar uma nova.
 */
@Entity(tableName = "dashboard_cache")
public class DashboardCache {

    @PrimaryKey
    public int pacienteId;

    public int totalExercicios;
    public int exerciciosHoje;

    /** Dor media dos ultimos 7 dias. Pode ser null se ainda nao tem execucoes. */
    public Double dorMedia7d;

    /** Lista de proximas consultas em formato JSON (mais simples que outra tabela). */
    @NonNull
    public String proximasConsultasJson = "[]";

    /** Quando esse cache foi atualizado pela ultima vez (timestamp em ms). */
    public long atualizadoEm;
}
