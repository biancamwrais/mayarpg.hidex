package com.mayarpg.app.models;

import java.util.List;

public class HistoricoResponse {
    public Resumo resumo;
    public List<PontoGrafico> grafico;
    public List<Execucao> execucoes;

    public static class Resumo {
        public Integer total;
        public Double dor_media;
    }

    public static class PontoGrafico {
        public String dia;
        public Double dor_media;
    }

    public static class Execucao {
        public Integer id;
        public Integer nivel_dor;
        public String observacoes;
        public String data_execucao;
        public String titulo;
        public String categoria;
    }
}
