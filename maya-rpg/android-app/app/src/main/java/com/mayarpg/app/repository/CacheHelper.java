package com.mayarpg.app.repository;

import android.content.Context;

import com.google.gson.Gson;
import com.mayarpg.app.local.AppDatabase;
import com.mayarpg.app.local.dao.CacheJsonDao;
import com.mayarpg.app.local.entities.CacheJson;

import java.lang.reflect.Type;


public class CacheHelper {

    private final CacheJsonDao dao;
    private final Gson gson = new Gson();

    public CacheHelper(Context context) {
        this.dao = AppDatabase.get(context.getApplicationContext()).cacheJsonDao();
    }


    public <T> void salvar(String chave, T objeto) {
        if (objeto == null) return;
        CacheJson c = new CacheJson();
        c.chave = chave;
        c.json = gson.toJson(objeto);
        c.atualizadoEm = System.currentTimeMillis();
        dao.salvar(c);
    }


    public <T> T buscar(String chave, Class<T> classe) {
        CacheJson c = dao.buscar(chave);
        if (c == null) return null;
        try {
            return gson.fromJson(c.json, classe);
        } catch (Exception e) {
            return null;
        }
    }


    public <T> T buscar(String chave, Type tipo) {
        CacheJson c = dao.buscar(chave);
        if (c == null) return null;
        try {
            return gson.fromJson(c.json, tipo);
        } catch (Exception e) {
            return null;
        }
    }

    public void apagar(String chave) {
        dao.apagar(chave);
    }

    public void limparTudo() {
        dao.limparTudo();
    }
}
