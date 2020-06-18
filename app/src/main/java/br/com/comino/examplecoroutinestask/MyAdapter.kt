/*
 * Created by Murillo Comino on 17/06/20 13:18
 * Github: github.com/onimur
 * StackOverFlow: pt.stackoverflow.com/users/128573
 * Email: murillo_comino@hotmail.com
 *
 *  Copyright (c) 2020.
 *  Last modified 06/06/20 22:23
 */

package br.com.comino.examplecoroutinestask

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MyAdapter(private val list: MutableList<String>) :
    RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        // create a new view
        val textView = LayoutInflater.from(parent.context)
            .inflate(R.layout.text_view_recycler, parent, false) as TextView
        return MyViewHolder(textView)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holderMy: MyViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        val string = "${position + 1} - ${list[position]}"
        holderMy.textView.text = string
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return list.size
    }

    //UpdateList
    fun updateListChanged(newList: List<String>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    inner class MyViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

}
