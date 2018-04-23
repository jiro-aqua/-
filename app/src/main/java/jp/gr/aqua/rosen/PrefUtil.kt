package jp.gr.aqua.rosen

import android.content.SharedPreferences
import rx.Observable
import rx.subscriptions.Subscriptions
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

abstract open public class PrefUtil(val sp : SharedPreferences )
{
    private var edit : SharedPreferences.Editor? = null

    public fun <T:PrefUtil> edit( reciever: T , f : T.(Unit)->Unit ) {
//        assert( edit == null , "editor is already opened!")
        reciever.f(Unit)
        edit?.apply()
        edit = null
    }

    public fun reset() {
        sp.edit().clear().apply()
    }

    public fun getAll(): MutableMap<String, *>? {
        return sp.getAll()
    }

    public fun keyChanges() : Observable<String> {
        return Observable.create {
            subscriber->
            val listener = SharedPreferences.OnSharedPreferenceChangeListener {
                sp, key ->  subscriber.onNext(key);
            }
            subscriber.add( Subscriptions.create {  sp.unregisterOnSharedPreferenceChangeListener(listener) })
            sp.registerOnSharedPreferenceChangeListener(listener)
        }
    }

    public fun contains(key:String) : Boolean{
        return sp.contains(key)
    }

    protected fun booleanPref( default : Boolean ) : ReadWriteProperty<PrefUtil, Boolean>  = BooleanPref(default)
    protected fun intPref( default : Int ) : ReadWriteProperty<PrefUtil, Int>  = IntPref(default)
    protected fun floatPref( default : Float ) : ReadWriteProperty<PrefUtil, Float>  = FloatPref(default)
    protected fun longPref( default : Long ) : ReadWriteProperty<PrefUtil, Long>  = LongPref(default)
    protected fun stringPref( default : String ) : ReadWriteProperty<PrefUtil, String>  = StringPref(default)
    protected fun stringSetPref( default : Set<String> ) : ReadWriteProperty<PrefUtil, Set<String>>  = StringSetPref(default)

    private class BooleanPref( val default : Boolean ) : ReadWriteProperty<PrefUtil, Boolean> {
        override fun getValue( thisRef: PrefUtil , prop: KProperty<*>) : Boolean {
            return thisRef.sp.getBoolean(prop.name, default )
        }
        override fun setValue(thisRef: PrefUtil , prop: KProperty<*>, value: Boolean) {
            if ( thisRef.edit != null ) {
                thisRef.edit?.putBoolean(prop.name, value)
            }else{
                thisRef.sp.edit().putBoolean(prop.name, value).apply()
            }
        }
    }

    private class IntPref( val default : Int ) : ReadWriteProperty<PrefUtil,Int> {
        override fun getValue( thisRef: PrefUtil , prop: KProperty<*> ) : Int {
            return thisRef.sp.getInt(prop.name, default )
        }
        override fun setValue(thisRef: PrefUtil , prop: KProperty<*>, value: Int) {
            if ( thisRef.edit != null ) {
                thisRef.edit?.putInt(prop.name,value)
            }else{
                thisRef.sp.edit().putInt(prop.name,value).apply()
            }
        }
    }

    private class FloatPref( val default : Float ) : ReadWriteProperty<PrefUtil,Float> {
        override fun getValue( thisRef: PrefUtil , prop: KProperty<*> ) : Float {
            return thisRef.sp.getFloat(prop.name, default )
        }
        override fun setValue(thisRef: PrefUtil , prop: KProperty<*>, value: Float) {
            if ( thisRef.edit != null ) {
                thisRef.edit?.putFloat(prop.name,value)
            }else{
                thisRef.sp.edit().putFloat(prop.name,value).apply()
            }
        }
    }

    private class LongPref( val default : Long ) : ReadWriteProperty<PrefUtil,Long> {
        override fun getValue( thisRef: PrefUtil , prop: KProperty<*> ) : Long{
            return thisRef.sp.getLong(prop.name, default )
        }
        override fun setValue(thisRef: PrefUtil , prop: KProperty<*>, value: Long) {
            if ( thisRef.edit != null ) {
                thisRef.edit?.putLong(prop.name,value)
            }else{
                thisRef.sp.edit().putLong(prop.name,value).apply()
            }
        }
    }

    private class StringPref( val default : String ) :ReadWriteProperty<PrefUtil,String> {
        override fun getValue( thisRef: PrefUtil , prop: KProperty<*> ) : String{
            return thisRef.sp.getString(prop.name, default )
        }
        override fun setValue(thisRef: PrefUtil , prop: KProperty<*>, value: String) {
            if ( thisRef.edit != null ) {
                thisRef.edit?.putString(prop.name,value)
            }else{
                thisRef.sp.edit().putString(prop.name,value).apply()
            }
        }
    }

    private class StringSetPref( val default : Set<String> ) :ReadWriteProperty<PrefUtil,Set<String>>{
        override  fun getValue( thisRef: PrefUtil , prop: KProperty<*> ) : Set<String>{
            return thisRef.sp.getStringSet(prop.name, default )
        }
        override fun setValue(thisRef: PrefUtil , prop: KProperty<*>, value: Set<String>) {
            if ( thisRef.edit != null ) {
                thisRef.edit?.putStringSet(prop.name,value)
            }else{
                thisRef.sp.edit().putStringSet(prop.name,value).apply()
            }
        }
    }
}

public fun <T : PrefUtil > T.edit( f: T.(Unit)->Unit ) : Unit = this.edit(this,f)

