package io.monteirodev.todorx;


public class TodoListFilter implements TodoListener {

    public static final int ALL = 0;
    public static final int INCOMPLETE = 1;
    public static final int COMPLETE = 2;

    private int filterMode = ALL;

    private TodoList list = new TodoList();

    public TodoListFilter(TodoList list) {
        this.list = list;
        this.list.setListener(this);
    }

    public void setFilterMode(int mode) {
        filterMode = mode;
    }

    public TodoList getFilteredData() {
        switch (filterMode) {
            case ALL:
                return list;
            case INCOMPLETE:
                TodoList incompleteOnly = new TodoList();
                for (int i = 0; i < list.size(); i++) {
                    Todo item = list.get(i);
                    if (!item.isCompleted) {
                        incompleteOnly.add(item);
                    }
                }
                return incompleteOnly;
            case COMPLETE:
                TodoList completedOnly = new TodoList();
                for (int i = 0; i < list.size(); i++) {
                    Todo item = list.get(i);
                    if (item.isCompleted) {
                        completedOnly.add(item);
                    }
                }
                return completedOnly;
            default:
                return list;
        }
    }

    @Override
    public void onTodoListChanged(TodoList updatedList) {
        list = updatedList;
    }
}
