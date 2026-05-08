package com.mayarpg.app.models;

import com.google.gson.annotations.SerializedName;

public class AgendamentoRequest {
    @SerializedName("servicoId")
    public int servicoId;

    public String data;
    public String horario;

    public AgendamentoRequest(int servicoId, String data, String horario) {
        this.servicoId = servicoId;
        this.data = data;
        this.horario = horario;
    }
}
