package com.crystal.airbnb

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class HouseViewPagerAdapter(val itemClicked: (HouseModel)-> Unit): ListAdapter<HouseModel, HouseViewPagerAdapter.ViewHolder>(diffUtil) {
    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(houseModel: HouseModel){
            view.findViewById<TextView>(R.id.titleTextView).text = houseModel.title
            view.findViewById<TextView>(R.id.priceTextView).text = houseModel.price

            val thumbnailImageView = view.findViewById<ImageView>(R.id.thumbnailImageView)
            Glide.with(thumbnailImageView.context)
                .load(houseModel.imgUrl)
                .into(thumbnailImageView)
            view.setOnClickListener {
                itemClicked(houseModel)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater.inflate(R.layout.item_house_detail_for_viewpager, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        val diffUtil = object:DiffUtil.ItemCallback<HouseModel>(){
            override fun areItemsTheSame(oldItem: HouseModel, newItem: HouseModel): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: HouseModel, newItem: HouseModel): Boolean {
                return oldItem == newItem
            }

        }
    }

}