package com.example.musicplayer;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.musicplayer.albumlist.AlbumListFragment;
import com.example.musicplayer.albumlist.AlbumListPresenter;
import com.example.musicplayer.controlspanel.PlayerControlsFragment;
import com.example.musicplayer.playlistlist.PlaylistListPresenter;
import com.example.musicplayer.playlistlist.PlaylistListFragment;
import com.example.musicplayer.tracklist.TrackListFragment;
import com.example.musicplayer.tracklist.TrackListPresenter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class TabViewFragment extends Fragment {

    private static final String TAG = "TabViewFragment";
    private final ExecutorService executorService;
    private final Handler mainThreadHandler;

    public TabViewFragment(ExecutorService executorService, Handler mainThreadHandler) {
        this.executorService = executorService;
        this.mainThreadHandler = mainThreadHandler;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tab_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ViewPager2 viewPager = view.findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        TabLayout tabLayout = view.findViewById(R.id.tabs);
        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
            tab.setText(getTabText(position));
        });
        tabLayoutMediator.attach();

        MaterialToolbar toolbar = (MaterialToolbar) view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);
    }

    private String getTabText(int position) {
        if (position == 0) return getString(R.string.albums);
        if (position == 1) return getString(R.string.tracks);
        return getString(R.string.playlists);
    }

    private int getTabIconId(int position) {
        if (position == 0) return R.drawable.ic_round_album_24;
        return R.drawable.ic_round_library_music_24;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.top_app_bar, menu);
        super.onCreateOptionsMenu(menu, inflater);

        SearchManager searchManager = (SearchManager) getContext().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();

        searchView.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(getContext(), SearchableActivity.class)));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "text submitted");
                searchView.onActionViewCollapsed();
                FragmentManager manager = getActivity().getSupportFragmentManager();
                Fragment fragment = manager.findFragmentByTag(PlayerControlsFragment.FRAGMENT_TAG);
                if (fragment != null) {
                    fragment.getView().setFocusableInTouchMode(true);
                    fragment.getView().requestFocus();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        ImageView closeButton = (ImageView) searchView.findViewById(R.id.search_close_btn);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.onActionViewCollapsed();
            }
        });
    }

    private void setupViewPager(ViewPager2 viewPager) {
        ViewPagerAdapter pagerAdapter = new ViewPagerAdapter(this);

        AlbumListFragment albumListFragment = new AlbumListFragment();
        AlbumListPresenter albumListPresenter = new AlbumListPresenter(new DataProvider(getContext(),
                executorService, mainThreadHandler), albumListFragment, executorService, mainThreadHandler);
        pagerAdapter.addFragment(albumListFragment);

        TrackListFragment trackListFragment = new TrackListFragment();
        TrackListPresenter trackListPresenter = new TrackListPresenter(new DataProvider(getContext(),
                executorService, mainThreadHandler), trackListFragment, getContext());
        pagerAdapter.addFragment(trackListFragment);

        PlaylistListFragment playlistsFragment = new PlaylistListFragment();
        PlaylistListPresenter playlistPresenter = new PlaylistListPresenter(new PlaylistDataProvider(getContext()),
                playlistsFragment, executorService, mainThreadHandler);

        pagerAdapter.addFragment(playlistsFragment);
        viewPager.setAdapter(pagerAdapter);
    }

    private static class ViewPagerAdapter extends FragmentStateAdapter {
        private final List<Fragment> fragments = new ArrayList<>();

        public ViewPagerAdapter(Fragment fragment) { super(fragment); }

        public void addFragment(Fragment fragment) { fragments.add(fragment); }

        @NonNull
        @Override
        public Fragment createFragment(int position) { return fragments.get(position); }

        @Override
        public int getItemCount() { return fragments.size(); }
    }
}
