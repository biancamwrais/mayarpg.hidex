package com.mayarpg.app.models;

import java.util.List;

public class PagamentosResponse {
    public Double total;
    public List<Pagamento> pagamentos;

    public static class Pagamento {
        public Integer id;
        public String descricao;
        public Double valor;
        public String forma_pagamento;
        public String status;
        public String data_pagamento;
    }
}
