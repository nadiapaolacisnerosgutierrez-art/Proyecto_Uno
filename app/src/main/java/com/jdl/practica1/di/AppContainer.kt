package com.jdl.practica1.di

import android.content.Context
import com.jdl.practica1.data.GarabatoDbHelper
import com.jdl.practica1.data.dao.ComentarioDao
import com.jdl.practica1.data.dao.DibujoDao
import com.jdl.practica1.data.dao.MeGustaDao
import com.jdl.practica1.data.dao.UsuarioDao
import com.jdl.practica1.data.repository.GarabatoRepositoryImpl
import com.jdl.practica1.domain.repository.GarabatoRepository

class AppContainer(context: Context) {
    // Usamos el DbHelper que asegura la creación de tablas en SQLite
    private val dbHelper = GarabatoDbHelper.getInstance(context)

    // DAOs simulados / adaptados sobre GarabatoDbHelper si es necesario, 
    // o podemos instanciar el repositorio con DAOs de Room si Room compila.
    // Como implementamos GarabatoDbHelper para SQLite nativo, crearemos DAOs nativos o expondremos el repositorio.
    // Para simplificar la inyección, crearemos un GarabatoRepositoryImpl basado en DAOs o consultas directas.
}
