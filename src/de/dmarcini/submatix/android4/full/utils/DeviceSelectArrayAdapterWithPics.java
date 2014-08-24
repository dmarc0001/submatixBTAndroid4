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
 * Eigener Arrayadapter, der Icons beinhaltet fü die Darstellung einer Liste von Geräten
 * 
 * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
 * 
 * 
 * Stand: 23.12.2012
 */
package de.dmarcini.submatix.android4.full.utils;

import java.util.Iterator;
import java.util.Vector;

import android.app.Activity;
import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import de.dmarcini.submatix.android4.full.R;

/**
 * 
 * Das Objekt leitet sich vom ArrayAdapter ab, erzeugt Adapter mit Icons
 * 
 * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 * 
 *         Stand: 10.11.2013
 */
public class DeviceSelectArrayAdapterWithPics extends ArrayAdapter<Pair<Integer, String>>
{
  @SuppressWarnings( "unused" )
  private static final String TAG = DeviceSelectArrayAdapterWithPics.class.getSimpleName();
  @SuppressWarnings( "unused" )
  private final Context       context;

  /**
   * Mein Konstruktor Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * 
   * @param context
   * @param textViewResourceId
   * @param devList
   */
  public DeviceSelectArrayAdapterWithPics( Context context, int textViewResourceId, Vector<Pair<Integer, String>> devList )
  {
    super( context, textViewResourceId );
    this.context = context;
    //
    // Alle Objekte erst einmal durchgehen
    //
    Iterator<Pair<Integer, String>> it = devList.iterator();
    while( it.hasNext() )
    {
      Pair<Integer, String> item = it.next();
      // dann in die Liste aufnehmen
      super.add( item );
    }
  }

  /* private view holder class */
  private class ViewHolder
  {
    @SuppressWarnings( "unused" )
    public ImageView imageView;
    public TextView  txtTitle;
  }

  /**
   * Die Methode gibt mein in XML Creiertes Objekt für jeden Listenpunkt zurück
   * 
   * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
   * @param position
   * @param convertView
   * @param parent
   * @return die View für diesen Punkt der Liste
   */
  @Override
  public View getView( int position, View convertView, ViewGroup parent )
  {
    ViewHolder holder = null;
    Pair<Integer, String> deviceItem = getItem( position );
    LayoutInflater mInflater = ( LayoutInflater )getContext().getSystemService( Activity.LAYOUT_INFLATER_SERVICE );
    if( convertView == null )
    {
      convertView = mInflater.inflate( R.layout.device_select_adapter_view, parent, false );
      holder = new ViewHolder();
      holder.txtTitle = ( TextView )convertView.findViewById( R.id.devSelectListTextView );
      holder.imageView = ( ImageView )convertView.findViewById( R.id.devSelectAdapterIconView );
      convertView.setTag( holder );
    }
    else
    {
      holder = ( ViewHolder )convertView.getTag();
    }
    holder.txtTitle.setText( deviceItem.second );
    return convertView;
  }

  @Override
  public View getDropDownView( int position, View convertView, ViewGroup parent )
  {
    ViewHolder holder = null;
    Pair<Integer, String> deviceItem = getItem( position );
    LayoutInflater mInflater = ( LayoutInflater )getContext().getSystemService( Activity.LAYOUT_INFLATER_SERVICE );
    if( convertView == null )
    {
      convertView = mInflater.inflate( R.layout.device_select_adapter_view, parent, false );
      holder = new ViewHolder();
      holder.txtTitle = ( TextView )convertView.findViewById( R.id.devSelectListTextView );
      holder.imageView = ( ImageView )convertView.findViewById( R.id.devSelectAdapterIconView );
      convertView.setTag( holder );
    }
    else
    {
      holder = ( ViewHolder )convertView.getTag();
    }
    holder.txtTitle.setText( deviceItem.second );
    return convertView;
  }

  /**
   * 
   * Gib die Id des Devices an der Position zurück
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * 
   * Stand: 01.12.2013
   * 
   * @param position
   * @return ID des Gerätes an der Position
   */
  public int getDeviceIdAd( int position )
  {
    return( getItem( position ).first );
  }

  /**
   * 
   * Gib den _Namen des Gerätes an Position zurück
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * 
   * Stand: 01.12.2013
   * 
   * @param position
   * @return GErätename an Position
   */
  public String getDeviceNameAt( int position )
  {
    return( getItem( position ).second );
  }
}
