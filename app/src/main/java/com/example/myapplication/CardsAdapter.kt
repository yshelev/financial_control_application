package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CardsAdapter(private val cards: List<Card>) :
    RecyclerView.Adapter<CardsAdapter.CardViewHolder>() {

    class CardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val balanceText: TextView = view.findViewById(R.id.cardBalance)
        val cardName: TextView = view.findViewById(R.id.cardName)
        val cardNumber: TextView = view.findViewById(R.id.cardNumber)
        val cardDate: TextView = view.findViewById(R.id.cardDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card, parent, false)
        return CardViewHolder(view)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val card = cards[position]
        holder.balanceText.text = "${card.balance}₽"
        holder.cardName.text = card.name
        holder.cardNumber.text = card.maskedNumber
        holder.cardDate.text = card.date
    }

    override fun getItemCount(): Int = cards.size
}
