package org.wit.fyp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.recycler_offer_car_view.view.*
import org.wit.fyp.R
import org.wit.fyp.models.OfferModel

class OfferAdapter(var list:ArrayList<OfferModel>) : RecyclerView.Adapter<OfferAdapter.ViewHolder>(){

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var offerName = itemView.list_offers_name
        var offerAmount = itemView.list_offers_amount
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.recycler_offer_car_view, parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.offerName.text = list[position].authorName
        holder.offerAmount.text = list[position].amount
    }
}