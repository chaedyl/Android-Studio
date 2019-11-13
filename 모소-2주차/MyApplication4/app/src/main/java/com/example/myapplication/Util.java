package com.example.myapplication;

import android.content.Context;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Util {
    public static List<Page> getPages(Context context) {
        InputStreamReader inputStream = new InputStreamReader(context.getResources().openRawResource(R.raw.data));
        BufferedReader buffreader = new BufferedReader(inputStream);

        List<Page> pages = new ArrayList<>();
        try{
            String line;
            while((line = buffreader.readLine()) != null) {
                String []fields = line.split(",");
                pages.add(new Page(fields[0], fields[1]));
            }
        }catch (Exception e) {
            e.printStackTrace();
        }

        return pages;
    }
}
