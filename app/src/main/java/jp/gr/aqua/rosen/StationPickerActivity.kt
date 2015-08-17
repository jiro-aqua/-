package jp.gr.aqua.rosen

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import kotlinx.android.synthetic.location_list.*
import rx.Observable
import rx.lang.kotlin.PublishSubject
import rx.subscriptions.CompositeSubscription
import kotlin.platform.platformStatic
import kotlin.properties.Delegates

public class StationPickerActivity : AppCompatActivity() {

    val subscriptions = CompositeSubscription()
    val sp : SharedPreferences by Delegates.lazy {getSharedPreferences("stations", Context.MODE_PRIVATE )}

    val stations = arrayListOf("--")
    val stationAdapter : ArrayAdapter<String> by Delegates.lazy { ArrayAdapter<String>(this,R.layout.location_list_row,  stations) }

    val contextMenuClickObservable = PublishSubject<MenuItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.app_title)
        setContentView(R.layout.location_list)

        val init = getIntent().getStringExtra(EXTRA_INITIAL_STRING)
        init?.let { station_edit.setText(it) }

        list.setAdapter(stationAdapter)
        registerForContextMenu(list)

        refresh()

        // 登録ボタンまたは入力欄でエンターキー
        subscriptions.add( Observable.merge(
                station_register.clickEvents(),
                station_edit.editorActionEvents().filter { it.actionId() == EditorInfo.IME_ACTION_DONE }
        ) .filter { station_register.isEnabled()}
          .subscribe {
            sp.edit().putLong( station_edit.getText().toString(), System.currentTimeMillis() ) .apply()
            refresh()
            station_edit.setText("")
        })
        // 入力欄の内容で登録ボタンを無効化
        subscriptions.add(station_edit.textChanges().subscribe {
            station_register.setEnabled( it.length() != 0 )
        })
        // リストのアイテムクリック
        subscriptions.add(list.itemClickEvents().subscribe {
            val item = stationAdapter.getItem(it.position())
            val result = Intent()
            result.putExtra(EXTRA_ADDRESS, item)
            setResult( Activity.RESULT_OK , result )
            finish()
        })
        // リストの内容変更
        subscriptions.add(stationAdapter.dataChanges().subscribe {
            empty.setVisibility( if ( it.getCount()== 0) View.VISIBLE else View.GONE )
        })
        // コンテクストメニューのアイテムクリック
        subscriptions.add(contextMenuClickObservable.subscribe {
            val contextmenuinfo = it.getMenuInfo() as AdapterView.AdapterContextMenuInfo
            val textview = contextmenuinfo.targetView as TextView
            val text = textview.getText().toString()
            when( it.getItemId() ){
                1 -> sp.edit().remove(text).apply()         // 削除
                2 -> sp.edit().putLong(text,System.currentTimeMillis()).apply() // 先頭に移動
            }
            refresh()
        })
    }

    private fun refresh() {
        stations.clear()
//        SharedPreferencesの中身を列挙して、
//        キー／バリューPairの配列に変換して、
//        バリュー値で降順にソートして、
//        stationsの中にキー値を格納する。
//        ※ここはRxJavaではなく、KotlinのコレクションAPIで操作
        sp.getAll().map { Pair(it.getKey(), it.getValue() as Long ) }.sortBy { -it.second }.mapTo(stations) { it.first }
        stationAdapter.notifyDataSetChanged()
    }

    override fun onDestroy() {
        super.onDestroy()
        subscriptions.unsubscribe()
    }

    override fun onCreateContextMenu(contextmenu: ContextMenu, view: View , contextmenuinfo: android.view.ContextMenu.ContextMenuInfo )
    {
        super.onCreateContextMenu(contextmenu, view, contextmenuinfo);
        contextmenu.add(0, 1, 0, R.string.menu_remove);
        contextmenu.add(0, 2, 1, R.string.menu_move_to_top);
    }

    override fun onContextItemSelected(item: MenuItem?): Boolean {
        item?.let{ contextMenuClickObservable.onNext(item) }
        return true;
    }

    companion object {
        platformStatic public fun getStartIntent(context : Context, init: String?) : Intent{
            val intent = Intent( context , javaClass<StationPickerActivity>() )
            init?.let { intent.putExtra(EXTRA_INITIAL_STRING, init ) }
            return intent
        }

        platformStatic public fun getAddress(intent : Intent) : String {
            return intent.getStringExtra(EXTRA_ADDRESS)!!
        }

        private val EXTRA_ADDRESS = "addr"
        private val EXTRA_INITIAL_STRING = "init"
    }
}
