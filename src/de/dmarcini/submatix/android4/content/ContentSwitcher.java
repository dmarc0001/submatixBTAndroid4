package de.dmarcini.submatix.android4.content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;
import de.dmarcini.submatix.android4.R;

/**
 * 
 * Objekt zum Erzeugen der Liteinträge in der AreaListActivity
 * 
 * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.content
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 * 
 *         Stand: 17.12.2012
 */
@SuppressWarnings( "javadoc" )
public class ContentSwitcher
{
  public static final String           TAG          = ContentSwitcher.class.getSimpleName();
  public static List<ProgItem>         progItems    = new ArrayList<ProgItem>();
  private static Map<String, ProgItem> progItemsMap = new HashMap<String, ProgItem>();

  /**
   * 
   * Ein Programmlisteneintrag als Klasse
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.content
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 17.12.2012
   */
  public static class ProgItem
  {
    public static final String TAG = ProgItem.class.getSimpleName();
    public String              sId;
    public String              content;
    public boolean             workOffline;
    public int                 nId;
    public int                 resIdOffline;
    public int                 resIdOnline;

    /**
     * 
     * Einen Programmlisteneintrag erzeugen
     * 
     * 
     * @author Dirk Marciniak (dirk_marciniak@arcor.de)
     * 
     *         Stand: 17.12.2012
     * @param id
     * @param content
     * @param workOffline
     */
    public ProgItem( String id, String content, boolean workOffline )
    {
      this.sId = id;
      this.content = content;
      this.workOffline = workOffline;
      this.resIdOffline = R.drawable.placeholder;
      this.resIdOnline = R.drawable.placeholder;
      try
      {
        this.nId = Integer.parseInt( id );
      }
      catch( NumberFormatException ex )
      {
        this.nId = -1;
        Log.e( TAG, "Number format Exception while scanning id <" + id + ">, " + ex.getLocalizedMessage() );
      }
    }

    /**
     * 
     * Einen Programmlisteneintrag erzeugen
     * 
     * 
     * @author Dirk Marciniak (dirk_marciniak@arcor.de)
     * 
     *         Stand: 17.12.2012
     * @param id
     * @param content
     * @param workOffline
     */
    public ProgItem( int id, String content, boolean workOffline )
    {
      this.sId = String.format( "%d", id );
      this.nId = id;
      this.content = content;
      this.workOffline = workOffline;
      this.resIdOffline = R.drawable.placeholder;
      this.resIdOnline = R.drawable.placeholder;
    }

    /**
     * 
     * Einen Eintrag mit Grafik-Resourcenid einfügen
     * 
     * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.content
     * 
     * @author Dirk Marciniak (dirk_marciniak@arcor.de)
     * 
     *         Stand: 23.12.2012
     * @param id
     * @param resIdOffline
     * @param resIdOnline
     * @param content
     * @param workOffline
     */
    public ProgItem( int id, int resIdOffline, int resIdOnline, String content, boolean workOffline )
    {
      this.sId = String.format( "%d", id );
      this.nId = id;
      this.content = content;
      this.workOffline = workOffline;
      this.resIdOffline = resIdOffline;
      this.resIdOnline = resIdOnline;
    }

    @Override
    public String toString()
    {
      return content;
    }
  }

  /**
   * 
   * Einträge für den Switcher eintragen
   * 
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 17.12.2012
   * @param item
   */
  public static void addItem( ProgItem item )
  {
    progItems.add( item );
    progItemsMap.put( item.sId, item );
  }

  /**
   * 
   * Die Switcheinträge löschen
   * 
   * Project: Android_4_BlueThoothTest Package: de.dmarcini.submatix.android4.contents
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 17.12.2012
   */
  public static void clearItems()
  {
    progItems.clear();
    progItemsMap.clear();
  }

  static
  {
    addItem( new ProgItem( "1", "dummy 1", true ) );
  }

  /**
   * 
   * Gib einen Eintrag für die ID zurück
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.content
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 07.01.2013
   * @param showId
   * @return Programmeintrag
   */
  public static ProgItem getProgItemForId( int showId )
  {
    return( progItemsMap.get( String.format( "%d", showId ) ) );
  }

  /**
   * 
   * Liste von Programmeinträgen zurückgeben
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.content
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 31.01.2013
   * @return Liste von Programmeinträgen
   */
  public static List<ProgItem> getProgramItemsList()
  {
    return( progItems );
  }
}
