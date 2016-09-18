package com.example.listviewcustomadapmultiselection2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by ShiuFai on 16/9/2016.
 */
public class TeamListViewAdapter extends ArrayAdapter<Team>
{
    View row;
    ArrayList<Team> myTeams;
    int resLayout;
    Context context;

    public TeamListViewAdapter(Context context, int textViewResourceId, ArrayList<Team> myTeams) {
        super(context, textViewResourceId, myTeams);
        this.myTeams = myTeams;
        resLayout = textViewResourceId;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        row = convertView;
        if(row == null)
        {   // inflate our custom layout. resLayout == R.layout.row_team_layout.xml
            LayoutInflater ll = (LayoutInflater) context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            row = ll.inflate(resLayout, parent, false);
        }

        Team item = myTeams.get(position); // Produce a row for each Team.

        if(item != null)
        {   // Find our widgets and populate them with the Team data.
            TextView myTeamDescription = (TextView) row.findViewById(R.id.listview_TeamDescription);
            TextView myTeamWins = (TextView) row.findViewById(R.id.listview_TeamWins);
            if(myTeamDescription != null)
                myTeamDescription.setText(item.getTeamName());
            if(myTeamWins != null)
                myTeamWins.setText("Wins: " + String.valueOf(item.getTeamWins()));
        }
        return row;
    }
}
