package com.example.tiotec.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "revisiones")
data class Revision(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fecha: Long = System.currentTimeMillis(),
    val nombre: String,
    val dispositivo: String,
    val sistema: String,
    val experiencia: String,
    val foto: String,
    @ColumnInfo(defaultValue = "30") val diasRevision: Int = 30,
    val respaldo: String,
    val actualizacion: String,
    val autenticacion: String,
    val recuperacion: String,
    val accionesHechas: Int = 0
) {
    val respuestas: List<String>
        get() = listOf(respaldo, actualizacion, autenticacion, recuperacion)
}

@Dao
interface RevisionDao {
    @Query("SELECT * FROM revisiones ORDER BY fecha DESC, id DESC")
    fun observar(): Flow<List<Revision>>

    @Query("SELECT * FROM revisiones WHERE id = :id LIMIT 1")
    suspend fun buscar(id: Long): Revision?

    @Insert
    suspend fun insertar(revision: Revision): Long

    @Update
    suspend fun actualizar(revision: Revision)

    @Query("UPDATE revisiones SET accionesHechas = CASE WHEN :hecha THEN accionesHechas | :bit ELSE accionesHechas & ~:bit END WHERE id = :id")
    suspend fun marcarAccion(id: Long, bit: Int, hecha: Boolean)

    @Delete
    suspend fun eliminar(revision: Revision)
}

@Database(entities = [Revision::class], version = 2, exportSchema = false)
abstract class RevisionesDb : RoomDatabase() {
    abstract fun dao(): RevisionDao

    companion object {
        @Volatile private var instancia: RevisionesDb? = null
        private val migracion1a2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE revisiones ADD COLUMN diasRevision INTEGER NOT NULL DEFAULT 30")
            }
        }

        fun obtener(context: Context): RevisionesDb = instancia ?: synchronized(this) {
            instancia ?: Room.databaseBuilder(
                context.applicationContext, RevisionesDb::class.java, "tiotec.db"
            ).addMigrations(migracion1a2).build().also { instancia = it }
        }
    }
}
