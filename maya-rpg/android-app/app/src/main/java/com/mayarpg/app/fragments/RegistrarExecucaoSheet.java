package com.mayarpg.app.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.mayarpg.app.R;
import com.mayarpg.app.models.ExecucaoRequest;
import com.mayarpg.app.network.ApiClient;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

public class RegistrarExecucaoSheet extends BottomSheetDialogFragment {

    private static final String ARG_PRESCRICAO_ID = "prescricaoId";
    private static final String ARG_TITULO = "titulo";
    private static final String ARG_CATEGORIA = "categoria";
    private static final String ARG_INSTRUCOES = "instrucoes";


    public interface OnRegistradoCallback {
        void onRegistrado();
    }

    private OnRegistradoCallback callback;

    public static RegistrarExecucaoSheet novaInstancia(int prescricaoId, String titulo,
                                                       String categoria, String instrucoes) {
        RegistrarExecucaoSheet f = new RegistrarExecucaoSheet();
        Bundle b = new Bundle();
        b.putInt(ARG_PRESCRICAO_ID, prescricaoId);
        b.putString(ARG_TITULO, titulo);
        b.putString(ARG_CATEGORIA, categoria);
        b.putString(ARG_INSTRUCOES, instrucoes);
        f.setArguments(b);
        return f;
    }

    public void setCallback(OnRegistradoCallback c) {
        this.callback = c;
    }

    @NonNull @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog d = new BottomSheetDialog(requireContext(), getTheme());
        d.setOnShowListener(dialog -> {
            BottomSheetDialog bsd = (BottomSheetDialog) dialog;
            View bottom = bsd.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottom != null) {
                com.google.android.material.bottomsheet.BottomSheetBehavior.from(bottom)
                        .setState(com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED);
            }
        });
        return d;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.sheet_registrar_execucao, container, false);

        Bundle args = getArguments();
        if (args == null) { dismiss(); return v; }

        final int prescricaoId = args.getInt(ARG_PRESCRICAO_ID);
        String titulo = args.getString(ARG_TITULO, "");
        String categoria = args.getString(ARG_CATEGORIA, "");
        String instrucoes = args.getString(ARG_INSTRUCOES, "");

        TextView tvNomeEx = v.findViewById(R.id.tvNomeEx);
        TextView tvCategoriaEx = v.findViewById(R.id.tvCategoriaEx);
        TextView tvInstrucoes = v.findViewById(R.id.tvInstrucoes);
        TextView tvDorValor = v.findViewById(R.id.tvDorValor);
        SeekBar sliderDor = v.findViewById(R.id.sliderDor);
        EditText etObs = v.findViewById(R.id.etObs);
        Button btnConfirmar = v.findViewById(R.id.btnConfirmar);
        ImageView btnFechar = v.findViewById(R.id.btnFechar);

        tvNomeEx.setText(titulo);
        tvCategoriaEx.setText(categoria);


        if (instrucoes != null && !instrucoes.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String linha : instrucoes.split("\n")) {
                if (!linha.trim().isEmpty()) {
                    sb.append("• ").append(linha.trim()).append("\n");
                }
            }
            tvInstrucoes.setText(sb.toString().trim());
        } else {
            tvInstrucoes.setText("Sem instruções específicas.");
        }

        sliderDor.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int v1, boolean u) {
                tvDorValor.setText(String.valueOf(v1));
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {}
        });

        btnFechar.setOnClickListener(x -> dismiss());

        btnConfirmar.setOnClickListener(x -> {
            int dor = sliderDor.getProgress();
            String obs = etObs.getText().toString().trim();
            registrar(prescricaoId, dor, obs.isEmpty() ? null : obs, btnConfirmar);
        });

        return v;
    }

    private void registrar(int prescricaoId, int nivelDor, String obs, Button btn) {
        btn.setEnabled(false);
        btn.setText("Enviando...");

        ExecucaoRequest req = new ExecucaoRequest(prescricaoId, nivelDor, obs);
        ApiClient.getApi(requireContext()).registrarExecucao(req)
                .enqueue(new retrofit2.Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call,
                                           Response<Map<String, Object>> response) {
                        if (!isAdded()) return;
                        btn.setEnabled(true);
                        btn.setText(R.string.reg_confirmar);
                        if (response.isSuccessful()) {
                            Toast.makeText(requireContext(),
                                    "Execução registrada!", Toast.LENGTH_SHORT).show();
                            if (callback != null) callback.onRegistrado();
                            dismiss();
                        } else {
                            Toast.makeText(requireContext(),
                                    "Erro ao registrar (" + response.code() + ")",
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        if (!isAdded()) return;
                        btn.setEnabled(true);
                        btn.setText(R.string.reg_confirmar);
                        Toast.makeText(requireContext(),
                                "Falha de conexão", Toast.LENGTH_LONG).show();
                    }
                });
    }
}