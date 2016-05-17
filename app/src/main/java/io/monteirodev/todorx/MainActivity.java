package io.monteirodev.todorx;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxAdapterView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subscriptions.CompositeSubscription;

public class MainActivity extends AppCompatActivity {

    private static final String LIST = "list";
    public static final String FILTER = "filter";

    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.spinner) Spinner spinner;
    @Bind(R.id.add_todo_input) EditText addInput;
    @Bind(R.id.recyclerview) RecyclerView recyclerView;

    TodoList list;
    int filterPosition = FilterPositions.ALL;

    CompositeSubscription subscriptions = new CompositeSubscription();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // setup the toolbar
        setSupportActionBar(toolbar);

        // retrieve from Saved State or SharedPreferences if we are starting fresh
        if (savedInstanceState != null) {
            list = new TodoList(savedInstanceState.getString(LIST));
            filterPosition = savedInstanceState.getInt(FILTER, FilterPositions.ALL);
        } else {
            list = new TodoList(getSharedPreferences("data", Context.MODE_PRIVATE).getString(LIST, null));
            list.add(new Todo("Sample 1", true));
            list.add(new Todo("Sample 2", false));
            list.add(new Todo("Sample 3", false));
        }

        // setup the Adapter, this contains a callback when an item is checked/unchecked
        TodoAdapter adapter = new TodoAdapter(this, list);

        // setup the list with the adapter
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);

        // setup adding new items to the list
        findViewById(R.id.add_todo_container).requestFocus(); // ensures the edittext isn't focused when entering the Activity


//        findViewById(R.id.btn_add_todo).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String item = addInput.getText().toString();
//
//                // ensure we don't add empty items
//                if (!TextUtils.isEmpty(item.trim())) {
//
//                    // update our list
//                    list.add(new Todo(item, false));
//
//                    // update the adapter with the latest filtered data
//                    // TodoList Will push out any changes automatically to any subscribers
//                    //adapter.onTodoListChanged(filter.getFilteredData());
//
//                    // clear input, remove focus, and hide keyboard
//                    addInput.setText("");
//                    findViewById(R.id.add_todo_container).requestFocus();
//                    dismissKeyboard();
//                }
//            }
//        });

        // emit items when clicked
        subscriptions.add(RxView.clicks(findViewById(R.id.btn_add_todo))
                .map(new Func1<Void, String>() {
                    @Override
                    public String call(Void aVoid) {
                        return addInput.getText().toString();
                    }
                })
                .filter(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String s) {
                        return !TextUtils.isEmpty(s);
                    }
                })
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        // update our list
                        list.add(new Todo(s, false));

                        // clear input, remove focus, and hide keyboard
                        addInput.setText("");
                        findViewById(R.id.add_todo_container).requestFocus();
                        dismissKeyboard();
                    }
                })
        );



        // setup the filter in the toolbar
        subscriptions.add(Observable.combineLatest(
                RxAdapterView.itemSelections(spinner).skip(1),
                list.asObservable(),
                new Func2<Integer, TodoList, List<Todo>>() {
                    @Override
                    public List<Todo> call(Integer integer, TodoList todoList) {
                        switch (integer) {
                            case FilterPositions.INCOMPLETE:
                                return list.getIncomplete();
                            case FilterPositions.COMPLETE:
                                return list.getComplete();
                            default:
                                return list.getAll();
                        }
                    }
                }
                ).subscribe(adapter)
        );
        spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new String[]{"All", "Incomplete", "Completed"}));
        spinner.setSelection(filterPosition);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(LIST, list.toString());
        outState.putInt(FILTER, spinner.getSelectedItemPosition());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        subscriptions.unsubscribe();

        SharedPreferences.Editor editor = getSharedPreferences("data", Context.MODE_PRIVATE).edit();
        editor.putString(LIST, list.toString());
        editor.apply();
    }

    private void dismissKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(addInput.getWindowToken(), 0);
    }
}
