package com.mayarpg.app.repository;

import android.content.Context;

import com.mayarpg.app.models.ExerciciosResponse;
import com.mayarpg.app.network.ApiClient;
import com.mayarpg.app.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Response;

public class ExerciciosRepository {

    public interface Callback {
        void onCache(ExerciciosResponse cache);
        void onFresh(ExerciciosResponse fresco);
        void onError(String mensagem);
    }

    private final Context context;
    private final CacheHelper cache;

    public ExerciciosRepository(Context context) {
        this.context = context.getApplicationContext();
        this.cache = new CacheHelper(this.context);
    }

    private String chave() {
        return "exercicios_" + SessionManager.getInstance(context).getPacienteId();
    }

    public void carregar(Callback cb) {
        // Cache
        ExerciciosResponse local = cache.buscar(chave(), ExerciciosResponse.class);
        cb.onCache(local);

        // Backend
        ApiClient.getApi(context).meusExercicios()
                .enqueue(new retrofit2.Callback<ExerciciosResponse>() {
                    @Override
                    public void onResponse(Call<ExerciciosResponse> call, Response<ExerciciosResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ExerciciosResponse fresco = response.body();
                            cache.salvar(chave(), fresco);
                            cb.onFresh(fresco);
                        } else {
                            cb.onError("Erro do servidor (" + response.code() + ")");
                        }
                    }
                    @Override
                    public void onFailure(Call<ExerciciosResponse> call, Throwable t) {
                        cb.onError("Falha de conexão");
                    }
                });
    }
}
