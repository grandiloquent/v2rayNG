package com.v2ray.ang.ui

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import android.view.*
import com.v2ray.ang.R
import com.v2ray.ang.util.Utils
import kotlinx.android.synthetic.main.fragment_routing_settings.*
import android.view.MenuInflater
import androidx.activity.result.contract.ActivityResultContracts
import com.tbruyelle.rxpermissions.RxPermissions
import com.v2ray.ang.AppConfig
import com.v2ray.ang.extension.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.URL

class RoutingSettingsFragment : Fragment() {
    companion object {
        private const val routing_arg = "routing_arg"
    }

    val defaultSharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(context) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_routing_settings, container, false)
    }

    fun newInstance(arg: String): Fragment {
        val fragment = RoutingSettingsFragment()
        val bundle = Bundle()
        bundle.putString(routing_arg, arg)
        fragment.arguments = bundle
        return fragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val content = defaultSharedPreferences.getString(requireArguments().getString(routing_arg), "")
        et_routing_content.text = Utils.getEditable(content!!)

        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_routing, menu)
        return super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.save_routing -> {
            val content = et_routing_content.text.toString()
            defaultSharedPreferences.edit().putString(requireArguments().getString(routing_arg), content).apply()
            activity?.toast(R.string.toast_success)
            true
        }
        R.id.del_routing -> {
            et_routing_content.text = null
            true
        }

        R.id.default_rules -> {
            setDefaultRules()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }


    private val scanQRCodeForReplace = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            val content = it.data?.getStringExtra("SCAN_RESULT")
            et_routing_content.text = Utils.getEditable(content!!)
        }
    }

    private val scanQRCodeForAppend = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            val content = it.data?.getStringExtra("SCAN_RESULT")
            et_routing_content.text = Utils.getEditable("${et_routing_content.text},$content")
        }
    }

    fun setDefaultRules(): Boolean {
        var url = AppConfig.v2rayCustomRoutingListUrl
        when (requireArguments().getString(routing_arg)) {
            AppConfig.PREF_V2RAY_ROUTING_AGENT -> {
                url += AppConfig.TAG_AGENT
            }
            AppConfig.PREF_V2RAY_ROUTING_DIRECT -> {
                url += AppConfig.TAG_DIRECT
            }
            AppConfig.PREF_V2RAY_ROUTING_BLOCKED -> {
                url += AppConfig.TAG_BLOCKED
            }
        }

        activity?.toast(R.string.msg_downloading_content)
        GlobalScope.launch(Dispatchers.IO) {
            val content = try {
                URL(url).readText()
            } catch (e: Exception) {
                e.printStackTrace()
                ""
            }
            launch(Dispatchers.Main) {
                et_routing_content.text = Utils.getEditable(content)
                activity?.toast(R.string.toast_success)
            }
        }
        return true
    }
}
