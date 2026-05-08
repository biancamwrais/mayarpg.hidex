package com.mayarpg.app.models;

import java.util.List;

public class ExerciciosResponse {
    public List<Prescricao> prescricoes;
    public Estatisticas estatisticas;

    public static class Prescricao {
        public Integer prescricao_id;
        public String frequencia;
        public String orientacoes;
        public String data_inicio;
        public Integer exercicio_id;
        public String titulo;
        public String descricao;
        public String instrucoes;
        public Integer duracao_minutos;
        public String video_url;
        public String imagem_url;
        public String categoria;
    }

    public static class Estatisticas {
        public Integer ativos;
        public Integer esta_semana;
    }
}
