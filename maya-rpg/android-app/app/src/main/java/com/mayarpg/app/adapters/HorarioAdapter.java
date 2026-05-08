package com.mayarpg.app.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mayarpg.app.R;
import com.mayarpg.app.models.HorarioDisponivel;

import java.util.List;

public class HorarioAdapter extends RecyclerView.Adapter<HorarioAdapter.VH> {

    public interface OnSelecionar { void onSelecionar(String horario); }

    private final List<HorarioDisponivel> itens;
    private final OnSelecionar listener;
    private int posSelecionada = -1;

    public HorarioAdapter(List<HorarioDisponivel> itens, OnSelecionar listener) {
        this.itens = itens;
        this.listener = listener;
    }

    public void limparSelecao() {
        int p = posSelecionada;
        posSelecionada = -1;
        if (p >= 0) notifyItemChanged(p);
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_horario, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        HorarioDisponivel hd = itens.get(pos);
        h.tv.setText(hd.horario);

        boolean disponivel = hd.disponivel != null && hd.disponivel;
        boolean selecionado = pos == posSelecionada;

        h.tv.setSelected(selecionado);
        h.tv.setEnabled(disponivel);

        if (!disponivel) {
            h.tv.setAlpha(0.4f);
            h.tv.setTextColor(Color.parseColor("#9AAEC2"));
        } else {
            h.tv.setAlpha(1f);
            h.tv.setTextColor(selecionado ? Color.WHITE : Color.parseColor("#1A2B3C"));
        }

        h.tv.setOnClickListener(v -> {
            if (!disponivel) return;
            int prev = posSelecionada;
            posSelecionada = h.getAdapterPosition();
            if (prev >= 0) notifyItemChanged(prev);
            notifyItemChanged(posSelecionada);
            if (listener != null) listener.onSelecionar(hd.horario);
        });
    }

    @Override public int getItemCount() { return itens.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tv;
        VH(View v) {
            super(v);
            tv = v.findViewById(R.id.tvHorario);
        }
    }
}
