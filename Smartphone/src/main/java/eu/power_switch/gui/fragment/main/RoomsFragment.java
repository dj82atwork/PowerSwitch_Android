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

package eu.power_switch.gui.fragment.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.developer.PlayStoreModeDataModel;
import eu.power_switch.gui.IconicsHelper;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.adapter.RoomRecyclerViewAdapter;
import eu.power_switch.gui.animation.AnimationHandler;
import eu.power_switch.gui.dialog.ConfigureReceiverDialog;
import eu.power_switch.gui.fragment.RecyclerViewFragment;
import eu.power_switch.obj.Room;
import eu.power_switch.settings.DeveloperPreferencesHandler;
import eu.power_switch.settings.SmartphonePreferencesHandler;
import eu.power_switch.shared.constants.LocalBroadcastConstants;
import eu.power_switch.shared.constants.SettingsConstants;
import eu.power_switch.shared.log.Log;
import eu.power_switch.wear.service.UtilityService;

/**
 * Fragment containing a List of all Rooms and Receivers
 */
public class RoomsFragment extends RecyclerViewFragment {

    private ArrayList<Room> rooms;

    private BroadcastReceiver broadcastReceiver;
    private View rootView;
    private FloatingActionButton addReceiverFAB;
    private RoomRecyclerViewAdapter roomsRecyclerViewAdapter;
    private RecyclerView recyclerViewRooms;
    private CoordinatorLayout contentLayout;
    private LinearLayout layoutLoading;

    /**
     * Used to notify Room Fragment (this) that Rooms have changed
     *
     * @param context any suitable context
     */
    public static void sendReceiverChangedBroadcast(Context context) {
        Log.d("RoomsFragment", "sendReceiverChangedBroadcast");
        Intent intent = new Intent(LocalBroadcastConstants.INTENT_RECEIVER_CHANGED);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        UtilityService.forceWearDataUpdate(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        rootView = inflater.inflate(R.layout.fragment_rooms, container, false);
        setHasOptionsMenu(true);

        layoutLoading = (LinearLayout) rootView.findViewById(R.id.layoutLoading);
        contentLayout = (CoordinatorLayout) rootView.findViewById(R.id.contentLayout);

        rooms = new ArrayList<>();
        recyclerViewRooms = (RecyclerView) rootView.findViewById(R.id.recyclerview_list_of_rooms);
        roomsRecyclerViewAdapter = new RoomRecyclerViewAdapter(this, getActivity(), rooms);
        recyclerViewRooms.setAdapter(roomsRecyclerViewAdapter);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(
                getResources().getInteger(R.integer.room_grid_span_count), StaggeredGridLayoutManager.VERTICAL);
        recyclerViewRooms.setLayoutManager(layoutManager);
        updateUI();

        addReceiverFAB = (FloatingActionButton) rootView.findViewById(R.id.add_receiver_fab);
        addReceiverFAB.setImageDrawable(IconicsHelper.getAddIcon(getActivity(), android.R.color.white));
        final RecyclerViewFragment recyclerViewFragment = this;
        addReceiverFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AnimationHandler.checkTargetApi()) {
//                    Intent intent = new Intent();
//
//                    ActivityOptionsCompat options =
//                            ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
//                                    addReceiverFAB,   // The view which starts the transition
//                                    "configureReceiverTransition"    // The transitionName of the view we’re transitioning to
//                            );
//                    ActivityCompat.startActivity(getActivity(), intent, options.toBundle());
                } else {

                }

                if (SettingsConstants.INVALID_APARTMENT_ID == SmartphonePreferencesHandler.getCurrentApartmentId()) {
                    new AlertDialog.Builder(getContext())
                            .setMessage(R.string.please_create_or_activate_apartment_first)
                            .setNeutralButton(android.R.string.ok, null)
                            .show();
                    return;
                }

                ConfigureReceiverDialog configureReceiverDialog = new ConfigureReceiverDialog();
                configureReceiverDialog.setTargetFragment(recyclerViewFragment, 0);
                configureReceiverDialog.show(getFragmentManager(), null);
            }
        });

        // BroadcastReceiver to get notifications from background service if room data has changed
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("RoomsFragment", "received intent: " + intent.getAction());
                updateUI();
            }
        };

        return rootView;
    }

    private void updateUI() {
        layoutLoading.setVisibility(View.VISIBLE);
        contentLayout.setVisibility(View.GONE);

        new AsyncTask<Context, Void, Exception>() {
            @Override
            protected Exception doInBackground(Context... contexts) {
                try {
                    rooms.clear();

                    if (DeveloperPreferencesHandler.getPlayStoreMode()) {
                        PlayStoreModeDataModel playStoreModeDataModel = new PlayStoreModeDataModel(getActivity());
                        rooms.addAll(playStoreModeDataModel.getRooms());
                    } else {
                        long currentApartmentId = SmartphonePreferencesHandler.getCurrentApartmentId();
                        if (currentApartmentId != SettingsConstants.INVALID_APARTMENT_ID) {
                            // Get Rooms and Receivers
                            rooms.addAll(DatabaseHandler.getRooms(currentApartmentId));
                        }
                    }

                    return null;
                } catch (Exception e) {
                    return e;
                }
            }

            @Override
            protected void onPostExecute(Exception exception) {
                layoutLoading.setVisibility(View.GONE);

                if (exception == null) {
                    roomsRecyclerViewAdapter.notifyDataSetChanged();
                    contentLayout.setVisibility(View.VISIBLE);
                } else {
                    contentLayout.setVisibility(View.GONE);
                    StatusMessageHandler.showErrorMessage(getContext(), exception);
                }
            }
        }.execute(getContext());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (super.onOptionsItemSelected(menuItem)) {
            return true;
        }

        switch (menuItem.getItemId()) {
            case R.id.create_receiver:
                ConfigureReceiverDialog configureReceiverDialog = new ConfigureReceiverDialog();
                configureReceiverDialog.setTargetFragment(this, 0);
                configureReceiverDialog.show(getFragmentManager(), null);
            default:
                break;

        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (SmartphonePreferencesHandler.getHideAddFAB()) {
            inflater.inflate(R.menu.room_fragment_menu, menu);
            if (SettingsConstants.THEME_DARK_BLUE == SmartphonePreferencesHandler.getTheme()) {
                menu.findItem(R.id.create_receiver)
                        .setIcon(IconicsHelper.getAddIcon(getActivity(), android.R.color.white));
            } else {
                menu.findItem(R.id.create_receiver)
                        .setIcon(IconicsHelper.getAddIcon(getActivity(), android.R.color.black));
            }
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (SmartphonePreferencesHandler.getHideAddFAB()) {
            addReceiverFAB.setVisibility(View.GONE);
        } else {
            addReceiverFAB.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocalBroadcastConstants.INTENT_APARTMENT_CHANGED);
        intentFilter.addAction(LocalBroadcastConstants.INTENT_RECEIVER_CHANGED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        super.onStop();
    }

    @Override
    public RecyclerView getRecyclerView() {
        return recyclerViewRooms;
    }
}