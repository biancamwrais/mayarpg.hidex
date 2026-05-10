package com.mayarpg.app.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.mayarpg.app.R;
import com.mayarpg.app.fragments.AgendamentoFragment;
import com.mayarpg.app.fragments.ExerciciosFragment;
import com.mayarpg.app.fragments.HistoricoFragment;
import com.mayarpg.app.fragments.HomeFragment;
import com.mayarpg.app.fragments.NotificacoesFragment;
import com.mayarpg.app.fragments.PerfilFragment;
import com.mayarpg.app.utils.SessionManager;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Aplica o tema
        boolean dark = SessionManager.getInstance(this).isDarkMode();
        AppCompatDelegate.setDefaultNightMode(
                dark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment f;
            if (id == R.id.nav_home) {
                f = new HomeFragment();
            } else if (id == R.id.nav_historico) {
                f = new HistoricoFragment();
            } else if (id == R.id.nav_exercicios) {
                f = ExerciciosFragment.novaInstancia(null);
            } else if (id == R.id.nav_notificacoes) {
                f = new NotificacoesFragment();
            } else if (id == R.id.nav_agendamento) {
                f = new AgendamentoFragment();
            } else {
                return false;
            }
            trocarFragment(f);
            return true;
        });

        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
    }

    private void trocarFragment(Fragment f) {
        getSupportFragmentManager().popBackStack(null,
                androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, f)
                .commit();
    }

    public void selecionarAba(int menuId) {
        bottomNav.setSelectedItemId(menuId);
    }


    public void abrirPerfil() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new PerfilFragment())
                .addToBackStack("perfil")
                .commit();
    }
}
