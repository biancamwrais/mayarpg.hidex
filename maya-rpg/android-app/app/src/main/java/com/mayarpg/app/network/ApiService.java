package com.mayarpg.app.network;

import com.mayarpg.app.models.AgendamentoRequest;
import com.mayarpg.app.models.CadastroRequest;
import com.mayarpg.app.models.DashboardResponse;
import com.mayarpg.app.models.ExecucaoRequest;
import com.mayarpg.app.models.ExerciciosResponse;
import com.mayarpg.app.models.HistoricoResponse;
import com.mayarpg.app.models.HorarioDisponivel;
import com.mayarpg.app.models.LoginRequest;
import com.mayarpg.app.models.LoginResponse;
import com.mayarpg.app.models.Notificacao;
import com.mayarpg.app.models.PagamentosResponse;
import com.mayarpg.app.models.PerfilResponse;
import com.mayarpg.app.models.Servico;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // Autenticacao
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest body);

    @POST("auth/cadastro")
    Call<Map<String, String>> cadastro(@Body CadastroRequest body);

    // Home
    @GET("pacientes/me/dashboard")
    Call<DashboardResponse> dashboard();

    // Exercicios
    @GET("pacientes/me/exercicios")
    Call<ExerciciosResponse> meusExercicios();

    @POST("execucoes")
    Call<Map<String, Object>> registrarExecucao(@Body ExecucaoRequest body);

    // Historico
    @GET("pacientes/me/historico")
    Call<HistoricoResponse> historico();

    // Notificacoes
    @GET("pacientes/me/notificacoes")
    Call<List<Notificacao>> notificacoes();

    @PUT("notificacoes/{id}/lida")
    Call<Map<String, Object>> marcarLida(@Path("id") int id);

    @DELETE("notificacoes")
    Call<Map<String, Object>> limparNotificacoes();

    // Agendamento
    @GET("servicos")
    Call<List<Servico>> servicos();

    @GET("agendamentos/horarios")
    Call<List<HorarioDisponivel>> horariosDisponiveis(@Query("data") String data);

    @POST("agendamentos")
    Call<Map<String, Object>> criarAgendamento(@Body AgendamentoRequest body);

    // Perfil
    @GET("pacientes/me/perfil")
    Call<PerfilResponse> perfil();

    @GET("pacientes/me/pagamentos")
    Call<PagamentosResponse> pagamentos();
}
