package com.mayarpg.app.fragments;

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

import com.mayarpg.app.R;
import com.mayarpg.app.activities.MainActivity;
import com.mayarpg.app.adapters.CategoriaAdapter;
import com.mayarpg.app.adapters.ConsultaAdapter;
import com.mayarpg.app.models.DashboardResponse;
import com.mayarpg.app.repository.DashboardRepository;
import com.mayarpg.app.utils.SessionManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeFragment extends Fragment {

    private TextView tvSaudacao, tvTotalEx, tvHoje, tvDorMedia, tvSemConsultas;
    private RecyclerView rvCategorias, rvConsultas;
    private View btnMeusEx, btnVerHist, btnAddConsulta;
    private ImageView btnPerfil;

    private DashboardRepository repository;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        tvSaudacao = v.findViewById(R.id.tvSaudacao);
        tvTotalEx = v.findViewById(R.id.tvTotalEx);
        tvHoje = v.findViewById(R.id.tvHoje);
        tvDorMedia = v.findViewById(R.id.tvDorMedia);
        tvSemConsultas = v.findViewById(R.id.tvSemConsultas);
        rvCategorias = v.findViewById(R.id.rvCategorias);
        rvConsultas = v.findViewById(R.id.rvConsultas);
        btnMeusEx = v.findViewById(R.id.btnMeusEx);
        btnVerHist = v.findViewById(R.id.btnVerHist);
        btnAddConsulta = v.findViewById(R.id.btnAddConsulta);
        btnPerfil = v.findViewById(R.id.btnPerfil);

        repository = new DashboardRepository(requireContext());

        String nome = SessionManager.getInstance(requireContext()).getUserName();
        String primeiroNome = nome != null && nome.contains(" ") ? nome.substring(0, nome.indexOf(' ')) : nome;
        tvSaudacao.setText(getString(R.string.home_saudacao, primeiroNome != null ? primeiroNome : "Paciente"));

        montarCategorias();
        configurarBotoes();
        carregarDashboard();

        return v;
    }

    private void montarCategorias() {
        List<CategoriaAdapter.Categoria> cats = Arrays.asList(
                new CategoriaAdapter.Categoria(
                        getString(R.string.cat_alongamento),
                        R.drawable.bg_cat_alongamento,
                        R.drawable.cat_alongamento),
                new CategoriaAdapter.Categoria(
                        getString(R.string.cat_fortalecimento),
                        R.drawable.bg_cat_fortalecimento,
                        R.drawable.cat_fortalecimento),
                new CategoriaAdapter.Categoria(
                        getString(R.string.cat_respiracao),
                        R.drawable.bg_cat_respiracao,
                        R.drawable.cat_respiracao),
                new CategoriaAdapter.Categoria(
                        getString(R.string.cat_mobilidade),
                        R.drawable.bg_cat_mobilidade,
                        R.drawable.cat_mobilidade)
        );
        rvCategorias.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rvCategorias.setAdapter(new CategoriaAdapter(cats, c -> abrirExerciciosComFiltro(c.nome)));
    }

    private void configurarBotoes() {
        btnMeusEx.setOnClickListener(v -> abrirExerciciosComFiltro(null));

        btnVerHist.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).selecionarAba(R.id.nav_historico);
            }
        });
        btnAddConsulta.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).selecionarAba(R.id.nav_agendamento);
            }
        });

        if (btnPerfil != null) {
            btnPerfil.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).abrirPerfil();
                }
            });
        }
    }

    private void abrirExerciciosComFiltro(@Nullable String categoria) {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, ExerciciosFragment.novaInstancia(categoria))
                .addToBackStack(null)
                .commit();
    }

    /**
     * Carrega o dashboard usando o repository.
     * Mostra primeiro o que tem em cache (rapido) e depois atualiza com os dados frescos.
     */
    private void carregarDashboard() {
        repository.carregar(new DashboardRepository.Callback() {
            @Override
            public void onCache(DashboardResponse dadosCache) {
                // Tem cache? Atualiza a tela imediatamente
                if (dadosCache != null && isAdded()) {
                    aplicarDashboard(dadosCache);
                }
            }

            @Override
            public void onFresh(DashboardResponse dadosFrescos) {
                // Backend respondeu, atualiza com dados novos
                if (isAdded()) {
                    aplicarDashboard(dadosFrescos);
                }
            }

            @Override
            public void onError(String mensagem) {
                if (isAdded()) {
                    Toast.makeText(requireContext(),
                            mensagem + " (mostrando dados em cache)",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void aplicarDashboard(DashboardResponse d) {
        if (d.resumo != null) {
            tvTotalEx.setText(String.valueOf(
                    d.resumo.total_exercicios != null ? d.resumo.total_exercicios : 0));
            tvHoje.setText(String.valueOf(
                    d.resumo.exercicios_hoje != null ? d.resumo.exercicios_hoje : 0));
            tvDorMedia.setText(d.resumo.dor_media_7d != null
                    ? String.format("%.1f", d.resumo.dor_media_7d)
                    : "-");
        }

        List<DashboardResponse.Consulta> lista = d.proximasConsultas != null
                ? d.proximasConsultas : new ArrayList<>();
        if (lista.isEmpty()) {
            tvSemConsultas.setVisibility(View.VISIBLE);
            rvConsultas.setVisibility(View.GONE);
        } else {
            tvSemConsultas.setVisibility(View.GONE);
            rvConsultas.setVisibility(View.VISIBLE);
            rvConsultas.setLayoutManager(new LinearLayoutManager(requireContext()));
            rvConsultas.setAdapter(new ConsultaAdapter(lista));
        }
    }
}
