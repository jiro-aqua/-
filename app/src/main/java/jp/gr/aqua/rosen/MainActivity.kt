package jp.gr.aqua.rosen

import android.app.Activity
import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import kotlinx.android.synthetic.main.*
import rx.Observable
import rx.lang.kotlin.PublishSubject
import rx.subscriptions.CompositeSubscription
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Calendar
import kotlin.properties.Delegates


public class MainActivity : AppCompatActivity() {

    val subscriptions = CompositeSubscription()

    var delayMinutes = 0
    var time: Calendar? = null

    val delays : IntArray by Delegates.lazy { getResources().getIntArray(R.array.time_select_delay_array)    }
    val walkSpeeds : IntArray by Delegates.lazy { getResources().getIntArray(R.array.walk_speed_values)    }
    val delayNames : Array<String> by Delegates.lazy { getResources().getStringArray(R.array.time_select_button_array) }
    val settings : Settings by Delegates.lazy { Settings( getApplicationContext() ) }

    val optionsMenuClickObservable = PublishSubject<MenuItem>()
    val activityResultObservable = PublishSubject<Triple<Int, Int, Intent?>>()

    enum class RequestCode(val code:Int) {
        FADDR(1),
        TADDR(2),
        VIA(3) ,
        INVALID(-1)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.app_title)
        setContentView(R.layout.main)
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

        load()

        delay_spinner.setSelection(1)

