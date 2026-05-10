package com.mayarpg.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mayarpg.app.R;

import java.util.List;

public class CategoriaAdapter extends RecyclerView.Adapter<CategoriaAdapter.VH> {

    public static class Categoria {
        public String nome;
        public int iconeRes;
        public int bgRes;


        public Categoria(String nome, @DrawableRes int bgRes, @DrawableRes int iconeRes) {
            this.nome = nome;
            this.bgRes = bgRes;
            this.iconeRes = iconeRes;
        }
    }

    private final List<Categoria> itens;
    private final OnClick listener;

    public interface OnClick { void onClick(Categoria c); }

    public CategoriaAdapter(List<Categoria> itens, OnClick l) {
        this.itens = itens;
        this.listener = l;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_categoria, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Categoria c = itens.get(pos);
        h.tvNome.setText(c.nome);


        if (c.bgRes != 0) {
            h.circle.setBackgroundResource(c.bgRes);
        }


        if (c.iconeRes != 0) {
            h.ivIcone.setImageResource(c.iconeRes);
            h.ivIcone.setVisibility(View.VISIBLE);
        } else {
            h.ivIcone.setVisibility(View.GONE);
        }

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(c);
        });
    }

    @Override public int getItemCount() { return itens.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvNome;
        FrameLayout circle;
        ImageView ivIcone;
        VH(View v) {
            super(v);
            tvNome = v.findViewById(R.id.tvNomeCategoria);
            circle = v.findViewById(R.id.circleBg);
            ivIcone = v.findViewById(R.id.ivIcone);
        }
    }
}
