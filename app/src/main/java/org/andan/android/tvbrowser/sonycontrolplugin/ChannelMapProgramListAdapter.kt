package org.andan.android.tvbrowser.sonycontrolplugin

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import org.andan.av.sony.model.SonyProgram

import java.util.ArrayList

class ChannelMapProgramListAdapter(context: Context?, programUriMatchList: ArrayList<String>,  programUriMap: MutableMap<String,SonyProgram>) :
    BaseAdapter() {


    internal var ctx: Context? = null
    internal var programUriMap: MutableMap<String, SonyProgram>? = null
    internal var programUriMatchList: ArrayList<String>? = null

    private var mInflater: LayoutInflater? = null

    init {
        this.ctx = context
        mInflater = (ctx as Activity).layoutInflater
        this.programUriMap = programUriMap
        this.programUriMatchList = programUriMatchList

    }

    override fun getCount(): Int {
        return programUriMatchList!!.size
    }

    override fun getItem(arg0: Int): Any? {
        return null
    }


    override fun getItemId(arg0: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View?, arg2: ViewGroup): View {
        var convertView = convertView

        val program: SonyProgram? = programUriMap!![programUriMatchList!![position]]

        val holder: ViewHolder

        if (convertView == null) {
            holder = ViewHolder()
            convertView = mInflater!!.inflate(R.layout.map_channnel_single_item, null)
            holder.programPosView =
                convertView!!.findViewById<View>(R.id.channel_map_program_pos) as TextView
            holder.programNameView =
                convertView.findViewById<View>(R.id.channel_map_program_name) as TextView
            holder.programSourceView =
                convertView.findViewById<View>(R.id.channel_map_program_source) as TextView
            convertView.tag = holder

        } else {
            holder = convertView.tag as ViewHolder
        }

        //convertView.setBackgroundResource(R.drawable.list_selector)
        holder.programPosView!!.text = Integer.toString(position+1) + "."
        holder.programNameView!!.text = program?.title
        holder.programSourceView!!.text = program?.sourceWithType

        return convertView

    }

    private class ViewHolder {
        internal var programPosView: TextView? = null
        internal var programNameView: TextView? = null
        internal var programSourceView: TextView? = null
    }

}