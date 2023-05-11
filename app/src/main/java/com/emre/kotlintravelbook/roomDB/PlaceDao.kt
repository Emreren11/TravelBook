package com.emre.kotlintravelbook.roomDB

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.emre.kotlintravelbook.model.Place
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable

@Dao // Verilere Erişim Objesi
interface PlaceDao {

    //@Query("select * from Place where id = :id") -> Filtreleme
    //fun getAll(id: String): List<Place>
    @Query("select * from Place")
    fun getAll():Flowable<List<Place>> // Bir liste döndürür ve içerisinde Place verileri bulundurur
    // Flowable olarak tanımlama -> Asenkron(arka planda) çalıştırır

    @Insert
    fun insert(place: Place) : Completable

    @Delete
    fun delete(place: Place) : Completable
}