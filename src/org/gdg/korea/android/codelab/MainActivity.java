
package org.gdg.korea.android.codelab;

import java.util.List;

import net.simonvt.menudrawer.MenuDrawer;

import org.gdg.korea.android.codelab.YouTubeChannelClient.Callbacks;
import org.gdg.korea.android.oscl1.R;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.youtube.player.YouTubeStandalonePlayer;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemSnippet;
import com.google.api.services.youtube.model.PlaylistSnippet;
import com.google.api.services.youtube.model.Thumbnail;
import com.google.api.services.youtube.model.ThumbnailDetails;
import com.novoda.imageloader.core.ImageManager;
import com.novoda.imageloader.core.LoaderSettings;
import com.novoda.imageloader.core.LoaderSettings.SettingsBuilder;
import com.novoda.imageloader.core.model.ImageTag;
import com.novoda.imageloader.core.model.ImageTagFactory;

public class MainActivity extends SherlockActivity implements Callbacks{

	private static final String API_KEY = "AIzaSyDLp9N7LvofGKKowD2FmtjRHoAGeCtURGk";
	private static final String CHANNEL_ID = "UC_x5XG1OV2P6uZZ5FSM9Ttw";

	private ActionBar mActionBar;
	private MenuDrawer mDrawer;
	private ListView mMenuList;
	private MenuAdapter mMenuAdapter;

	private ListView mPlayListView;
	private PlayListItemAdapter mPlayListItemAdapter;
	private int mActivePosition = -1;

	private YouTubeChannelClient mClient;

	private static ImageManager sImageManager;

	private ImageTagFactory mTagFactory;

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

		mMenuAdapter = new MenuAdapter(this, R.id.row_title);
		mMenuList.setAdapter(mMenuAdapter);
		mMenuList.setOnItemClickListener(mMenuItemClickListener);

		mDrawer = MenuDrawer.attach(this);
		mDrawer.setContentView(R.layout.activity_main);
		mDrawer.setMenuView(mMenuList);

		mPlayListView = (ListView) findViewById(R.id.list);

		mPlayListItemAdapter = new PlayListItemAdapter(this, R.id.row_title);
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

	private static class Category {

		String mTitle;

		Category(String title) {
			mTitle = title;
		}
	}

	private class PlayListItemAdapter extends ArrayAdapter<Object> {

		public PlayListItemAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			Object item = getItem(position);

			if (v == null)
				v = getLayoutInflater().inflate(
						R.layout.menu_playlist_item, 
						parent, 
						false
						);

			PlaylistItem pItem = (PlaylistItem) item;

			TextView tv  = (TextView)  v.findViewById(R.id.row_title);
			ImageView im = (ImageView) v.findViewById(R.id.row_thumbnail);

			tv.setText(pItem.getSnippet().getTitle());

			String url = getThumbnailUrl(pItem.getSnippet());
			ImageTag tag = 	mTagFactory.build(url, MainActivity.this);
			im.setTag(tag);

			getImageManager().getLoader().load(im);

			v.setTag(R.id.mdActiveViewPosition, position);

			if (position == mActivePosition) {
				mDrawer.setActiveView(v, position);
			}

			return v;
		}

		private String getThumbnailUrl(PlaylistItemSnippet snippet) {
			ThumbnailDetails thumbs = snippet.getThumbnails();
			Thumbnail thumb = thumbs.getDefault();
			if (thumb == null)
				return null;

			return thumb.getUrl();
		}
	}

	private class MenuAdapter extends ArrayAdapter<Object> {

		public MenuAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);
		}

		@Override
		public boolean isEnabled(int position) {
			Object item = getItem(position);
			if (item instanceof Playlist)
				return true;
			else 
				return false;
		}

		@Override
		public boolean areAllItemsEnabled() {
			return false;
		}

		@Override
		public View getView(
				int position, 
				View convertView, 
				ViewGroup parent
				) {
			View v = convertView;
			Object item = getItem(position);

			if (item instanceof Category) {
				v = getLayoutInflater().inflate(
						R.layout.menu_row_category, 
						parent, 
						false
						);

				((TextView) v).setText(((Category) item).mTitle); 

			} else if (item instanceof Playlist) {
				v = getLayoutInflater().inflate(
						R.layout.menu_playlist, 
						parent, 
						false
						);

				Playlist pItem = (Playlist) item;

				TextView tv  = (TextView)  v.findViewById(R.id.row_title);
				ImageView im = (ImageView) v.findViewById(R.id.row_thumbnail);

				tv.setText(pItem.getSnippet().getTitle());

				String url = getThumbnailUrl(pItem.getSnippet());
				ImageTag tag = 	mTagFactory.build(url, MainActivity.this);
				im.setTag(tag);

				getImageManager().getLoader().load(im);

				v.setTag(R.id.mdActiveViewPosition, position);

				if (position == mActivePosition) {
					mDrawer.setActiveView(v, position);
				}
			}

			v.setTag(R.id.mdActiveViewPosition, position);

			if (position == mActivePosition) {
				mDrawer.setActiveView(v, position);
			}

			return v;
		}

		private String getThumbnailUrl(PlaylistSnippet snippet) {
			ThumbnailDetails thumbs = snippet.getThumbnails();
			Thumbnail thumb = thumbs.getDefault();
			if (thumb == null)
				return null;

			return thumb.getUrl();
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
