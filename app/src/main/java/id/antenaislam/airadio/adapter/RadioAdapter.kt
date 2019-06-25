package id.antenaislam.airadio.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import id.antenaislam.airadio.BuildConfig
import id.antenaislam.airadio.R
import id.antenaislam.airadio.model.Radio
import kotlinx.android.synthetic.main.item_radio.view.*

class RadioAdapter(private val context: Context, private val radios: List<Radio>, private val listener: PlayerListener) : RecyclerView.Adapter<RadioAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                LayoutInflater.from(context).inflate(R.layout.item_radio, parent, false)
        )
    }

    override fun getItemCount(): Int = radios.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.tv_title.text = radios[position].title

        Glide.with(context)
                .load("${BuildConfig.BASE_URL}assets/${radios[position].poster}")
                .into(holder.itemView.iv_poster)

        holder.itemView.container_radio.setOnClickListener {
            listener.onRadioClicked(radios[position])
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    interface PlayerListener {
        fun onRadioClicked(radio: Radio)
    }
}