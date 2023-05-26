package com.tudorvalentine.augmentedimages.associationslist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.tudorvalentine.augmentedimages.R;

import java.util.List;

public class AssociationAdapter extends RecyclerView.Adapter<AssociationAdapter.ViewHolder> {

    private final LayoutInflater inflater;
    private final List<Association> associations;

    public AssociationAdapter(LayoutInflater inflater, List<Association> associations){
        this.associations = associations;
        this.inflater = inflater;
    }

    @NonNull
    @Override
    public AssociationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_list, parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AssociationAdapter.ViewHolder holder, int position) {
        Association association = associations.get(position);

        Picasso.get().load(association.getUrl_image()).fit().into(holder.image_assoc);
        holder.arrow.setImageResource(association.getSource_arrow());
        Picasso.get().load(association.getUrl_prev()).fit().into(holder.doc_prev);
    }

    @Override
    public int getItemCount() {
        return associations.size();
    }
    public void restoreItem(Association item, int position) {
        associations.add(position, item);
        notifyItemInserted(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        final ImageView image_assoc, doc_prev, arrow;
        ViewHolder(View view){
            super(view);
            image_assoc = view.findViewById(R.id.image_assoc);
            doc_prev = view.findViewById(R.id.document_assoc);
            arrow = view.findViewById(R.id.arrow_assoc);
        }
    }
}
