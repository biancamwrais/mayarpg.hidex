package com.mayarpg.app.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.mayarpg.app.R;
import com.mayarpg.app.activities.MainActivity;
import com.mayarpg.app.adapters.HorarioAdapter;
import com.mayarpg.app.models.AgendamentoRequest;
import com.mayarpg.app.models.HorarioDisponivel;
import com.mayarpg.app.models.Servico;
import com.mayarpg.app.network.ApiClient;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

public class AgendamentoFragment extends Fragment {

    private RadioGroup rgServicos;
    private LinearLayout btnEscolherData;
    private TextView tvDataEscolhida, tvSemHorarios;
    private RecyclerView rvHorarios;
    private ProgressBar progressHorarios;
    private MaterialButton btnConfirmar, btnLimpar;
    private ImageView btnPerfil;

    private final List<Servico> servicos = new ArrayList<>();
    private final List<HorarioDisponivel> horarios = new ArrayList<>();
    private HorarioAdapter horarioAdapter;

    private Integer servicoIdSelecionado;
    private String dataSelecionada;
    private String horarioSelecionado;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_agendamento, container, false);

        rgServicos = v.findViewById(R.id.rgServicos);
        btnEscolherData = v.findViewById(R.id.btnEscolherData);
        tvDataEscolhida = v.findViewById(R.id.tvDataEscolhida);
        tvSemHorarios = v.findViewById(R.id.tvSemHorarios);
        rvHorarios = v.findViewById(R.id.rvHorarios);
        progressHorarios = v.findViewById(R.id.progressHorarios);
        btnConfirmar = v.findViewById(R.id.btnConfirmar);
        btnLimpar = v.findViewById(R.id.btnLimpar);
        btnPerfil = v.findViewById(R.id.btnPerfil);

        horarioAdapter = new HorarioAdapter(horarios, h -> horarioSelecionado = h);
        rvHorarios.setLayoutManager(new GridLayoutManager(requireContext(), 4));
        rvHorarios.setAdapter(horarioAdapter);

        btnEscolherData.setOnClickListener(x -> abrirDatePicker());
        btnConfirmar.setOnClickListener(x -> confirmar());
        btnLimpar.setOnClickListener(x -> limpar());

        // Avatar -> abre Perfil
        if (btnPerfil != null) {
            btnPerfil.setOnClickListener(x -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).abrirPerfil();
                }
            });
        }

        carregarServicos();
        return v;
    }

    private void carregarServicos() {
        ApiClient.getApi(requireContext()).servicos()
                .enqueue(new retrofit2.Callback<List<Servico>>() {
                    @Override
                    public void onResponse(Call<List<Servico>> call, Response<List<Servico>> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            servicos.clear();
                            servicos.addAll(response.body());
                            montarRadioServicos();
                        }
                    }
                    @Override
                    public void onFailure(Call<List<Servico>> call, Throwable t) {
                        if (!isAdded()) return;
                        Toast.makeText(requireContext(),
                                "Falha ao carregar serviços", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void montarRadioServicos() {
        rgServicos.removeAllViews();
        for (Servico s : servicos) {
            RadioButton rb = new RadioButton(requireContext());
            rb.setText(s.nome);
            rb.setTextColor(getResources().getColor(R.color.text_primary, null));
            rb.setTextSize(14);
            rb.setId(View.generateViewId());
            rb.setPadding(12, 12, 12, 12);
            rb.setTag(s.id);
            rgServicos.addView(rb, new RadioGroup.LayoutParams(
                    RadioGroup.LayoutParams.MATCH_PARENT,
                    RadioGroup.LayoutParams.WRAP_CONTENT));
        }
        rgServicos.setOnCheckedChangeListener((g, id) -> {
            View r = g.findViewById(id);
            if (r != null && r.getTag() instanceof Integer) {
                servicoIdSelecionado = (Integer) r.getTag();
            }
        });
    }

    private void abrirDatePicker() {
        Calendar c = Calendar.getInstance();
        DatePickerDialog dp = new DatePickerDialog(requireContext(),
                (view, ano, mes, dia) -> {
                    dataSelecionada = String.format(Locale.US, "%04d-%02d-%02d", ano, mes + 1, dia);
                    tvDataEscolhida.setText(String.format(Locale.US, "%02d/%02d/%04d", dia, mes + 1, ano));
                    tvDataEscolhida.setTextColor(getResources().getColor(R.color.text_primary, null));
                    carregarHorarios();
                },
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        dp.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        dp.show();
    }

    private void carregarHorarios() {
        progressHorarios.setVisibility(View.VISIBLE);
        tvSemHorarios.setVisibility(View.GONE);
        horarios.clear();
        horarioAdapter.notifyDataSetChanged();
        horarioSelecionado = null;

        ApiClient.getApi(requireContext()).horariosDisponiveis(dataSelecionada)
                .enqueue(new retrofit2.Callback<List<HorarioDisponivel>>() {
                    @Override
                    public void onResponse(Call<List<HorarioDisponivel>> call,
                                           Response<List<HorarioDisponivel>> response) {
                        if (!isAdded()) return;
                        progressHorarios.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            horarios.clear();
                            horarios.addAll(response.body());
                            horarioAdapter.notifyDataSetChanged();
                            if (horarios.isEmpty()) {
                                tvSemHorarios.setText("Nenhum horário disponível nesta data");
                                tvSemHorarios.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                    @Override
                    public void onFailure(Call<List<HorarioDisponivel>> call, Throwable t) {
                        if (!isAdded()) return;
                        progressHorarios.setVisibility(View.GONE);
                        Toast.makeText(requireContext(),
                                "Falha de conexão", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void confirmar() {
        if (servicoIdSelecionado == null) {
            Toast.makeText(requireContext(),
                    "Selecione um serviço", Toast.LENGTH_SHORT).show();
            return;
        }
        if (dataSelecionada == null) {
            Toast.makeText(requireContext(),
                    "Selecione uma data", Toast.LENGTH_SHORT).show();
            return;
        }
        if (horarioSelecionado == null) {
            Toast.makeText(requireContext(),
                    "Selecione um horário", Toast.LENGTH_SHORT).show();
            return;
        }

        btnConfirmar.setEnabled(false);
        btnConfirmar.setText("Agendando...");

        AgendamentoRequest req = new AgendamentoRequest(
                servicoIdSelecionado, dataSelecionada, horarioSelecionado);

        ApiClient.getApi(requireContext()).criarAgendamento(req)
                .enqueue(new retrofit2.Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call,
                                           Response<Map<String, Object>> response) {
                        if (!isAdded()) return;
                        btnConfirmar.setEnabled(true);
                        btnConfirmar.setText(R.string.ag_confirmar);
                        if (response.isSuccessful()) {
                            Toast.makeText(requireContext(),
                                    "Consulta agendada com sucesso!",
                                    Toast.LENGTH_LONG).show();
                            limpar();
                        } else if (response.code() == 409) {
                            Toast.makeText(requireContext(),
                                    "Esse horário acabou de ser ocupado",
                                    Toast.LENGTH_LONG).show();
                            carregarHorarios();
                        } else {
                            Toast.makeText(requireContext(),
                                    "Erro ao agendar (" + response.code() + ")",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        if (!isAdded()) return;
                        btnConfirmar.setEnabled(true);
                        btnConfirmar.setText(R.string.ag_confirmar);
                        Toast.makeText(requireContext(),
                                "Falha de conexão", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void limpar() {
        servicoIdSelecionado = null;
        dataSelecionada = null;
        horarioSelecionado = null;
        rgServicos.clearCheck();
        tvDataEscolhida.setText("DD/MM/AAAA");
        tvDataEscolhida.setTextColor(getResources().getColor(R.color.text_secondary, null));
        horarios.clear();
        horarioAdapter.notifyDataSetChanged();
        horarioAdapter.limparSelecao();
        tvSemHorarios.setText("Selecione uma data acima");
        tvSemHorarios.setVisibility(View.VISIBLE);
    }
}
