package jp.gr.aqua.rosen

import android.view.View
import android.widget.Adapter
import android.widget.AdapterView
import android.widget.TextView
import com.jakewharton.rxbinding.view.RxView
import com.jakewharton.rxbinding.widget.*
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers


public inline fun View.clickEvents(): Observable<Void> = RxView.clicks(this)
public inline fun View.longClickEvents(): Observable<Void> = RxView.longClicks(this)
public inline fun TextView.textChanges(): Observable<CharSequence> = RxTextView.textChanges(this)
public inline fun TextView.editorActionEvents(): Observable<TextViewEditorActionEvent> = RxTextView.editorActionEvents(this)
public inline fun <T : Adapter> AdapterView<T>.itemClickEvents(): Observable<AdapterViewItemClickEvent> = RxAdapterView.itemClickEvents(this)
public inline fun <T : Adapter> AdapterView<T>.itemLongClickEvents(): Observable<AdapterViewItemLongClickEvent> = RxAdapterView.itemLongClickEvents(this)
public inline fun <T : Adapter> AdapterView<T>.selectionEvents(): Observable<AdapterViewSelectionEvent> = RxAdapterView.selectionEvents(this)
public inline fun <T : Adapter> AdapterView<T>.itemSelections(): Observable<Int> = RxAdapterView.itemSelections(this)
public inline fun <T : Adapter> T.dataChanges(): Observable<T> = RxAdapter.dataChanges(this)

fun <T> Observable<T>.doInBackground() : Observable<T> {
    return this.subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
}

public inline fun Int.format(digits: Int) : String = java.lang.String.format("%0${digits}d", this)

