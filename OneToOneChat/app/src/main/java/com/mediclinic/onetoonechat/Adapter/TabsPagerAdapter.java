package com.mediclinic.onetoonechat.Adapter;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;


import com.mediclinic.onetoonechat.Fragments.ChatsFragment;
import com.mediclinic.onetoonechat.Fragments.RequestsFragment;

import org.jetbrains.annotations.NotNull;

public class TabsPagerAdapter extends FragmentPagerAdapter {


    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @NotNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new ChatsFragment();
            case 1:
                return new RequestsFragment();
            default:
                return null;

        }
    }

    @Override
    public int getCount() {
        return 2; // 2 is total fragment number (e.x- Chats, Requests)
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return "CHATS"; // ChatsFragment
            case 1:
                return "REQUESTS"; // ttttRequestsFragment
            default:
                return null;
        }
        //return super.getPageTitle(position);
    }
}
