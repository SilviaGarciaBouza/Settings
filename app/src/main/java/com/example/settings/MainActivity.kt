package com.example.settings

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
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
data class SettingsModel(var volume: Int)
class MainActivity : AppCompatActivity() {

    //DataStore(3- guarda las claves en un companion object)
    companion object {
        const val VOLUME = "volume"
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
                    runOnUiThread {
                        binding.rsVolume.setValues(settingsModel.volume.toFloat())
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


    //DataStore(4:guarda los tatos en su contenedor)
    private fun initUI() {
        binding.rsVolume.addOnChangeListener { _, value, _ ->
            //en un hilo secundario
            CoroutineScope(Dispatchers.IO).launch {
                saveVolume(value.toInt())
            }
        }
    }


    //DataStore(5:nFlow: una fun q devuelva los valores constantemente, para saber el valor en cada momento.Usas Flow
    private fun getSettings(): Flow<SettingsModel> {
        return dataStore.data.map { preferences ->
            //?: 50  si no hay valor, el valor es 50
            SettingsModel(volume = preferences[intPreferencesKey(VOLUME)] ?: 50)
        }
    }

}