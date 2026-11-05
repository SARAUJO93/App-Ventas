package com.example.myapplication.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.BusinessSettingsRepository
import com.example.myapplication.data.repository.UpdateInfo
import com.example.myapplication.data.repository.UpdateRepository
import com.example.myapplication.data.repository.UpdateState
import com.example.myapplication.model.BusinessSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SettingsAndUpdateViewModel @Inject constructor(
    private val settingsRepository: BusinessSettingsRepository,
    private val updateRepository: UpdateRepository
) : ViewModel() {

    val businessSettings: StateFlow<BusinessSettings> = settingsRepository.settings

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    fun saveBusinessSettings(newSettings: BusinessSettings) {
        settingsRepository.saveSettings(newSettings)
    }

    fun checkForUpdates() {
        viewModelScope.launch {
            _updateState.value = UpdateState.Checking
            val currentVersion = "1.0.0"
            val info = updateRepository.checkForUpdate(currentVersion)
            if (info.isUpdateAvailable) {
                _updateState.value = UpdateState.UpdateAvailable(info)
            } else {
                _updateState.value = UpdateState.NoUpdateAvailable
            }
        }
    }

    fun startUpdateDownload(info: UpdateInfo) {
        viewModelScope.launch {
            _updateState.value = UpdateState.Downloading(0f)
            val apk = updateRepository.downloadApk(info.downloadUrl) { progress ->
                _updateState.value = UpdateState.Downloading(progress)
            }
            if (apk != null) {
                _updateState.value = UpdateState.ReadyToInstall(apk)
            } else {
                _updateState.value = UpdateState.Error("Error al descargar el archivo de actualización.")
            }
        }
    }

    fun installApk(context: Context, apkFile: File) {
        updateRepository.triggerApkInstallation(context, apkFile)
    }

    fun resetUpdateState() {
        _updateState.value = UpdateState.Idle
    }
}