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
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mayarpg.app.R;
import com.mayarpg.app.adapters.PagamentoAdapter;
import com.mayarpg.app.models.PagamentosResponse;
import com.mayarpg.app.models.PerfilResponse;
import com.mayarpg.app.repository.PerfilRepository;
import com.mayarpg.app.utils.Formatters;
import com.mayarpg.app.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PerfilFragment extends Fragment {

    private TextView tvNome, tvCpf, tvIniciais;
    private TextView tvEmail, tvTelefone, tvEndereco;
    private TextView tvClinicaNome, tvClinicaEndereco, tvClinicaTel, tvClinicaHorario;
    private TextView tvTotalPago, tvSemPagamentos;
    private LinearLayout headerInfo, conteudoInfo;
    private LinearLayout headerClinica, conteudoClinica;
    private LinearLayout headerPag;
    private ImageView seta1, seta2, seta3, btnTema;
    private RecyclerView rvPagamentos;
    private final List<PagamentosResponse.Pagamento> pagamentos = new ArrayList<>();
    private PagamentoAdapter pagAdapter;

    private PerfilRepository repository;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_perfil, container, false);

        tvNome = v.findViewById(R.id.tvNome);
        tvCpf = v.findViewById(R.id.tvCpf);
        tvIniciais = v.findViewById(R.id.tvIniciais);
        tvEmail = v.findViewById(R.id.tvEmail);
        tvTelefone = v.findViewById(R.id.tvTelefone);
        tvEndereco = v.findViewById(R.id.tvEndereco);
        tvClinicaNome = v.findViewById(R.id.tvClinicaNome);
        tvClinicaEndereco = v.findViewById(R.id.tvClinicaEndereco);
        tvClinicaTel = v.findViewById(R.id.tvClinicaTel);
        tvClinicaHorario = v.findViewById(R.id.tvClinicaHorario);
        tvTotalPago = v.findViewById(R.id.tvTotalPago);
        tvSemPagamentos = v.findViewById(R.id.tvSemPagamentos);
        headerInfo = v.findViewById(R.id.headerInfo);
        conteudoInfo = v.findViewById(R.id.conteudoInfo);
        headerClinica = v.findViewById(R.id.headerClinica);
        conteudoClinica = v.findViewById(R.id.conteudoClinica);
        headerPag = v.findViewById(R.id.headerPag);
        seta1 = v.findViewById(R.id.seta1);
        seta2 = v.findViewById(R.id.seta2);
        seta3 = v.findViewById(R.id.seta3);
        btnTema = v.findViewById(R.id.btnTema);
        rvPagamentos = v.findViewById(R.id.rvPagamentos);

        repository = new PerfilRepository(requireContext());

        pagAdapter = new PagamentoAdapter(pagamentos);
        rvPagamentos.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvPagamentos.setNestedScrollingEnabled(false);
        rvPagamentos.setAdapter(pagAdapter);

        // Cards expansiveis
        headerInfo.setOnClickListener(x -> alternar(conteudoInfo, seta1));
        headerClinica.setOnClickListener(x -> alternar(conteudoClinica, seta2));
        headerPag.setOnClickListener(x -> alternar(rvPagamentos, seta3));

        // Tema noturno
        atualizarIconeTema();
        btnTema.setOnClickListener(x -> alternarTema());

        carregarPerfil();
        carregarPagamentos();

        return v;
    }

    private void alternar(View conteudo, ImageView seta) {
        if (conteudo.getVisibility() == View.VISIBLE) {
            conteudo.setVisibility(View.GONE);
            seta.setRotation(0f);
        } else {
            conteudo.setVisibility(View.VISIBLE);
            seta.setRotation(90f);
        }
    }

    private void atualizarIconeTema() {
        boolean dark = SessionManager.getInstance(requireContext()).isDarkMode();
        btnTema.setImageResource(dark ? R.drawable.ic_light_mode : R.drawable.ic_dark_mode);
    }

    private void alternarTema() {
        SessionManager s = SessionManager.getInstance(requireContext());
        boolean novo = !s.isDarkMode();
        s.setDarkMode(novo);
        AppCompatDelegate.setDefaultNightMode(
                novo ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }

    private void carregarPerfil() {
        repository.carregarPerfil(new PerfilRepository.PerfilCallback() {
            @Override
            public void onCache(PerfilResponse cache) {
                if (!isAdded()) return;
                if (cache != null) preencher(cache);
            }
            @Override
            public void onFresh(PerfilResponse fresco) {
                if (!isAdded()) return;
                preencher(fresco);
            }
            @Override
            public void onError(String mensagem) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(),
                        mensagem, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void preencher(PerfilResponse pr) {
        if (pr.paciente != null) {
            tvNome.setText(pr.paciente.nome != null ? pr.paciente.nome : "");
            tvIniciais.setText(Formatters.iniciais(pr.paciente.nome));
            tvCpf.setText(pr.paciente.cpf != null ? "CPF: " + pr.paciente.cpf : "");
            tvEmail.setText(pr.paciente.email != null ? pr.paciente.email : "—");
            tvTelefone.setText(pr.paciente.telefone != null ? pr.paciente.telefone : "—");

            StringBuilder end = new StringBuilder();
            if (pr.paciente.endereco != null) end.append(pr.paciente.endereco);
            if (pr.paciente.cidade != null) {
                if (end.length() > 0) end.append("\n");
                end.append(pr.paciente.cidade);
                if (pr.paciente.estado != null) end.append(" - ").append(pr.paciente.estado);
            }
            if (pr.paciente.cep != null) {
                if (end.length() > 0) end.append("\nCEP: ");
                end.append(pr.paciente.cep);
            }
            tvEndereco.setText(end.length() > 0 ? end.toString() : "—");
        }

        if (pr.clinica != null) {
            tvClinicaNome.setText(pr.clinica.nome != null ? pr.clinica.nome : "");
            StringBuilder cend = new StringBuilder();
            if (pr.clinica.endereco != null) cend.append(pr.clinica.endereco);
            if (pr.clinica.cidade != null) {
                if (cend.length() > 0) cend.append("\n");
                cend.append(pr.clinica.cidade);
            }
            if (pr.clinica.cep != null) {
                if (cend.length() > 0) cend.append("\nCEP: ");
                cend.append(pr.clinica.cep);
            }
            tvClinicaEndereco.setText(cend.toString());
            tvClinicaTel.setText(pr.clinica.telefone != null ? pr.clinica.telefone : "");
            tvClinicaHorario.setText(pr.clinica.horario != null ? pr.clinica.horario : "");
        }
    }

    private void carregarPagamentos() {
        repository.carregarPagamentos(new PerfilRepository.PagamentosCallback() {
            @Override
            public void onCache(PagamentosResponse cache) {
                if (!isAdded()) return;
                if (cache != null) aplicarPagamentos(cache);
            }
            @Override
            public void onFresh(PagamentosResponse fresco) {
                if (!isAdded()) return;
                aplicarPagamentos(fresco);
            }
            @Override
            public void onError(String mensagem) {
                if (!isAdded()) return;

            }
        });
    }

    private void aplicarPagamentos(PagamentosResponse pr) {
        pagamentos.clear();
        if (pr.pagamentos != null) pagamentos.addAll(pr.pagamentos);
        pagAdapter.notifyDataSetChanged();

        double total = pr.total != null ? pr.total : 0.0;
        tvTotalPago.setText("R$ " +
                String.format(Locale.US, "%.2f", total).replace('.', ','));

        if (pagamentos.isEmpty()) {
            tvSemPagamentos.setVisibility(View.VISIBLE);
        } else {
            tvSemPagamentos.setVisibility(View.GONE);
        }
    }
}
