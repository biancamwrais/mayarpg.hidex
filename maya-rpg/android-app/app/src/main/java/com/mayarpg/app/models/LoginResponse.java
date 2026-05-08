package com.mayarpg.app.models;

public class LoginResponse {
    public String token;
    public Usuario usuario;

    public static class Usuario {
        public int id;
        public String nome;
        public String email;
        public String perfil;
        public Integer pacienteId;
    }
}
