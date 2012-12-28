/**
 * Activity für Details des Projektes
 * 
 * Wird ausgeführt, wenn bei kleinen Schirmen (Smartphones) eine Option des Menüs ausgewählt wurde. Bei grossen Schirmen wird diese Activity nicht ausgeführt Der Gemeisame Code für
 * beide Version ist im Parentobjekt combienedFragmentActivity
 */
package de.dmarcini.submatix.android4.gui;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;
import de.dmarcini.submatix.android4.R;
import de.dmarcini.submatix.android4.content.ContentSwitcher;
import de.dmarcini.submatix.android4.utils.ProjectConst;

public class areaDetailActivity extends Activity implements AreYouSureDialogFragment.NoticeDialogListener
{
  private static final String TAG = areaDetailActivity.class.getSimpleName();

  @Override
  public void finishFromChild( Activity child )
  {
    Log.i( TAG, "child process called finish()..." );
    //
    // wenn eine Clientactivity mit finish() beendet
    // wurde, ist hier auch schluss
    // TODO: Service bescheid geben!
    //
    finish();
  }

  //
  //
  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    String showId = null;
    ContentSwitcher.ProgItem mItem = null;
    Fragment fragment = null;
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
    showId = getIntent().getStringExtra( ProjectConst.ARG_ITEM_ID );
    // gibt es eine ID?
    if( showId != null )
    {
      Log.v( TAG, "SowId found: <" + showId + ">" );
      // argumente basteln
      Bundle arguments = new Bundle();
      arguments.putString( ProjectConst.ARG_ITEM_ID, getIntent().getStringExtra( ProjectConst.ARG_ITEM_ID ) );
      // Welcher Programmmenüpunkt war denn das?
      mItem = ContentSwitcher.progItemsMap.get( showId );
      // hab ich einen Eintrag vorrätig?
      if( mItem != null )
      {
        switch ( mItem.nId )
        {
          case R.string.progitem_connect:
            //
            // erzeuge die Connect fragmentActivity
            //
            Log.v( TAG, "create connect fragmentActivity..." );
            connectFragment conFragment = new connectFragment();
            fragment = conFragment;
            resourceId = 0;
            setContentView( R.layout.fragment_connect );
            getActionBar().setTitle( R.string.connect_headline );
            getActionBar().setLogo( mItem.resId );
            break;
          case R.string.progitem_config:
            Log.v( TAG, "create config PreferenceActivity..." );
            getActionBar().setTitle( R.string.config_headline );
            getActionBar().setLogo( mItem.resId );
            setContentView( R.layout.activity_area_detail );
            getFragmentManager().beginTransaction().replace( R.id.area_detail_container, new SPX42PreferencesFragment() ).commit();
            // getFragmentManager().beginTransaction().replace( android.R.id.content, new SPX42PreferencesFragment() ).commit();
            return;
          case R.string.progitem_gaslist:
            //
            // gaslist edit Activity erzeugen
            //
            Log.w( TAG, "Not programitem found for <" + showId + ">" );
            getActionBar().setTitle( R.string.gaslist_headline );
            getActionBar().setLogo( mItem.resId );
            areaDetailFragment gFragment = new areaDetailFragment();
            resourceId = R.id.area_detail_container;
            fragment = gFragment;
            setContentView( R.layout.activity_area_detail );
            break;
          default:
            //
            // Dann ist was faul, und ich zeig DUMMY
            //
            Log.w( TAG, "Not programitem found for <" + showId + ">" );
            // Dann ist was faul, und ich zeig DUMMY
            getActionBar().setTitle( R.string.dummy_headline );
            getActionBar().setLogo( R.drawable.ic_launcher );
            areaDetailFragment dFragment = new areaDetailFragment();
            resourceId = R.id.area_detail_container;
            fragment = dFragment;
            setContentView( R.layout.activity_area_detail );
        }
      }
    }
    else
    {
      // Dann ist was faul, und ich zeig DUMMY
      Log.w( TAG, "Not showId found, show DUMMY !" );
      areaDetailFragment dFragment = new areaDetailFragment();
      resourceId = R.id.area_detail_container;
      fragment = dFragment;
      setContentView( R.layout.activity_area_detail );
      getActionBar().setTitle( R.string.dummy_headline );
    }
    //
    // und nun die Seite aufrufen, welche auch immer
    //
    // Log.v( TAG, "add transaction..." );
    Log.v( TAG, "getSupportFragmentManager..." );
    FragmentManager fm = getFragmentManager();
    Log.v( TAG, "beginTransaction..." );
    FragmentTransaction ft = fm.beginTransaction();
    Log.v( TAG, "add(resourceId,fragment) ..." );
    ft.add( resourceId, fragment );
    Log.v( TAG, "commit..." );
    ft.commit();
    // getSupportFragmentManager().beginTransaction().add( resourceId, fragment ).commit();
    Log.v( TAG, "add transaction...OK" );
  }

  @Override
  public void onDialogNegativeClick( DialogFragment dialog )
  {
    Log.v( TAG, "Negative dialog click!" );
  }

  @Override
  public void onDialogPositiveClick( DialogFragment dialog )
  {
    Log.v( TAG, "Positive dialog click!" );
    //
    // war es ein AreYouSureDialogFragment Dialog?
    //
    if( dialog instanceof AreYouSureDialogFragment )
    {
      AreYouSureDialogFragment aDial = ( AreYouSureDialogFragment )dialog;
      //
      // War der Tag für den Dialog zum Exit des Programmes?
      //
      if( aDial.getTag().equals( "programexit" ) )
      {
        Log.i( TAG, "User close app..." );
        Toast.makeText( this, R.string.toast_exit, Toast.LENGTH_SHORT ).show();
        finish();
      }
    }
  }

  @Override
  public boolean onOptionsItemSelected( MenuItem item )
  {
    switch ( item.getItemId() )
    {
      case android.R.id.home:
        Log.v( TAG, "onOptionsItemSelected: HOME" );
        Intent intent = new Intent( this, areaListActivity.class );
        intent.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
        startActivity( intent );
        // This ID represents the Home or Up button. In the case of this
        // activity, the Up button is shown. Use NavUtils to allow users
        // to navigate up one level in the application structure. For
        // more details, see the Navigation pattern on Android Design:
        //
        // http://developer.android.com/design/patterns/navigation.html#up-vs-back
        //
        // Log.v( TAG, "onOptionsItemSelected: HOME" );
        // NavUtils.navigateUpTo( this, new Intent( this, areaListActivity.class ) );
        return true;
    }
    return super.onOptionsItemSelected( item );
  }

  @Override
  public void onPause()
  {
    super.onPause();
    Log.v( TAG, "onPause..." );
  }

  @Override
  public void onResume()
  {
    super.onResume();
    Log.v( TAG, "onResume..." );
  }
}
