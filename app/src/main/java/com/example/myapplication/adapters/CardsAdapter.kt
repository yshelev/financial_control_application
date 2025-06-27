package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.database.entities.Card
import com.example.myapplication.database.entities.UserTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class CardsAdapter(
    private val cardsFlow: Flow<List<Card>>,
    private val transactionsFlow: Flow<List<UserTransaction>>,
    private val coroutineScope: CoroutineScope,
    private val onAddCardClicked: () -> Unit,
    private val onDeleteCardClicked: (Card) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var cards: List<Card> = emptyList()
    private var transactions: List<UserTransaction> = emptyList()

    private val VIEW_TYPE_CARD = 0
    private val VIEW_TYPE_ADD_BUTTON = 1

    private val currencySymbols = mapOf(
        "RUB" to "₽",
        "USD" to "$",
        "EUR" to "€"
    )

    init {
        observeCards()
        observeTransactions()
    }

    private fun observeCards() {
        coroutineScope.launch(Dispatchers.Main) {
            cardsFlow.collectLatest { newCards ->
                cards = newCards
                notifyDataSetChanged()
            }
        }
    }

    private fun observeTransactions() {
        coroutineScope.launch(Dispatchers.Main) {
            transactionsFlow.collectLatest { newTransactions ->
                transactions = newTransactions
                notifyDataSetChanged()
            }
        }
    }

    class CardViewHolder(
        view: View,
        private val onDeleteCardClicked: (Card) -> Unit
    ) : RecyclerView.ViewHolder(view) {
        val balanceText: TextView = view.findViewById(R.id.cardBalance)
        val cardName: TextView = view.findViewById(R.id.cardName)
        val cardNumber: TextView = view.findViewById(R.id.cardNumber)
        val cardDate: TextView = view.findViewById(R.id.cardDate)
        val deleteButton: ImageButton = view.findViewById(R.id.deleteButton)
        val incomeText: TextView = view.findViewById(R.id.cardIncome)
        val expenseText: TextView = view.findViewById(R.id.cardExpenses)

        fun bind(card: Card, transactions: List<UserTransaction>, currencySymbol: String) {
            balanceText.text = "${card.balance}${currencySymbol}"

            cardName.text = card.name
            cardNumber.text = card.maskedNumber
            cardDate.text = card.date
            deleteButton.setOnClickListener { onDeleteCardClicked(card) }

            // Calculate income and expenses for this card
            val cardTransactions = transactions.filter { it.cardId == card.id }
            val income = cardTransactions.filter { it.isIncome }.sumOf { it.amount }
            val expenses = cardTransactions.filter { !it.isIncome }.sumOf { it.amount }

            val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault())

            incomeText.text = "+${numberFormat.format(income)}$currencySymbol"
            expenseText.text = "-${numberFormat.format(expenses)}$currencySymbol"
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
            CardViewHolder(view, onDeleteCardClicked)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_add_card, parent, false)
            AddCardViewHolder(view, onAddCardClicked)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is CardViewHolder && position < cards.size) {
            val card = cards[position]
            val symbol = currencySymbols[card.currency] ?: "₽" // Получаем символ валюты или используем рубль по умолчанию
            holder.bind(card, transactions, symbol)
        }
    }

    override fun getItemCount(): Int = cards.size + 1
}