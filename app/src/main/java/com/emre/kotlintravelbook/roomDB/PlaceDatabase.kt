package com.emre.kotlintravelbook.roomDB

import androidx.room.Database
import androidx.room.RoomDatabase
import com.emre.kotlintravelbook.model.Place

@Database(entities = [Place::class], version = 1) // Place tablosu ile bir database oluşturur
abstract class PlaceDatabase : RoomDatabase() {
    abstract fun placeDao(): PlaceDao // Placedao döndürür
}