package com.mayarpg.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.mayarpg.app.R;
import com.mayarpg.app.models.Notificacao;

import java.util.List;

public class NotificacaoAdapter extends RecyclerView.Adapter<NotificacaoAdapter.VH> {

    public interface OnAcao {
        void onVisualizar(Notificacao n);
        void onFechar(Notificacao n);
    }

    private final List<Notificacao> itens;
    private final OnAcao listener;

    public NotificacaoAdapter(List<Notificacao> itens, OnAcao listener) {
        this.itens = itens;
        this.listener = listener;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notificacao, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Notificacao n = itens.get(pos);
        h.tvTitulo.setText(n.titulo != null ? n.titulo : "Notificação");
        h.tvMensagem.setText(n.mensagem != null ? n.mensagem : "");
        h.tvTempo.setText(tempoRelativo(n.criada_em));

        // Icone conforme tipo
        int icone = R.drawable.ic_bell;
        if (n.tipo != null) {
            switch (n.tipo) {
                case "CONSULTA":           icone = R.drawable.ic_calendar; break;
                case "PROGRESSO":          icone = R.drawable.ic_trending; break;
                case "LEMBRETE_EXERCICIO": icone = R.drawable.ic_bell;     break;
                default:                   icone = R.drawable.ic_bell;     break;
            }
        }
        h.ivIcone.setImageResource(icone);

        h.btnVisualizar.setOnClickListener(v -> {
            if (listener != null) listener.onVisualizar(n);
        });
        h.btnFechar.setOnClickListener(v -> {
            if (listener != null) listener.onFechar(n);
        });
    }

    @Override public int getItemCount() { return itens.size(); }

    /** "5 min atras", "1 hora atras", etc */
    private static String tempoRelativo(String iso) {
        if (iso == null) return "";
        try {
            java.text.SimpleDateFormat f = new java.text.SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss", java.util.Locale.US);
            long t = f.parse(iso.replace("T"," ").substring(0, 19)).getTime();
            long diff = (System.currentTimeMillis() - t) / 1000;
            if (diff < 60)        return "agora";
            if (diff < 3600)      return (diff / 60) + " min atrás";
            if (diff < 86400)     return (diff / 3600) + " h atrás";
            return (diff / 86400) + " dias atrás";
        } catch (Exception e) {
            return iso;
        }
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvMensagem, tvTempo;
        ImageView ivIcone, btnFechar;
        MaterialButton btnVisualizar;
        VH(View v) {
            super(v);
            tvTitulo     = v.findViewById(R.id.tvTitulo);
            tvMensagem   = v.findViewById(R.id.tvMensagem);
            tvTempo      = v.findViewById(R.id.tvTempo);
            ivIcone      = v.findViewById(R.id.ivIcone);
            btnFechar    = v.findViewById(R.id.btnFechar);
            btnVisualizar= v.findViewById(R.id.btnVisualizar);
        }
    }
}
