package com.mayarpg.app.models;

public class Notificacao {
    public Integer id;
    public String tipo;
    public String titulo;
    public String mensagem;
    public Integer lida;
    public String criada_em;

    public boolean isLida() {
        return lida != null && lida == 1;
    }
}