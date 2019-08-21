package com.daksh.kuro.recycelerfragment;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * A custom adapter to use with the RecyclerView widget.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {




    private OnBottomReachedListener onBottomReachedListener;

    private Context mContext;
    private ArrayList<Contact> modelList;

    private ItemClickListener mItemClickListener;


    public RecyclerViewAdapter(Context context, ArrayList<Contact> modelList) {
        this.mContext = context;
        this.modelList = modelList;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
            return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {


            if (position == modelList.size() - 1){

                onBottomReachedListener.onBottomReached(position);

            }
            ViewHolder genericViewHolder = (ViewHolder) holder;
            genericViewHolder.contacticon.setImageResource(modelList.get(position).getPhoto());
            genericViewHolder.contactname.setText(modelList.get(position).getName());
            genericViewHolder.phonenumber.setText(modelList.get(position).getPhone());
    }



    @Override
    public int getItemCount() {

        return modelList.size();
    }

    public void SetOnItemClickListener(final ItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    private Contact getItem(int position) {
        return modelList.get(position);
    }

    public void setOnBottomReachedListener(OnBottomReachedListener onBottomReachedListener){

        this.onBottomReachedListener = onBottomReachedListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private ImageView contacticon;
        private TextView contactname;
        private TextView phonenumber;

        public ViewHolder(final View itemView) {
            super(itemView);

            this.contacticon =itemView.findViewById(R.id.contact_icon);
            this.contactname = itemView.findViewById(R.id.contact_name);
            this.phonenumber = itemView.findViewById(R.id.phone_number);


            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            mItemClickListener.onItemClick(v,getAdapterPosition());
        }
    }

}
