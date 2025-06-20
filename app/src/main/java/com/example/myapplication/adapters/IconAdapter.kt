package com.example.myapplication

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.BaseAdapter
import android.widget.ImageView

class IconAdapter(private val context: Context, private val icons: List<Int>) : BaseAdapter() {
    override fun getCount(): Int = icons.size
    override fun getItem(position: Int): Any = icons[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val imageView = ImageView(context)
        imageView.setImageResource(icons[position])
        imageView.layoutParams = AbsListView.LayoutParams(100, 100)
        imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
        return imageView
    }
}
