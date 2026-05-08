package com.mayarpg.app.models;

import com.google.gson.annotations.SerializedName;

public class ExecucaoRequest {
    @SerializedName("prescricaoId")
    public int prescricaoId;

    @SerializedName("nivelDor")
    public int nivelDor;

    public String observacoes;

    public ExecucaoRequest(int prescricaoId, int nivelDor, String observacoes) {
        this.prescricaoId = prescricaoId;
        this.nivelDor = nivelDor;
        this.observacoes = observacoes;
    }
}
