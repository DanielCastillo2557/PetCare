package com.example.petcareapp.adapters

import android.content.Context
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.BaseAdapter
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.petcareapp.R

class FotoAdapter(
    private val context: Context,
    private val fotos: List<String>,
    private val onAgregarFotoClick: () -> Unit
) : BaseAdapter() {

    override fun getCount(): Int = fotos.size

    override fun getItem(position: Int): Any = fotos[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val imageView = ImageView(context)
        val sizeInPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            100f,
            context.resources.displayMetrics
        ).toInt()

        imageView.layoutParams = AbsListView.LayoutParams(sizeInPx, sizeInPx)
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        imageView.setPadding(8, 8, 8, 8)

        val url = fotos[position]

        if (url == "boton" && onAgregarFotoClick != {}) {
            imageView.setImageResource(R.drawable.ic_agregar_foto_24px) // tu ícono de cámara
            imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
            imageView.setOnClickListener { onAgregarFotoClick() }
        } else {
            Glide.with(context)
                .load(url)
                .placeholder(R.drawable.ic_agregar_foto_24px)
                .into(imageView)
        }

        return imageView
    }
}
