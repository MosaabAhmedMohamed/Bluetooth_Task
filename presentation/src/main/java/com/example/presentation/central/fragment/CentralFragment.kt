package com.example.presentation.central.fragment

import android.app.Activity.RESULT_OK
import android.bluetooth.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.presentation.R
import com.example.presentation.base.ViewModelFactory
import com.example.core.ble.BLELifecycleState
import com.example.presentation.central.viewmodel.BleCentralViewModel
import com.example.presentation.base.ui.BaseFragment
import com.example.presentation.base.ui.NavManager
import com.example.presentation.base.ui.ext.*
import com.example.presentation.central.viewstate.CentralViewState
import com.example.presentation.databinding.FragmentCenteralBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class CentralFragment : BaseFragment() {

    lateinit var binding: FragmentCenteralBinding

    @Inject
    lateinit var bleCentralViewModelViewModelFactory: ViewModelFactory<BleCentralViewModel>
    private val bleViewModel by lazy {
        ViewModelProvider(this, bleCentralViewModelViewModelFactory)[BleCentralViewModel::class.java]
    }

    private val userWantsToScanAndConnect: Boolean get() = binding.switchConnect.isChecked

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentCenteralBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun init() {
        binding.switchConnect.setOnCheckedChangeListener { _, isChecked ->
            when (isChecked) {
                true -> {
                    val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
                    requireActivity().registerReceiver(bleOnOffListener, filter)
                }
                false -> {
                    requireActivity().unregisterReceiver(bleOnOffListener)
                }
            }
            bleRestartLifecycle()
        }
        appendLog("MainActivity.onCreate")
        collectViewState()
    }

    private fun collectViewState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                bleViewModel.state().collect(::renderState)
            }
        }
    }

    private fun renderState(viewState: CentralViewState) {
        when (viewState) {
            is CentralViewState.ConnectionLifeCycle -> {
                binding.textViewLifecycleState.text = "State: ${viewState.state.name}"

                if (viewState.state != BLELifecycleState.Connected) {
                    binding.textViewSubscription.text = getString(R.string.text_not_subscribed)
                } else if (viewState.state == BLELifecycleState.Connected) {
                    binding.textViewSubscription.text = getString(R.string.text_subscribed)
                }

                appendLog("onDescriptorWrite ${viewState.state}")
            }
            CentralViewState.Initial -> {}
            is CentralViewState.Log -> {
                viewState.message?.let { appendLog(it) }
            }
            is CentralViewState.Indicate -> binding.textViewIndicateValue.text = viewState.message
            is CentralViewState.Read -> binding.textViewReadValue.text = viewState.message
        }
    }

    override fun onViewClicked() {
        super.onViewClicked()

        binding.buttonRead.setOnClickListener {
            bleViewModel.onTapRead()
        }

        binding.buttonWrite.setOnClickListener {
            bleViewModel.onTapWrite(binding.editTextWriteValue.text.toString().toByteArray(Charsets.UTF_8))
        }

        binding.buttonClearLog.setOnClickListener {
            onTapClearLog()
        }

        binding.buttonAdverties.setOnClickListener {
            NavManager.navigate(CentralFragmentDirections.actionCentralFragmentToPeripheralFragment())
        }

    }

    override fun onDestroy() {
        bleViewModel.bleEndLifecycle()
        super.onDestroy()
    }
    
    private fun onTapClearLog() {
        binding.textViewLog.text = "Logs:"
        appendLog("log cleared")
    }

    private fun appendLog(message: String) {
        Log.d("appendLog", message)
        lifecycleScope.launch {
            val strTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            binding.textViewLog.text = binding.textViewLog.text.toString() + "\n$strTime $message"

            // scroll after delay, because textView has to be updated first
            binding.scrollViewLog.postDelayed({
                binding.scrollViewLog.fullScroll(View.FOCUS_DOWN)
            }, 16)
        }
    }

    private fun prepareAndStartBleScan() {
        if (ensureBluetoothCanBeUsed()) {
            bleViewModel.init()
        }
    }

    private fun bleRestartLifecycle() {
        if (ensureBluetoothCanBeUsed()) {
            bleViewModel.bleRestartLifecycle(userWantsToScanAndConnect)
        }
    }

    private var bleOnOffListener = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF)) {
                BluetoothAdapter.STATE_ON -> {
                    appendLog("onReceive: Bluetooth ON")
                    lifecycleScope.launch {
                        bleViewModel.state().collectLatest {
                            if (it == CentralViewState.ConnectionLifeCycle(BLELifecycleState.Disconnected)) {
                                bleRestartLifecycle()
                            }
                        }
                    }
                }
                BluetoothAdapter.STATE_OFF -> {
                    appendLog("onReceive: Bluetooth OFF")
                    bleViewModel.bleEndLifecycle()
                }
            }
        }
    }


    private var requestBluetooth = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            //granted
            prepareAndStartBleScan()
        } else {
            //deny
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions.values.any { !it }

        if (isGranted) {
            // PERMISSION GRANTED
            prepareAndStartBleScan()
        } else {
            // PERMISSION NOT GRANTED
            appendLog("ERROR: onRequestPermissionsResult requestCode=$isGranted not handled")
        }
    }

    private fun ensureBluetoothCanBeUsed(): Boolean {
        if (isBluetoothCentralPermissionGranted(AskType.AskOnce, requestPermissionLauncher)) {
            return if (bleViewModel.isBluetoothEnabled()) {
                if (isLocationPermissionRequired(AskType.AskOnce,requestPermissionLauncher)) {
                    appendLog("Bluetooth ON, permissions OK, ready")
                    true
                } else {
                    appendLog("Location permission denied")
                    false
                }
            } else {
                // start activity for the request
                requestBluetooth.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                appendLog("Bluetooth OFF")
                false
            }
        } else {
            appendLog("Bluetooth permissions denied")
            return false
        }
    }
}