package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class TransactionsAdapter(private val transactions: List<Transaction>) :
    RecyclerView.Adapter<TransactionsAdapter.TransactionViewHolder>() {

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
        holder.date.text = tx.date
        holder.amount.text = if (tx.isIncome) "+${tx.amount}₽" else "-${tx.amount}₽"
        holder.amount.setTextColor(
            holder.itemView.context.getColor(
                if (tx.isIncome) R.color.green else R.color.red
            )
        )
        holder.icon.setImageResource(tx.iconResId)
    }

    override fun getItemCount(): Int = transactions.size
}
