package org.wit.fyp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.recycler_card_view.view.*
import org.wit.fyp.R
import org.wit.fyp.models.RequestModel

class RequestAdapter(var list:ArrayList<RequestModel>, private val listener: OnItemClickListener) : RecyclerView.Adapter<RequestAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener{
        var title = itemView.list_request_title
        var details = itemView.list_request_details
        var deadline = itemView.list_request_deadline
        var location = itemView.list_request_location
        var requestName = itemView.list_request_poster_name
        var reqId = itemView.list_request_request_id

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            if(position != RecyclerView.NO_POSITION) {
                listener.onItemClick(position)
            }
        }

    }

    interface OnItemClickListener{
        fun onItemClick(position: Int)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.recycler_card_view, parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.title.text = list[position].requestTitle
        holder.details.text = list[position].requestDetails
        holder.deadline.text = "Accepting offers until: " + list[position].requestDeadline
        holder.location.text = list[position].requestLocation
        holder.requestName.text = "Posted by: " + list[position].authorName
        holder.reqId.text = list[position].reqId

    }

}