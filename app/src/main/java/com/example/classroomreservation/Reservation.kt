package com.example.classroomreservation



import com.google.gson.annotations.SerializedName

import java.io.Serializable
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date

class Reservation : Serializable, Comparable<Reservation> {

    @SerializedName("id")
    var id: Int = 0

    @SerializedName("fromTime")
    var fromTimeOld: Int = 0
        private set

    @SerializedName("toTime")
    var toTimeOld: Int = 0
        private set

    @SerializedName("userId")
    var userId: String? = null

    @SerializedName("purpose")
    var purpose: String? = null

    @SerializedName("roomId")
    var roomId: Int = 0

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm")

    val fromTime: Date
        get() = Date((fromTimeOld * 1000).toLong())

    val toTime: Date
        get() = Date((toTimeOld * 1000).toLong())

    constructor() {}

    constructor(id: Int, fromTime: Int, toTime: Int, userId: String, purpose: String, roomId: Int) {
        this.id = id
        this.fromTimeOld = fromTime
        this.toTimeOld = toTime
        this.userId = userId
        this.purpose = purpose
        this.roomId = roomId
    }

    fun setFromTime(fromTime: Int) {
        this.fromTimeOld = fromTime
    }

    fun setToTime(toTime: Int) {
        this.toTimeOld = toTime
    }

    override fun compareTo(reservation: Reservation): Int {
        return if (this.toTimeOld < reservation.toTimeOld)
            -1
        else
            if (this.toTimeOld == reservation.toTimeOld) 0 else 1
    }

    override fun toString(): String {
        return "Purpose: " + purpose + "\nFrom: " + dateFormat.format(fromTime) + "\nTo: " + dateFormat.format(toTime)
    }


}
