package io.monteirodev.todorx;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.jakewharton.rxbinding.widget.RxCompoundButton;

import java.util.Collections;
import java.util.List;

import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoHolder>
        //implements TodoListener
        //implements Action1<TodoList> {
        implements Action1<List<Todo>> {

    LayoutInflater inflater;

    //TodoCompletedChangeListener todoChangeListener;
    Action1<Todo> subscriber;

    //TodoList data = new TodoList();
    List<Todo> data = Collections.emptyList();

    public TodoAdapter(Activity activity, Action1<Todo> listener) {
        inflater = LayoutInflater.from(activity);
        subscriber = listener;
    }

    @Override
    public TodoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TodoHolder(inflater.inflate(R.layout.item_todo, parent, false));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public void onBindViewHolder(TodoHolder holder, int position) {
        final Todo todo = data.get(position);
        holder.checkbox.setText(todo.description);
        // set the current value
        holder.checkbox.setChecked(todo.isCompleted);

//        // ensure existing listener is nulled out, setting the value causes a check changed listener callback
//        holder.checkbox.setOnCheckedChangeListener(null);
//
//        // set the current value, then setup the listener
//        holder.checkbox.setChecked(todo.isCompleted);
//        holder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                todoChangeListener.onTodoCompletedChanged(todo);
//            }
//        });

        holder.subscription = RxCompoundButton.checkedChanges(holder.checkbox)
                .skip(1)
                .map(new Func1<Boolean, Todo>() {
                    @Override
                    public Todo call(Boolean aBoolean) {
                        return todo;
                    }
                })
                .subscribe(subscriber);
    }

    @Override
    public void onViewDetachedFromWindow(TodoHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.subscription.unsubscribe();
    }

    //    @Override
//    public void onTodoListChanged(TodoList updatedList) {
//        data = updatedList;
//        notifyDataSetChanged();
//    }

//    @Override
//    public void call(TodoList todoList) {
//        data = todoList;
//        notifyDataSetChanged();
//    }

    @Override
    public void call(List<Todo> todos) {
        data = todos;
        notifyDataSetChanged();
    }

    public class TodoHolder extends RecyclerView.ViewHolder {

        public CheckBox checkbox;
        // un-bind subscriptions
        public Subscription subscription;

        public TodoHolder(View itemView) {
            super(itemView);
            checkbox = (CheckBox) itemView;
        }
    }
}
