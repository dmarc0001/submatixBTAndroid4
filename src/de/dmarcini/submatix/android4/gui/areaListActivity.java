/**
 * Hauptaktivity des Projektes
 * 
 * Wird als erstes ausgeführt. Bei kleinen Schirmen wird bei Auswahl einer Option an die detailActivity übergeben (eine Liste der Optionen angezeigt), bei großen Schirmen wird
 * direkt in dieser Activity der Detailschirm aufgebaut und bedient. Der Gemeisame Code für beide Version ist im Parentobjekt combienedFragmentActivity
 */
package de.dmarcini.submatix.android4.gui;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;
import de.dmarcini.submatix.android4.R;
import de.dmarcini.submatix.android4.content.ContentSwitcher;
import de.dmarcini.submatix.android4.content.ContentSwitcher.ProgItem;
import de.dmarcini.submatix.android4.utils.ProjectConst;

public class areaListActivity extends Activity implements AreYouSureDialogFragment.NoticeDialogListener
{
  private static final String TAG      = areaListActivity.class.getSimpleName();
  protected static boolean    mTwoPane = false;

  /**
   * 
   * Frage, ob BR erlaubt werden sollte
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 20.12.2012
   */
  private void askEnableBT()
  {
    Log.v( TAG, "askEnableBT()..." );
    Intent enableIntent = new Intent( BluetoothAdapter.ACTION_REQUEST_ENABLE );
    startActivityForResult( enableIntent, ProjectConst.REQUEST_ENABLE_BT );
    Log.v( TAG, "askEnableBT()...OK" );
  }

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

  /**
   * 
   * Erzeuge die Menüeinträge
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 17.12.2012
   */
  private void initStaticContenSwitcher()
  {
    Log.v( TAG, "initStaticContent..." );
    // zuerst aufräumen
    ContentSwitcher.clearItems();
    //
    // irgendeine Kennung muss der String bekommen, also gibts halt die String-ID
    //
    ContentSwitcher.addItem( new ProgItem( R.string.progitem_connect, R.drawable.bluetooth_icon_color, getString( R.string.progitem_connect ) ) );
    ContentSwitcher.addItem( new ProgItem( R.string.progitem_config, R.drawable.toolboxwhite, getString( R.string.progitem_config ) ) );
    ContentSwitcher.addItem( new ProgItem( R.string.progitem_gaslist, R.drawable.pinion, getString( R.string.progitem_gaslist ) ) );
    ContentSwitcher.addItem( new ProgItem( R.string.progitem_logging, R.drawable.logging, getString( R.string.progitem_logging ) ) );
    ContentSwitcher.addItem( new ProgItem( R.string.progitem_exit, R.drawable.shutoff, getString( R.string.progitem_exit ) ) );
  }

  @Override
  public void onActivityResult( int requestCode, int resultCode, Intent data )
  {
    Log.v( TAG, "onActivityResult()... " );
    switch ( requestCode )
    {
    //
    // Bluethooth erlauben
    //
      case ProjectConst.REQUEST_ENABLE_BT:
        // When the request to enable Bluetooth returns
        if( resultCode == Activity.RESULT_OK )
        {
          Log.v( TAG, "REQUEST_ENABLE_BT => BT Device ENABLED" );
          Toast.makeText( this, R.string.toast_bt_enabled, Toast.LENGTH_SHORT ).show();
        }
        else
        {
          // User did not enable Bluetooth or an error occured
          Log.v( TAG, "REQUEST_ENABLE_BT => BT Device NOT ENABLED" );
          Toast.makeText( this, R.string.toast_exit_nobt, Toast.LENGTH_LONG ).show();
          finish();
        }
        break;
      case ProjectConst.REQUEST_SPX_PREFS:
        //
        // wenn die Activity der SPX-Einstellungen zurückkehrt...
        //
        Log.v( TAG, "spx42 preferences activity returns..." );
        // finishActivity( ProjectConst.REQUEST_SPX_PREFS_F );
        setContentView( R.layout.activity_area_list );
        break;
      default:
        Log.w( TAG, "unknown Request code for activity result" );
    }
  }

  //
  //
  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
    Log.v( TAG, "onCreate..." );
    Log.v( TAG, "onCreate: setContentView..." );
    setContentView( R.layout.activity_area_list );
    Log.v( TAG, "onCreate: initStaticContentSwitcher()..." );
    initStaticContenSwitcher();
    Log.v( TAG, "onCreate: initStaticContentSwitcher()...OK" );
    //
    // guck mal. ob das ein grosses Display ist,
    // dann ist da nämlich auch der Detailcontainer vorhanden
    //
    if( findViewById( R.id.area_detail_container ) != null )
    {
      Log.v( TAG, "onCreate: twoPane-mode" );
      // Der Detailcontainer ist nur vorhanden, wenn die App
      // (durch das System) festgestellt hat, dass es auf einem
      // Gerät mirt grossem Display (res/values-large and
      // res/values-sw600dp) läuft.
      mTwoPane = true;
      // Im twoPane Modus soll der aktivierte Eintrag immer gekennzeichnet sein!
      Log.v( TAG, "onCreate: set \"activate on item click\"..." );
      ( ( areaListFragment )getFragmentManager().findFragmentById( R.id.area_list ) ).setActivateOnItemClick( true );
    }
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

