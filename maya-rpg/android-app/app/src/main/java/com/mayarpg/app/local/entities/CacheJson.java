package com.mayarpg.app.local.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Cache generico para guardar respostas de API em formato JSON.
 *
 * Usamos uma chave string (ex: "exercicios_paciente_1", "perfil_paciente_1")
 * para diferenciar os tipos de dados, e guardamos o JSON inteiro como texto.
 *
 * Isso e bem mais simples que criar uma tabela pra cada tipo de dado e
 * funciona muito bem como cache (que e descartavel).
 */
@Entity(tableName = "cache_json")
public class CacheJson {

    @PrimaryKey
    @NonNull
    public String chave = "";

    /** JSON da resposta da API. */
    @NonNull
    public String json = "";

    /** Timestamp em ms de quando foi salvo. */
    public long atualizadoEm;
}
