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
import android.widget.TextView;

import java.io.File;

import de.dmarcini.submatix.android4.full.R;

/**
 * Das Objekt leitet sich vom ArrayAdapter ab, erzeugt Adapter mit Icons
 * <p/>
 * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
 *
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 *         <p/>
 *         Stand: 10.11.2013
 */
public class FileSelectorArrayAdapterWithPics extends ArrayAdapter<File>
{
  private int themeId = R.style.AppDarkTheme;

  /**
   * Mein Konstruktor Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   *
   * @param context            Kontext der Activity
   * @param textViewResourceId ResourcenId zum Anzeigen der Einträge
   * @param themeId            Welches Thema hat die Activity
   * @param rootDir            Verzeichnis, welches angezeigt werden soll
   */
  public FileSelectorArrayAdapterWithPics(Context context, int textViewResourceId, int themeId, File rootDir)
  {
    super(context, textViewResourceId);
    //
    // Alle Objekte erst einmal durchgehen
    //
    if( rootDir.getParent() != null )
    {
      // nur, wenn das nicht schon rootdir ist
      super.add(new File(".."));
    }
    if( rootDir.exists() )
    {
      File[] lFiles = rootDir.listFiles();
      if( lFiles != null && lFiles.length > 0 )
      {
        super.addAll(rootDir.listFiles());
      }
    }
    this.themeId = themeId;
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
    ViewHolder     holder      = null;
    LayoutInflater mInflater   = null;
    File           currFileObj = null;
    boolean        isFileUp    = false;
    //
    mInflater = ( LayoutInflater ) getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
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
    // hier immer leer darstellen (ist für ArrayAdapter vorhanden
    holder.imageLeftIndicatorView.setImageResource(R.drawable.deactivated_and_space);
    holder.imageRightIndicatorView.setImageResource(R.drawable.deactivated_and_space);
    // File Objekt erstellen
    if( getItem(position).equals("..") )
    {
      isFileUp = true;
    }
    else
    {
      isFileUp = false;
    }
    currFileObj = getItem(position);
    // Das Thema einstellen
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
    // Ordner/Datei/Fehler
    if( currFileObj.exists() )
    {
      // Dateinamen eintragen
      holder.txtTitle.setText(currFileObj.getName());
      // Zeiger auf Parent?
      if( isFileUp )
      {
        // mach das Icon durchsichtig
        holder.imageView.setAlpha(0.0F);
      }
      else
      {
        // mach das Icon sichtbar
        holder.imageView.setAlpha(1.0F);
      }
      //
      // Datei oder Verzeichnis
      //
      if( currFileObj.isDirectory() )
      {
        holder.imageView.setImageResource(R.drawable.folder);
      }
      else
      {
        holder.imageView.setImageResource(R.drawable.file);
      }
    }
    else
    {
      // FEHLER darstellen!
      holder.imageView.setImageResource(R.drawable.ask_little1);
    }
    holder.txtTitle.setText(currFileObj.getName());
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