  /**
   * 
   * Wird ein Item der Auswahlliste angeklickt
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 22.12.2012
   * @param listView
   * @param view
   * @param position
   * @param id
   */
  public void onListItemClick( ListView listView, View view, int position, long id )
  {
    ContentSwitcher.ProgItem mItem = null;
    Fragment fragment = null;
    String itemContent = null;
    String itemSid = null;
    //
    //
    // zunächst will ich mal wissen, was das werden soll!
    //
    Log.v( TAG, "onListItemClick()..." );
    Log.v( TAG, "onListItemClick: ID was: <" + position + ">" );
    mItem = ( ContentSwitcher.ProgItem )listView.getItemAtPosition( position );
    if( mItem == null )
    {
      Log.e( TAG, "onListItemClick: program menu item was NOT explored!" );
      return;
    }
    itemContent = mItem.content;
    itemSid = mItem.sId;
    Log.v( TAG, "onListItemClick: item content was: " + itemContent );
    Log.v( TAG, "onListItemClick: item id was: " + itemSid );
    //
    // wenn EXIT angeordnet wurde
    //
    if( mItem.nId == R.string.progitem_exit )
    {
      Log.v( TAG, "onListItemClick: make dialog for USER..." );
      AreYouSureDialogFragment sureDial = new AreYouSureDialogFragment( getString( R.string.dialog_sure_exit ) );
      sureDial.show( getFragmentManager().beginTransaction(), "programexit" );
      return;
    }
    //
    // jetzt noch zwischen Tablett mit Schirmsplitt und Smartphone unterscheiden
    //
    if( mTwoPane )
    {
      //
      // zweischirmbetrieb
      //
      Log.v( TAG, "onListItemClick: towPane mode!" );
      Bundle arguments = new Bundle();
      arguments.putString( ProjectConst.ARG_ITEM_ID, itemContent );
      switch ( mItem.nId )
      {
        case R.string.progitem_connect:
          //
          // der Benutzer wählt den Verbindungseintrag
          //
          connectFragment connFragment = new connectFragment();
          fragment = connFragment;
          getActionBar().setTitle( R.string.connect_headline );
          getActionBar().setLogo( mItem.resId );
          fragment.setArguments( arguments );
          getFragmentManager().beginTransaction().replace( R.id.area_detail_container, fragment ).commit();
          break;
        case R.string.progitem_config:
          //
          // Der Benutzer wählt den Konfigurationseintrag für den SPX
          //
          SPX42PreferencesFragment cFragment = new SPX42PreferencesFragment();
          arguments.putString( ProjectConst.ARG_ITEM_ID, itemContent );
          cFragment.setArguments( arguments );
          getActionBar().setTitle( R.string.config_headline );
          getActionBar().setLogo( mItem.resId );
          // getFragmentManager().beginTransaction()
          getFragmentManager().beginTransaction().replace( R.id.area_detail_container, cFragment ).commit();
          break;
        case R.string.progitem_gaslist:
          //
          // der Benutzer wählt den Gaslisten Editmode
          //
          areaDetailFragment dFragment = new areaDetailFragment();
          fragment = dFragment;
          getActionBar().setTitle( R.string.gaslist_headline );
          getActionBar().setLogo( mItem.resId );
          fragment.setArguments( arguments );
          getFragmentManager().beginTransaction().replace( R.id.area_detail_container, fragment ).commit();
          break;
        default:
          //
          // keine passende ID gefunden
          //
          Log.e( TAG, "detail fragment for id <" + id + "> not found!" );
          areaDetailFragment aFragment = new areaDetailFragment();
          fragment = aFragment;
          getActionBar().setTitle( R.string.app_name );
          getActionBar().setLogo( R.drawable.ic_launcher );
          fragment.setArguments( arguments );
          getFragmentManager().beginTransaction().replace( R.id.area_detail_container, fragment ).commit();
      }
    }
    else
    {
      //
      // kleiner Schirm
      // da wird jeder Eintrag als einzelne activity ausgeführt
      //
      Log.v( TAG, "onItemSelected: onePane modue! Call intent areadDetailActivity fur itenid<" + itemSid + ">" );
      Intent detailIntent = new Intent( this, areaDetailActivity.class );
      detailIntent.putExtra( ProjectConst.ARG_ITEM_ID, itemSid );
      startActivity( detailIntent );
    }
  }

  @Override
  public boolean onOptionsItemSelected( MenuItem item )
  {
    switch ( item.getItemId() )
    {
      case android.R.id.home:
        // This ID represents the Home or Up button. In the case of this
        // activity, the Up button is shown. Use NavUtils to allow users
        // to navigate up one level in the application structure. For
        // more details, see the Navigation pattern on Android Design:
        //
        // http://developer.android.com/design/patterns/navigation.html#up-vs-back
        //
        Log.v( TAG, "onOptionsItemSelected: HOME" );
        NavUtils.navigateUpTo( this, new Intent( this, areaListActivity.class ) );
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
    if( BluetoothAdapter.getDefaultAdapter() == null )
    {
      if( ProjectConst.CHECK_PHYSICAL_BT )
      {
        // es gibt gar keinen adapter!
        Toast.makeText( this, R.string.toast_exit_nobt, Toast.LENGTH_LONG ).show();
        finish();
        return;
      }
      else
      {
        Toast.makeText( this, R.string.toast_exit_nobt, Toast.LENGTH_LONG ).show();
        return;
      }
    }
    if( !BluetoothAdapter.getDefaultAdapter().isEnabled() )
    {
      // Eh, kein BT erlaubt!
      askEnableBT();
    }
    else
    {
      // läuft der Task?
    }
  }
}
