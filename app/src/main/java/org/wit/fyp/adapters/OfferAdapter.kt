package org.wit.fyp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.recycler_offer_car_view.view.*
import org.wit.fyp.R
import org.wit.fyp.models.OfferModel

class OfferAdapter(var list:ArrayList<OfferModel>, private val listener: OnItemClickListener) : RecyclerView.Adapter<OfferAdapter.ViewHolder>(){

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener{
        var offerName = itemView.list_offers_name
        var offerAmount = itemView.list_offers_amount

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            if(position != RecyclerView.NO_POSITION){
                listener.onItemClick(position)
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.recycler_offer_car_view, parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.offerName.text = list[position].authorName
        holder.offerAmount.text = "€" + list[position].amount
    }
}