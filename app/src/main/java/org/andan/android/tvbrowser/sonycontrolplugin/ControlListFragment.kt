package org.andan.android.tvbrowser.sonycontrolplugin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ControlListFragment : Fragment() {

    private lateinit var controlViewModel: ControlViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_control_list, container, false)
        val textView: TextView = root.findViewById(R.id.text_control_list)
        val fab: FloatingActionButton = root.findViewById(R.id.fab)

        controlViewModel = activity?.run {
            ViewModelProviders.of(this)[ControlViewModel::class.java]
        } ?: throw Exception("Invalid Activity")



        fab.setOnClickListener { view ->
            /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                   .setAction("Action", null).show()*/
            val nickname: String = controlViewModel.getControls().value!![0].nickname
            controlViewModel.getControls().value!![0].nickname = "$nickname*"
            controlViewModel.saveControls(true)
        }

        controlViewModel.getControls().observe(this, Observer {
            textView.text = it[0].nickname ?: "NULL Nickname"
        })

        return root
    }


}