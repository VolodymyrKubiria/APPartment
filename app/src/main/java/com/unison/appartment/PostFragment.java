package com.unison.appartment;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;

import com.unison.appartment.dummy.DummyContent;
import com.unison.appartment.dummy.DummyContent.DummyItem;

import java.util.List;

/**
 * Fragment che rappresenta la lista di post
 */
public class PostFragment extends Fragment {
    // Numero di colonne della lista
    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;

    // Adapter della recyclerview
    RecyclerView.Adapter myAdapter;
    RecyclerView myRecyclerView;
    // Questo fragment non effettua alcuna comunicazione con l'activity che lo contiene
    // private OnListFragmentInteractionListener mListener;

    /**
     * Costruttore vuoto obbligatorio che viene usato nella creazione del fragment
     */
    public PostFragment() {
    }

    @SuppressWarnings("unused")
    public static PostFragment newInstance(int columnCount) {
        PostFragment fragment = new PostFragment();
        // Parametri del fragment
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
        View view = inflater.inflate(R.layout.fragment_post_list, container, false);

        // Imposto l'adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            myRecyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                myRecyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                myRecyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            // Questo fragment non effettua alcuna comunicazione con l'activity che lo contiene
            // recyclerView.setAdapter(new MyPostRecyclerViewAdapter(DummyContent.ITEMS, mListener));
            myAdapter = new MyPostRecyclerViewAdapter(DummyContent.ITEMS/*, null*/);
            myRecyclerView.setAdapter(myAdapter);
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Questo fragment non effettua alcuna comunicazione con l'activity che lo contiene
        /*if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        // Questo fragment non effettua alcuna comunicazione con l'activity che lo contiene
        // mListener = null;
    }

    /**
     * Metodi implementati da noi
     */
    public void addTextPost(String message){
        DummyContent.ITEMS.add(0, new DummyItem("id", message, message));
        myAdapter.notifyItemInserted(0);
        myRecyclerView.scrollToPosition(0);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    // Questo fragment non effettua alcuna comunicazione con l'activity che lo contiene
    /*public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(DummyItem item);
    }*/
}
