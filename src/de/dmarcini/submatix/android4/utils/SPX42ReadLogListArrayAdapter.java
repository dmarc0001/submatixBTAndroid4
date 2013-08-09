package de.dmarcini.submatix.android4.utils;

import java.util.List;
import java.util.Vector;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import de.dmarcini.submatix.android4.R;

public class SPX42ReadLogListArrayAdapter extends ArrayAdapter<ReadLogItemObj>
{
  @SuppressWarnings( "unused" )
  private static final String TAG     = SPX42ReadLogListArrayAdapter.class.getSimpleName();
  @SuppressWarnings( "unused" )
  private int                 themeId = R.style.AppDarkTheme;

  public SPX42ReadLogListArrayAdapter( Context context, int textViewResourceId, int themeId )
  {
    super( context, textViewResourceId );
    this.themeId = themeId;
  }

  private SPX42ReadLogListArrayAdapter( Context context, int resource, int textViewResourceId, int themeId )
  {
    super( context, resource, textViewResourceId );
    this.themeId = themeId;
  }

  private SPX42ReadLogListArrayAdapter( Context context, int textViewResourceId, List<ReadLogItemObj> objects, int themeId )
  {
    super( context, textViewResourceId, objects );
    this.themeId = themeId;
  }

  @Override
  public View getView( int position, View convertView, ViewGroup parent )
  {
    View cView = convertView;
    ReadLogItemObj rlio;
    LayoutInflater mInflater;
    //
    mInflater = ( ( Activity )getContext() ).getLayoutInflater();
    rlio = getItem( position );
    //
    // guck mal, ob es das View gibt
    //
    if( cView == null )
    {
      cView = mInflater.inflate( R.layout.read_log_array_adapter_view, parent, false );
    }
    //
    // verorte die Objekte
    //
    ImageView ivSaved = ( ImageView )cView.findViewById( R.id.readLogListIconView );
    ImageView ivMarked = ( ImageView )cView.findViewById( R.id.readLogMarkedIconView );
    TextView tvName = ( TextView )cView.findViewById( R.id.readLogNameListTextView );
    TextView tvDetail = ( TextView )cView.findViewById( R.id.readLogDetailsTextView );
    try
    {
      //
      // Beschriftung setzen
      //
      tvName.setText( rlio.itemName );
      tvDetail.setText( rlio.itemDetail );
      //
      // Icon setzen
      //
      if( rlio.isSaved )
      {
        ivSaved.setImageResource( R.drawable.saved_log );
        if( themeId == R.style.AppDarkTheme )
        {
          tvDetail.setTextColor( cView.getResources().getColor( R.color.logReadDark_savedColor ) );
        }
        else
        {
          tvDetail.setTextColor( cView.getResources().getColor( R.color.logReadLight_savedColor ) );
        }
      }
      else
      {
        ivSaved.setImageResource( R.drawable.unsaved_log );
        if( themeId == R.style.AppDarkTheme )
        {
          tvDetail.setTextColor( cView.getResources().getColor( R.color.logReadDark_notSavedColor ) );
        }
        else
        {
          tvDetail.setTextColor( cView.getResources().getColor( R.color.logReadLight_notSavedColor ) );
        }
      }
      if( rlio.isMarked )
      {
        ivMarked.setImageResource( R.drawable.star_full_yellow );
      }
      else
      {
        ivMarked.setImageResource( R.drawable.star_empty_yellow );
      }
    }
    catch( NullPointerException ex )
    {
      // MACHWAS
    }
    return( cView );
  }

  /**
   * 
   * Ist der Logeintrag als gesichert markiert
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 06.08.2013
   * @param position
   * @return ist der Eintrag als gesichert markiert
   */
  public boolean isSaved( int position )
  {
    if( position > getCount() ) return( false );
    return( getItem( position ).isSaved );
  }

  /**
   * 
   * markiere einen Eintrag als gesichert in der Datenbank
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 06.08.2013
   * @param position
   * @param isSaved
   */
  public void setSaved( int position, boolean isSaved )
  {
    if( position > getCount() ) return;
    getItem( position ).isSaved = isSaved;
    if( !isSaved )
    {
      getItem( position ).dbId = -1;
    }
  }

  /**
   * 
   * Markiere einen Eintrag als gesichert mit Datenbank-ID
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 06.08.2013
   * @param position
   * @param isSaved
   * @param dbId
   */
  public void setSaved( int position, boolean isSaved, int dbId )
  {
    if( position > getCount() ) return;
    getItem( position ).isSaved = isSaved;
    getItem( position ).dbId = dbId;
  }

  /**
   * 
   * Gib den Namen des Eintrages zurück
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 06.08.2013
   * @param position
   * @return Name des Eintrages
   */
  public String getName( int position )
  {
    if( position > getCount() ) return( null );
    return( getItem( position ).itemName );
  }

  /**
   * 
   * Gib die nummer des Eintrages auf dem SPX zurück
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 06.08.2013
   * @param position
   * @return die NMummer des Eintrages
   */
  public int getNumberOnSPX( int position )
  {
    if( position > getCount() ) return( -1 );
    return( getItem( position ).numberOnSPX );
  }

  /**
   * 
   * Gib den Dateinamen auf dem SPX zurück
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 06.08.2013
   * @param position
   * @return Dateiname auf dem SPX
   */
  public String getNameOnSPX( int position )
  {
    if( position > getCount() ) return( null );
    return( getItem( position ).itemNameOnSPX );
  }

  /**
   * 
   * Setze markiert oder nicht
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 06.08.2013
   * @param position
   * @param marked
   */
  public void setMarked( int position, boolean marked )
  {
    if( position > getCount() ) return;
    getItem( position ).isMarked = marked;
  }

  /**
   * 
   * Gib zurück, ob markiert oder nicht
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 06.08.2013
   * @param position
   * @return markiert oder nicht
   */
  public boolean getMarked( int position )
  {
    if( position > getCount() ) return( false );
    return( getItem( position ).isMarked );
  }

  /**
   * 
   * gib die Nummern der markierten Einträge zurück
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 07.08.2013
   * @return Vector mit Nummern
   */
  public Vector<Integer> getMarkedItems()
  {
    Vector<Integer> lst = new Vector<Integer>();
    //
    for( int i = 0; i < getCount(); i++ )
    {
      if( getItem( i ).isMarked ) lst.add( i );
    }
    return( lst );
  }
}
