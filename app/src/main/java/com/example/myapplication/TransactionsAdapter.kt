package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.database.entities.UserTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TransactionsAdapter(
    private val lifecycleOwner: LifecycleOwner,
    private val transactionsFlow: Flow<List<UserTransaction>>
) : RecyclerView.Adapter<TransactionsAdapter.TransactionViewHolder>() {

    private val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private var transactions: List<UserTransaction> = emptyList()

    init {
        lifecycleOwner.lifecycleScope.launch {
            transactionsFlow.collectLatest { newTransactions ->
                transactions = newTransactions
                notifyDataSetChanged()
            }
        }
    }

    class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.transactionIcon)
        val title: TextView = view.findViewById(R.id.transactionTitle)
        val date: TextView = view.findViewById(R.id.transactionDate)
        val amount: TextView = view.findViewById(R.id.transactionAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val tx = transactions[position]
        holder.title.text = tx.category
        holder.date.text = dateFormatter.format(Date(tx.date))
        holder.amount.text = if (tx.isIncome) "+${tx.amount}${tx.currency}" else "-${tx.amount}${tx.currency}"
        holder.amount.setTextColor(
            holder.itemView.context.getColor(
                if (tx.isIncome) R.color.green else R.color.red
            )
        )
        holder.icon.setImageResource(tx.iconResId)
    }

    override fun getItemCount(): Int = transactions.size
}