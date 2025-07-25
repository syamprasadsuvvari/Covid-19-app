package com.example.covid_19;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CardSliderAdapter extends RecyclerView.Adapter<CardSliderAdapter.ViewHolder> {

    private List<SlideCard> cardList;
    private Context context;

    public interface OnCardClickListener {
        void onCardClick(SlideCard card);
    }

    private OnCardClickListener listener;

    public CardSliderAdapter(List<SlideCard> cardList, Context context, OnCardClickListener listener) {
        this.cardList = cardList;
        this.context = context;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textCard;
        ImageView cardIcon;

        public ViewHolder(View view) {
            super(view);
            textCard = view.findViewById(R.id.textCard);
            cardIcon = view.findViewById(R.id.cardIcon);
        }
    }

    @Override
    public CardSliderAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CardSliderAdapter.ViewHolder holder, int position) {
        SlideCard card = cardList.get(position);
        holder.textCard.setText(card.title);
        holder.cardIcon.setImageResource(card.imageResId);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCardClick(card);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cardList.size();
    }
}
