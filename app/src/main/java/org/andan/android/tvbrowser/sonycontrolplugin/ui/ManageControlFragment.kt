package org.andan.android.tvbrowser.sonycontrolplugin.ui

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.google.gson.GsonBuilder
import org.andan.android.tvbrowser.sonycontrolplugin.BuildConfig
import org.andan.android.tvbrowser.sonycontrolplugin.R
import org.andan.android.tvbrowser.sonycontrolplugin.databinding.FragmentManageControlBinding
import org.andan.android.tvbrowser.sonycontrolplugin.repository.EventObserver
import org.andan.android.tvbrowser.sonycontrolplugin.viewmodels.SonyControlViewModel
import timber.log.Timber
import java.io.File

class ManageControlFragment : Fragment() {
    private val sonyControlViewModel: SonyControlViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentManageControlBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_manage_control, container, false
        )
        binding.lifecycleOwner = this
        binding.sonyControlViewModel = sonyControlViewModel

        Timber.d("sonyControls: ${sonyControlViewModel.sonyControls.value!!.controls.size}")
        Timber.d("sonyControlViewModel: $sonyControlViewModel")

        sonyControlViewModel.selectedSonyControl.observe(viewLifecycleOwner, Observer {
            Timber.d("observed change ${sonyControlViewModel.selectedSonyControl.value}")
        })

        sonyControlViewModel.sonyIpAndDeviceList.observe(viewLifecycleOwner, Observer {
            Timber.d("observed change ${sonyControlViewModel.sonyIpAndDeviceList.value}")
        })

        sonyControlViewModel.requestErrorMessage.observe(viewLifecycleOwner,
            EventObserver<String> {
                Timber.d("observed requestError")
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            }
        )

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.manage_control_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if (sonyControlViewModel.selectedSonyControl.value == null) {
            menu.findItem(R.id.delete_control).isEnabled = false
            menu.findItem(R.id.register_control).isEnabled = false
            menu.findItem(R.id.get_program_list).isEnabled = false
            menu.findItem(R.id.enable_wol).isEnabled = false
            menu.findItem(R.id.check_set_host).isEnabled = false
        }
        if (!BuildConfig.DEBUG) {
            menu.findItem(R.id.save_controls).isVisible = false
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.delete_control -> {
                val builder = AlertDialog.Builder(requireContext())
                builder.setMessage("Do you want to delete this control?").setTitle("Confirm delete")
                builder.setPositiveButton("Yes") { dialog, id ->
                    Timber.d("deleteControl")
                    sonyControlViewModel.deleteSelectedControl()
                }
                builder.setNegativeButton(
                    "No"
                ) { dialog, id -> dialog.dismiss() }
                val dialog = builder.create()
                dialog.show()
            }
            R.id.register_control -> {
                sonyControlViewModel.registerControl(sonyControlViewModel.selectedSonyControl.value!!)
            }
            R.id.get_program_list -> {
                sonyControlViewModel.fetchChannelList()
            }
            R.id.enable_wol -> {
                sonyControlViewModel.wakeOnLan()
            }
            R.id.check_set_host -> {
                requireView().findNavController()
                    .navigate(R.id.action_nav_manage_control_to_nav_add_control_host)
            }
            R.id.save_controls -> {
                if (sonyControlViewModel.sonyControls.value != null) {
                    val file = File(requireContext().filesDir, "controls.json")
                    var fileContent = GsonBuilder().setPrettyPrinting().create()
                        .toJson(sonyControlViewModel.sonyControls.value!!)
                    file.writeText(sonyControlViewModel.removeUTFCharacters(fileContent))
                }
            }

        }
        return super.onOptionsItemSelected(item)
    }
}
