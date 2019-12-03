package edu.temple.bookcase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import edu.temple.audiobookplayer.AudiobookService;

public class MainActivity extends AppCompatActivity implements BookListFragment.OnFragmentInteractionListener {

    BookDetailsFragment bdf;
    BookListFragment blf;
    ViewPagerFragment vpf;
    FragmentManager fragmentManager;
    Boolean twoFragment;
    Boolean tablet;
    ArrayList<Book> books;
    Bundle bundle;
    String search = "";
    AudiobookService.MediaControlBinder binder;
    Boolean mBound;

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            binder = (AudiobookService.MediaControlBinder) service;
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getBooksFromAPI();
        tablet = getResources().getBoolean(R.bool.isTablet);
        fragmentManager = getSupportFragmentManager();
        twoFragment = (findViewById(R.id.detailFragment) != null);
        bundle = new Bundle();

        //determine orientation and display fragments
        if(fragmentManager.findFragmentById(R.id.listFragment) == null) {
            bdf = new BookDetailsFragment();
            blf = new BookListFragment();
            vpf = new ViewPagerFragment();
            bundle.putParcelableArrayList("BookList", books);
            vpf.setArguments(bundle);
            bdf.setArguments(bundle);
            blf.setArguments(bundle);
            displayFragments();
        } else if (!twoFragment){
            blf = (BookListFragment) fragmentManager.findFragmentById(R.id.listFragment);
            bundle.putParcelableArrayList("BookList", blf.getBooks());
            vpf = new ViewPagerFragment();
            vpf.setArguments(bundle);
            fragmentManager
                    .beginTransaction()
                    .addToBackStack(null)
                    .replace(R.id.listFragment, vpf)
                    .commit();
        } else {
            if(!tablet) {
                vpf = (ViewPagerFragment) fragmentManager.findFragmentById(R.id.listFragment);
                bundle.putParcelableArrayList("BookList", vpf.getBooks());
                blf = new BookListFragment();
                bdf = new BookDetailsFragment();
                blf.setArguments(bundle);
                bdf.setArguments(bundle);
                fragmentManager
                        .beginTransaction()
                        .addToBackStack(null)
                        .replace(R.id.detailFragment, bdf)
                        .commit();
                fragmentManager
                        .beginTransaction()
                        .addToBackStack(null)
                        .replace(R.id.listFragment, blf)
                        .commit();
            } else {
                blf = (BookListFragment) fragmentManager.findFragmentById(R.id.listFragment);
                bdf = (BookDetailsFragment) fragmentManager.findFragmentById(R.id.detailFragment);
            }
        }

