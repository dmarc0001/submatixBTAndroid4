/**
 * Activity für Details des Projektes
 * 
 * Wird ausgeführt, wenn bei kleinen Schirmen (Smartphones) eine Option des Menüs ausgewählt wurde. Bei grossen Schirmen wird diese Activity nicht ausgeführt Der Gemeisame Code für
 * beide Version ist im Parentobjekt combienedFragmentActivity
 */
package de.dmarcini.submatix.android4.gui;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import de.dmarcini.submatix.android4.R;
import de.dmarcini.submatix.android4.content.ContentSwitcher;
import de.dmarcini.submatix.android4.utils.BluetoothDeviceArrayAdapter;
import de.dmarcini.submatix.android4.utils.ProjectConst;

/**
 * Diese Activity kommt nur bei kleinen Screens zum Einsatz, wenn vom Menü un der araeListActivity ein Eintrag gewählt wurde
 * 
 * @author dmarc
 */
public class areaDetailActivity extends FragmentCommonActivity
{
  private static final String TAG = areaDetailActivity.class.getSimpleName();

  //
  //
  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    int showId = 0;
    ContentSwitcher.ProgItem mItem = null;
    // Fragment fragment = null;
    int resourceId = 0;
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
      // argumente basteln
      Bundle arguments = new Bundle();
      arguments.putInt( ProjectConst.ARG_ITEM_ID, showId );
      // Welcher Programmmenüpunkt war denn das?
      mItem = ContentSwitcher.getProgItemForId( showId );
      // hab ich einen Eintrag vorrätig?
      if( mItem != null )
      {
        switch ( mItem.nId )
        {
          case R.string.progitem_config:
            //
            // SPX42 Configuration starten
            //
            Log.v( TAG, "onCreate: create config PreferenceActivity..." );
            getActionBar().setTitle( R.string.conf_headline );
            getActionBar().setLogo( mItem.resId );
            Log.v( TAG, "onCreate: set layout..." );
            setContentView( R.layout.activity_area_detail );
            Log.v( TAG, "onCreate: begin replace view..." );
            getFragmentManager().beginTransaction().replace( R.id.area_detail_container, new SPX42PreferencesFragment( isIndividual ) ).commit();
            Log.v( TAG, "onCreate: begin replace view...OK" );
            break;
          //
          case R.string.progitem_progpref:
            //
            // Programmconfiguration starten
            //
            Log.v( TAG, "onCreate: create program PreferenceActivity..." );
            getActionBar().setTitle( R.string.conf_prog_headline );
            getActionBar().setLogo( mItem.resId );
            Log.v( TAG, "onCreate: set layout..." );
            setContentView( R.layout.activity_area_detail );
            Log.v( TAG, "onCreate: begin replace view..." );
            getFragmentManager().beginTransaction().replace( R.id.area_detail_container, new ProgramPreferencesFragment() ).commit();
            Log.v( TAG, "onCreate: begin replace view...OK" );
            break;
          //
          case R.string.progitem_gaslist:
            //
            // gaslist edit Activity erzeugen
            //
            Log.w( TAG, "onCreate: create galsist preference activity..." );
            getActionBar().setTitle( R.string.gaslist_headline );
            getActionBar().setLogo( mItem.resId );
            Log.v( TAG, "onCreate: set layout..." );
            setContentView( R.layout.activity_area_detail );
            Log.v( TAG, "onCreate: begin replace view..." );
            getFragmentManager().beginTransaction().replace( R.id.area_detail_container, new SPX42GaslistPreferencesFragment( isTrimix ) ).commit();
            Log.v( TAG, "onCreate: begin replace view...OK" );
            break;
          //
          default:
            Log.w( TAG, "onCreate: Not programitem found for <" + showId + ">" );
          case R.string.progitem_connect:
            //
            // erzeuge die Connect fragmentActivity, auch wenn nix passendes gefunden
            //
            Log.v( TAG, "onCreate: create connect fragmentActivity..." );
            connectFragment connFragment = new connectFragment();
            resourceId = 0;
            setContentView( R.layout.fragment_connect );
            getActionBar().setTitle( R.string.connect_headline );
            getActionBar().setLogo( mItem.resId );
            Log.v( TAG, "onCreate: beginTransaction..." );
            getFragmentManager().beginTransaction().add( resourceId, connFragment ).commit();
            Log.v( TAG, "onCreate: add transaction...OK" );
            //
            // die Listener auf das Fragment setzen ( der Code soll da ausgeführt werden )
            //
            ( ( Button )findViewById( R.id.connectDiscoverButton ) ).setOnClickListener( connFragment );
            //
            ( ( Spinner )findViewById( R.id.connectBlueToothDeviceSpinner ) ).setAdapter( new BluetoothDeviceArrayAdapter( this, R.layout.bt_array_with_pic_adapter_view,
                    FragmentCommonActivity.getAppStyle() ) );
            ( ( ImageButton )findViewById( R.id.connectButton ) ).setOnClickListener( connFragment );
        }
      }
    }
    else
    {
      // Dann ist was faul, und ich zeig DUMMY
      Log.w( TAG, "onCreate: Not showId found, show DUMMY !" );
      areaDetailFragment dFragment = new areaDetailFragment();
      resourceId = R.id.area_detail_container;
      setContentView( R.layout.activity_area_detail );
      getActionBar().setTitle( R.string.dummy_headline );
      Log.v( TAG, "onCreate: beginTransaction..." );
      getFragmentManager().beginTransaction().add( resourceId, dFragment ).commit();
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
}
