package com.atahyaat.app.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PrayerLogDao {

    @Query("SELECT * FROM prayer_log WHERE dateKey = :dateKey LIMIT 1")
    suspend fun getByDate(dateKey: String): PrayerLogEntity?

    @Query("SELECT * FROM prayer_log WHERE dateKey = :dateKey LIMIT 1")
    fun observeByDate(dateKey: String): LiveData<PrayerLogEntity?>

    @Query("SELECT * FROM prayer_log ORDER BY dateKey DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<PrayerLogEntity>

    @Query("SELECT * FROM prayer_log ORDER BY dateKey DESC LIMIT :limit")
    fun observeRecent(limit: Int): LiveData<List<PrayerLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: PrayerLogEntity)
}
