package com.mayarpg.app.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class Formatters {

    public static String formatDataBr(String iso) {
        if (iso == null || iso.isEmpty()) return "";
        try {
            SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            SimpleDateFormat out = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
            return out.format(in.parse(iso.substring(0, 10)));
        } catch (Exception e) {
            return iso;
        }
    }

    public static String formatDataHora(String iso) {
        if (iso == null || iso.isEmpty()) return "";
        try {
            SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            SimpleDateFormat out = new SimpleDateFormat("dd/MM 'às' HH:mm", new Locale("pt", "BR"));
            return out.format(in.parse(iso.replace("T", " ").substring(0, 19)));
        } catch (Exception e) {
            return iso;
        }
    }

    public static String iniciais(String nome) {
        if (nome == null || nome.trim().isEmpty()) return "?";
        String[] partes = nome.trim().split("\\s+");
        if (partes.length == 1) return partes[0].substring(0, 1).toUpperCase();
        return (partes[0].charAt(0) + "" + partes[partes.length - 1].charAt(0)).toUpperCase();
    }

    public static boolean emailValido(String email) {
        return email != null && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }


    public static void aplicarMascaraCpf(final EditText et) {
        et.addTextChangedListener(new TextWatcher() {
            boolean ignorar = false;
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                if (ignorar) return;
                String digitos = s.toString().replaceAll("\\D", "");
                if (digitos.length() > 11) digitos = digitos.substring(0, 11);
                StringBuilder fmt = new StringBuilder();
                for (int i = 0; i < digitos.length(); i++) {
                    if (i == 3 || i == 6) fmt.append('.');
                    if (i == 9) fmt.append('-');
                    fmt.append(digitos.charAt(i));
                }
                ignorar = true;
                et.setText(fmt.toString());
                et.setSelection(et.getText().length());
                ignorar = false;
            }
        });
    }


    public static void aplicarMascaraTelefone(final EditText et) {
        et.addTextChangedListener(new TextWatcher() {
            boolean ignorar = false;
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                if (ignorar) return;
                String d = s.toString().replaceAll("\\D", "");
                if (d.length() > 11) d = d.substring(0, 11);
                StringBuilder fmt = new StringBuilder();
                for (int i = 0; i < d.length(); i++) {
                    if (i == 0) fmt.append('(');
                    if (i == 2) fmt.append(") ");
                    if (i == 7 && d.length() == 11) fmt.append('-');
                    else if (i == 6 && d.length() == 10) fmt.append('-');
                    fmt.append(d.charAt(i));
                }
                ignorar = true;
                et.setText(fmt.toString());
                et.setSelection(et.getText().length());
                ignorar = false;
            }
        });
    }


    public static void aplicarMascaraData(final EditText et) {
        et.addTextChangedListener(new TextWatcher() {
            boolean ignorar = false;
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                if (ignorar) return;
                String d = s.toString().replaceAll("\\D", "");
                if (d.length() > 8) d = d.substring(0, 8);
                StringBuilder fmt = new StringBuilder();
                for (int i = 0; i < d.length(); i++) {
                    if (i == 2 || i == 4) fmt.append('/');
                    fmt.append(d.charAt(i));
                }
                ignorar = true;
                et.setText(fmt.toString());
                et.setSelection(et.getText().length());
                ignorar = false;
            }
        });
    }


    public static String dataBrParaIso(String br) {
        if (br == null) return null;
        String d = br.replaceAll("\\D", "");
        if (d.length() != 8) return null;
        return d.substring(4) + "-" + d.substring(2, 4) + "-" + d.substring(0, 2);
    }
}
