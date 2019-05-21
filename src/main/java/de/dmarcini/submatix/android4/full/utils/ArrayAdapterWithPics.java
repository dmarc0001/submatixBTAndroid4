//@formatter:off
/*
    programm: SubmatixBTLoggerAndroid
    purpose:  configuration and read logs from SUBMATIX SPX42 divecomputer via Bluethooth    
    Copyright (C) 2012  Dirk Marciniak

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/
*/
//@formatter:on
/**
 * Eigener Arrayadapter, der Icons beinhaltet
 * <p>
 * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
 * <p>
 * <p>
 * Stand: 23.12.2012
 */
package de.dmarcini.submatix.android4.full.utils;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Iterator;
import java.util.List;

import de.dmarcini.submatix.android4.full.R;
import de.dmarcini.submatix.android4.full.content.ContentSwitcher;

/**
 * Das Objekt leitet sich vom ArrayAdapter ab, erzeugt Adapter mit Icons
 * <p/>
 * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
 *
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 *         <p/>
 *         Stand: 10.11.2013
 */
public class ArrayAdapterWithPics extends ArrayAdapter<ContentSwitcher.ProgItem>
{
  private int     themeId  = R.style.AppDarkTheme;
  private boolean isOnline = false;
  ;

  /**
   * Mein Konstruktor Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   *
   * @param context
   * @param textViewResourceId
   * @param isOnline
   * @param objects
   * @param themeId
   */
  public ArrayAdapterWithPics(Context context, int textViewResourceId, boolean isOnline, List<ContentSwitcher.ProgItem> objects, int themeId)
  {
    super(context, textViewResourceId);
    //
    // Alle Objekte erst einmal durchgehen
    //
    Iterator<ContentSwitcher.ProgItem> it = objects.iterator();
    while( it.hasNext() )
    {
      ContentSwitcher.ProgItem item = it.next();
      if( item.isDummy )
      {
        continue;
      }
      if( isOnline || item.workOffline )
      {
        // wenn wir online sind oder die Funktion auch offline erlaubt ist
        // dann in die Liste aufnehmen
        super.add(item);
      }
    }
    this.themeId = themeId;
    this.isOnline = isOnline;
  }

  /**
   * Die Methode gibt mein in XML Creiertes Objekt für jeden Listenpunkt zurück
   *
   * @param position
   * @param convertView
   * @param parent
   * @return die View für diesen Punkt der Liste
   * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
   */
  @Override
  public View getView(int position, View convertView, ViewGroup parent)
  {
    ViewHolder               holder    = null;
    ContentSwitcher.ProgItem progItem  = getItem(position);
    LayoutInflater           mInflater = ( LayoutInflater ) getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
    if( convertView == null )
    {
      convertView = mInflater.inflate(R.layout.array_with_pic_adapter_view, parent, false);
      holder = new ViewHolder();
      holder.txtTitle = ( TextView ) convertView.findViewById(R.id.arrayListTextView);
      holder.imageView = ( ImageView ) convertView.findViewById(R.id.arrayListIconView);
      holder.imageRightIndicatorView = ( ImageView ) convertView.findViewById(R.id.arrayListRightActiveIndicator);
      holder.imageLeftIndicatorView = ( ImageView ) convertView.findViewById(R.id.arrayListLeftActiveIndicator);
      convertView.setTag(holder);
    }
    else
    {
      holder = ( ViewHolder ) convertView.getTag();
    }
    holder.txtTitle.setText(progItem.content);
    if( this.isOnline )
    {
      holder.imageView.setImageResource(progItem.resIdOnline);
    }
    else
    {
      holder.imageView.setImageResource(progItem.resIdOffline);
    }
    if( parent instanceof ListView )
    {
      ListView pView = ( ListView ) parent;
      if( pView.getCheckedItemPosition() == position )
      {
        //
        // Hier noch je nach hellem oder dunklen Thema den Marker wählen
        //
        if( this.themeId == R.style.AppDarkTheme )
        {
          holder.imageLeftIndicatorView.setImageResource(R.drawable.activated_red_icon_color);
          holder.imageRightIndicatorView.setImageResource(R.drawable.activated_red_icon_color);
          convertView.setBackgroundColor(getContext().getResources().getColor(R.color.navigatorDark_markerColor));
          holder.txtTitle.setTextColor(getContext().getResources().getColor(R.color.navigatorDark_activeTextColor));
        }
        else
        {
          holder.imageLeftIndicatorView.setImageResource(R.drawable.activated_blue_icon_color);
          holder.imageRightIndicatorView.setImageResource(R.drawable.activated_blue_icon_color);
          convertView.setBackgroundColor(getContext().getResources().getColor(R.color.navigatorLight_markerColor));
          holder.txtTitle.setTextColor(getContext().getResources().getColor(R.color.navigatorLight_activeTextColor));
        }
      }
      else
      {
        holder.imageLeftIndicatorView.setImageResource(R.drawable.deactivated_and_space);
        holder.imageRightIndicatorView.setImageResource(R.drawable.deactivated_and_space);
        if( this.themeId == R.style.AppDarkTheme )
        {
          convertView.setBackgroundColor(getContext().getResources().getColor(R.color.navigatorDark_backgroundColor));
          holder.txtTitle.setTextColor(getContext().getResources().getColor(R.color.navigatorDark_inactiveTextColor));
        }
        else
        {
          convertView.setBackgroundColor(getContext().getResources().getColor(R.color.navigatorLight_backgroundColor));
          holder.txtTitle.setTextColor(getContext().getResources().getColor(R.color.navigatorLight_inactiveTextColor));
        }
      }
    }
    return convertView;
  }

  /* private view holder class */
  private class ViewHolder
  {
    public ImageView imageView;
    public TextView  txtTitle;
    public ImageView imageRightIndicatorView;
    public ImageView imageLeftIndicatorView;
  }
}
