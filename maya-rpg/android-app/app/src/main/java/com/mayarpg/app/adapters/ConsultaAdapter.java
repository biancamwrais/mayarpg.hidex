package com.mayarpg.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mayarpg.app.R;
import com.mayarpg.app.models.DashboardResponse;

import java.util.List;

public class ConsultaAdapter extends RecyclerView.Adapter<ConsultaAdapter.VH> {

    private final List<DashboardResponse.Consulta> itens;

    public ConsultaAdapter(List<DashboardResponse.Consulta> itens) { this.itens = itens; }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_consulta, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        DashboardResponse.Consulta c = itens.get(pos);
        h.tvNome.setText(c.servico != null ? c.servico : c.paciente);
        String horario = c.horario != null ? c.horario.substring(0, 5) : "";
        h.tvHorario.setText(horario);
    }

    @Override public int getItemCount() { return itens.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvNome, tvHorario;
        VH(View v) {
            super(v);
            tvNome = v.findViewById(R.id.tvNome);
            tvHorario = v.findViewById(R.id.tvHorario);
        }
    }
}
