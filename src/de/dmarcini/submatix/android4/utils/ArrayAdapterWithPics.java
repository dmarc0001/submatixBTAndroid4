/**
 * Eigener Arrayadapter, der Icons beinhaltet
 * 
 * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 * 
 *         Stand: 23.12.2012
 */
package de.dmarcini.submatix.android4.utils;


import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import de.dmarcini.submatix.android4.R;
import de.dmarcini.submatix.android4.content.ContentSwitcher;

/**
 * Das Objekt leitet sich vom ArrayAdapter ab Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 23.12.2012
 */
public class ArrayAdapterWithPics extends ArrayAdapter<ContentSwitcher.ProgItem>
{
  /**
   * Mein Konstruktor Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 23.12.2012
   * @param context
   * @param textViewResourceId
   * @param objects
   */
  public ArrayAdapterWithPics(Context context, int textViewResourceId, List<ContentSwitcher.ProgItem> objects)
  {
    super(context, textViewResourceId, objects);
  }

  /* private view holder class */
  private class ViewHolder
  {
    public ImageView imageView;
    public TextView  txtTitle;
    public ImageView imageRightIndicatorView;
    public ImageView imageLeftIndicatorView;
  }

  /**
   * Die Methode gibt mein in XML Creiertes Objekt für jeden Listenpunkt zurück
   * 
   * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
   * @author Dirk Marciniak 03.01.2013
   * @param position
   * @param convertView
   * @param parent
   * @return die View für diesen Punkt der Liste
   */
  @Override
  public View getView(int position, View convertView, ViewGroup parent)
  {
    ViewHolder holder = null;
    ContentSwitcher.ProgItem progItem = getItem(position);
    LayoutInflater mInflater = (LayoutInflater) getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
    if (convertView == null)
    {
      convertView = mInflater.inflate(R.layout.array_with_pic_adapter_view, parent, false);
      holder = new ViewHolder();
      holder.txtTitle = (TextView) convertView.findViewById(R.id.arrayListTextView);
      holder.imageView = (ImageView) convertView.findViewById(R.id.arrayListIconView);
      holder.imageRightIndicatorView = (ImageView) convertView.findViewById(R.id.arrayListRightActiveIndicator);
      holder.imageLeftIndicatorView = (ImageView) convertView.findViewById(R.id.arrayListLeftActiveIndicator);
      convertView.setTag(holder);
    }
    else
    {
      holder = (ViewHolder) convertView.getTag();
    }
    holder.txtTitle.setText(progItem.content);
    holder.imageView.setImageResource(progItem.resId);
    if (parent instanceof ListView)
    {
      ListView pView = (ListView) parent;
      if (pView.getCheckedItemPosition() == position)
      {
        Log.i("DEBUG", "is selected at <" + position + ">");
        holder.imageLeftIndicatorView.setImageResource(R.drawable.activated_red_icon_color);
        holder.imageRightIndicatorView.setImageResource(R.drawable.activated_red_icon_color);
      }
      else
      {
        holder.imageLeftIndicatorView.setImageResource(R.drawable.deactivated_and_space);
        holder.imageRightIndicatorView.setImageResource(R.drawable.deactivated_and_space);
      }
    }
    return convertView;
  }
}
