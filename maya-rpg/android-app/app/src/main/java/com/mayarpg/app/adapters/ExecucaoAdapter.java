package com.mayarpg.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mayarpg.app.R;
import com.mayarpg.app.models.HistoricoResponse;
import com.mayarpg.app.utils.Formatters;

import java.util.List;

public class ExecucaoAdapter extends RecyclerView.Adapter<ExecucaoAdapter.VH> {

    private final List<HistoricoResponse.Execucao> itens;

    public ExecucaoAdapter(List<HistoricoResponse.Execucao> itens) {
        this.itens = itens;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_execucao, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        HistoricoResponse.Execucao e = itens.get(pos);
        h.tvTitulo.setText(e.titulo != null ? e.titulo : "Exercício");
        h.tvData.setText(Formatters.formatDataHora(e.data_execucao));

        int dor = e.nivel_dor != null ? e.nivel_dor : 0;
        h.tvDor.setText(dor + "/10");
        h.barra.setProgress(dor);

        if (e.observacoes != null && !e.observacoes.trim().isEmpty()) {
            h.tvObs.setVisibility(View.VISIBLE);
            h.tvObs.setText("Obs: " + e.observacoes);
        } else {
            h.tvObs.setVisibility(View.GONE);
        }
    }

    @Override public int getItemCount() { return itens.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvData, tvDor, tvObs;
        ProgressBar barra;
        VH(View v) {
            super(v);
            tvTitulo = v.findViewById(R.id.tvTitulo);
            tvData = v.findViewById(R.id.tvData);
            tvDor = v.findViewById(R.id.tvDor);
            tvObs = v.findViewById(R.id.tvObs);
            barra = v.findViewById(R.id.barraDor);
        }
    }
}
