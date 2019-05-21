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
package de.dmarcini.submatix.android4.full.utils;

// Eigener Adapter für Geräte
// Es werden String gespeichert, Felder werden durch "\n" getrennt
// Feld 0 = Geräte alias / gerätename
// Feld 1 = Geräte-MAC
// Feld 2 = Geräte-Name
// Feld 3 = Datenbank-Id (wenn vorhanden) sonst 0

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import de.dmarcini.submatix.android4.full.R;

/**
 * Meine spezielle Klasse für Anzeige der BT Geräte
 * <p/>
 * Gespeichert werden Strings, durch Trennzeichen "\n" sind Felder voneinander getrennt
 * <p/>
 * Project: SubmatixBluethoothLoggerAndroid4Tablet Package: de.dmarcini.submatix.android4.utils
 *
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 *         <p/>
 *         Stand: 05.11.2012
 */
public class BluetoothDeviceArrayAdapter extends ArrayAdapter<String[]>
{
  @SuppressWarnings( "javadoc" )
  public static final  int    BT_DEVAR_ALIAS    = 0;
  @SuppressWarnings( "javadoc" )
  public static final  int    BT_DEVAR_MAC      = 1;
  @SuppressWarnings( "javadoc" )
  public static final  int    BT_DEVAR_NAME     = 2;
  @SuppressWarnings( "javadoc" )
  public static final  int    BT_DEVAR_ISPAIRED = 3;
  @SuppressWarnings( "javadoc" )
  public static final  int    BT_DEVAR_ISONLINE = 4;
  @SuppressWarnings( "javadoc" )
  public static final  int    BT_DEVAR_COUNT    = 5;
  @SuppressWarnings( "unused" )
  private static final String TAG               = BluetoothDeviceArrayAdapter.class.getSimpleName();
  private              int    themeId           = R.style.AppDarkTheme;

  /**
   * Mein Konstruktor
   * <p/>
   * Project: SubmatixBluethoothLoggerAndroid4Tablet Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 05.11.2012
   *
   * @param context
   * @param textViewResourceId
   * @param themeId
   */
  public BluetoothDeviceArrayAdapter(Context context, int textViewResourceId, int themeId)
  {
    super(context, textViewResourceId);
    this.themeId = themeId;
  }

  private BluetoothDeviceArrayAdapter(Context context, int resource, int textViewResourceId, int themeId)
  {
    super(context, resource, textViewResourceId);
    this.themeId = themeId;
  }

  private BluetoothDeviceArrayAdapter(Context context, int textViewResourceId, List<String[]> objects, int themeId)
  {
    super(context, textViewResourceId, objects);
    this.themeId = themeId;
  }

  /**
   * Das Stringarray in die Liste!
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 17.02.2013
   *
   * @param items
   */
  @Override
  public void add(String[] items)
  {
    if( isMacThere(items) < 0 )
    {
      super.add(items);
    }
  }

  /**
   * zufügen oder Daten updaten
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 13.03.2013
   *
   * @param items
   */
  public void addOrUpdate(String[] items)
  {
    int position = isMacThere(items);
    if( position < 0 )
    {
      super.add(items);
    }
    else
    {
      String[] fields = getItem(position);
      fields[ BT_DEVAR_ALIAS ] = items[ BT_DEVAR_ALIAS ];
      fields[ BT_DEVAR_NAME ] = items[ BT_DEVAR_NAME ];
      fields[ BT_DEVAR_ISPAIRED ] = items[ BT_DEVAR_ISPAIRED ];
      fields[ BT_DEVAR_ISONLINE ] = items[ BT_DEVAR_ISONLINE ];
      fields[ BT_DEVAR_COUNT ] = items[ BT_DEVAR_COUNT ];
    }
  }

  /**
   * Gib den Aliasnamen des Eintrages zurück
   * <p/>
   * Project: SubmatixBluethoothLoggerAndroid4Tablet Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 05.11.2012
   *
   * @param position
   * @return den String
   */
  public String getAlias(int position)
  {
    return (getStringAt(position, BT_DEVAR_ALIAS));
  }

  /**
   * Ein eigenes View zur Anzeige machen
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 28.04.2013
   *
   * @param position
   * @param convertView
   * @param parent
   * @param isFirst
   * @return
   */
  private View getCustomView(int position, View convertView, ViewGroup parent, boolean isFirst)
  {
    View           cView     = convertView;
    ViewHolder     holder    = null;
    LayoutInflater mInflater = (( Activity ) getContext()).getLayoutInflater();
    if( cView == null || !(cView.getTag() instanceof ViewHolder) )
    {
      cView = mInflater.inflate(R.layout.bt_array_with_pic_adapter_view, parent, false);
      holder = new ViewHolder();
      holder.txtTitle = ( TextView ) cView.findViewById(R.id.btArrayListTextView);
      holder.imageView = ( ImageView ) cView.findViewById(R.id.btArrayListIconView);
      cView.setTag(holder);
    }
    else
    {
      holder = ( ViewHolder ) cView.getTag();
    }
    /*
    if( cView != null && (cView.getTag() instanceof ViewHolder) )
    {
      holder = ( ViewHolder ) cView.getTag();
    }
    else
    {
      cView = mInflater.inflate(R.layout.bt_array_with_pic_adapter_view, parent, false);
      holder = new ViewHolder();
    }
    */
    //
    // Icon setzen
    //
    if( isDevicePaired(position) )
    {
      // gepaarte Geräte
      if( isDeviceOnline(position) )
      {
        holder.imageView.setImageResource(R.drawable.device_is_paired_and_online);
      }
      else
      {
        holder.imageView.setImageResource(R.drawable.device_is_paired_no_connection);
      }
    }
    else
    {
      // nicht gepaarte Geräte
      if( isDeviceOnline(position) )
      {
        holder.imageView.setImageResource(R.drawable.device_is_not_paired_and_online);
      }
      else
      {
        holder.imageView.setImageResource(R.drawable.device_is_not_paired_no_connection);
      }
    }
    // Beschrifting setzen
    holder.txtTitle.setText(getAlias(position));
    //
    // ist die Beschriftung die ausgewählte, einfärben
    //
    if( this.themeId == R.style.AppDarkTheme )
    {
      holder.txtTitle.setTextColor(cView.getResources().getColor(R.color.connectFragmentDark_spinnerText));
      if( !isFirst )
      {
        cView.setBackgroundColor(cView.getResources().getColor(R.color.connectFragmentDark_spinnerBackgroundColor));
      }
    }
    else
    {
      holder.txtTitle.setTextColor(cView.getResources().getColor(R.color.connectFragmentLight_spinnerText));
      if( !isFirst )
      {
        cView.setBackgroundColor(cView.getResources().getColor(R.color.connectFragmentLight_spinnerBackgroundColor));
      }
    }
    return cView;
  }

