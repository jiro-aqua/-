package jp.gr.aqua.rosen

import android.content.Context

public class Settings(context : Context) : PrefUtil(context.getSharedPreferences("settings",Context.MODE_PRIVATE)){
    var typeDeparture by booleanPref(true)
    var typeArrival by booleanPref(false)
    var typeFirst by booleanPref(false)
    var typeLast by booleanPref(false)

    var sortTime by booleanPref(true)
    var sortFare by booleanPref(false)
    var sortNum by booleanPref(false)

    var ticketIc by booleanPref(true)
    var ticketNormal by booleanPref(false)

    var Express  by booleanPref(false)
    var Shinkansen by booleanPref(false)
    var Airline by booleanPref(false)
    var highwayBus by booleanPref(false)
    var localBus by booleanPref(true)
    var Ferry by booleanPref(false)

    var walkSpeed by intPref(0)
}

