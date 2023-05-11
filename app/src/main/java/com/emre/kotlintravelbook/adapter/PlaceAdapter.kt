package com.emre.kotlintravelbook.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.emre.kotlintravelbook.databinding.RecyclerRowBinding
import com.emre.kotlintravelbook.model.Place
import com.emre.kotlintravelbook.view.MapsActivity

class PlaceAdapter(val placeList: List<Place>): RecyclerView.Adapter<PlaceAdapter.PlaceHolder>() {
    class PlaceHolder(val binding: RecyclerRowBinding): RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceHolder { // :PlaceHolder olduğu için return olarak bir PlaceHodler döndürmemiz lazım
        val binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlaceHolder(binding)
    }

    override fun getItemCount(): Int {
        return placeList.size
    }

    override fun onBindViewHolder(holder: PlaceHolder, position: Int) {
        holder.binding.recyclerTextView.text = placeList.get(position).name

        holder.itemView.setOnClickListener {
            val intentToMapsActivity = Intent(holder.itemView.context,MapsActivity::class.java)
            intentToMapsActivity.putExtra("place", placeList.get(position))
            // Sabit bir değer yollanmıyor direkt sınıfı gönderdiğimiz için veriyi çektiğimiz sınıfı Serializable yapmamız lazım
            intentToMapsActivity.putExtra("info","old")
            holder.itemView.context.startActivity(intentToMapsActivity)
        }
    }
}