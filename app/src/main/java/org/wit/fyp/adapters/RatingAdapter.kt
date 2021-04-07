package org.wit.fyp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.recycler_rating_view_card.view.*
import org.wit.fyp.R
import org.wit.fyp.models.RatingModel

class RatingAdapter(var list:ArrayList<RatingModel>, private val listener: OnItemClickListener) : RecyclerView.Adapter<RatingAdapter.ViewHolder>(){

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener{
        var ratingDetails = itemView.list_rating_details
        var ratingAuthor = itemView.list_rating_author_name
        var ratingValue = itemView.list_rating_bar_small

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?){
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
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.recycler_rating_view_card, parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int){
        holder.ratingDetails.text = list[position].ratingDetails
        holder.ratingAuthor.text = "Rated By: " + list[position].authorName
        holder.ratingValue.rating = list[position].ratingNumber.toFloat()

    }
}