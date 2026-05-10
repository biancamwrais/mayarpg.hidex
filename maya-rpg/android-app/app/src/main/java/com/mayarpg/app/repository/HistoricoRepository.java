package com.mayarpg.app.repository;

import android.content.Context;

import com.mayarpg.app.models.HistoricoResponse;
import com.mayarpg.app.network.ApiClient;
import com.mayarpg.app.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Response;

public class HistoricoRepository {

    public interface Callback {
        void onCache(HistoricoResponse cache);
        void onFresh(HistoricoResponse fresco);
        void onError(String mensagem);
    }

    private final Context context;
    private final CacheHelper cache;

    public HistoricoRepository(Context context) {
        this.context = context.getApplicationContext();
        this.cache = new CacheHelper(this.context);
    }

    private String chave() {
        return "historico_" + SessionManager.getInstance(context).getPacienteId();
    }

    public void carregar(Callback cb) {
        HistoricoResponse local = cache.buscar(chave(), HistoricoResponse.class);
        cb.onCache(local);

        ApiClient.getApi(context).historico()
                .enqueue(new retrofit2.Callback<HistoricoResponse>() {
                    @Override
                    public void onResponse(Call<HistoricoResponse> call, Response<HistoricoResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            HistoricoResponse fresco = response.body();
                            cache.salvar(chave(), fresco);
                            cb.onFresh(fresco);
                        } else {
                            cb.onError("Erro do servidor (" + response.code() + ")");
                        }
                    }
                    @Override
                    public void onFailure(Call<HistoricoResponse> call, Throwable t) {
                        cb.onError("Falha de conexão");
                    }
                });
    }
}
