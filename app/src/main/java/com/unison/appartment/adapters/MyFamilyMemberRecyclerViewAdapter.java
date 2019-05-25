package com.unison.appartment.adapters;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.unison.appartment.R;
import com.unison.appartment.model.User;
import com.unison.appartment.fragments.FamilyMemberListFragment.OnFamilyMemberListFragmentInteractionListener;

import java.util.List;

/**
 * {@link RecyclerView.Adapter Adapter} che può visualizzare una lista di
 * {@link com.unison.appartment.model.HomeUser HomeUser} e che effettua una
 * chiamata al {@link OnFamilyMemberListFragmentInteractionListener listener} specificato.
 */
public class MyFamilyMemberRecyclerViewAdapter extends RecyclerView.Adapter<MyFamilyMemberRecyclerViewAdapter.ViewHolderMember> {

    private final List<User> userList;
    // TODO: implementare listener

    private final OnFamilyMemberListFragmentInteractionListener listener;

    public MyFamilyMemberRecyclerViewAdapter(List<User> userList, OnFamilyMemberListFragmentInteractionListener listener) {
        this.userList = userList;
        this.listener = listener;
    }

    @Override
    public ViewHolderMember onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_family_member, parent, false);
        return new ViewHolderMember(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolderMember holder, int position) {
        ViewHolderMember holderMember = (ViewHolderMember) holder;
        final User user = (User) userList.get(position);
        // TODO risistemare con HomeUser e non User
        holderMember.textMemberName.setText("riccardo");
        holderMember.textMemberPoints.setText("578");
//        holderMember.textMemberName.setText(user.getName());
//        holderMember.textMemberPoints.setText(String.valueOf(user.getLastPoints()));
//      holderMember.imageMember.setImageURI(memberItem.getImage());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onFamilyMemberListFragmentOpenMember(user);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class ViewHolderMember extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView imageMember;
        public final TextView textMemberName;
        public final TextView textMemberPoints;
        public final TextView textMemberRole;

        public ViewHolderMember(View view) {
            super(view);
            mView = view;
            imageMember = view.findViewById(R.id.fragment_family_member_img_member);
            textMemberName = view.findViewById(R.id.fragment_family_member_text_name);
            textMemberPoints = view.findViewById(R.id.fragment_family_member_text_points_value);
            textMemberRole = view.findViewById(R.id.fragment_family_member_text_role);
        }
    }
}