package com.mayarpg.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mayarpg.app.R;
import com.mayarpg.app.activities.MainActivity;
import com.mayarpg.app.adapters.NotificacaoAdapter;
import com.mayarpg.app.models.Notificacao;
import com.mayarpg.app.network.ApiClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

public class NotificacoesFragment extends Fragment {

    private RecyclerView rv;
    private TextView tvSubtitulo, tvBadge, btnLimpar;
    private ImageView btnPerfil;
    private LinearLayout boxVazio, cardNaoLidas;
    private final List<Notificacao> dados = new ArrayList<>();
    private NotificacaoAdapter adapter;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_notificacoes, container, false);

        rv = v.findViewById(R.id.rvNotif);
        tvSubtitulo = v.findViewById(R.id.tvSubtitulo);
        tvBadge = v.findViewById(R.id.tvBadgeNaoLidas);
        btnLimpar = v.findViewById(R.id.btnLimpar);
        boxVazio = v.findViewById(R.id.boxVazio);
        cardNaoLidas = v.findViewById(R.id.cardNaoLidas);
        btnPerfil = v.findViewById(R.id.btnPerfil);

        adapter = new NotificacaoAdapter(dados, new NotificacaoAdapter.OnAcao() {
            @Override
            public void onVisualizar(Notificacao n) {
                marcarLida(n);
            }
            @Override
            public void onFechar(Notificacao n) {
                marcarLida(n);
            }
        });
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        btnLimpar.setOnClickListener(x -> limparTudo());

        if (btnPerfil != null) {
            btnPerfil.setOnClickListener(x -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).abrirPerfil();
                }
            });
        }

        carregar();
        return v;
    }

    private void carregar() {
        ApiClient.getApi(requireContext()).notificacoes()
                .enqueue(new retrofit2.Callback<List<Notificacao>>() {
                    @Override
                    public void onResponse(Call<List<Notificacao>> call,
                                           Response<List<Notificacao>> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            dados.clear();
                            dados.addAll(response.body());
                            adapter.notifyDataSetChanged();
                            atualizarUI();
                        } else {
                            Toast.makeText(requireContext(),
                                    "Erro ao carregar notificações",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<List<Notificacao>> call, Throwable t) {
                        if (!isAdded()) return;
                        Toast.makeText(requireContext(),
                                "Falha de conexão", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void atualizarUI() {
        int naoLidas = 0;
        for (Notificacao n : dados) {
            if (!n.isLida()) naoLidas++;
        }
        tvSubtitulo.setText(naoLidas + " não lida" + (naoLidas == 1 ? "" : "s"));
        tvBadge.setText(String.valueOf(naoLidas));

        if (dados.isEmpty()) {
            cardNaoLidas.setVisibility(View.GONE);
            rv.setVisibility(View.GONE);
            boxVazio.setVisibility(View.VISIBLE);
        } else {
            cardNaoLidas.setVisibility(View.VISIBLE);
            rv.setVisibility(View.VISIBLE);
            boxVazio.setVisibility(View.GONE);
        }
    }

    private void marcarLida(Notificacao n) {
        if (n.id == null) return;
        ApiClient.getApi(requireContext()).marcarLida(n.id)
                .enqueue(new retrofit2.Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call,
                                           Response<Map<String, Object>> response) {
                        if (!isAdded()) return;
                        int idx = dados.indexOf(n);
                        if (idx >= 0) {
                            dados.remove(idx);
                            adapter.notifyItemRemoved(idx);
                            atualizarUI();
                        }
                    }
                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {}
                });
    }

    private void limparTudo() {
        if (dados.isEmpty()) return;
        ApiClient.getApi(requireContext()).limparNotificacoes()
                .enqueue(new retrofit2.Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call,
                                           Response<Map<String, Object>> response) {
                        if (!isAdded()) return;
                        dados.clear();
                        adapter.notifyDataSetChanged();
                        atualizarUI();
                        Toast.makeText(requireContext(),
                                "Notificações limpas",
                                Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        if (!isAdded()) return;
                        Toast.makeText(requireContext(),
                                "Falha de conexão", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}