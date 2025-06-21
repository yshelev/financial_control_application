package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.database.entities.Card
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CardsAdapter(
    private val cardsFlow: Flow<List<Card>>,
    private val coroutineScope: CoroutineScope,
    private val onAddCardClicked: () -> Unit,
    private val onDeleteCardClicked: (Card) -> Unit // Добавляем callback для удаления
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var cards: List<Card> = emptyList()

    private val VIEW_TYPE_CARD = 0
    private val VIEW_TYPE_ADD_BUTTON = 1

    init {
        observeCards()
    }

    private fun observeCards() {
        coroutineScope.launch(Dispatchers.Main) {
            cardsFlow.collectLatest { newCards ->
                cards = newCards
                notifyDataSetChanged()
            }
        }
    }

    class CardViewHolder(
        view: View,
        private val onDeleteCardClicked: (Card) -> Unit // Добавляем параметр в конструктор
    ) : RecyclerView.ViewHolder(view) {
        val balanceText: TextView = view.findViewById(R.id.cardBalance)
        val cardName: TextView = view.findViewById(R.id.cardName)
        val cardNumber: TextView = view.findViewById(R.id.cardNumber)
        val cardDate: TextView = view.findViewById(R.id.cardDate)
        val deleteButton: ImageButton = view.findViewById(R.id.deleteButton)

        fun bind(card: Card) {
            balanceText.text = "${card.balance}" // balanceText.text = "${card.balance}${card.currency}"
            cardName.text = card.name
            cardNumber.text = card.maskedNumber
            cardDate.text = card.date
            deleteButton.setOnClickListener { onDeleteCardClicked(card) }
        }
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
            CardViewHolder(view, onDeleteCardClicked) // Передаем callback в ViewHolder
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_add_card, parent, false)
            AddCardViewHolder(view, onAddCardClicked)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is CardViewHolder && position < cards.size) {
            holder.bind(cards[position]) // Используем метод bind
        }
    }

    override fun getItemCount(): Int = cards.size + 1
}