        final SeekBar seekBar = findViewById(R.id.seekBar);
        Handler seekBarHandler = new Handler();

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(binder != null && fromUser){
                    binder.seekTo(progress);
                }
            }
        });

        //pause button
        final Button pauseButton = findViewById(R.id.pauseButton);
        pauseButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(binder.isPlaying()) {
                    binder.pause();
                    pauseButton.setText("Resume");
                } else {
                    binder.pause();
                    pauseButton.setText("Pause");
                }
            }
        });

        //play button
        final Button playButton = findViewById(R.id.playButton);
        playButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(twoFragment) {
                    BookDetailsFragment bookDF = (BookDetailsFragment) fragmentManager.findFragmentById(R.id.detailFragment);
                    Bundle tempBundle = bookDF.getArguments();
                    int id = tempBundle.getInt("bookID");
                    String playingBookTitle = tempBundle.getString("bookTitle");
                    int duration = tempBundle.getInt("bookDuration");

                    binder.play(id);
                    TextView bookPlaying = findViewById(R.id.bookPlayingText);
                    bookPlaying.setText(playingBookTitle);
                    seekBar.setMax(duration);
                    pauseButton.setText("Pause");

                    Handler progressHandler = new Handler(new Handler.Callback() {
                        @Override
                        public boolean handleMessage(@NonNull Message message) {
                            if (message.obj!= null) {
                                int newProgress = ((AudiobookService.BookProgress) message.obj).getProgress();
                                seekBar.setProgress(newProgress);
                            }
                            return false; }
                    });
                    binder.setProgressHandler(progressHandler);
                } else {
                    ViewPager vp = findViewById(R.id.bookPager);
                    int page = vp.getCurrentItem();
                    Book current = books.get(page);
                    binder.play(current.id);
                    TextView bookPlaying = findViewById(R.id.bookPlayingText);
                    bookPlaying.setText(current.title);
                    seekBar.setMax(current.duration);
                    pauseButton.setText("Pause");

                    Handler progressHandler = new Handler(new Handler.Callback() {
                        @Override
                        public boolean handleMessage(@NonNull Message message) {
                            if (message.obj!= null) {
                                int newProgress = ((AudiobookService.BookProgress) message.obj).getProgress();
                                seekBar.setProgress(newProgress);
                            }
                            return false; }
                    });
                    binder.setProgressHandler(progressHandler);
                }
            }
        });

        //stop button
        final Button stopButton = findViewById(R.id.stopButton);
        stopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                binder.stop();
                TextView bookPlaying = findViewById(R.id.bookPlayingText);
                bookPlaying.setText("Nothing Playing");
                pauseButton.setText("Pause");
                seekBar.setProgress(0);
            }
        });

        //search button
        final Button searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText text = findViewById(R.id.searchText);
                search = text.getText().toString();
                getBooksFromAPI();
                bundle.putParcelableArrayList("BookList", books);
                //Create fragments and bundle books
                blf = new BookListFragment();
                bdf = new BookDetailsFragment();
                vpf = new ViewPagerFragment();
                vpf.setArguments(bundle);
                bdf.setArguments(bundle);
                blf.setArguments(bundle);
                if(twoFragment) {
                    ImageView iv = findViewById(R.id.imageView);
                    iv.setImageResource(android.R.color.transparent);
                }
                displayFragments();
                if(twoFragment) { playButton.setVisibility(View.INVISIBLE); }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Intent intent = new Intent(this, AudiobookService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onFragmentInteraction(int position) {
        books = blf.getBooks();
        bdf.displayBook(books.get(position));
        new getCoverImage((ImageView) findViewById(R.id.imageView))
                .execute(books.get(position).coverURL);
        Button playButton = findViewById(R.id.playButton);
        playButton.setVisibility(View.VISIBLE);
    }

    public void displayFragments() {
        if(!twoFragment) {
            fragmentManager
                    .beginTransaction()
                    .addToBackStack(null)
                    .replace(R.id.listFragment, vpf)
                    .commit();
        } else {
            fragmentManager
                    .beginTransaction()
                    .addToBackStack(null)
                    .replace(R.id.detailFragment, bdf)
                    .commit();
            fragmentManager
                    .beginTransaction()
                    .addToBackStack(null)
                    .replace(R.id.listFragment, blf)
                    .commit();
        }

    }

    public void getBooksFromAPI() {
        //thread for getting books from URL
        final Thread t = new Thread(){
            @Override
            public void run(){
                books = new ArrayList<>();
                URL url;

                try {
                    if(search.equals("")) {
                        url = new URL("https://kamorris.com/lab/audlib/booksearch.php");
                    } else {
                        url = new URL("https://kamorris.com/lab/audlib/booksearch.php?search=" + search);
                    }
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(
                                    url.openStream()));

                    String response = "", tmpResponse;

                    tmpResponse = reader.readLine();
                    while (tmpResponse != null) {
                        response = response + tmpResponse;
                        tmpResponse = reader.readLine();
                    }

                    JSONArray jsonArray = new JSONArray(response);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonobject = jsonArray.getJSONObject(i);
                        int id = jsonobject.getInt("book_id");
                        String title = jsonobject.getString("title");
                        String author = jsonobject.getString("author");
                        int published = jsonobject.getInt("published");
                        String coverURL = jsonobject.getString("cover_url");
                        int duration = jsonobject.getInt("duration");
                        Book newBook = new Book(id, title, author, published, coverURL, duration);
                        books.add(newBook);
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        };
        t.start();
        try { t.join(); }
        catch (Exception e) { System.out.println(e); }
    }

    private class getCoverImage extends AsyncTask<String, Void, Bitmap> {
        ImageView imageView;

        public getCoverImage(ImageView iv) {
            this.imageView = iv;
        }

        protected Bitmap doInBackground(String... urls) {
            String coverURL = urls[0];
            Bitmap cover = null;
            try {
                InputStream inputStream = new java.net.URL(coverURL).openStream();
                cover = BitmapFactory.decodeStream(inputStream);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return cover;
        }

        protected void onPostExecute(Bitmap result) {
            imageView.setImageBitmap(result);
        }
    }
}
