package adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cometchat.pro.uikit.R
import com.cometchat.pro.uikit.sticker.model.Sticker
import java.util.*

class StickersAdapter(context: Context?, stickerArrayList: List<Sticker>?) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var context: Context? = null

    private var stickerArrayList: List<Sticker> = ArrayList<Sticker>()

    private var TAG = "StickerAdapter"

    private var STICKER_IMAGE = 1

    /**
     * It is a contructor which is used to initialize wherever we needed.
     *
     * @param context is a object of Context.
     */
    init {
        setStickerList(stickerArrayList!!)
        this.context = context
    }

//    /**
//     * It is constructor which takes stickerArrayList as parameter and bind it with stickerArrayList in adapter.
//     *
//     * @param context          is a object of Context.
//     * @param stickerArrayList is a list of stickers used in this adapter.
//     */
//    constructor(context: Context?, stickerArrayList: List<Sticker>?) {
//        setStickerList(stickerArrayList!!)
//        this.context = context
//    }

    private fun setStickerList(stickerArrayList: List<Sticker>) {
        Collections.sort(stickerArrayList, object : Comparator<Sticker?> {
            override fun compare(sticker: Sticker?, o2: Sticker?): Int {
                return sticker?.setName!!.compareTo(o2?.setName!!)
            }
        })
        this.stickerArrayList = stickerArrayList
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view: View = layoutInflater.inflate(R.layout.stickers_row, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        setStickerData(holder as ImageViewHolder, position)
    }

    override fun getItemCount(): Int {
        return stickerArrayList.size
    }
    private fun setStickerData(viewHolder: ImageViewHolder, i: Int) {
        val sticker = stickerArrayList[i]
        Glide.with(context!!).asBitmap().load(sticker.url)
                .into(viewHolder.imageView)
        viewHolder.itemView.setTag(R.string.sticker, sticker)
    }


    internal class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)

    }
}