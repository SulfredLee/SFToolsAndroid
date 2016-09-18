package com.example.listviewcustomadapmultiselection2;

/**
 * Created by ShiuFai on 16/9/2016.
 */
public class Team {
    private String teamName;
    private int teamWins;
    public  Team(String name, int wins)
    {
        teamName = name;
        teamWins = wins;
    }

    public String getTeamName() {
        return teamName;
    }

    public int getTeamWins() {
        return teamWins;
    }
}
