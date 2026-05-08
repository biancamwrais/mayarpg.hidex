package com.mayarpg.app.models;

public class Servico {
    public Integer id;
    public String nome;
    public String descricao;
    public Integer duracao_min;
    public Double valor;

    @Override
    public String toString() {
        return nome;
    }
}
