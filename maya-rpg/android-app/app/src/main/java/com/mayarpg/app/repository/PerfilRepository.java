package com.mayarpg.app.repository;

import android.content.Context;

import com.mayarpg.app.models.PagamentosResponse;
import com.mayarpg.app.models.PerfilResponse;
import com.mayarpg.app.network.ApiClient;
import com.mayarpg.app.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Response;

public class PerfilRepository {

    public interface PerfilCallback {
        void onCache(PerfilResponse cache);
        void onFresh(PerfilResponse fresco);
        void onError(String mensagem);
    }

    public interface PagamentosCallback {
        void onCache(PagamentosResponse cache);
        void onFresh(PagamentosResponse fresco);
        void onError(String mensagem);
    }

    private final Context context;
    private final CacheHelper cache;

    public PerfilRepository(Context context) {
        this.context = context.getApplicationContext();
        this.cache = new CacheHelper(this.context);
    }

    private String chavePerfil() {
        return "perfil_" + SessionManager.getInstance(context).getPacienteId();
    }

    private String chavePagamentos() {
        return "pagamentos_" + SessionManager.getInstance(context).getPacienteId();
    }

    public void carregarPerfil(PerfilCallback cb) {
        PerfilResponse local = cache.buscar(chavePerfil(), PerfilResponse.class);
        cb.onCache(local);

        ApiClient.getApi(context).perfil()
                .enqueue(new retrofit2.Callback<PerfilResponse>() {
                    @Override
                    public void onResponse(Call<PerfilResponse> call, Response<PerfilResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            PerfilResponse fresco = response.body();
                            cache.salvar(chavePerfil(), fresco);
                            cb.onFresh(fresco);
                        } else {
                            cb.onError("Erro do servidor (" + response.code() + ")");
                        }
                    }
                    @Override
                    public void onFailure(Call<PerfilResponse> call, Throwable t) {
                        cb.onError("Falha de conexão");
                    }
                });
    }

    public void carregarPagamentos(PagamentosCallback cb) {
        PagamentosResponse local = cache.buscar(chavePagamentos(), PagamentosResponse.class);
        cb.onCache(local);

        ApiClient.getApi(context).pagamentos()
                .enqueue(new retrofit2.Callback<PagamentosResponse>() {
                    @Override
                    public void onResponse(Call<PagamentosResponse> call, Response<PagamentosResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            PagamentosResponse fresco = response.body();
                            cache.salvar(chavePagamentos(), fresco);
                            cb.onFresh(fresco);
                        } else {
                            cb.onError("Erro do servidor (" + response.code() + ")");
                        }
                    }
                    @Override
                    public void onFailure(Call<PagamentosResponse> call, Throwable t) {
                        cb.onError("Falha de conexão");
                    }
                });
    }
}
