package data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherReportDao {
    @Query("SELECT * FROM weather_reports ORDER BY createdAt DESC")
    fun observeReports(): Flow<List<WeatherReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: WeatherReportEntity): Long
}
