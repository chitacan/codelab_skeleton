
package org.gdg.korea.android.codelab;

import java.util.List;

import net.simonvt.menudrawer.MenuDrawer;

import org.gdg.korea.android.codelab.YouTubeChannelClient.Callbacks;
import org.gdg.korea.android.oscl1.R;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.youtube.player.YouTubeStandalonePlayer;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistItem;
import com.novoda.imageloader.core.ImageManager;
import com.novoda.imageloader.core.LoaderSettings;
import com.novoda.imageloader.core.LoaderSettings.SettingsBuilder;
import com.novoda.imageloader.core.model.ImageTagFactory;

public class MainActivity extends SherlockActivity implements Callbacks{

	private static final String API_KEY = "AIzaSyDLp9N7LvofGKKowD2FmtjRHoAGeCtURGk";
	private static final String CHANNEL_ID = "UC_x5XG1OV2P6uZZ5FSM9Ttw";

	private ActionBar mActionBar;
	MenuDrawer mDrawer;
	private ListView mMenuList;
	private MenuAdapter mMenuAdapter;

	private ListView mPlayListView;
	private PlayListItemAdapter mPlayListItemAdapter;
	int mActivePosition = -1;

	private YouTubeChannelClient mClient;

	private static ImageManager sImageManager;

	ImageTagFactory mTagFactory;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mActionBar = getSupportActionBar();
		mActionBar.setTitle("Code Lab");
		mActionBar.setSubtitle("gdg code lab");
		mActionBar.setDisplayHomeAsUpEnabled(true);

		mMenuList = new ListView(this);

		mTagFactory = ImageTagFactory.newInstance(
				MainActivity.this,
				R.drawable.ic_action_select_all_dark
				);

		mMenuAdapter = new MenuAdapter(this, this, R.id.row_title);
		mMenuList.setAdapter(mMenuAdapter);
		mMenuList.setOnItemClickListener(mMenuItemClickListener);

		mDrawer = MenuDrawer.attach(this);
		mDrawer.setContentView(R.layout.activity_main);
		mDrawer.setMenuView(mMenuList);

		mPlayListView = (ListView) findViewById(R.id.list);

		mPlayListItemAdapter = new PlayListItemAdapter(this, R.id.row_title, mTagFactory);
		mPlayListView.setAdapter(mPlayListItemAdapter);
		mPlayListView.setOnItemClickListener(mPlayListItemClickListener);

		LoaderSettings settings = new SettingsBuilder()
		.withDisconnectOnEveryCall(true).build(this);
		sImageManager = new ImageManager(this, settings);
	}

	public static final ImageManager getImageManager() {
		return sImageManager;
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

			Intent intent = YouTubeStandalonePlayer.createVideoIntent(MainActivity.this, API_KEY, pl.getSnippet().getResourceId().getVideoId());
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
			Playlist item = (Playlist) mMenuAdapter.getItem(position);
			mClient.getPlaylistItem(item.getId(), MainActivity.this);
			mDrawer.closeMenu();
		}
	};

	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("Get Playlist").setShowAsAction(
				MenuItem.SHOW_AS_ACTION_IF_ROOM | 
				MenuItem.SHOW_AS_ACTION_WITH_TEXT
				);

		return super.onCreateOptionsMenu(menu);
	};

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d("chitacan", ""+item.getItemId());
		switch (item.getItemId()) {
		case 0:
			if(!mDrawer.isMenuVisible())
				mDrawer.openMenu();
			getPlaylist();
			break;
		case android.R.id.home:
			mDrawer.toggleMenu();
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	protected void getPlaylist() {
		mClient = YouTubeChannelClient.newYouTubeChannelClient(
				API_KEY, 
				CHANNEL_ID
				);

		mClient.getPlayList(this);
	}

	static class Category {

		String mTitle;

		Category(String title) {
			mTitle = title;
		}
	}

	@Override
	public void onLoadPlaylist(List<Playlist> playlist) {
		mMenuAdapter.clear();
		mMenuAdapter.add(new Category("Google Developers"));
		mMenuAdapter.addAll(playlist);
		mMenuAdapter.notifyDataSetChanged();
	}

	@Override
	public void onLoadPlaylistItem(
			String playlistId,
			List<PlaylistItem> playlistItem) {
		mPlayListItemAdapter.clear();
		mPlayListItemAdapter.addAll(playlistItem);
		mPlayListItemAdapter.notifyDataSetChanged();
	}

}
