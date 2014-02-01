/**
 * Activity für grafische Datstellung eines Logs bei kleinen screens
 * 
 * Wird ausgeführt, wenn bei kleinen Schirmen (Smartphones) eine Option des Menüs ausgewählt wurde. Bei grossen Schirmen wird diese Activity nicht ausgeführt Der Gemeisame Code für
 * beide Version ist im Parentobjekt FragmentCommonActivity
 */
package de.dmarcini.submatix.android4.full.gui;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import de.dmarcini.submatix.android4.full.ApplicationDEBUG;
import de.dmarcini.submatix.android4.full.R;
import de.dmarcini.submatix.android4.full.utils.ProjectConst;

/**
 * 
 * Diese Activity kommt nur bei kleinen Screens zum Einsatz, wenn vom Menü un der araeListActivity ein Eintrag gewählt wurde
 * 
 * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 * 
 *         Stand: 01.02.2014
 */
public class AreaDetailGraphActivity extends FragmentCommonActivity
{
  private static final String TAG          = AreaDetailGraphActivity.class.getSimpleName();
  private static Fragment     currFragment = null;

  //
  //
  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    //
    super.onCreate( savedInstanceState );
    if( ApplicationDEBUG.DEBUG ) Log.v( TAG, "onCreate:..." );
    // Aktiviere Zurückfunktion via Actionbar Home
    getActionBar().setHomeButtonEnabled( true );
    getActionBar().setDisplayHomeAsUpEnabled( true );
    //
    // was soll ich anzeigen?
    //
    if( getIntent().getBooleanExtra( ProjectConst.ARG_ITEM_GRAPHEXTRA, false ) && ( getIntent().getIntExtra( ProjectConst.ARG_ITEM_DBID, -1 ) > 0 ) )
    {
      //
      // Ist als EXTRA die Logid und das Flag für das EXTRA gesetzt
      // übergib die Extras gleich wieder an das neue Element
      //
      Log.i( TAG, "onCreate: start SPX42LogGraphFragment..." );
      currFragment = new SPX42LogGraphFragment();
      currFragment.setArguments( getIntent().getExtras() );
      setContentView( R.layout.fragment_log_protocol_graph );
      getActionBar().setTitle( R.string.graphlog_header );
      getActionBar().setLogo( R.drawable.graphsbar_online );
      getFragmentManager().beginTransaction().replace( R.id.logGraphOuterLayout, currFragment ).commit();
    }
    else
    {
      //
      // Seite zum selektieren zeigen
      //
      Log.i( TAG, "onCreate: start SPX42LogGraphSelectFragment..." );
      currFragment = ( new SPX42LogGraphSelectFragment() );
      setContentView( R.layout.fragment_log_protocol );
      getActionBar().setTitle( R.string.graphlog_header );
      getActionBar().setLogo( R.drawable.graphsbar_online );
      getFragmentManager().beginTransaction().replace( R.id.logGraphOuterLayout, currFragment ).commit();
    }
  }

  @Override
  public void onPause()
  {
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onPause..." );
    super.onPause();
  }

  @Override
  public void onResume()
  {
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onResume..." );
    super.onResume();
  }

  @Override
  public void onDestroy()
  {
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onDestroy..." );
    super.onDestroy();
  }

  @Override
  public void onStop()
  {
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onStop..." );
    super.onStop();
  }
}
