/**
 * Activity für Details des Projektes
 * 
 * Wird ausgeführt, wenn bei kleinen Schirmen (Smartphones) eine Option des Menüs ausgewählt wurde. Bei grossen Schirmen wird diese Activity nicht ausgeführt Der Gemeisame Code für
 * beide Version ist im Parentobjekt combienedFragmentActivity
 */
package de.dmarcini.submatix.android4.full.gui;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import de.dmarcini.submatix.android4.full.R;
import de.dmarcini.submatix.android4.full.content.ContentSwitcher;
import de.dmarcini.submatix.android4.full.utils.BuildVersion;
import de.dmarcini.submatix.android4.full.utils.ProjectConst;

/**
 * 
 * Diese Activity kommt nur bei kleinen Screens zum Einsatz, wenn vom Menü un der araeListActivity ein Eintrag gewählt wurde
 * 
 * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 * 
 *         Stand: 10.11.2013
 */
public class AreaDetailActivity extends FragmentCommonActivity implements OnItemSelectedListener
{
  private static final String TAG          = AreaDetailActivity.class.getSimpleName();
  private static Fragment     currFragment = null;

  //
  //
  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    int showId = 0;
    ContentSwitcher.ProgItem mItem = null;
    //
    super.onCreate( savedInstanceState );
    Log.v( TAG, "onCreate:..." );
    // Aktiviere Zurückfunktion via Actionbar Home
    getActionBar().setHomeButtonEnabled( true );
    getActionBar().setDisplayHomeAsUpEnabled( true );
    //
    // was soll ich anzeigen?
    //
    showId = getIntent().getIntExtra( ProjectConst.ARG_ITEM_ID, 0 );
    // gibt es eine ID?
    if( showId != 0 )
    {
      Log.v( TAG, "onCreate: SowId found: <" + showId + ">" );
      // Welcher Programmmenüpunkt war denn das?
      mItem = ContentSwitcher.getProgItemForId( showId );
      // hab ich einen Eintrag vorrätig?
      if( mItem != null )
      {
        switch ( mItem.nId )
        {
          case R.string.progitem_config:
            //
            // SPX42 Konfiguration starten
            //
            Log.i( TAG, "onCreate: start SPX42PreferencesFragment..." );
            getActionBar().setTitle( R.string.conf_headline );
            getActionBar().setLogo( mItem.resIdOffline );
            setContentView( R.layout.activity_area_detail );
            currFragment = ( new SPX42PreferencesFragment() );
            getFragmentManager().beginTransaction().replace( R.id.area_detail_container, currFragment ).commit();
            break;
          //
          case R.string.progitem_progpref:
            //
            // ProgrammKonfiguration starten
            //
            Log.i( TAG, "onCreate: start ProgramPreferencesFragment..." );
            getActionBar().setTitle( R.string.conf_prog_headline );
            getActionBar().setLogo( mItem.resIdOffline );
            setContentView( R.layout.activity_area_detail );
            currFragment = ( new ProgramPreferencesFragment() );
            getFragmentManager().beginTransaction().replace( R.id.area_detail_container, currFragment ).commit();
            break;
          //
          case R.string.progitem_gaslist:
            //
            // gaslist edit Activity erzeugen
            //
            Log.i( TAG, "onCreate: start SPX42GaslistPreferencesFragment..." );
            getActionBar().setTitle( R.string.gaslist_headline );
            getActionBar().setLogo( mItem.resIdOffline );
            setContentView( R.layout.activity_area_detail );
            currFragment = ( new SPX42GaslistPreferencesFragment() );
            getFragmentManager().beginTransaction().replace( R.id.area_detail_container, currFragment ).commit();
            break;
          //
          case R.string.progitem_about:
            Log.i( TAG, "onCreate: start ProgramAboutFragment..." );
            currFragment = ( new ProgramAboutFragment() );
            setContentView( R.layout.fragment_about );
            getActionBar().setTitle( R.string.about_headline );
            getActionBar().setLogo( mItem.resIdOffline );
            getFragmentManager().beginTransaction().replace( R.id.aboutOuterLayout, currFragment ).commit();
            break;
          //
          case R.string.progitem_logging:
            if( !BuildVersion.isLightVersion )
            {
              Log.i( TAG, "onCreate: start SPX42ReadLogFragment..." );
              currFragment = ( new SPX42ReadLogFragment() );
              setContentView( R.layout.fragment_read_log );
              getActionBar().setTitle( R.string.logread_headline );
              getActionBar().setLogo( mItem.resIdOffline );
              getFragmentManager().beginTransaction().replace( R.id.readLogOuterLayout, currFragment ).commit();
            }
            break;
          //
          case R.string.progitem_loggraph:
            if( !BuildVersion.isLightVersion )
            {
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
                getActionBar().setLogo( mItem.resIdOffline );
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
                getActionBar().setLogo( mItem.resIdOffline );
                getFragmentManager().beginTransaction().replace( R.id.logGraphOuterLayout, currFragment ).commit();
              }
            }
            break;
          case R.string.progitem_export:
            if( !BuildVersion.isLightVersion )
            {
              Log.i( TAG, "onCreate: SPX42ExportLogFragment..." );
              currFragment = ( new SPX42ExportLogFragment() );
              setContentView( R.layout.fragment_export_log );
              getActionBar().setTitle( R.string.export_header );
              getActionBar().setLogo( mItem.resIdOffline );
              getFragmentManager().beginTransaction().replace( R.id.exportLogOuterLayout, currFragment ).commit();
            }
            break;
          case R.string.progitem_spx_status:
            //
            // Eine Statussetie des SPX anzeigen
            //
            Log.i( TAG, "onCreate: start SPX42HealthFragment..." );
            currFragment = ( new SPX42HealthFragment() );
            setContentView( R.layout.fragment_spx42_health );
            getActionBar().setTitle( R.string.health_header );
            getActionBar().setLogo( mItem.resIdOffline );
            getFragmentManager().beginTransaction().replace( R.id.healthOuterLayout, currFragment ).commit();
            break;
          default:
            Log.w( TAG, "onCreate: Not programitem found for <" + showId + ">" );
          case R.string.progitem_connect:
            //
            // erzeuge die Connect fragmentActivity, auch wenn nix passendes gefunden
            //
            Log.v( TAG, "onCreate: start SPX42ConnectFragment..." );
            currFragment = ( new SPX42ConnectFragment() );
            setContentView( R.layout.fragment_connect );
            getActionBar().setTitle( R.string.connect_headline );
            getActionBar().setLogo( mItem.resIdOffline );
            getFragmentManager().beginTransaction().replace( R.id.connectOuterLayout, currFragment ).commit();
        }
      }
    }
    else
    {
      // Dann ist was faul, und ich zeig DUMMY
      Log.w( TAG, "onCreate: Not showId found, show DUMMY !" );
      currFragment = new AreaDetailFragment();
      setContentView( R.layout.activity_area_detail );
      getActionBar().setTitle( R.string.dummy_headline );
      Log.v( TAG, "onCreate: beginTransaction..." );
      getFragmentManager().beginTransaction().add( R.id.area_detail_container, currFragment ).commit();
      Log.v( TAG, "onCreate: add transaction...OK" );
    }
  }

  @Override
  public void onPause()
  {
    Log.v( TAG, "onPause..." );
    super.onPause();
  }

  @Override
  public void onResume()
  {
    Log.v( TAG, "onResume..." );
    super.onResume();
  }

  @Override
  public void onDestroy()
  {
    Log.v( TAG, "onDestroy..." );
    super.onDestroy();
  }

  @Override
  public void onStop()
  {
    Log.v( TAG, "onStop..." );
    super.onStop();
  }

  @Override
  public void onItemSelected( AdapterView<?> arg0, View arg1, int arg2, long arg3 )
  {
    Log.v( TAG, "ITEM Selected!" );
  }

  @Override
  public void onNothingSelected( AdapterView<?> arg0 )
  {
    Log.v( TAG, "ITEM NOT Selected!" );
  }
}
