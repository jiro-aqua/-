package jp.gr.aqua.rosen

import android.content.Context

public class Settings(context : Context) : PrefUtil(context.getSharedPreferences("settings",Context.MODE_PRIVATE)){
    var type_departure : Boolean by PrefUtil.BooleanPref(true)
    var type_arrival : Boolean by PrefUtil.BooleanPref(false)
    var type_first : Boolean by PrefUtil.BooleanPref(false)
    var type_last : Boolean by PrefUtil.BooleanPref(false)

    var sort_time : Boolean by PrefUtil.BooleanPref(true)
    var sort_fare : Boolean by PrefUtil.BooleanPref(false)
    var sort_num : Boolean by PrefUtil.BooleanPref(false)

    var ticket_ic : Boolean by PrefUtil.BooleanPref(true)
    var ticket_normal : Boolean by PrefUtil.BooleanPref(false)

    var express : Boolean by PrefUtil.BooleanPref(false)
    var shinkansen : Boolean by PrefUtil.BooleanPref(false)
    var airline : Boolean by PrefUtil.BooleanPref(false)
    var highwaybus : Boolean by PrefUtil.BooleanPref(false)
    var localbus : Boolean by PrefUtil.BooleanPref(true)
    var ferry : Boolean by PrefUtil.BooleanPref(false)

    var walkspeed : Int by PrefUtil.IntPref(0)
}

