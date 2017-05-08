package com.koszacharis.bss.app.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.koszacharis.bss.app.R;
import com.koszacharis.bss.app.activities.StationActivity;
import com.koszacharis.bss.app.models.Station;

import java.util.ArrayList;

public class StationsListFragment extends Fragment {
    private static final String KEY_STATION = "station";
    private static final String KEY_STATIONS = "stations";
    private static final String KEY_EMPTY_LIST_RESOURCE_ID = "emptyListResourceId";

    private ArrayList<Station> stations;
    private StationsListAdapter stationsListAdapter;
    private int emptyListResourceId;


    public static StationsListFragment stationsListFragment(ArrayList<Station> stations) {
        StationsListFragment stationsListFragment = new StationsListFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(KEY_STATIONS, stations);
        bundle.putInt(KEY_EMPTY_LIST_RESOURCE_ID, R.string.no_stations);
        stationsListFragment.setArguments(bundle);
        return stationsListFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        stations = (ArrayList<Station>) getArguments().getSerializable(KEY_STATIONS);
        emptyListResourceId = getArguments().getInt(KEY_EMPTY_LIST_RESOURCE_ID);

        stationsListAdapter = new StationsListAdapter(getActivity(),
                stations);
        stationsListAdapter.clear();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stations_list, container, false);
        final ListView listView = (ListView) view.findViewById(R.id.stationsListView);
        listView.setAdapter(stationsListAdapter);
        TextView emptyView = (TextView) view.findViewById(R.id.emptyList);
        emptyView.setText(emptyListResourceId);
        listView.setEmptyView(view.findViewById(R.id.emptyList));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent intent = new Intent(getActivity(), StationActivity.class);
                intent.putExtra(KEY_STATION, stations.get(position));
                startActivity(intent);
//                SpannableStringBuilder builder = new SpannableStringBuilder();
//                builder.append("Station selected").append(" ");
//                builder.setSpan(new ImageSpan(getActivity(), R.drawable.ic_done), builder.length() - 1, builder.length(), 0);
//                Snackbar.make(getView().findViewById(R.id.myCoordinatorLayoutStation), builder, Snackbar.LENGTH_LONG).show();
            }
        });

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition = (listView == null || listView.getChildCount() == 0) ? 0 : listView.getChildAt(0).getTop();

            }
        });


        return view;
    }

    public void updateStationsList(ArrayList<Station> stations) {
        if (stationsListAdapter != null) {
            stationsListAdapter.clear();
            stationsListAdapter.addAll(stations);
            stationsListAdapter.notifyDataSetChanged();
        }
    }


    private class StationsListAdapter extends ArrayAdapter<Station> {
        TextView stationName;
        TextView freeBikes;
        TextView emptySlots;

        public StationsListAdapter(Context context, ArrayList<Station> stations) {
            super(context, R.layout.station_list_item, stations);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                LayoutInflater layoutInflater = LayoutInflater.from(getContext());
                convertView = layoutInflater.inflate(R.layout.station_list_item, parent, false);
            }

            final Station station = getItem(position);

            if (station != null) {
                stationName = (TextView) convertView.findViewById(R.id.stations);
                freeBikes = (TextView) convertView.findViewById(R.id.bicyclesNumber);
                emptySlots = (TextView) convertView.findViewById(R.id.slotsNumber);

                if (stationName != null) {
                    stationName.setText(station.getName());
                }

                if (freeBikes != null) {
                    int bicycles = station.getFreeBikes();
                    freeBikes.setText(String.valueOf(bicycles));
                }

                if (emptySlots != null) {
                    int slots = station.getEmptySlots();

                    if (slots == -1) {
                        emptySlots.setText("0");
                    } else {
                        emptySlots.setText(String.valueOf(slots));
                    }
                }
            }

            return convertView;
        }
    }

}
