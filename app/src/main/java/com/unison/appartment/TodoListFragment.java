package com.unison.appartment;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.unison.appartment.dummy.DummyContent;
import com.unison.appartment.dummy.DummyContent.DummyItem;
import com.unison.appartment.model.Task;

import java.util.List;


public class TodoListFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;

    private RecyclerView.Adapter myAdapter;
    private RecyclerView myRecyclerView;

    /**
     * Costruttore vuoto obbligatorio che viene usato nella creazione del fragment
     */
    public TodoListFragment() {
    }

    @SuppressWarnings("unused")
    public static TodoListFragment newInstance(int columnCount) {
        TodoListFragment fragment = new TodoListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Quando il fragment è creato recupero i parametri
        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_todo_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            myRecyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                myRecyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                myRecyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            myAdapter = new MyTodoListRecyclerViewAdapter(Task.TASKS/*, mListener*/);
            myRecyclerView.setAdapter(myAdapter);
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
    
    public void addTask(Task newTask) {
        Task.addTask(0, newTask);
        myAdapter.notifyItemInserted(0);
        myRecyclerView.scrollToPosition(0);
    }
}
