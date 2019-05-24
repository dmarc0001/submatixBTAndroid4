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

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Vector;

import de.dmarcini.submatix.android4.full.R;

/**
 * Erzeugt einen eigenen Adapter für die Darstellung der Logs auf dem SPX mit Icons
 * <p/>
 * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
 *
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 *         <p/>
 *         Stand: 10.11.2013
 */
public class SPX42ReadLogListArrayAdapter extends ArrayAdapter<ReadLogItemObj>
{
  @SuppressWarnings( "unused" )
  private static final String  TAG             = SPX42ReadLogListArrayAdapter.class.getSimpleName();
  private              int     themeId         = R.style.AppDarkTheme;
  private              boolean showSavedStatus = true;

  /**
   * Konstruktor mit Parametern
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * Stand: 03.12.2013
   *
   * @param context
   * @param textViewResourceId
   * @param themeId
   */
  public SPX42ReadLogListArrayAdapter(Context context, int textViewResourceId, int themeId)
  {
    super(context, textViewResourceId);
    this.themeId = themeId;
  }

  /**
   * Konstruktor mit Parametern
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * Stand: 03.12.2013
   *
   * @param context
   * @param resource
   * @param textViewResourceId
   * @param themeId
   */
  private SPX42ReadLogListArrayAdapter(Context context, int resource, int textViewResourceId, int themeId)
  {
    super(context, resource, textViewResourceId);
    this.themeId = themeId;
  }

  /**
   * Konstruktor mit Parametern
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * Stand: 03.12.2013
   *
   * @param context
   * @param textViewResourceId
   * @param objects
   * @param themeId
   */
  private SPX42ReadLogListArrayAdapter(Context context, int textViewResourceId, List<ReadLogItemObj> objects, int themeId)
  {
    super(context, textViewResourceId, objects);
    this.themeId = themeId;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent)
  {
    View           cView = convertView;
    ReadLogItemObj rlio;
    LayoutInflater mInflater;
    //
    mInflater = (( Activity ) getContext()).getLayoutInflater();
    rlio = getItem(position);
    //
    // guck mal, ob es das View gibt
    //
    if( cView == null )
    {
      cView = mInflater.inflate(R.layout.read_log_array_adapter_view, parent, false);
    }
    //
    // verorte die Objekte
    //
    ImageView ivSaved  = ( ImageView ) cView.findViewById(R.id.readLogListIconView);
    ImageView ivMarked = ( ImageView ) cView.findViewById(R.id.readLogMarkedIconView);
    TextView  tvName   = ( TextView ) cView.findViewById(R.id.readLogNameListTextView);
    TextView  tvDetail = ( TextView ) cView.findViewById(R.id.readLogDetailsTextView);
    try
    {
      //
      // Beschriftung setzen
      //
      tvName.setText(rlio.itemName);
      tvDetail.setText(rlio.itemDetail);
      //
      // Icon setzen
      //
      if( rlio.isSaved )
      {
        if( showSavedStatus )
        {
          ivSaved.setImageResource(R.drawable.saved_log);
        }
        else
        {
          ivSaved.setImageResource(R.drawable.unsaved_log);
        }
        if( themeId == R.style.AppDarkTheme )
        {
          tvDetail.setTextColor(cView.getResources().getColor(R.color.logReadDark_savedColor));
        }
        else
        {
          tvDetail.setTextColor(cView.getResources().getColor(R.color.logReadLight_savedColor));
        }
      }
      else
      {
        ivSaved.setImageResource(R.drawable.unsaved_log);
        if( themeId == R.style.AppDarkTheme )
        {
          tvDetail.setTextColor(cView.getResources().getColor(R.color.logReadDark_notSavedColor));
        }
        else
        {
          tvDetail.setTextColor(cView.getResources().getColor(R.color.logReadLight_notSavedColor));
        }
      }
      if( rlio.isMarked )
      {
        if( themeId == R.style.AppDarkTheme )
        {
          ivMarked.setImageResource(R.drawable.circle_full_yellow);
        }
        else
        {
          ivMarked.setImageResource(R.drawable.circle_full_green);
        }
      }
      else
      {
        if( themeId == R.style.AppDarkTheme )
        {
          ivMarked.setImageResource(R.drawable.circle_empty_yellow);
        }
        else
        {
          ivMarked.setImageResource(R.drawable.circle_empty_green);
        }
      }
    }
    catch( NullPointerException ex )
    {
      Log.e(TAG, "NullPointer Exeption while generate LisListAdapter View");
      // TODO: MACHWAS
    }
    return (cView);
  }

