package com.daksh.kuro.recycelerfragment;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class FragmentContact extends Fragment {
    private RecyclerViewAdapter mAdapter;
    private RecyclerView recyclerView;
    private ArrayList<Contact> modelList = new ArrayList<>();
    private Dialog mdialog;

    View v;
    public FragmentContact()
    {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.contact_fragment,container,false);
        recyclerView = v.findViewById(R.id.contact_recycler);
        mAdapter = new RecyclerViewAdapter(getActivity(),modelList);
        recyclerView.setHasFixedSize(true);


        // use a linear layout manager

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        recyclerView.setLayoutManager(layoutManager);


        recyclerView.setAdapter(mAdapter);


        mAdapter.SetOnItemClickListener(new ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                mdialog = new Dialog(getActivity());
                mdialog.setContentView(R.layout.contact_dialog);
                TextView dialog_name_tv = mdialog.findViewById(R.id.dialog_contact_name);
                TextView dialog_phone_tv = mdialog.findViewById(R.id.dialog_contact_number);
                ImageView dialog_person_iv = mdialog.findViewById(R.id.dialog_contact_image);
                dialog_name_tv.setText(modelList.get(position).getName());
                dialog_phone_tv.setText(modelList.get(position).getPhone());
                dialog_person_iv.setImageResource(modelList.get(position).getPhoto());
                mdialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                mdialog.show();
                //handle item click events here

            }
        });

        mAdapter.setOnBottomReachedListener(new OnBottomReachedListener() {
            @Override
            public void onBottomReached(int position) {
                Toast.makeText(getActivity(), "End Of List Reached" , Toast.LENGTH_SHORT).show();

            }
        });
        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        modelList.add(new Contact("Su Yang","159355712",R.mipmap.profile1));
        modelList.add(new Contact("Emelia Clark","357951258",R.mipmap.profile2));
        modelList.add(new Contact("Kaley Cuco ","111111111",R.mipmap.profile3));
        modelList.add(new Contact("Kim Sae-ron","333333333",R.mipmap.profile4));
        modelList.add(new Contact("Kim Yoo-jung","444444444",R.mipmap.profile5));
        modelList.add(new Contact("Jo bo-Ah","888888888",R.mipmap.profile6));
        modelList.add(new Contact("Su Yang","159355712",R.mipmap.profile1));
        modelList.add(new Contact("Emelia Clark","357951258",R.mipmap.profile2));
        modelList.add(new Contact("Kaley Cuco ","111111111",R.mipmap.profile3));
        modelList.add(new Contact("Kim Sae-ron","333333333",R.mipmap.profile4));
        modelList.add(new Contact("Kim Yoo-jung","444444444",R.mipmap.profile5));
        modelList.add(new Contact("Jo bo-Ah","888888888",R.mipmap.profile6));
        modelList.add(new Contact("Su Yang","159355712",R.mipmap.profile1));
        modelList.add(new Contact("Emelia Clark","357951258",R.mipmap.profile2));
        modelList.add(new Contact("Kaley Cuco ","111111111",R.mipmap.profile3));
        modelList.add(new Contact("Kim Sae-ron","333333333",R.mipmap.profile4));
        modelList.add(new Contact("Kim Yoo-jung","444444444",R.mipmap.profile5));
        modelList.add(new Contact("Jo bo-Ah","888888888",R.mipmap.profile6));
        modelList.add(new Contact("Su Yang","159355712",R.mipmap.profile1));
        modelList.add(new Contact("Emelia Clark","357951258",R.mipmap.profile2));
        modelList.add(new Contact("Kaley Cuco ","111111111",R.mipmap.profile3));
        modelList.add(new Contact("Kim Sae-ron","333333333",R.mipmap.profile4));
        modelList.add(new Contact("Kim Yoo-jung","444444444",R.mipmap.profile5));
        modelList.add(new Contact("Jo bo-Ah","888888888",R.mipmap.profile6));
    }
}
