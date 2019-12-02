package edu.temple.bookcase;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.ArrayList;


public class BookListFragment extends Fragment {

    ArrayList<Book> books;
    private OnFragmentInteractionListener fragmentParent;

    public BookListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (getArguments() != null) { books = getArguments().getParcelableArrayList("books"); }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ListView lv = (ListView) inflater.inflate(R.layout.fragment_list,container,false);
        books = getArguments().getParcelableArrayList("BookList");
        ArrayList<String> titles = new ArrayList<>();
        for(int i = 0; i< books.size(); i++) {
            titles.add(books.get(i).title);
        }
        lv.setAdapter(new ArrayAdapter<>((Context) fragmentParent, android.R.layout.simple_list_item_1, titles));
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                fragmentParent.onFragmentInteraction(position);
            }
        });

        return lv;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            fragmentParent = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        fragmentParent = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(int position);
    }

    public ArrayList<Book> getBooks() {
        return books;
    }
}
