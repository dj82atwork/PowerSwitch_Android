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
import android.os.Build;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

import eu.power_switch.R;
import eu.power_switch.gui.IconicsHelper;
import eu.power_switch.gui.animation.AnimationHandler;
import eu.power_switch.obj.Room;
import eu.power_switch.shared.log.Log;

/**
 * * Adapter to visualize Room name items in RecyclerView for reordering
 * <p/>
 * Created by Markus on 13.10.2015.
 */
public class RoomNameRecyclerViewAdapter extends RecyclerView.Adapter<RoomNameRecyclerViewAdapter.ViewHolder> implements ItemTouchHelperAdapter {
    private ArrayList<Room> rooms;
    private Context context;
    private OnStartDragListener onStartDragListener;

    public RoomNameRecyclerViewAdapter(Context context, ArrayList<Room> rooms, OnStartDragListener onStartDragListener) {
        this.rooms = rooms;
        this.context = context;
        this.onStartDragListener = onStartDragListener;
    }

    @Override
    public RoomNameRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.list_item_receiver_name, parent, false);
        return new RoomNameRecyclerViewAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final RoomNameRecyclerViewAdapter.ViewHolder holder, int position) {
        final Room room = rooms.get(holder.getAdapterPosition());
        holder.roomName.setText(room.getName());

        holder.dragHandle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEventCompat.getActionMasked(event) ==
                        MotionEvent.ACTION_DOWN) {
                    onStartDragListener.onStartDrag(holder);
                }
                return false;
            }
        });
    }

    // Return the total count of items
    @Override
    public int getItemCount() {
        return rooms.size();
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(rooms, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(rooms, i, i - 1);
            }
        }

        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onItemDismiss(int position) {
        Log.d("Item " + position + " dismissed");
        rooms.remove(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {
        public LinearLayout mainLayout;
        public TextView roomName;
        public ImageView dragHandle;

        public ViewHolder(View itemView) {
            super(itemView);
            this.mainLayout = (LinearLayout) itemView.findViewById(R.id.linear_layout_main);
            this.roomName = (TextView) itemView.findViewById(R.id.txt_name);
            this.dragHandle = (ImageView) itemView.findViewById(R.id.drag_handle);
            this.dragHandle.setImageDrawable(IconicsHelper.getReorderHandleIcon(context));
        }

        @Override
        public void onItemSelected() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                float toElevation = context.getResources().getDimension(R.dimen.list_element_elevation_while_moving);
                AnimationHandler.animateElevation(mainLayout, 0, toElevation, 200);
            }
        }

        @Override
        public void onItemClear() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                float fromElevation = context.getResources().getDimension(R.dimen.list_element_elevation_while_moving);
                AnimationHandler.animateElevation(mainLayout, fromElevation, 0, 200);
            }
        }
    }
}