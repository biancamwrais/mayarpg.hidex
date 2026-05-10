package com.mayarpg.app.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.mayarpg.app.R;
import com.mayarpg.app.activities.MainActivity;
import com.mayarpg.app.adapters.ExecucaoAdapter;
import com.mayarpg.app.models.HistoricoResponse;
import com.mayarpg.app.repository.HistoricoRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HistoricoFragment extends Fragment {

    private TextView tvTotal, tvDorMedia, tvTendencia, tvVazio;
    private ImageView btnPerfil;
    private LineChart grafico;
    private RecyclerView rv;
    private ExecucaoAdapter adapter;
    private final List<HistoricoResponse.Execucao> dados = new ArrayList<>();

    private HistoricoRepository repository;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_historico, container, false);

        tvTotal = v.findViewById(R.id.tvTotal);
        tvDorMedia = v.findViewById(R.id.tvDorMedia);
        tvTendencia = v.findViewById(R.id.tvTendencia);
        tvVazio = v.findViewById(R.id.tvVazio);
        grafico = v.findViewById(R.id.grafico);
        rv = v.findViewById(R.id.rvExecucoes);
        btnPerfil = v.findViewById(R.id.btnPerfil);

        repository = new HistoricoRepository(requireContext());

        adapter = new ExecucaoAdapter(dados);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setNestedScrollingEnabled(false);
        rv.setAdapter(adapter);

        if (btnPerfil != null) {
            btnPerfil.setOnClickListener(x -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).abrirPerfil();
                }
            });
        }

        configurarGraficoVazio();
        carregar();
        return v;
    }

    private void configurarGraficoVazio() {
        Description d = new Description();
        d.setText("");
        grafico.setDescription(d);
        grafico.setNoDataText("Sem dados ainda");
        grafico.setNoDataTextColor(Color.GRAY);
        grafico.getLegend().setEnabled(false);

        XAxis x = grafico.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setDrawGridLines(false);
        x.setGranularity(1f);
        x.setTextColor(Color.parseColor("#6B7B8C"));

        YAxis yL = grafico.getAxisLeft();
        yL.setAxisMinimum(0f);
        yL.setAxisMaximum(10f);
        yL.setGranularity(1f);
        yL.setTextColor(Color.parseColor("#6B7B8C"));

        grafico.getAxisRight().setEnabled(false);
        grafico.setTouchEnabled(true);
        grafico.setDragEnabled(false);
        grafico.setScaleEnabled(false);
    }

    private void carregar() {
        repository.carregar(new HistoricoRepository.Callback() {
            @Override
            public void onCache(HistoricoResponse cache) {
                if (!isAdded()) return;
                if (cache != null) aplicarTudo(cache);
            }
            @Override
            public void onFresh(HistoricoResponse fresco) {
                if (!isAdded()) return;
                aplicarTudo(fresco);
            }
            @Override
            public void onError(String mensagem) {
                if (!isAdded()) return;
                if (dados.isEmpty()) {
                    Toast.makeText(requireContext(),
                            mensagem, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(requireContext(),
                            mensagem + " (mostrando dados em cache)",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void aplicarTudo(HistoricoResponse h) {
        aplicarStats(h);
        aplicarGrafico(h);
        aplicarLista(h);
    }

    private void aplicarStats(HistoricoResponse h) {
        if (h.resumo != null) {
            tvTotal.setText(String.valueOf(h.resumo.total != null ? h.resumo.total : 0));
            tvDorMedia.setText(h.resumo.dor_media != null
                    ? String.format(Locale.getDefault(), "%.1f", h.resumo.dor_media) : "—");
        } else {
            tvTotal.setText("0");
            tvDorMedia.setText("—");
        }

        if (h.grafico != null && h.grafico.size() >= 2) {
            int meio = h.grafico.size() / 2;
            double mPrim = 0, mUlt = 0;
            int cP = 0, cU = 0;
            for (int i = 0; i < h.grafico.size(); i++) {
                Double dor = h.grafico.get(i).dor_media;
                if (dor == null) continue;
                if (i < meio) { mPrim += dor; cP++; }
                else { mUlt += dor; cU++; }
            }
            if (cP > 0 && cU > 0) {
                double mediaP = mPrim / cP;
                double mediaU = mUlt / cU;
                double delta = mediaU - mediaP;
                String simbolo;
                if (delta < -0.5) simbolo = "↓";
                else if (delta > 0.5) simbolo = "↑";
                else simbolo = "→";
                tvTendencia.setText(simbolo + " " + String.format(Locale.getDefault(), "%.1f", Math.abs(delta)));
            }
        }
    }

    private void aplicarGrafico(HistoricoResponse h) {
        if (h.grafico == null || h.grafico.isEmpty()) {
            grafico.clear();
            grafico.invalidate();
            return;
        }

        List<Entry> entradas = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        SimpleDateFormat out = new SimpleDateFormat("dd/MM", Locale.getDefault());

        for (int i = 0; i < h.grafico.size(); i++) {
            HistoricoResponse.PontoGrafico p = h.grafico.get(i);
            float dor = p.dor_media != null ? p.dor_media.floatValue() : 0f;
            entradas.add(new Entry(i, dor));
            try {
                labels.add(out.format(in.parse(p.dia.substring(0, 10))));
            } catch (Exception e) {
                labels.add(p.dia);
            }
        }

        LineDataSet set = new LineDataSet(entradas, "Dor média");
        set.setColor(Color.parseColor("#3DB5B0"));
        set.setLineWidth(2.5f);
        set.setCircleColor(Color.parseColor("#3DB5B0"));
        set.setCircleRadius(5f);
        set.setDrawCircleHole(false);
        set.setValueTextSize(10f);
        set.setValueTextColor(Color.parseColor("#1A2B3C"));
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setDrawFilled(true);
        set.setFillColor(Color.parseColor("#3DB5B0"));
        set.setFillAlpha(40);

        LineData ld = new LineData(set);
        grafico.setData(ld);
        grafico.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        grafico.getXAxis().setLabelCount(labels.size(), false);
        grafico.invalidate();
    }

    private void aplicarLista(HistoricoResponse h) {
        dados.clear();
        if (h.execucoes != null) dados.addAll(h.execucoes);
        adapter.notifyDataSetChanged();
        tvVazio.setVisibility(dados.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
