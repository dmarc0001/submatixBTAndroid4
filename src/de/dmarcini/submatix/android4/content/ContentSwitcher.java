package de.dmarcini.submatix.android4.content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;

/**
 * 
 * Objekt zum Erzeugen der Liteinträge in der areaListActivity
 * 
 * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.content
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 * 
 *         Stand: 17.12.2012
 */
public class ContentSwitcher
{
  public static List<ProgItem>        progItems    = new ArrayList<ProgItem>();
  public static Map<String, ProgItem> progItemsMap = new HashMap<String, ProgItem>();

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
    public int                 nId;

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
     */
    public ProgItem( String id, String content )
    {
      this.sId = id;
      this.content = content;
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
     */
    public ProgItem( int id, String content )
    {
      this.sId = String.format( "%d", id );
      this.nId = id;
      this.content = content;
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
    addItem( new ProgItem( "first", "dummy 1" ) );
  }
}
