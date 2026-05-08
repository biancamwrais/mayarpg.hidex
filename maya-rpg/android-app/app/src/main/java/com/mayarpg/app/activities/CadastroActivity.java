package com.mayarpg.app.activities;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mayarpg.app.R;
import com.mayarpg.app.models.CadastroRequest;
import com.mayarpg.app.network.ApiClient;
import com.mayarpg.app.utils.Formatters;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CadastroActivity extends AppCompatActivity {

    private EditText etNome, etEmail, etCpf, etTelefone, etData, etSenha, etConfSenha;
    private CheckBox cbLgpd;
    private Button btnCriar;
    private TextView tvLogin;
    private ImageView ivToggleSenha, ivToggleConfSenha;
    private boolean visivel1 = false, visivel2 = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        etNome = findViewById(R.id.etNome);
        etEmail = findViewById(R.id.etEmail);
        etCpf = findViewById(R.id.etCpf);
        etTelefone = findViewById(R.id.etTelefone);
        etData = findViewById(R.id.etData);
        etSenha = findViewById(R.id.etSenha);
        etConfSenha = findViewById(R.id.etConfSenha);
        cbLgpd = findViewById(R.id.cbLgpd);
        btnCriar = findViewById(R.id.btnCriar);
        tvLogin = findViewById(R.id.tvLogin);
        ivToggleSenha = findViewById(R.id.ivToggleSenha);
        ivToggleConfSenha = findViewById(R.id.ivToggleConfSenha);

        // Máscaras
        Formatters.aplicarMascaraCpf(etCpf);
        Formatters.aplicarMascaraTelefone(etTelefone);
        Formatters.aplicarMascaraData(etData);

        btnCriar.setOnClickListener(v -> criarConta());
        tvLogin.setOnClickListener(v -> finish());

        ivToggleSenha.setOnClickListener(v -> {
            visivel1 = !visivel1;
            etSenha.setInputType(visivel1
                    ? InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            etSenha.setSelection(etSenha.getText().length());
        });
        ivToggleConfSenha.setOnClickListener(v -> {
            visivel2 = !visivel2;
            etConfSenha.setInputType(visivel2
                    ? InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            etConfSenha.setSelection(etConfSenha.getText().length());
        });
    }

    private void criarConta() {
        String nome = etNome.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String cpf = etCpf.getText().toString().trim();
        String tel = etTelefone.getText().toString().trim();
        String data = etData.getText().toString().trim();
        String senha = etSenha.getText().toString();
        String conf = etConfSenha.getText().toString();

        if (nome.length() < 3) { etNome.setError("Informe seu nome completo"); return; }
        if (!Formatters.emailValido(email)) { etEmail.setError("E-mail inválido"); return; }
        if (cpf.replaceAll("\\D","").length() != 11) { etCpf.setError("CPF inválido"); return; }
        if (senha.length() < 6) { etSenha.setError("Mínimo 6 caracteres"); return; }
        if (!senha.equals(conf)) { etConfSenha.setError("Senhas não conferem"); return; }
        if (!cbLgpd.isChecked()) {
            Toast.makeText(this, "Você precisa aceitar os termos LGPD", Toast.LENGTH_LONG).show();
            return;
        }

        CadastroRequest req = new CadastroRequest();
        req.nome = nome;
        req.email = email;
        req.senha = senha;
        req.cpf = cpf;
        req.telefone = tel;
        req.dataNascimento = Formatters.dataBrParaIso(data);
        req.aceitouLgpd = true;

        btnCriar.setEnabled(false);
        btnCriar.setText("Aguarde...");

        ApiClient.getApi(this).cadastro(req).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                btnCriar.setEnabled(true);
                btnCriar.setText(R.string.cadastro_btn);

                if (response.isSuccessful()) {
                    Toast.makeText(CadastroActivity.this,
                            "Cadastro realizado! Faça login para continuar.",
                            Toast.LENGTH_LONG).show();
                    finish();
                } else if (response.code() == 409) {
                    Toast.makeText(CadastroActivity.this,
                            "E-mail já cadastrado", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(CadastroActivity.this,
                            "Erro no cadastro (" + response.code() + ")", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                btnCriar.setEnabled(true);
                btnCriar.setText(R.string.cadastro_btn);
                Toast.makeText(CadastroActivity.this,
                        "Falha de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
