package com.mayarpg.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mayarpg.app.R;
import com.mayarpg.app.models.LoginRequest;
import com.mayarpg.app.models.LoginResponse;
import com.mayarpg.app.network.ApiClient;
import com.mayarpg.app.utils.Formatters;
import com.mayarpg.app.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etSenha;
    private Button btnEntrar;
    private TextView tvCadastrar, tvEsqueceu;
    private ImageView ivToggleSenha;
    private ProgressBar progress;
    private boolean senhaVisivel = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etSenha = findViewById(R.id.etSenha);
        btnEntrar = findViewById(R.id.btnEntrar);
        tvCadastrar = findViewById(R.id.tvCadastrar);
        tvEsqueceu = findViewById(R.id.tvEsqueceu);
        ivToggleSenha = findViewById(R.id.ivToggleSenha);
        progress = findViewById(R.id.progress);

        btnEntrar.setOnClickListener(v -> fazerLogin());
        tvCadastrar.setOnClickListener(v ->
                startActivity(new Intent(this, CadastroActivity.class)));
        tvEsqueceu.setOnClickListener(v ->
                Toast.makeText(this, "Recuperação de senha em breve", Toast.LENGTH_SHORT).show());
        ivToggleSenha.setOnClickListener(v -> alternarSenha());
    }

    private void alternarSenha() {
        senhaVisivel = !senhaVisivel;
        etSenha.setInputType(senhaVisivel
                ? InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        etSenha.setSelection(etSenha.getText().length());
    }

    private void fazerLogin() {
        String email = etEmail.getText().toString().trim();
        String senha = etSenha.getText().toString();

        if (!Formatters.emailValido(email)) {
            etEmail.setError("E-mail inválido");
            return;
        }
        if (senha.length() < 6) {
            etSenha.setError("Senha deve ter no mínimo 6 caracteres");
            return;
        }

        setLoading(true);
        ApiClient.getApi(this).login(new LoginRequest(email, senha)).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse lr = response.body();
                    SessionManager.getInstance(LoginActivity.this).salvarLogin(
                            lr.token, lr.usuario.id, lr.usuario.nome, lr.usuario.email, lr.usuario.pacienteId
                    );
                    Intent i = new Intent(LoginActivity.this, MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    finish();
                } else if (response.code() == 401) {
                    Toast.makeText(LoginActivity.this, "E-mail ou senha incorretos", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(LoginActivity.this, "Erro ao fazer login (" + response.code() + ")", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                setLoading(false);
                Toast.makeText(LoginActivity.this,
                        "Falha de conexão. Verifique se o servidor está rodando.",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnEntrar.setEnabled(!loading);
        btnEntrar.setText(loading ? "Aguarde..." : getString(R.string.login_entrar));
    }
}
