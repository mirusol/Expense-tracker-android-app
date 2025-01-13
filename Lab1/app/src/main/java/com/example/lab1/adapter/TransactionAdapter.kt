package com.example.lab1.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.lab1.R
import com.example.lab1.database.Transaction

class TransactionAdapter(
    private val transactions: MutableList<Transaction>,
    private val onDeleteClick: (Transaction) -> Unit,
    private val onUpdateClick: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val description: EditText = itemView.findViewById(R.id.et_transaction_description)
        val amount: EditText = itemView.findViewById(R.id.et_transaction_amount)
        val categorySpinner: Spinner = itemView.findViewById(R.id.spinner_transaction_category)
        val editButton: Button = itemView.findViewById(R.id.btn_edit_transaction)
        val updateButton: Button = itemView.findViewById(R.id.btn_update_transaction)
        val deleteButton: Button = itemView.findViewById(R.id.btn_delete_transaction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = transactions[position]


        holder.description.setText(transaction.description)
        holder.amount.setText(transaction.amount.toString())


        val categories = arrayOf("Rent", "Shopping", "Groceries", "Tech", "Online Purchases", "Entertainment")
        val adapter = ArrayAdapter(holder.itemView.context, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        holder.categorySpinner.adapter = adapter


        val categoryIndex = categories.indexOf(transaction.category)
        if (categoryIndex != -1) {
            holder.categorySpinner.setSelection(categoryIndex)
        }


        holder.description.isEnabled = false
        holder.amount.isEnabled = false
        holder.categorySpinner.isEnabled = false


        holder.editButton.setOnClickListener {

            holder.description.isEnabled = true
            holder.amount.isEnabled = true
            holder.categorySpinner.isEnabled = true

            holder.editButton.visibility = View.GONE
            holder.updateButton.visibility = View.VISIBLE
        }


        holder.updateButton.setOnClickListener {
            val updatedDescription = holder.description.text.toString()
            val updatedAmount = holder.amount.text.toString().toDoubleOrNull()
            val updatedCategory = holder.categorySpinner.selectedItem.toString()

            if (updatedDescription.isBlank() || updatedAmount == null) {
                Toast.makeText(
                    holder.itemView.context,
                    "Please fill out all fields correctly.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {

                transaction.description = updatedDescription
                transaction.amount = updatedAmount
                transaction.category = updatedCategory

                holder.description.isEnabled = false
                holder.amount.isEnabled = false
                holder.categorySpinner.isEnabled = false
                holder.editButton.visibility = View.VISIBLE
                holder.updateButton.visibility = View.GONE

                onUpdateClick(transaction)
            }
        }

        holder.deleteButton.setOnClickListener {
            onDeleteClick(transaction)
        }
    }

    override fun getItemCount(): Int {
        return transactions.size
    }

    fun updateTransaction(updatedTransaction: Transaction) {
        val index = transactions.indexOfFirst { it.id == updatedTransaction.id }
        if (index != -1) {
            transactions[index] = updatedTransaction
            notifyItemChanged(index)
        }
    }

    fun removeTransaction(transaction: Transaction) {
        val index = transactions.indexOf(transaction)
        if (index != -1) {
            transactions.removeAt(index)
            notifyItemRemoved(index)
        }
    }
}
