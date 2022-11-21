package com.cockandroid.finalcapstone.message

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.cockandroid.finalcapstone.R
import com.cockandroid.finalcapstone.auth.UserDataModel
import kotlinx.coroutines.NonDisposableHandle.parent

class ListViewAdapter(val context : Context, val items : MutableList<UserDataModel>) : BaseAdapter() {
    override fun getCount(): Int {
        return items.size
    }

    override fun getItem(p0: Int): Any {
        return items[p0]
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {

        var convertView = p1
        if(convertView == null){
            convertView = LayoutInflater.from(p2?.context).inflate(R.layout.list_view_item,p2,false)
        }

        val nickname = convertView!!.findViewById<TextView>(R.id.listViewItemNickname)
        nickname.text = items[p0].nickname

        return convertView!!

    }
}
