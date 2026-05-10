package com.mayarpg.app.adapters;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mayarpg.app.R;
import com.mayarpg.app.models.ExerciciosResponse;

import java.util.List;

public class ExercicioAdapter extends RecyclerView.Adapter<ExercicioAdapter.VH> {

    public interface OnAcao {
        void onRegistrar(ExerciciosResponse.Prescricao p);
        void onDetalhes(ExerciciosResponse.Prescricao p);
    }

    private final List<ExerciciosResponse.Prescricao> itens;
    private final OnAcao listener;

    public ExercicioAdapter(List<ExerciciosResponse.Prescricao> itens, OnAcao listener) {
        this.itens = itens;
        this.listener = listener;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercicio, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        ExerciciosResponse.Prescricao p = itens.get(pos);
        h.tvTitulo.setText(p.titulo != null ? p.titulo : "");
        h.tvDescricao.setText(p.descricao != null ? p.descricao : "");
        h.tagCategoria.setText(p.categoria != null ? p.categoria : "Exercício");

        h.tvDuracao.setText(p.duracao_minutos != null
                ? p.duracao_minutos + " minutos" : "—");
        h.tvFrequencia.setText(p.frequencia != null ? p.frequencia : "");


        h.tagCategoria.setBackground(corPorCategoria(p.categoria));


        if (p.imagem_url != null && !p.imagem_url.isEmpty()) {
            Glide.with(h.itemView.getContext())
                    .load(p.imagem_url)
                    .into(h.ivThumb);
        } else {
            h.ivThumb.setImageResource(android.R.color.darker_gray);
        }

        h.btnRegistrar.setOnClickListener(v -> {
            if (listener != null) listener.onRegistrar(p);
        });
        h.btnDetalhes.setOnClickListener(v -> {
            if (listener != null) listener.onDetalhes(p);
        });
        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onDetalhes(p);
        });
    }

    @Override public int getItemCount() { return itens.size(); }

    private static GradientDrawable corPorCategoria(String cat) {
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(28f);
        if (cat == null) cat = "";
        switch (cat.toLowerCase()) {
            case "fortalecimento": bg.setColor(Color.parseColor("#F08372")); break;
            case "respiracao":
            case "respiração":     bg.setColor(Color.parseColor("#0E5A7C")); break;
            case "mobilidade":     bg.setColor(Color.parseColor("#7A4A2A")); break;
            default:               bg.setColor(Color.parseColor("#3DB5B0")); break;
        }
        return bg;
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivThumb, btnDetalhes;
        TextView tvTitulo, tvDescricao, tvDuracao, tvFrequencia, tagCategoria;
        android.widget.Button btnRegistrar;

        VH(View v) {
            super(v);
            ivThumb = v.findViewById(R.id.ivThumb);
            tvTitulo = v.findViewById(R.id.tvTitulo);
            tvDescricao = v.findViewById(R.id.tvDescricao);
            tvDuracao = v.findViewById(R.id.tvDuracao);
            tvFrequencia = v.findViewById(R.id.tvFrequencia);
            tagCategoria = v.findViewById(R.id.tagCategoria);
            btnRegistrar = v.findViewById(R.id.btnRegistrar);
            btnDetalhes = v.findViewById(R.id.btnDetalhes);
        }
    }
}
