package com.mayarpg.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mayarpg.app.R;
import com.mayarpg.app.adapters.ExercicioAdapter;
import com.mayarpg.app.models.ExerciciosResponse;
import com.mayarpg.app.network.ApiClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class ExerciciosFragment extends Fragment {

    private static final String ARG_CATEGORIA = "categoria";

    /**
     * Cria o fragment com filtro de categoria pre-aplicado.
     * Passe null se quiser mostrar todos os exercicios.
     */
    public static ExerciciosFragment novaInstancia(@Nullable String categoria) {
        ExerciciosFragment f = new ExerciciosFragment();
        if (categoria != null) {
            Bundle b = new Bundle();
            b.putString(ARG_CATEGORIA, categoria);
            f.setArguments(b);
        }
        return f;
    }

    private RecyclerView rv;
    private TextView tvAtivos, tvSemana, tvAdesao, tvVazioTitulo, tvVazioMsg, tvChipFiltro;
    private LinearLayout boxVazio, chipFiltro;
    private Button btnVerTodos;
    private ImageView btnFecharChip;
    private ProgressBar progress;

    /** Lista completa vinda da API (sem filtro). */
    private final List<ExerciciosResponse.Prescricao> todos = new ArrayList<>();
    /** Lista exibida apos aplicar o filtro. */
    private final List<ExerciciosResponse.Prescricao> exibidos = new ArrayList<>();
    private ExercicioAdapter adapter;

    /** Categoria do filtro atual (null = sem filtro). */
    private String categoriaFiltro;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_exercicios, container, false);

        rv = v.findViewById(R.id.rvExercicios);
        tvAtivos = v.findViewById(R.id.tvAtivos);
        tvSemana = v.findViewById(R.id.tvSemana);
        tvAdesao = v.findViewById(R.id.tvAdesao);
        boxVazio = v.findViewById(R.id.boxVazio);
        tvVazioTitulo = v.findViewById(R.id.tvVazioTitulo);
        tvVazioMsg = v.findViewById(R.id.tvVazioMsg);
        btnVerTodos = v.findViewById(R.id.btnVerTodos);
        progress = v.findViewById(R.id.progress);
        chipFiltro = v.findViewById(R.id.chipFiltro);
        tvChipFiltro = v.findViewById(R.id.tvChipFiltro);
        btnFecharChip = v.findViewById(R.id.btnFecharChip);

        // Le categoria dos arguments (se houver)
        if (getArguments() != null) {
            categoriaFiltro = getArguments().getString(ARG_CATEGORIA);
        }

        adapter = new ExercicioAdapter(exibidos, new ExercicioAdapter.OnAcao() {
            @Override
            public void onRegistrar(ExerciciosResponse.Prescricao p) {
                abrirSheetRegistro(p);
            }
            @Override
            public void onDetalhes(ExerciciosResponse.Prescricao p) {
                Toast.makeText(requireContext(),
                        p.titulo + " - " + (p.frequencia != null ? p.frequencia : ""),
                        Toast.LENGTH_SHORT).show();
            }
        });
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        btnFecharChip.setOnClickListener(x -> removerFiltro());
        btnVerTodos.setOnClickListener(x -> removerFiltro());

        carregar();
        return v;
    }

    private void abrirSheetRegistro(ExerciciosResponse.Prescricao p) {
        RegistrarExecucaoSheet sheet = RegistrarExecucaoSheet.novaInstancia(
                p.prescricao_id, p.titulo, p.categoria, p.instrucoes);

        sheet.setCallback(new RegistrarExecucaoSheet.OnRegistradoCallback() {
            @Override
            public void onRegistrado() {
                carregar();
            }
        });

        sheet.show(getParentFragmentManager(), "registrar");
    }

    private void removerFiltro() {
        categoriaFiltro = null;
        aplicarFiltro();
    }

    /**
     * Aplica o filtro de categoria sobre a lista 'todos' e atualiza UI.
     * Tambem mostra/esconde o chip e o estado vazio amigavel.
     */
    private void aplicarFiltro() {
        exibidos.clear();

        if (categoriaFiltro == null || categoriaFiltro.isEmpty()) {
            chipFiltro.setVisibility(View.GONE);
            exibidos.addAll(todos);
        } else {
            chipFiltro.setVisibility(View.VISIBLE);
            tvChipFiltro.setText(categoriaFiltro);

            for (ExerciciosResponse.Prescricao p : todos) {
                if (p.categoria != null && categoriaFiltro.equalsIgnoreCase(p.categoria)) {
                    exibidos.add(p);
                }
            }
        }
        adapter.notifyDataSetChanged();

        // Estado vazio
        if (exibidos.isEmpty()) {
            boxVazio.setVisibility(View.VISIBLE);
            rv.setVisibility(View.GONE);

            if (categoriaFiltro != null) {
                tvVazioTitulo.setText("Nenhum exercício de " + categoriaFiltro);
                tvVazioMsg.setText("A Dra. Maya ainda não prescreveu exercícios desta categoria pra você.");
                btnVerTodos.setVisibility(View.VISIBLE);
            } else {
                tvVazioTitulo.setText("Nenhum exercício prescrito");
                tvVazioMsg.setText("Aguarde a Dra. Maya prescrever seus exercícios.");
                btnVerTodos.setVisibility(View.GONE);
            }
        } else {
            boxVazio.setVisibility(View.GONE);
            rv.setVisibility(View.VISIBLE);
        }
    }

    private void carregar() {
        progress.setVisibility(View.VISIBLE);
        boxVazio.setVisibility(View.GONE);

        ApiClient.getApi(requireContext()).meusExercicios()
                .enqueue(new retrofit2.Callback<ExerciciosResponse>() {
                    @Override
                    public void onResponse(Call<ExerciciosResponse> call,
                                           Response<ExerciciosResponse> response) {
                        if (!isAdded()) return;
                        progress.setVisibility(View.GONE);

                        if (response.isSuccessful() && response.body() != null) {
                            ExerciciosResponse er = response.body();
                            todos.clear();
                            if (er.prescricoes != null) todos.addAll(er.prescricoes);

                            if (er.estatisticas != null) {
                                int ativos = er.estatisticas.ativos != null ? er.estatisticas.ativos : 0;
                                int semana = er.estatisticas.esta_semana != null ? er.estatisticas.esta_semana : 0;
                                tvAtivos.setText(String.valueOf(ativos));
                                tvSemana.setText(String.valueOf(semana));

                                int meta = ativos * 7;
                                int pct = meta > 0 ? Math.min(100, semana * 100 / meta) : 0;
                                tvAdesao.setText(pct + "%");
                            }

                            aplicarFiltro();
                        } else {
                            Toast.makeText(requireContext(),
                                    "Erro ao carregar exercícios", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ExerciciosResponse> call, Throwable t) {
                        if (!isAdded()) return;
                        progress.setVisibility(View.GONE);
                        Toast.makeText(requireContext(),
                                "Falha de conexão", Toast.LENGTH_LONG).show();
                    }
                });
    }
}
