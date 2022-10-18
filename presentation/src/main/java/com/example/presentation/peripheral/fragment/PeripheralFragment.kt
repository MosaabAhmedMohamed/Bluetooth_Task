package com.example.presentation.peripheral.fragment

import android.app.Activity
import android.bluetooth.*
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.presentation.R
import com.example.presentation.base.ui.BaseFragment
import com.example.presentation.base.ui.ext.*
import com.example.presentation.databinding.FragmentPeripheralBinding
import com.example.presentation.peripheral.viewmodel.BlePeripheralViewModel
import com.example.presentation.peripheral.viewstate.PeripheralViewState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class PeripheralFragment : BaseFragment() {


    lateinit var binding: FragmentPeripheralBinding
    private val blePeripheralViewModel by viewModels<BlePeripheralViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        binding = FragmentPeripheralBinding.inflate(inflater, container, false)

        return binding.root
    }


    override fun init() {
        appendLog("MainActivity.onCreate")

        binding.switchAdvertising.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                prepareAndStartAdvertising()
            } else {
                blePeripheralViewModel.bleStopAdvertising()
            }
        }

        blePeripheralViewModel.setReadMessage(binding.editTextCharForRead.text.toString())
        binding.editTextCharForRead.addTextChangedListener {
            blePeripheralViewModel.setReadMessage(it.toString())
        }

        collectViewState()
    }

    private fun collectViewState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                blePeripheralViewModel.state().collect(::renderState)
            }
        }
    }

    private fun renderState(viewState: PeripheralViewState) {
        when (viewState) {
            is PeripheralViewState.Advertising -> {
                binding.switchAdvertising.postDelayed({
                    if (viewState.isAdvertising != binding.switchAdvertising.isChecked)
                        binding.switchAdvertising.isChecked = viewState.isAdvertising
                }, 200)
            }
            is PeripheralViewState.ConnectionState -> {
                if (viewState.state == BluetoothProfile.STATE_CONNECTED) {
                    binding.textViewConnectionState.text = getString(R.string.text_connected)
                    appendLog("Central did connect")
                } else {
                    binding.textViewConnectionState.text = getString(R.string.text_disconnected)
                    appendLog("Central did disconnect")
                }
            }
            PeripheralViewState.Initial -> {}
            is PeripheralViewState.Log -> {
                viewState.message?.let { appendLog(it) }
            }
            is PeripheralViewState.Subscribers -> {
                binding.textViewSubscribers.text = viewState.subscribedDevices
            }
            is PeripheralViewState.Write -> {
                binding.textViewCharForWrite.text = viewState.message
            }
        }
    }

    override fun onViewClicked() {
        super.onViewClicked()
        binding.buttonSend.setOnClickListener {
            onTapSend()
        }

        binding.buttonClearLog.setOnClickListener {
            onTapClearLog()
        }

    }

    override fun onDestroy() {
        blePeripheralViewModel.bleStopAdvertising()
        super.onDestroy()
    }

    private fun onTapSend() {
        blePeripheralViewModel.bleIndicate(binding.editTextCharForIndicate.text.toString())
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

    private fun prepareAndStartAdvertising() {
        if (ensureBluetoothCanBeUsed()) {
            blePeripheralViewModel.bleStartAdvertising()
        } else {
            blePeripheralViewModel.bleStopAdvertising()
        }
    }

    private var requestBluetooth = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            //granted
            prepareAndStartAdvertising()
        } else {
            //deny
            appendLog("Error: onActivityResult result=${result.resultCode} not handled")
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions.values.any { !it }
        if (isGranted) {
            // PERMISSION GRANTED
            prepareAndStartAdvertising()
        } else {
            // PERMISSION NOT GRANTED
            appendLog("ERROR: onRequestPermissionsResult requestCode=$isGranted not handled")
        }
    }

    private fun ensureBluetoothCanBeUsed(): Boolean {
        /*return if (isBluetoothPeripheralPermissionGranted(
                AskType.AskOnce,
                requestPermissionLauncher
            )
        ) {
            if (blePeripheralViewModel.isBluetoothEnabled()) {
                appendLog("BLE ready for use")
                true
            } else {
                // start activity for the request
                requestBluetooth.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                appendLog("Bluetooth OFF")
                false
            }
        } else {
            appendLog("Bluetooth permissions denied")
            false
        }*/
        return true
    }


}