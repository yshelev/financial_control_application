package com.example.myapplication

import Card
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CardsAdapter(
    private val cards: List<Card>,
    private val onAddCardClicked: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_CARD = 0
    private val VIEW_TYPE_ADD_BUTTON = 1

    class CardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val balanceText: TextView = view.findViewById(R.id.cardBalance)
        val cardName: TextView = view.findViewById(R.id.cardName)
        val cardNumber: TextView = view.findViewById(R.id.cardNumber)
        val cardDate: TextView = view.findViewById(R.id.cardDate)
    }

    class AddCardViewHolder(view: View, onAddCardClicked: () -> Unit) : RecyclerView.ViewHolder(view) {
        init {
            view.setOnClickListener {
                onAddCardClicked()
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < cards.size) VIEW_TYPE_CARD else VIEW_TYPE_ADD_BUTTON
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_CARD) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_card, parent, false)
            CardViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_add_card, parent, false)  // layout с кнопкой "Добавить карту"
            AddCardViewHolder(view, onAddCardClicked)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is CardViewHolder && position < cards.size) {
            val card = cards[position]
            holder.balanceText.text = "${card.balance}₽"
            holder.cardName.text = card.name
            holder.cardNumber.text = card.maskedNumber
            holder.cardDate.text = card.date
        }
    }

    override fun getItemCount(): Int = cards.size + 1
}
