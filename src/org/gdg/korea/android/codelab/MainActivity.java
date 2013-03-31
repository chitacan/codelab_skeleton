
package org.gdg.korea.android.codelab;

import java.util.List;

import net.simonvt.menudrawer.MenuDrawer;
import net.simonvt.menudrawer.MenuDrawer.OnDrawerStateChangeListener;

import org.gdg.korea.android.codelab.YouTubeChannelClient.Callbacks;
import org.gdg.korea.android.oscl1.R;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.ListView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.youtube.player.YouTubeStandalonePlayer;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistItem;
import com.novoda.imageloader.core.ImageManager;
import com.novoda.imageloader.core.LoaderSettings;
import com.novoda.imageloader.core.LoaderSettings.SettingsBuilder;
import com.novoda.imageloader.core.model.ImageTagFactory;

public class MainActivity extends SherlockActivity 
implements Callbacks, OnDrawerStateChangeListener{

	private static final String API_KEY = "AIzaSyDLp9N7LvofGKKowD2FmtjRHoAGeCtURGk";
	private static final String CHANNEL_ID = "UC_x5XG1OV2P6uZZ5FSM9Ttw";

	private static final String PREF_NAME = "pref";
	private static final String PREF_PLAYLIST_ITEMID = "playlistitemid";
	private static final String PREF_PLAYLIST_TITLE = "playlistTitle";

	private ActionBar mActionBar;
	private MenuDrawer mDrawer;

	private View mMenuProgress;
	private View mContentProgress;
	private View mEmptyView;
	private ListView mMenuList;
	private ListView mPlayListView;

	private MenuAdapter mMenuAdapter;
	private PlayListItemAdapter mPlayListItemAdapter;

	private YouTubeChannelClient mYoutubeClient;

	private static ImageManager sImageManager;
	private ImageTagFactory mTagFactory;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mActionBar = getSupportActionBar();
		mActionBar.setTitle("Google Developers Video");
		mActionBar.setDisplayHomeAsUpEnabled(true);

		mTagFactory = ImageTagFactory.newInstance(
				MainActivity.this,
				R.drawable.ic_action_select_all_dark
				);

		createDrawer();
		createMenu();

		mPlayListItemAdapter = new PlayListItemAdapter(this, R.id.row_title, mTagFactory);
		mPlayListView = (ListView) findViewById(R.id.list);
		mPlayListView.setAdapter(mPlayListItemAdapter);
		mPlayListView.setOnItemClickListener(mPlayListItemClickListener);

		LoaderSettings settings = new SettingsBuilder()
		.withDisconnectOnEveryCall(true)
		.build(this);

		sImageManager = new ImageManager(this, settings);

		mYoutubeClient = YouTubeChannelClient.newYouTubeChannelClient(
				API_KEY, 
				CHANNEL_ID
				);
	}

	private void createDrawer() {
		mDrawer = MenuDrawer.attach(this, MenuDrawer.MENU_DRAG_CONTENT);
		mDrawer.setContentView(R.layout.activity_main);
		mDrawer.setMenuView(R.layout.menu);
		mDrawer.setOnDrawerStateChangeListener(this);
	}

	private void createMenu() {
		mMenuAdapter = new MenuAdapter(this, R.id.row_title, mTagFactory);
		mMenuAdapter.setMenuDrawer(mDrawer);
		mMenuList = (ListView) findViewById(R.id.menu_list);		
		mMenuList.setAdapter(mMenuAdapter);
		mMenuList.setOnItemClickListener(mMenuItemClickListener);
		mMenuList.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				mMenuList.invalidate();
			}
		});
	}

	public static final ImageManager getImageManager() {
		return sImageManager;
	}

	private void toggleEmptyView(boolean isEmpty) {
		if (mEmptyView == null)
			mEmptyView = findViewById(R.id.content_empy);

		mEmptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
	}

	private void toggleMenuProgress(boolean isInProgress) {
		if (mMenuProgress == null)
			mMenuProgress = findViewById(R.id.menu_progress);

		mMenuProgress.setVisibility(isInProgress ? View.VISIBLE : View.GONE);
		mMenuList.setVisibility(isInProgress ? View.GONE : View.VISIBLE);
	}

	private void toggleContentProgress(boolean isInProgress) {
		if (mContentProgress == null)
			mContentProgress = findViewById(R.id.content_progress);

		mContentProgress.setVisibility(isInProgress ? View.VISIBLE : View.GONE);
		mPlayListView.setVisibility(isInProgress ? View.GONE : View.VISIBLE );
	}

	private AdapterView.OnItemClickListener mPlayListItemClickListener = 
			new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(
				AdapterView<?> parent, 
				View view, 
				int position, 
				long id
				) {
			PlaylistItem pl = (PlaylistItem) parent.getItemAtPosition(position);
			Intent intent = YouTubeStandalonePlayer.createVideoIntent(
					MainActivity.this, 
					API_KEY, 
					pl.getSnippet().getResourceId().getVideoId()
					);

			startActivity(intent);
		}
	};

	private AdapterView.OnItemClickListener mMenuItemClickListener = 
			new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(
				AdapterView<?> parent, 
				View view, 
				int position, 
				long id
				) {
			mMenuAdapter.setActivePosition(position);
			toggleContentProgress(true);
			toggleEmptyView(false);
			Playlist item = (Playlist) mMenuAdapter.getItem(position);
			mActionBar.setSubtitle(item.getSnippet().getTitle());
			mYoutubeClient.getPlaylistItem(item.getId(), MainActivity.this);
			cachePlaylistItem(item);
			mDrawer.setActiveView(view, position);
			mDrawer.closeMenu();
		}
	};

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			mDrawer.toggleMenu();
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		if (mPlayListItemAdapter.isEmpty()) {
			SharedPreferences pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
			String cachedItemId = pref.getString(PREF_PLAYLIST_ITEMID, "");
			String title = pref.getString(PREF_PLAYLIST_TITLE, "");
			if (!cachedItemId.isEmpty()) {
				toggleContentProgress(true);
				mActionBar.setSubtitle(title);
				mYoutubeClient.getPlaylistItem(cachedItemId, MainActivity.this);
			} else {
				mDrawer.openMenu();
				toggleEmptyView(true);
			}
		}
		super.onResume();
	}

	protected void cachePlaylistItem(Playlist item) {
		SharedPreferences pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		editor.putString(PREF_PLAYLIST_ITEMID, item.getId());
		editor.putString(PREF_PLAYLIST_TITLE, item.getSnippet().getTitle());
		editor.commit();
	}

	@Override
	public void onBackPressed() {
		final int state = mDrawer.getDrawerState();
		if (state == MenuDrawer.STATE_OPEN || 
				state == MenuDrawer.STATE_OPENING) {
			mDrawer.closeMenu();
			return;
		}

		super.onBackPressed();
	}

	@Override
	public void onLoadPlaylist(List<Playlist> playlist) {
		mMenuAdapter.clear();
		mMenuAdapter.addAll(playlist);
		mMenuAdapter.notifyDataSetChanged();
		toggleMenuProgress(false);
	}

	@Override
	public void onLoadPlaylistItem(
			String playlistId,
			List<PlaylistItem> playlistItem) {
		mPlayListItemAdapter.clear();
		mPlayListItemAdapter.addAll(playlistItem);
		mPlayListItemAdapter.notifyDataSetChanged();
		toggleContentProgress(false);
	}

	@Override
	public void onDrawerStateChange(int oldState, int newState) {
		if ((oldState == MenuDrawer.STATE_DRAGGING && 
				newState == MenuDrawer.STATE_OPENING) || 
				(oldState == MenuDrawer.STATE_DRAGGING && 
				newState == MenuDrawer.STATE_OPENING) ||
				(oldState == MenuDrawer.STATE_CLOSED && 
				newState == MenuDrawer.STATE_OPENING)) {

			if (mMenuAdapter.isEmpty()) {
				toggleMenuProgress(true);
				mYoutubeClient.getPlayList(this);
			}
		}
	}

}
