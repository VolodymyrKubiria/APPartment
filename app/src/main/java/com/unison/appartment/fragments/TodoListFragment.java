package com.unison.appartment.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.unison.appartment.model.UncompletedTask;
import com.unison.appartment.viewmodel.TodoTaskViewModel;
import com.unison.appartment.adapters.MyTodoListRecyclerViewAdapter;
import com.unison.appartment.R;

import java.util.List;

/**
 * Fragment che rappresenta una lista di task
 */
public class TodoListFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;

    private TodoTaskViewModel viewModel;

    private ListAdapter myAdapter;
    private RecyclerView myRecyclerView;

    private OnTodoListFragmentInteractionListener listener;

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
        viewModel = ViewModelProviders.of(getActivity()).get(TodoTaskViewModel.class);
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

            myAdapter = new MyTodoListRecyclerViewAdapter(/*UncompletedTask.TASKS*//*uncompletedTasks,*/ listener);
            myAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onItemRangeInserted(int positionStart, int itemCount) {
                    myRecyclerView.smoothScrollToPosition(0);
                }
            });
            myRecyclerView.setAdapter(myAdapter);

            readUncompletedTasks();
        }
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getParentFragment() instanceof OnTodoListFragmentInteractionListener) {
            listener = (OnTodoListFragmentInteractionListener) getParentFragment();
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnTodoListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    private void readUncompletedTasks() {
        LiveData<List<UncompletedTask>> taskLiveData = viewModel.getTaskLiveData();
        taskLiveData.observe(this, new Observer<List<UncompletedTask>>() {
            @Override
            public void onChanged(List<UncompletedTask> uncompletedTasks) {
                myAdapter.submitList(uncompletedTasks);
                listener.onTodoListElementsLoaded(uncompletedTasks.size());
                Log.d("provaListAdapter", "aggiunto");
            }
        });
    }

    public void addTask(UncompletedTask newUncompletedTask) {
        /*UncompletedTask.addTask(0, newUncompletedTask);*/
/*        uncompletedTasks.add(0, newUncompletedTask);
        myAdapter.notifyItemInserted(0);
        myRecyclerView.scrollToPosition(0);
        Log.d("prova", "add");*/
        viewModel.addTask(newUncompletedTask);
    }

    /**
     * Questa interfaccia deve essere implementata dalle activity che contengono questo
     * fragment, per consentire al fragment di comunicare eventuali interazioni all'activity
     * che a sua volta può comunicare con altri fragment
     */
    public interface OnTodoListFragmentInteractionListener {
        void onTodoListFragmentOpenTask(UncompletedTask uncompletedTask);
        void onTodoListElementsLoaded(long elements);
    }
}
