package com.mayarpg.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mayarpg.app.R;
import com.mayarpg.app.models.PagamentosResponse;
import com.mayarpg.app.utils.Formatters;

import java.util.List;
import java.util.Locale;

public class PagamentoAdapter extends RecyclerView.Adapter<PagamentoAdapter.VH> {

    private final List<PagamentosResponse.Pagamento> itens;

    public PagamentoAdapter(List<PagamentosResponse.Pagamento> itens) {
        this.itens = itens;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pagamento, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        PagamentosResponse.Pagamento p = itens.get(pos);
        h.tvData.setText(Formatters.formatDataBr(p.data_pagamento));
        h.tvDescricao.setText(p.descricao != null ? p.descricao : "");
        h.tvForma.setText(formaLegivel(p.forma_pagamento));
        h.tvStatus.setText(statusLegivel(p.status));
        h.tvValor.setText(p.valor != null
                ? "R$ " + String.format(Locale.US, "%.2f", p.valor).replace('.', ',')
                : "R$ 0,00");
    }

    @Override public int getItemCount() { return itens.size(); }

    private static String formaLegivel(String forma) {
        if (forma == null) return "—";
        switch (forma) {
            case "PIX":      return "PIX";
            case "CARTAO":   return "Cartão";
            case "DEBITO":   return "Débito";
            case "DINHEIRO": return "Dinheiro";
            case "BOLETO":   return "Boleto";
            default:         return forma;
        }
    }

    private static String statusLegivel(String s) {
        if (s == null) return "";
        switch (s) {
            case "PAGO":      return "Pago";
            case "PENDENTE":  return "Pendente";
            case "CANCELADO": return "Cancelado";
            default:          return s;
        }
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvData, tvDescricao, tvForma, tvValor, tvStatus;
        VH(View v) {
            super(v);
            tvData      = v.findViewById(R.id.tvData);
            tvDescricao = v.findViewById(R.id.tvDescricao);
            tvForma     = v.findViewById(R.id.tvForma);
            tvValor     = v.findViewById(R.id.tvValor);
            tvStatus    = v.findViewById(R.id.tvStatus);
        }
    }
}
