/*
 *     PowerSwitch by Max Rosin & Markus Ressel
 *     Copyright (C) 2015  Markus Ressel
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.power_switch.gui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mikepenz.iconics.view.IconicsImageView;

import java.util.ArrayList;

import eu.power_switch.R;

/**
 * Adapter to visualize Action items in RecyclerView
 * <p/>
 * Created by Markus on 04.12.2015.
 */
public class SsidRecyclerViewAdapter extends RecyclerView.Adapter<SsidRecyclerViewAdapter.ViewHolder> {
    private ArrayList<String> ssids;
    private Context context;
    private OnItemClickListener onDeleteClickListener;

    public SsidRecyclerViewAdapter(Context context, ArrayList<String> ssids) {
        this.ssids = ssids;
        this.context = context;
    }

    public void setOnDeleteClickListener(OnItemClickListener onItemClickListener) {
        this.onDeleteClickListener = onItemClickListener;
    }

    @Override
    public SsidRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.list_item_ssid, parent, false);
        return new SsidRecyclerViewAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final SsidRecyclerViewAdapter.ViewHolder holder, int position) {
        final String ssid = ssids.get(holder.getAdapterPosition());
        holder.ssid.setText(ssid);

        if (holder.getAdapterPosition() == getItemCount() - 1) {
            holder.footer.setVisibility(View.VISIBLE);
        } else {
            holder.footer.setVisibility(View.GONE);
        }
    }

    // Return the total count of items
    @Override
    public int getItemCount() {
        return ssids.size();
    }

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView ssid;
        public IconicsImageView delete;
        public LinearLayout footer;

        public ViewHolder(View itemView) {
            super(itemView);
            this.ssid = (TextView) itemView.findViewById(R.id.txt_ssid);
            this.delete = (IconicsImageView) itemView.findViewById(R.id.delete);
            this.footer = (LinearLayout) itemView.findViewById(R.id.list_footer);

            this.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onDeleteClickListener != null) {
                        onDeleteClickListener.onItemClick(delete, getLayoutPosition());
                    }
                }
            });
        }
    }
}