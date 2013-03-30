package org.gdg.korea.android.codelab;

import org.gdg.korea.android.oscl1.R;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistSnippet;
import com.google.api.services.youtube.model.Thumbnail;
import com.google.api.services.youtube.model.ThumbnailDetails;
import com.novoda.imageloader.core.model.ImageTag;
import com.novoda.imageloader.core.model.ImageTagFactory;

class MenuAdapter extends ArrayAdapter<Object> {

	private ImageTagFactory mTagFactory;

	public MenuAdapter(Context context, int textViewResourceId, ImageTagFactory factory) {
		super(context, textViewResourceId);
		mTagFactory = factory;
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
			v = View.inflate(
					getContext(),
					R.layout.menu_row_category, 
					null
					);

			((TextView) v).setText(((Category) item).mTitle); 

		} else if (item instanceof Playlist) {
			v = View.inflate(
					getContext(),
					R.layout.menu_playlist, 
					null
					);

			Playlist pItem = (Playlist) item;

			TextView tv  = (TextView)  v.findViewById(R.id.row_title);
			ImageView im = (ImageView) v.findViewById(R.id.row_thumbnail);

			tv.setText(pItem.getSnippet().getTitle());

			String url = getThumbnailUrl(pItem.getSnippet());
			ImageTag tag = 	mTagFactory.build(url, getContext());
			im.setTag(tag);

			MainActivity.getImageManager().getLoader().load(im);

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
	
	static class Category {

		String mTitle;

		Category(String title) {
			mTitle = title;
		}
	}
}