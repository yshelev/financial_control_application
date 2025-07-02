package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.database.entities.Category
import com.example.myapplication.database.entities.UserTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.*

class TransactionsAdapter(
    lifecycleOwner: LifecycleOwner,
    transactionsFlow: Flow<List<UserTransaction>>,
    categoriesFlow: Flow<List<Category>>, // Добавляем Flow категорий
    private val onDeleteClicked: (UserTransaction) -> Unit
) : RecyclerView.Adapter<TransactionsAdapter.TransactionViewHolder>() {

    private val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val currencySymbols = mapOf(
        "RUB" to "₽",
        "USD" to "$",
        "EUR" to "€"
    )

    var transactions: List<UserTransaction> = emptyList()
        private set

    var categories: List<Category> = emptyList() // Добавляем список категорий
        private set

    init {
        lifecycleOwner.lifecycleScope.launch {
            // Подписываемся на обновления транзакций
            transactionsFlow.collectLatest { newTransactions ->
                transactions = newTransactions
                notifyDataSetChanged()
            }
        }

        lifecycleOwner.lifecycleScope.launch {
            // Подписываемся на обновления категорий
            categoriesFlow.collectLatest { newCategories ->
                categories = newCategories
                notifyDataSetChanged()
            }
        }
    }

    class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.transactionIcon)
        val title: TextView = view.findViewById(R.id.transactionTitle)
        val date: TextView = view.findViewById(R.id.transactionDate)
        val amount: TextView = view.findViewById(R.id.transactionAmount)
        val deleteButton: ImageButton = view.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    fun formatAmount(amount: Double): String {
        val symbols = DecimalFormatSymbols(Locale.getDefault()).apply {
            groupingSeparator = ' '
            decimalSeparator = '.'
        }
        val decimalFormat = DecimalFormat("#,###.##", symbols).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
        return decimalFormat.format(amount)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val tx = transactions[position]

        val category = categories.find { it.id == tx.categoryId }

        holder.title.text = category?.title ?: "Unknown"
        holder.date.text = dateFormatter.format(Date(tx.date))
        val symbol = currencySymbols[tx.currency] ?: "₽"

        val formattedAmount = formatAmount(tx.amount)
        holder.amount.text = if (tx.isIncome) "+$formattedAmount$symbol" else "-$formattedAmount$symbol"

        holder.amount.setTextColor(
            holder.itemView.context.getColor(
                if (tx.isIncome) R.color.green else R.color.red
            )
        )

        category?.iconResId?.let { resId ->
            holder.icon.setImageResource(resId)
        } ?: holder.icon.setImageResource(R.drawable.ic_default)

        holder.deleteButton.setOnClickListener {
            onDeleteClicked(tx)
        }
    }

    override fun getItemCount(): Int = transactions.size

    fun updateTransactions(newTransactions: List<UserTransaction>) {
        transactions = newTransactions
        notifyDataSetChanged()
    }
}