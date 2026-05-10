package com.mayarpg.app.repository;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mayarpg.app.local.AppDatabase;
import com.mayarpg.app.local.dao.DashboardDao;
import com.mayarpg.app.local.entities.DashboardCache;
import com.mayarpg.app.models.DashboardResponse;
import com.mayarpg.app.network.ApiClient;
import com.mayarpg.app.utils.SessionManager;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class DashboardRepository {

    public interface Callback {

        void onCache(DashboardResponse dadosCache);

        void onFresh(DashboardResponse dadosFrescos);

        void onError(String mensagem);
    }

    private final Context context;
    private final DashboardDao dao;
    private final Gson gson = new Gson();

    public DashboardRepository(Context context) {
        this.context = context.getApplicationContext();
        this.dao = AppDatabase.get(this.context).dashboardDao();
    }

    public void carregar(Callback cb) {
        int pacienteId = SessionManager.getInstance(context).getPacienteId();

        // 1. Le do cache imediatamente (se existir)
        if (pacienteId > 0) {
            DashboardCache cache = dao.buscar(pacienteId);
            if (cache != null) {
                cb.onCache(cacheParaResponse(cache));
            } else {
                cb.onCache(null);
            }
        } else {
            cb.onCache(null);
        }


        ApiClient.getApi(context).dashboard()
                .enqueue(new retrofit2.Callback<DashboardResponse>() {
                    @Override
                    public void onResponse(Call<DashboardResponse> call, Response<DashboardResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            DashboardResponse fresco = response.body();

                            salvarCache(pacienteId, fresco);
                            cb.onFresh(fresco);
                        } else {
                            cb.onError("Erro do servidor (" + response.code() + ")");
                        }
                    }
                    @Override
                    public void onFailure(Call<DashboardResponse> call, Throwable t) {
                        cb.onError("Falha de conexão");
                    }
                });
    }

    private DashboardResponse cacheParaResponse(DashboardCache c) {
        DashboardResponse r = new DashboardResponse();
        r.resumo = new DashboardResponse.Resumo();
        r.resumo.total_exercicios = c.totalExercicios;
        r.resumo.exercicios_hoje = c.exerciciosHoje;
        r.resumo.dor_media_7d = c.dorMedia7d;

        try {
            Type tipo = new TypeToken<List<DashboardResponse.Consulta>>(){}.getType();
            r.proximasConsultas = gson.fromJson(c.proximasConsultasJson, tipo);
        } catch (Exception e) {
            r.proximasConsultas = new ArrayList<>();
        }
        return r;
    }

    private void salvarCache(int pacienteId, DashboardResponse fresco) {
        if (pacienteId <= 0) return;

        DashboardCache c = new DashboardCache();
        c.pacienteId = pacienteId;
        c.atualizadoEm = System.currentTimeMillis();

        if (fresco.resumo != null) {
            c.totalExercicios = fresco.resumo.total_exercicios != null ? fresco.resumo.total_exercicios : 0;
            c.exerciciosHoje = fresco.resumo.exercicios_hoje != null ? fresco.resumo.exercicios_hoje : 0;
            c.dorMedia7d = fresco.resumo.dor_media_7d;
        }

        if (fresco.proximasConsultas != null) {
            c.proximasConsultasJson = gson.toJson(fresco.proximasConsultas);
        }

        dao.salvar(c);
    }
}
