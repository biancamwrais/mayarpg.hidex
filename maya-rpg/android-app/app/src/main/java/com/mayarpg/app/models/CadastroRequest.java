package com.mayarpg.app.models;

import com.google.gson.annotations.SerializedName;

public class CadastroRequest {
    public String nome;
    public String email;
    public String senha;
    public String cpf;
    public String telefone;

    @SerializedName("dataNascimento")
    public String dataNascimento;

    @SerializedName("aceitouLgpd")
    public boolean aceitouLgpd;
}
