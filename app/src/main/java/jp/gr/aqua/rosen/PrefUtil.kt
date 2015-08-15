package jp.gr.aqua.rosen

import android.content.Context
import android.content.SharedPreferences
import kotlin.properties.Delegates

abstract open public class PrefUtil(val context : Context , val prefName : String) {
    private var sp : SharedPreferences by Delegates.notNull()
    init {
        sp = context.getSharedPreferences(prefName,Context.MODE_PRIVATE)
    }

    private var edit : SharedPreferences.Editor? = null

    public fun edit( f : (Unit)->Unit ) {
        assert( edit == null , "editor is already opened!")
        edit = sp.edit()
        f(Unit)
        edit?.apply()
        edit = null
    }

    public fun reset() {
        sp.edit().clear().apply()
    }

    public fun getAll(): MutableMap<String, *>? {
        return sp.getAll()
    }

    public fun registerOnSharedPreferenceChangeListener(listener : SharedPreferences.OnSharedPreferenceChangeListener ){
        sp.registerOnSharedPreferenceChangeListener(listener)
    }

    public fun unregisterOnSharedPreferenceChangeListener(listener : SharedPreferences.OnSharedPreferenceChangeListener ){
        sp.unregisterOnSharedPreferenceChangeListener(listener)
    }
    // そのうちここからobserve()を生やしたい。

    public fun contains(key:String) : Boolean{
        return sp.contains(key)
    }

    public class BooleanPref( val default : Boolean ) {
        fun get( thisRef: PrefUtil , prop: PropertyMetadata ) : Boolean {
            return thisRef.sp.getBoolean(prop.name, default )
        }
        fun set(thisRef: PrefUtil , prop: PropertyMetadata, value: Boolean) {
            if ( thisRef.edit != null ) {
                thisRef.edit?.putBoolean(prop.name, value)
            }else{
                thisRef.sp.edit().putBoolean(prop.name, value).apply()
            }
        }
    }

    public class IntPref( val default : Int ) {
        fun get( thisRef: PrefUtil , prop: PropertyMetadata ) : Int {
            return thisRef.sp.getInt(prop.name, default )
        }
        fun set(thisRef: PrefUtil , prop: PropertyMetadata, value: Int) {
            if ( thisRef.edit != null ) {
                thisRef.edit?.putInt(prop.name,value)
            }else{
                thisRef.sp.edit().putInt(prop.name,value).apply()
            }
        }
    }

    public class FloatPref( val default : Float ) {
        fun get( thisRef: PrefUtil , prop: PropertyMetadata ) : Float {
            return thisRef.sp.getFloat(prop.name, default )
        }
        fun set(thisRef: PrefUtil , prop: PropertyMetadata, value: Float) {
            if ( thisRef.edit != null ) {
                thisRef.edit?.putFloat(prop.name,value)
            }else{
                thisRef.sp.edit().putFloat(prop.name,value).apply()
            }
        }
    }

    public class LongPref( val default : Long ) {
        fun get( thisRef: PrefUtil , prop: PropertyMetadata ) : Long{
            return thisRef.sp.getLong(prop.name, default )
        }
        fun set(thisRef: PrefUtil , prop: PropertyMetadata, value: Long) {
            if ( thisRef.edit != null ) {
                thisRef.edit?.putLong(prop.name,value)
            }else{
                thisRef.sp.edit().putLong(prop.name,value).apply()
            }
        }
    }

    public class StringPref( val default : String ) {
        fun get( thisRef: PrefUtil , prop: PropertyMetadata ) : String{
            return thisRef.sp.getString(prop.name, default )
        }
        fun set(thisRef: PrefUtil , prop: PropertyMetadata, value: String) {
            if ( thisRef.edit != null ) {
                thisRef.edit?.putString(prop.name,value)
            }else{
                thisRef.sp.edit().putString(prop.name,value).apply()
            }
        }
    }

    public class StringSetPref( val default : Set<String> ) {
        fun get( thisRef: PrefUtil , prop: PropertyMetadata ) : Set<String>{
            return thisRef.sp.getStringSet(prop.name, default )
        }
        fun set(thisRef: PrefUtil , prop: PropertyMetadata, value: Set<String>) {
            if ( thisRef.edit != null ) {
                thisRef.edit?.putStringSet(prop.name,value)
            }else{
                thisRef.sp.edit().putStringSet(prop.name,value).apply()
            }
        }
    }
}