  /**
   * Ist der Logeintrag als gesichert markiert
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 06.08.2013
   *
   * @param position
   * @return ist der Eintrag als gesichert markiert
   */
  public boolean isSaved(int position)
  {
    if( position >= getCount() )
    {
      return (false);
    }
    return (getItem(position).isSaved);
  }

  /**
   * markiere einen Eintrag als gesichert in der Datenbank
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 06.08.2013
   *
   * @param position
   * @param isSaved
   */
  public void setSaved(int position, boolean isSaved)
  {
    if( position >= getCount() )
    {
      return;
    }
    getItem(position).isSaved = isSaved;
    if( !isSaved )
    {
      getItem(position).dbId = -1;
    }
  }

  /**
   * Markiere einen Eintrag als gesichert mit Datenbank-ID
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 06.08.2013
   *
   * @param position
   * @param isSaved
   * @param dbId
   */
  public void setSaved(int position, boolean isSaved, int dbId)
  {
    if( position >= getCount() )
    {
      return;
    }
    getItem(position).isSaved = isSaved;
    getItem(position).dbId = dbId;
  }

  /**
   * Gib den Namen des Eintrages zurück
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 06.08.2013
   *
   * @param position
   * @return Name des Eintrages
   */
  public String getName(int position)
  {
    if( position >= getCount() )
    {
      return (null);
    }
    return (getItem(position).itemName);
  }

  /**
   * Gib die nummer des Eintrages auf dem SPX zurück
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 06.08.2013
   *
   * @param position
   * @return die NMummer des Eintrages
   */
  public int getNumberOnSPX(int position)
  {
    if( position >= getCount() )
    {
      return (-1);
    }
    return (getItem(position).numberOnSPX);
  }

  /**
   * Gib den Dateinamen auf dem SPX zurück
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 06.08.2013
   *
   * @param position
   * @return Dateiname auf dem SPX
   */
  public String getNameOnSPX(int position)
  {
    if( position >= getCount() )
    {
      return (null);
    }
    return (getItem(position).itemNameOnSPX);
  }

  /**
   * Setze markiert oder nicht
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 06.08.2013
   *
   * @param position
   * @param marked
   */
  public void setMarked(int position, boolean marked)
  {
    if( position >= getCount() )
    {
      return;
    }
    if( getItem(position) instanceof ReadLogItemObj )
    {
      getItem(position).isMarked = marked;
    }
  }

  /**
   * Gib zurück, ob markiert oder nicht
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 06.08.2013
   *
   * @param position
   * @return markiert oder nicht
   */
  public boolean getMarked(int position)
  {
    if( position >= getCount() )
    {
      return (false);
    }
    if( getItem(position) instanceof ReadLogItemObj )
    {
      return (getItem(position).isMarked);
    }
    return (false);
  }

  /**
   * gib die Nummern der markierten Einträge zurück
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 07.08.2013
   *
   * @return Vector mit Nummern
   */
  public Vector<Integer> getMarkedItems()
  {
    Vector<Integer> lst = new Vector<Integer>();
    //
    for( int i = 0; i < getCount(); i++ )
    {
      if( getItem(i).isMarked )
      {
        lst.add(i);
      }
    }
    return (lst);
  }

  /**
   * Gib die Anzahl markierter Einträge zurück
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.full.utils
   * <p/>
   * Stand: 21.01.2014
   *
   * @return Anzahl markierter Einträge
   */
  public int getCountMarkedItems()
  {
    int count = 0;
    for( int i = 0; i < getCount(); i++ )
    {
      if( getItem(i).isMarked )
      {
        count++;
      }
    }
    return (count);
  }

  /**
   * Lösche die Markierungen
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * Stand: 04.01.2014
   */
  public void clearMarkedItems()
  {
    for( int i = 0; i < getCount(); i++ )
    {
      setMarked(i, false);
    }
  }

  /**
   * @return showSavedStatus
   */
  public boolean isShowSavedStatus()
  {
    return showSavedStatus;
  }

  /**
   * @param showSavedStatus das zu setzende Objekt showSavedStatus
   */
  public void setShowSavedStatus(boolean showSavedStatus)
  {
    this.showSavedStatus = showSavedStatus;
  }
}