  /**
   * Gib den vom Gerät zurückgegebenen Namen
   * <p/>
   * Project: SubmatixBluethoothLoggerAndroid4Tablet Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 05.11.2012
   *
   * @param position
   * @return den String
   */
  public String getDevName(int position)
  {
    return (getStringAt(position, BT_DEVAR_NAME));
  }

  @Override
  public View getDropDownView(int position, View convertView, ViewGroup parent)
  {
    return (getCustomView(position, convertView, parent, false));
  }

  /**
   * Gib die MAC des BT-Devices als Sting zurück
   * <p/>
   * Project: SubmatixBluethoothLoggerAndroid4Tablet Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 05.11.2012
   *
   * @param position
   * @return den String
   */
  public String getMAC(int position)
  {
    return (getStringAt(position, BT_DEVAR_MAC));
  }

  /**
   * Die universelle Funktion zur Rückgabe einiger Teilstrings
   * <p/>
   * Project: SubmatixBluethoothLoggerAndroid4Tablet Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 05.11.2012
   *
   * @param position
   * @param index
   * @return
   */
  private String getStringAt(int position, int index)
  {
    String[] fields = getItem(position);
    if( fields.length >= index )
    {
      return (fields[ index ]);
    }
    return (null);
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent)
  {
    return (getCustomView(position, convertView, parent, true));
  }

  /**
   * Ist das Gerät bereits gepaart?
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 25.02.2013
   *
   * @param position
   * @return gepart?
   */
  public boolean isDevicePaired(int position)
  {
    String pairedStr = getStringAt(position, BT_DEVAR_ISPAIRED);
    return pairedStr.matches("true") || pairedStr.matches("1") || pairedStr.matches("yes");
  }

  /**
   * Ist das Gerät als Online markiert?
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 13.03.2013
   *
   * @param position
   * @return ist es online markiert?
   */
  public boolean isDeviceOnline(int position)
  {
    String pairedStr = getStringAt(position, BT_DEVAR_ISONLINE);
    return pairedStr.matches("true") || pairedStr.matches("1") || pairedStr.matches("yes");
  }

  /**
   * Setze das Gerät von "position" online
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 24.07.2013
   *
   * @param position
   */
  public void setDeviceIsOnline(int position)
  {
    if( position < getCount() )
    {
      for( int i = 0; i < getCount(); i++ )
      {
        String[] fields = getItem(i);
        if( position == i )
        {
          fields[ BT_DEVAR_ISONLINE ] = "true";
        }
        else
        {
          fields[ BT_DEVAR_ISONLINE ] = "false";
        }
      }
    }
  }

  /**
   * Alle Geräte offline setzen
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 24.07.2013
   */
  public void setDevicesOffline()
  {
    for( int i = 0; i < getCount(); i++ )
    {
      String[] fields = getItem(i);
      fields[ BT_DEVAR_ISONLINE ] = "false";
    }
  }

  /**
   * Ist die MAC-Adresse schon in der Liste?
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 17.02.2013
   *
   * @param item
   * @return -1 nicht, ansonsten Nummer des Eintrages
   */
  public int isMacThere(String[] item)
  {
    int count = super.getCount();
    for( int i = 0; i < count; i++ )
    {
      if( item[ BT_DEVAR_MAC ].equals(getItem(i)[ BT_DEVAR_MAC ]) )
      {
        return (i);
      }
    }
    return (-1);
  }

  /**
   * Gib die Position des Gerätes mit der MAC "item" zurück, oder -1
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 28.05.2013
   *
   * @param item
   * @return Index des Eintrages
   */
  public int getIndexForMac(String item)
  {
    int count = super.getCount();
    for( int i = 0; i < count; i++ )
    {
      if( item.equals(getItem(i)[ BT_DEVAR_MAC ]) )
      {
        return (i);
      }
    }
    return (-1);
  }

  /**
   * Setze den Aliasnamen des Gerätes neu
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 13.03.2013
   *
   * @param position
   * @param newName
   */
  public void setDevAlias(int position, String newName)
  {
    String[] fields = getItem(position);
    fields[ BT_DEVAR_ALIAS ] = newName;
  }

  /* private view holder class */
  private class ViewHolder
  {
    public ImageView imageView;
    public TextView  txtTitle;
  }
}
