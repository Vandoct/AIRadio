package id.antenaislam.airadio.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.antenaislam.airadio.R
import id.antenaislam.airadio.model.Data
import kotlinx.android.synthetic.main.item_main.view.*

class MainAdapter(private val data: List<Data>, private val listener: RadioAdapter.PlayerListener) : RecyclerView.Adapter<MainAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_main, parent, false)
        )
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.tv_category.text = data[position].category.capitalize()

        val context = holder.itemView.rv_radio.context
        val childLayoutManager = LinearLayoutManager(context, LinearLayout.HORIZONTAL, false)

        childLayoutManager.initialPrefetchItemCount = 4

        holder.itemView.rv_radio.apply {
            layoutManager = childLayoutManager
            adapter = RadioAdapter(context, data[position].radio, listener)
            setRecycledViewPool(RecyclerView.RecycledViewPool())
        }

        // Set more listener
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}