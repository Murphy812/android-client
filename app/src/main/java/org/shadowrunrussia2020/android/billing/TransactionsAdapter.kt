package org.shadowrunrussia2020.android.billing

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import org.ocpsoft.prettytime.PrettyTime
import org.shadowrunrussia2020.android.R
import org.shadowrunrussia2020.android.common.models.Transaction
import java.util.*

class TransactionsAdapter : RecyclerView.Adapter<TransactionsAdapter.ViewHolder>() {
    private var mDataset: List<Transaction> = ArrayList()

    fun setData(newData: List<Transaction>) {
        this.mDataset = newData
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.billing_recycler_view_item, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = mDataset[position]
        if (transaction.transferType == "Incoming") {
            holder.mDirectionView.setImageResource(R.drawable.ic_transfer_to)
            holder.mCommentView.text = "${commentInBrackets(transaction)}"
            holder.mAmountView.text = transaction.amount.toString()
        } else {
            holder.mDirectionView.setImageResource(R.drawable.ic_transfer_from)
            holder.mCommentView.text = "${commentInBrackets(transaction)}"
            holder.mAmountView.text = (-transaction.amount).toString()
        }
        holder.mTimeView.text = PrettyTime(Locale("ru")).format(transaction.operationTime)
        holder.itemView.setOnClickListener {
            it.findNavController().navigate(
                BillingFragmentDirections.actionTransactionDetails(
                    transaction
                )
            )
        }
    }

    private fun commentInBrackets(t: Transaction): String {
        val comment = t.comment
        if (comment == null || comment.isEmpty()) return ""
        return "($comment)"
    }

    override fun getItemCount(): Int {
        return mDataset.size
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var mDirectionView: ImageView = itemView.findViewById(R.id.direction)
        var mAmountView: TextView = itemView.findViewById(R.id.amount)
        var mCommentView: TextView = itemView.findViewById(R.id.comment)
        var mTimeView: TextView = itemView.findViewById(R.id.time)
    }
}
