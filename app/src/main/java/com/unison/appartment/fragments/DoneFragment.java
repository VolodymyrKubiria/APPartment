package com.unison.appartment.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.unison.appartment.R;
import com.unison.appartment.dummy.DummyContent;


public class DoneFragment extends Fragment implements AllCompletedTasksListFragment.OnAllCompletedTasksListFragmentInteractionListener {

    private TextView emptyTodoListTitle;
    private TextView emptyTodoListText;

    /**
     * Costruttore vuoto obbligatorio che viene usato nella creazione del fragment
     */
    public DoneFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment RewardsFragment.
     */
    public static DoneFragment newInstance(String param1, String param2) {
       return new DoneFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View myView = inflater.inflate(R.layout.fragment_done, container, false);
        emptyTodoListTitle = myView.findViewById(R.id.fragment_done_empty_completedtask_list_title);
        emptyTodoListText = myView.findViewById(R.id.fragment_done_empty_completedtask_list_text);
        // Inflate the layout for this fragment
        return myView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onAllCompletedTasksListFragmentInteraction(DummyContent.DummyItem item) {

    }

    @Override
    public void onAllCompletedTasksListElementsLoaded(int elements) {
        // Sia che la lista abbia elementi o meno, una volta fatta la lettura la
        // progress bar deve interrompersi
        ProgressBar progressBar = getView().findViewById(R.id.fragment_done_progress);
        progressBar.setVisibility(View.GONE);

        // Se gli elementi sono 0 allora mostro un testo che lo indichi all'utente
        if (elements == 0) {
            emptyTodoListTitle.setVisibility(View.VISIBLE);
            emptyTodoListText.setVisibility(View.VISIBLE);
        } else {
            emptyTodoListTitle.setVisibility(View.GONE);
            emptyTodoListText.setVisibility(View.GONE);
        }
    }
}
