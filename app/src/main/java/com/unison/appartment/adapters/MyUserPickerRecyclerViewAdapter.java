package com.unison.appartment.adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.unison.appartment.R;
import com.unison.appartment.fragments.UserPickerFragment.OnUserPickerFragmentInteractionListener;
import com.unison.appartment.model.HomeUser;

import java.util.List;

public class MyUserPickerRecyclerViewAdapter extends RecyclerView.Adapter<MyUserPickerRecyclerViewAdapter.ViewHolderHomeUser> {

    private final List<HomeUser> mValues;
    private final OnUserPickerFragmentInteractionListener mListener;

    public MyUserPickerRecyclerViewAdapter(List<HomeUser> items, OnUserPickerFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolderHomeUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_userpicker, parent, false);
        return new ViewHolderHomeUser(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolderHomeUser holder, int position) {
        final HomeUser item = mValues.get(position);
        String[] roles = holder.mView.getContext().getResources().getStringArray(R.array.desc_userhomes_uid_homename_role_values);
        holder.textName.setText(item.getNickname());
        holder.textRole.setText(roles[item.getRole()]);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onListFragmentInteraction(item);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolderHomeUser extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView textName;
        public final TextView textRole;

        public ViewHolderHomeUser(View view) {
            super(view);
            mView = view;
            textName = view.findViewById(R.id.fragment_userpicker_text_name);
            textRole = view.findViewById(R.id.fragment_userpicker_text_role);
        }
    }
}
