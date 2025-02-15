package edu.temple.bookcase;

import android.content.Context;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.fragment.app.Fragment;

public class BookDetailsFragment extends Fragment {

    Book book;
    TextView tv;

    public BookDetailsFragment() {}

    public static BookDetailsFragment newInstance(Book book) {
        BookDetailsFragment bdf = new BookDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("book", book);
        bdf.setArguments(bundle);

        return bdf;
    }

    public void displayBook(Book newBook) {
        book = newBook;
        tv.setText(book.title + "\n" + book.author + "\n" + book.published);
        tv.setTextSize(35);
        tv.setGravity(Gravity.CENTER_HORIZONTAL);

        Bundle bundle = getArguments();
        bundle.putInt("bookID", book.id);
        bundle.putString("bookTitle", book.title);
        bundle.putInt("bookDuration", book.duration);
        setArguments(bundle);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            book = getArguments().getParcelable("book");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        tv = (TextView) inflater.inflate(R.layout.fragment_details, container, false);

        if(book != null) {displayBook(book);}

        return tv;
    }

}
