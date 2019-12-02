package edu.temple.bookcase;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class ViewPagerFragment extends Fragment {

    ViewPager vp;
    PagerAdapter pa;
    ArrayList<Book> books;

    public ViewPagerFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        books = getArguments().getParcelableArrayList("BookList");
        View v = inflater.inflate(R.layout.fragment_pager, container,false);
        vp = v.findViewById(R.id.bookPager);
        pa = new vpAdapter(getChildFragmentManager());
        vp.setAdapter(pa);
        return v;
    }

    private class vpAdapter extends FragmentStatePagerAdapter {

        vpAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if(position < books.size()) {
                return BookDetailsFragment.newInstance(books.get(position));
            } else {
                return null;
            }
        }

        public int getCount() {
            return books.size();
        }
    }

    public ArrayList<Book> getBooks() {
        return books;
    }
}
