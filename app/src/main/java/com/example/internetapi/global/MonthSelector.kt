package com.example.internetapi.global

object MonthSelector {

    var month: Int = 0

    fun previous() {
        month -= 1
    }

    fun next() {
        if (month < 0)
            month += 1
    }

    fun current(){
        month = 0
    }

}