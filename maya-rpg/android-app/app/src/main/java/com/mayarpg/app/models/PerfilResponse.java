package com.mayarpg.app.models;

public class PerfilResponse {
    public Paciente paciente;
    public Clinica clinica;

    public static class Paciente {
        public Integer id;
        public String nome;
        public String email;
        public String cpf;
        public String telefone;
        public String data_nascimento;
        public String endereco;
        public String cidade;
        public String estado;
        public String cep;
    }

    public static class Clinica {
        public String nome;
        public String endereco;
        public String cidade;
        public String cep;
        public String telefone;
        public String horario;
    }
}
