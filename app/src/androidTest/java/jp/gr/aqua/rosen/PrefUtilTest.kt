package jp.gr.aqua.rosen

import android.content.Context
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4

import junit.framework.TestCase

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import rx.observers.TestSubscriber
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

public class PrefUtilTest : TestCase() {

    var mContext: Context by Delegates.notNull()

    Before
    throws(Exception::class)
    override fun setUp() {
        super.setUp()
        mContext = InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext()
    }

    After
    throws(Exception::class)
    override fun tearDown() {
        super.tearDown()
    }

    private class SampleSettings(context : Context) : PrefUtil( context.getSharedPreferences("testpref", Context.MODE_PRIVATE) ){
        var intSample0 by PrefUtil.IntPref(0)
        var intSample2 by PrefUtil.IntPref(2)
        var booleanSampleTrue by PrefUtil.BooleanPref(true)
        var booleanSampleFalse by PrefUtil.BooleanPref(false)
        var longSample by PrefUtil.LongPref(100L)
        var floatSample by PrefUtil.FloatPref(5.5F)
        var stringSample by PrefUtil.StringPref("sample")
        var stringSetSample by PrefUtil.StringSetPref(arrayOf("sample","example").toSet())
    }

    Test
    public fun testBoolean() {
        val s = SampleSettings(mContext)
        s.reset()
        assertEquals( s.intSample0 , 0 )
        assertEquals( s.intSample2 , 2 )
        assertEquals( s.booleanSampleFalse , false )
        assertEquals( s.booleanSampleTrue , true )
        assertEquals( s.longSample , 100L )
        assertEquals( s.floatSample , 5.5F )
        assertEquals( s.stringSample , "sample")
        assertEquals( s.stringSetSample, arrayOf("sample","example").toSet() )

        s.intSample0 = 5
        s.intSample2 = -1
        s.booleanSampleFalse = true
        s.booleanSampleTrue = false
        s.longSample  = 12300000L
        s.floatSample = 150.56F
        s.stringSample = "next generation"
        s.stringSetSample = setOf("one","two")

        assertEquals( s.intSample0 , 5 )
        assertEquals( s.intSample2 , -1 )
        assertEquals( s.booleanSampleFalse , true )
        assertEquals( s.booleanSampleTrue , false )
        assertEquals( s.longSample , 12300000L )
        assertEquals( s.floatSample , 150.56F )
        assertEquals( s.stringSample , "next generation")
        assertEquals( s.stringSetSample, setOf("one","two") )

        s.edit {
            s.intSample0 = 8
            s.intSample2 = 100
            s.booleanSampleFalse = false
            s.booleanSampleTrue = true
            s.longSample  = 5L
            s.floatSample = 0.1F
            s.stringSample = "end"
            s.stringSetSample = setOf("1","2")
        }
        assertEquals( s.intSample0 , 8 )
        assertEquals( s.intSample2 , 100 )
        assertEquals( s.booleanSampleFalse , false )
        assertEquals( s.booleanSampleTrue , true )
        assertEquals( s.longSample , 5L )
        assertEquals( s.floatSample , 0.1F )
        assertEquals( s.stringSample , "end")
        assertEquals( s.stringSetSample, setOf("1","2") )

        assertTrue( s.contains("stringSetSample") )
        assertFalse( s.contains("doubleSample") )

    }

    Test
    public fun testKeyChanges() {
        val s = SampleSettings(mContext)
        s.reset()
        val testSubscriber = TestSubscriber<String>()
        val subscription = s.keyChanges().subscribe(testSubscriber)

        testSubscriber.assertNoErrors()

        s.intSample0 = 100
        s.stringSample = "aaa"
        Thread.sleep(100)
        assertEquals(testSubscriber.getOnNextEvents().get(0) , "intSample0")
        assertEquals(testSubscriber.getOnNextEvents().get(1) , "stringSample")

        assertEquals( testSubscriber.getOnNextEvents().count() , 2 )

        subscription.unsubscribe()

        s.intSample0 = 10
        s.stringSample = "bbb"
        Thread.sleep(100)

        assertEquals( testSubscriber.getOnNextEvents().count() , 2 )

    }
}