        // 選択ボタンの処理
        subscriptions.add( Observable.merge(
            faddr_button.clickEvents().map { RequestCode.FADDR },
            taddr_button.clickEvents().map { RequestCode.TADDR },
            via_button.clickEvents().map { RequestCode.VIA }
        ).subscribe {
            val initString = when( it.code ){
                RequestCode.FADDR.code -> faddr_edit.getText().toString()
                RequestCode.TADDR.code -> taddr_edit.getText().toString()
                else->null
            }
            startActivityForResult( StationPickerActivity.getStartIntent(this,initString) , it.code)
        })
//      発駅選択ボタンを長押ししたら測位しようと思ったけど、Yahoo!のAPIでの指定方法が分からなかったので、ここに封印する
//        subscriptions.add(faddr_button.longClickEvents().flatMap{ RxLocation().request(this)}.subscribe{
//            faddr_edit.setText("${it.first},${it.second}")
//        })
        // 遅延時刻のスピナー
        subscriptions.add(delay_spinner.itemSelections().filter { it!= 0 }.subscribe {
            delayMinutes = delays[it]
            delay_display_text.setText(delayNames[it])
            time = null
            time_text.setText("")
        })
        // 時刻設定ボタン
        subscriptions.add(time_button.clickEvents().subscribe{
            val now = Calendar.getInstance()
            val sdf = SimpleDateFormat(getResources().getString(R.string.time_format));
            TimePickerDialog( this , {
                view, hourOfDay, minute ->
                delay_spinner.setSelection(0)
                val time = Calendar.getInstance()
                time.set( Calendar.HOUR_OF_DAY , hourOfDay )
                time.set( Calendar.MINUTE , minute )
                time.set( Calendar.SECOND , 0 )
                time_text.setText(sdf.format(time.getTime()))
                this.time = time

                delayMinutes = delays[0]
                delay_display_text.setText( R.string.label_time_button)
            } , now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true )
                    .show()
        })
        // 検索ボタンと経由フィールドでのエンターキー
        subscriptions.add(Observable.merge(
                search.clickEvents(),
                via_edit.editorActionEvents().filter { it.actionId()==EditorInfo.IME_ACTION_DONE }
        )       .filter { search.isEnabled() }
                .subscribe{
            startActivity(Intent( Intent.ACTION_VIEW , Uri.parse(makeQuery()) ))
        })
        // 発着駅のフィールドが空白の時は検索ボタンを無効化
        subscriptions.add(Observable.combineLatest(faddr_edit.textChanges(),taddr_edit.textChanges()){
            from, to -> from.length() > 0 && to.length() > 0 && !from.toString().equals(to.toString())
        }.subscribe{
            search.setEnabled(it)
            create_icon.setEnabled(it)
        })
        // アイコン作成ボタン
        subscriptions.add(create_icon.clickEvents().subscribe{
            AlertDialog
                    .Builder(this)
                    .setTitle(R.string.label_createicon)
                    .setMessage(R.string.message_withtime)
                    .setPositiveButton(R.string.label_yes){ d,w-> createIcon(withTime=false)}
                    .setNegativeButton(R.string.label_no){ d,w-> createIcon(withTime=true)}
                    .setNeutralButton(R.string.label_cancel, null)
                    .show()
        })
        // 発駅でのエンターキー
        subscriptions.add(faddr_edit.editorActionEvents().filter { it.actionId()==EditorInfo.IME_ACTION_NEXT}.subscribe{
            taddr_edit.requestFocus()
        })
        // 着駅でのエンターキー
        subscriptions.add(taddr_edit.editorActionEvents().filter { it.actionId()==EditorInfo.IME_ACTION_NEXT}.subscribe{
            via_edit.requestFocus()
        })
        // メニューのOSSライセンス
        subscriptions.add( optionsMenuClickObservable.filter {it.getItemId() == R.id.menu_oss } .subscribe {
            val message = getAssets().open("LICENSE.TXT").reader(charset = Charsets.UTF_8 ).use { it.readText() }
            AlertDialog.Builder(this).setTitle(R.string.menu_oss).setMessage(message).setPositiveButton(R.string.label_ok,null).show()
        })
        val resultOk = activityResultObservable.filter { it.second == Activity.RESULT_OK && it.third!=null }.map { Pair(it.first,StationPickerActivity.getAddress(it.third!!)) }
        subscriptions.add( Observable.merge(
                resultOk.filter { it.first == RequestCode.FADDR.code }.map {Pair(faddr_edit, it.second)},
                resultOk.filter { it.first == RequestCode.TADDR.code }.map {Pair(taddr_edit, it.second)}
                ).subscribe {it.first.setText(it.second)})
        subscriptions.add( resultOk.filter { it.first == RequestCode.VIA.code }.subscribe {
            via_edit.setText("${via_edit.getText()} ${it.second}")
        })
   }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        activityResultObservable.onNext(Triple(requestCode, resultCode, data))
    }

    override fun onPause() {
        super.onPause()
        save()
    }

    override fun onDestroy() {
        super.onDestroy()
        subscriptions.unsubscribe()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = getMenuInflater()
        inflater.inflate(R.menu.menu, menu);
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        item?.let { optionsMenuClickObservable.onNext(item) }
        return true
    }

    private fun makeQuery(withTime : Boolean = true) : String {
        var url = "http://transit.yahoo.co.jp/search/result?"

//        発緯度経度
//        flatlon=<ここに入れる数値のフォーマットが不明なので未使用 2015/08/11>
        url += "flatlon="
//                発駅
//        &from=%E7%9F%A2%E5%B7%9D
        val from = URLEncoder.encode(faddr_edit.getText().toString(), "utf-8")
        url += "&from=${from}"
//        着緯度経度
//        &tlatlon=
        url += "&tlatlon="
//                着駅
//        &to=%E5%B0%8F%E6%B8%95%E6%B2%A2
        val to = URLEncoder.encode(taddr_edit.getText().toString(), "utf-8")
        url += "&to=${to}"

//        経由地
//        &via=
//                &via=
//        &via=
        via_edit.getText().toString().split(" |　|、|。".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                .map{ url += "&via=${URLEncoder.encode(it,"utf-8")}" }

        if ( withTime ) {
            var qdate = time ?: Calendar.getInstance().let { it.add(Calendar.MINUTE, delayMinutes); it }
            //                年
            //        &y=2015
            url += "&y=${qdate.get(Calendar.YEAR).format(4)}"
            //        月
            //        &m=07
            url += "&m=${(qdate.get(Calendar.MONTH) + 1).format(2)}"
            //        日
            //        &d=17
            url += "&d=${qdate.get(Calendar.DAY_OF_MONTH).format(2)}"
            //        時
            //        &hh=16
            url += "&hh=${qdate.get(Calendar.HOUR_OF_DAY).format(2)}"
            //        分（上位）
            //        &m1=0
            url += "&m1=${(qdate.get(Calendar.MINUTE) / 10).format(1)}"
            //        分（下位）
            //        &m2=0
            url += "&m2=${(qdate.get(Calendar.MINUTE) % 10).format(1)}"
        }
//        時刻の意味（1:出発 4:到着 3:始発 2:終電 5:指定無し)
//        &type=1
        val type = when {
            type_departure.isChecked() -> 1
            type_arrival.isChecked() -> 4
            type_first.isChecked() ->3
            type_last.isChecked() ->2
            else -> 5
        }
        url += "&type=${type}"
//        切符種別(ic normal)
//        &ticket=ic
        val ticket = if ( ticket_ic.isChecked() ){ "ic" }else{"normal"}
        url += "&ticket=${ticket}"

//        空路(OFFの場合はパラメータ無し)
//        &al=1
        if (airline.isChecked()) {
            url += "&al=1"
        }

//        新幹線(OFFの場合はパラメータ無し)
//        &shin=1
        if (shinkansen.isChecked()) {
            url += "&shin=1"
        }
//        有料特急(OFFの場合はパラメータ無し)
//        &ex=1
        if (express.isChecked()) {
            url += "&ex=1"
        }
//        高速バス(OFFの場合はパラメータ無し)
//        &hb=1
        if (highwaybus.isChecked()) {
            url += "&hb=1"
        }
//        路線バス(OFFの場合はパラメータ無し)
//        &lb=1
        if (localbus.isChecked()) {
            url += "&lb=1"
        }
//        フェリー(OFFの場合はパラメータ無し)
//        &sr=1
        if (ferry.isChecked()) {
            url += "&sr=1"
        }
//        表示順序(0:到着が早い順 2:乗り換え回数順 1:料金が安い順)
//        &s=0
        val seq = when {
            sort_time.isChecked() -> 0
            sort_fare.isChecked() -> 1
            sort_num.isChecked() -> 2
            else -> 0
        }
        url += "&s=${seq}"
//        席指定 (1:自由席優先 2:指定席優先 3:グリーン車優先)
//        &expkind=1
        url += "&expkind=1"

//        歩く速度(1:急いで 2:標準 3:少しゆっくり 4:ゆっくり)
//        &ws=2
        val wspos = walkspeed.getSelectedItemPosition()
        val ws = walkSpeeds[wspos]
        url += "&ws=${ws}"

//        不明(着駅と同じ、無くても結果変わらない？）
//        &kw=%E5%B0%8F%E6%B8%95%E6%B2%A2
//        url += "&kw=${to}"

        if ( BuildConfig.DEBUG ) {
            Log.d("=========>", url)
        }
        return url
    }

    private fun createIcon(withTime : Boolean )
    {
        val targetIntent = Intent( Intent.ACTION_VIEW , Uri.parse(makeQuery(withTime)) )

        val intent = Intent("com.android.launcher.action.INSTALL_SHORTCUT")
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, targetIntent);
        intent.putExtra("duplicate", false);

        val icon = Intent.ShortcutIconResource.fromContext(this, R.mipmap.ic_shortcut );
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "${faddr_edit.getText()}.${taddr_edit.getText()}");

        sendBroadcast(intent);
    }

    private fun load()
    {
        type_departure.setChecked( settings.type_departure )
        type_arrival .setChecked( settings.type_arrival )
        type_first .setChecked( settings.type_first )
        type_last .setChecked( settings.type_last )

        sort_time .setChecked( settings.sort_time )
        sort_fare .setChecked( settings.sort_fare )
        sort_num .setChecked( settings.sort_num )

        ticket_ic .setChecked( settings.ticket_ic )
        ticket_normal .setChecked( settings.ticket_normal )

        express .setChecked( settings.express )
        shinkansen .setChecked( settings.shinkansen )
        airline .setChecked( settings.airline )
        highwaybus .setChecked( settings.highwaybus )
        localbus .setChecked( settings.localbus )
        ferry .setChecked( settings.ferry )

        walkspeed.setSelection( settings.walkspeed )
    }

    private fun save()
    {
        settings.edit {
            settings.type_departure = type_departure.isChecked()
            settings.type_arrival = type_arrival.isChecked()
            settings.type_first = type_first.isChecked()
            settings.type_last = type_last.isChecked()

            settings.sort_time = sort_time.isChecked()
            settings.sort_fare = sort_fare.isChecked()
            settings.sort_num = sort_num.isChecked()

            settings.ticket_ic = ticket_ic.isChecked()
            settings.ticket_normal = ticket_normal.isChecked()

            settings.express = express.isChecked()
            settings.shinkansen = shinkansen.isChecked()
            settings.airline = airline.isChecked()
            settings.highwaybus = highwaybus.isChecked()
            settings.localbus = localbus.isChecked()
            settings.ferry = ferry.isChecked()

            settings.walkspeed = walkspeed.getSelectedItemPosition()
        }
    }
}
