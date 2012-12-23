/**
 * Parentklasse für die beiden Hauptactivitis
 * 
 * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 * 
 *         Stand: 20.12.2012
 */
package de.dmarcini.submatix.android4.gui;

import android.app.Activity;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;
import de.dmarcini.submatix.android4.R;
import de.dmarcini.submatix.android4.comm.BluethoothComService;
import de.dmarcini.submatix.android4.comm.BluethoothComServiceBinder;
import de.dmarcini.submatix.android4.content.ContentSwitcher;
import de.dmarcini.submatix.android4.utils.ProjectConst;

/**
 * Zusammengefaßte Aktionen für Detail und List activity
 * 
 * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 * 
 *         Stand: 20.12.2012
 */
//@formatter:off
public class combinedFragmentActivity extends FragmentActivity  implements AreYouSureDialogFragment.NoticeDialogListener
{
  //@formatter:off
  private static final String        TAG                = combinedFragmentActivity.class.getSimpleName();
  //
  // Handler für die Verbindung mit dem Service
  private final ServiceConnection    mServiceConnection = new ServiceConnection() 
  {
    // Wird aufgerufen, sobald die Verbindung zum lokalen
    @Override
    public void onServiceConnected( ComponentName className, IBinder binder )
    {
      Log.v( TAG, "onServiceConnected()..." );
      mBinder = ( BluethoothComServiceBinder )binder;
    }
    
    @Override
    public void onServiceDisconnected( ComponentName className )
    {
      Log.v( TAG, "onServiceDisconnected()..." );
        mBinder = null;
    }
  };
  //@formatter:on
  protected static boolean           mTwoPane           = false;
  private BluethoothComServiceBinder mBinder            = null;

  /**
   * Nach dem Erzeugen des Objektes noch Einstellungen....
   */
  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    Log.v( TAG, "onCreate()..." );
    super.onCreate( savedInstanceState );
  }

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
  public void onActivityResult( int requestCode, int resultCode, Intent data )
  {
    Log.v( TAG, "onActivityResult()... " );
    // mBTDeviceBinder.setServiceHandler( msgHandler );
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
          Log.v( TAG, "Bind Service..." );
          if( bindService( new Intent( this, BluethoothComService.class ), mServiceConnection, Context.BIND_AUTO_CREATE ) )
          {
            Log.v( TAG, "bindService OK" );
          }
          else
          {
            Log.e( TAG, "Can't bind BT-Service!" );
          }
        }
        else
        {
          // User did not enable Bluetooth or an error occured
          Log.v( TAG, "REQUEST_ENABLE_BT => BT Device NOT ENABLED" );
          Toast.makeText( this, R.string.toast_exit_nobt, Toast.LENGTH_LONG ).show();
          finish();
        }
        break;
      default:
        Log.e( TAG, "unknown Request code for activity result" );
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
    android.support.v4.app.Fragment fragment = null;
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
      //
      // der Benutzer wählt den Verbindungseintrag
      //
      if( mItem.nId == R.string.progitem_connect )
      {
        connectFragment connFragment = new connectFragment();
        fragment = connFragment;
        getActionBar().setTitle( R.string.connect_headline );
        getActionBar().setLogo( mItem.resId );
      }
      //
      // Der Benutzer wählt den Konfigurationseintrag für den SPX
      //
      else if( mItem.nId == R.string.progitem_config )
      {
        configSPX42Fragment configFragment = new configSPX42Fragment();
        fragment = configFragment;
        getActionBar().setTitle( R.string.config_headline );
        getActionBar().setLogo( mItem.resId );
      }
      //
      // Was da sonst noch kommen könnte
      //
      else
      {
        Log.e( TAG, "detail fragment for id <" + id + "> not found!" );
        areaDetailFragment dFragment = new areaDetailFragment();
        fragment = dFragment;
        getActionBar().setTitle( R.string.app_name );
        getActionBar().setLogo( R.drawable.ic_launcher );
      }
      fragment.setArguments( arguments );
      getSupportFragmentManager().beginTransaction().replace( R.id.area_detail_container, fragment ).commit();
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
  public void onPause()
  {
    super.onPause();
    Log.v( TAG, "onPause..." );
    if( mBinder != null )
    {
      Log.v( TAG, "unbind service..." );
      unbindService( mServiceConnection );
      mBinder = null;
    }
  }

  @Override
  public void onResume()
  {
    super.onResume();
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
      if( mBinder == null )
      {
        Log.v( TAG, "Bind Service..." );
        if( bindService( new Intent( this, BluethoothComService.class ), mServiceConnection, Context.BIND_AUTO_CREATE ) )
        {
          Log.v( TAG, "bindService OK" );
        }
        else
        {
          Log.e( TAG, "Can't bind BT-Service!" );
        }
      }
    }
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
}
