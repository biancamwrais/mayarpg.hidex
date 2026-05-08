package com.mayarpg.app.models;

import java.util.List;

public class DashboardResponse {
    public Resumo resumo;
    public List<Consulta> proximasConsultas;

    public static class Resumo {
        public Integer paciente_id;
        public String nome;
        public Integer total_exercicios;
        public Integer exercicios_hoje;
        public Double dor_media_7d;
    }

    public static class Consulta {
        public Integer id;
        public String data;
        public String horario;
        public String servico;
        public String paciente;
    }
}
