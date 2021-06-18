package com.example.musicplayer;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
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
import java.util.Objects;

public class TabViewFragment extends Fragment {

    public static final String TAG = "TabViewFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tab_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ViewPager2 viewPager = view.findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        TabLayout tabLayout = view.findViewById(R.id.tabs);
        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(getTabText(position)));
        tabLayoutMediator.attach();

        MaterialToolbar toolbar = (MaterialToolbar) view.findViewById(R.id.top_toolbar);
        ((AppCompatActivity) Objects.requireNonNull(getContext())).setSupportActionBar(toolbar);

        setHasOptionsMenu(true);
    }

    private String getTabText(int position) {
        if (position == 0) return getString(R.string.albums);
        if (position == 1) return getString(R.string.tracks);

        return getString(R.string.playlists);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.top_app_bar, menu);

        SearchManager searchManager = (SearchManager) Objects.requireNonNull(getContext())
                .getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();

        searchView.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(getContext(), SearchableActivity.class)));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.onActionViewCollapsed();
                FragmentManager manager = Objects.requireNonNull(getActivity())
                        .getSupportFragmentManager();
                Fragment fragment = manager.findFragmentByTag(PlayerControlsFragment.TAG);
                if (fragment != null) {
                    Objects.requireNonNull(fragment.getView()).setFocusableInTouchMode(true);
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
        closeButton.setOnClickListener(v -> searchView.onActionViewCollapsed());
    }

    private void setupViewPager(ViewPager2 viewPager) {

        ViewPagerAdapter pagerAdapter = new ViewPagerAdapter(this);

        AlbumListFragment albumListFragment = new AlbumListFragment();
        albumListFragment.setRetainInstance(true);
        AlbumListPresenter albumListPresenter = new AlbumListPresenter(Objects.requireNonNull(getContext()),
                albumListFragment, new DataProvider(getContext()));
        pagerAdapter.addFragment(albumListFragment);

        TrackListFragment trackListFragment = new TrackListFragment();
//        trackListFragment.setRetainInstance(true);
        TrackListPresenter trackListPresenter = new TrackListPresenter(getContext(), trackListFragment);
        pagerAdapter.addFragment(trackListFragment);

        PlaylistListFragment playlistsFragment = new PlaylistListFragment();
        playlistsFragment.setRetainInstance(true);
        PlaylistListPresenter playlistPresenter = new PlaylistListPresenter(getContext(), playlistsFragment);
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
