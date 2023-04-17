package com.example.settings

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.settings.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

//DataStore(1-Crea la base de datos. Su nombre "settings")
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

//DataStore(6: crea una class con todos los datos para pasarle a la fun del paso 5, anterior)
data class SettingsModel(
    var wifi: Boolean,
    var bluetooth: Boolean,
    var volume: Int,
    var vibration: Boolean,
    var darkMode: Boolean
)

class MainActivity : AppCompatActivity() {

    //DataStore(3- guarda las claves en un companion object)
    companion object {
        const val VOLUME = "volume"
        const val BLUETOOTH = "bluetooth"
        const val DARK_MODE = "dark mode"
        const val VIBRATION = "vibration"
        const val WIFI = "wifi"
    }

    private lateinit var binding: ActivityMainBinding

    //DataStore8:Los datos q sepan cuando cambiar)
    private var firstTime: Boolean = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //DataStore(7:Según los datos le dices qué tiene q hacer)
        CoroutineScope(Dispatchers.IO).launch {
            getSettings().filter { firstTime }.collect() { settingsModel ->
                if (settingsModel != null) {
                    //hiloPrincipal pq va a pintar
                    runOnUiThread {
                        binding.scWifi.isChecked = settingsModel.wifi
                        binding.scBluetooth.isChecked = settingsModel.bluetooth
                        binding.rsVolume.setValues(settingsModel.volume.toFloat())
                        binding.scVibration.isChecked = settingsModel.vibration
                        binding.scDarkMode.isChecked = settingsModel.darkMode
                        firstTime = !firstTime
                    }
                }
            }
        }
        initUI()
    }

    //DataStore(2-Crea un método suspend(se va a usar en corrutina smpre)para guardar cada valor).intPreferencesKey pq es un entero, sino, stringPreferencesKey...
    private suspend fun saveVolume(value: Int) {
        dataStore.edit { it[intPreferencesKey(VOLUME)] = value }
    }

    private suspend fun saveBluetooth(value: Boolean) {
        dataStore.edit { it[booleanPreferencesKey(BLUETOOTH)] = value }
    }

    private suspend fun saveDarkMode(value: Boolean) {
        dataStore.edit { it[booleanPreferencesKey(DARK_MODE)] = value }
    }

    private suspend fun saveWifi(value: Boolean) {
        dataStore.edit { it[booleanPreferencesKey(WIFI)] = value }
    }

    private suspend fun saveVibration(value: Boolean) {
        dataStore.edit { it[booleanPreferencesKey(VIBRATION)] = value }
    }


    //DataStore(4:guarda los tatos en su contenedor)
    private fun initUI() {
        binding.rsVolume.addOnChangeListener { _, value, _ ->
            //en un hilo secundario
            CoroutineScope(Dispatchers.IO).launch {
                saveVolume(value.toInt())
            }
        }
        binding.scBluetooth.setOnCheckedChangeListener { _, value ->
            CoroutineScope(Dispatchers.IO).launch {
                saveBluetooth(value)
            }
        }
        binding.scWifi.setOnCheckedChangeListener { _, value ->
            CoroutineScope(Dispatchers.IO).launch {
                saveWifi(value)
            }
        }
        binding.scVibration.setOnCheckedChangeListener { _, value ->
            CoroutineScope(Dispatchers.IO).launch {
                saveVibration(value)
            }
        }
        binding.scDarkMode.setOnCheckedChangeListener { _, value ->
            if (value) {
                darkTheme()
             } else{whiteTheme()}
            CoroutineScope(Dispatchers.IO).launch {
                saveDarkMode(value)
            }
        }
    }


    //DataStore(5:nFlow: una fun q devuelva los valores constantemente, para saber el valor en cada momento.Usas Flow
    private fun getSettings(): Flow<SettingsModel> {
        return dataStore.data.map { preferences ->
            //?: 50  si no hay valor, el valor es 50
            SettingsModel(
                wifi = preferences[booleanPreferencesKey(WIFI)] ?: true,
                bluetooth = preferences[booleanPreferencesKey(BLUETOOTH)] ?: true,
                volume = preferences[intPreferencesKey(VOLUME)] ?: 50,
                vibration = preferences[booleanPreferencesKey(VIBRATION)] ?: true,
                darkMode = preferences[booleanPreferencesKey(DARK_MODE)] ?: true
            )
        }
    }

    private fun whiteTheme(){
        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
        delegate.applyDayNight()
    }

    private fun darkTheme(){
        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
        delegate.applyDayNight()
    }

}