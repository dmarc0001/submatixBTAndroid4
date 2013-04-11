/**
 * gemeinsamer Code der List- und der Detailactivity
 * 
 * FragmentCommonActivity.java de.dmarcini.submatix.android4.gui SubmatixBTLoggerAndroid_4
 * 
 * @author Dirk Marciniak 28.12.2012
 */
package de.dmarcini.submatix.android4.gui;

import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;
import de.dmarcini.submatix.android4.BuildConfig;
import de.dmarcini.submatix.android4.R;
import de.dmarcini.submatix.android4.comm.BlueThoothComService;
import de.dmarcini.submatix.android4.comm.BlueThoothComService.LocalBinder;
import de.dmarcini.submatix.android4.comm.BtServiceMessage;
import de.dmarcini.submatix.android4.content.ContentSwitcher;
import de.dmarcini.submatix.android4.utils.ProjectConst;

/**
 * Der gemeinsame Code der List- und Detailactivity
 * 
 * @author dmarc
 */
public class FragmentCommonActivity extends Activity implements AreYouSureDialogFragment.NoticeDialogListener, IBtServiceListener
{
  private static final String       TAG             = FragmentCommonActivity.class.getSimpleName();
  protected static boolean          mTwoPane        = false;
  protected static boolean          isIndividual    = false;
  protected static boolean          isTrimix        = true;
  protected static BluetoothAdapter mBtAdapter      = null;
  private BlueThoothComService      mService        = null;
  private LocalBinder               binder          = null;
  private IBtServiceListener        serviceListener = null;
  private volatile boolean          mIsBound        = false;
  private static int                currentStyleId  = R.style.AppDarkTheme;
  //
  //@formatter:off
  //
  // wird beim binden / unbinden benutzt
  //
  private final ServiceConnection mConnection = new ServiceConnection() 
  {
    @Override
    public void onServiceConnected( ComponentName name, IBinder service )
    {
      if( BuildConfig.DEBUG ) if( BuildConfig.DEBUG ) Log.d(TAG,"onServiceConnected...");
      binder = ( LocalBinder )service;
      mService = binder.getService();
      binder.registerServiceHandler( mHandler );
    }
  
    @Override
    public void onServiceDisconnected( ComponentName name )
    {
      if( BuildConfig.DEBUG ) Log.d(TAG,"onServiceDisconnected...");
      if( mService != null && binder != null )
      {
        if( BuildConfig.DEBUG ) Log.d(TAG,"onServiceDisconnected...unregister Handler...");
        binder.unregisterServiceHandler( mHandler );
      }
      mService = null;
      binder = null;
    }
  };
  
  //
  // Ein Messagehandler, der vom Service kommende Messages bearbeitet
  //
  private final Handler mHandler = new Handler() 
  {
    @Override
    public void handleMessage( Message msg )
    {
      if( !( msg.obj instanceof BtServiceMessage ) )
      {
        Log.e(TAG,"Recived Message is NOT tyoe of BtServiceMessage!");
        return;
      }
      BtServiceMessage smsg = (BtServiceMessage)msg.obj;
      switch ( msg.what )
      {
        // ################################################################
        // Service TICK empfangen
        // ################################################################
        case ProjectConst.MESSAGE_TICK:
          serviceListener.msgRecivedTick( smsg );
          break;
        // ################################################################
        // Computer wird gerade verbunden
        // ################################################################
        case ProjectConst.MESSAGE_CONNECTING:
          serviceListener.msgConnecting( smsg );
          break;
        // ################################################################
        // Computer wurde getrennt
        // ################################################################
        case ProjectConst.MESSAGE_CONNECTED:
          serviceListener.msgConnected( smsg );
          break;
        // ################################################################
        // Computer wurde getrennt
        // ################################################################
        case ProjectConst.MESSAGE_DISCONNECTED:
          serviceListener.msgDisconnected( smsg );
          break;
        // ################################################################
        // Computer wurde getrennt
        // ################################################################
        case ProjectConst.MESSAGE_CONNECTERROR:
          serviceListener.msgConnectError( smsg );
          break;

          // ################################################################
        // Sonst....
        // ################################################################
        default:
          Log.w( TAG, "unknown message with id <" + smsg.getId() + "> recived!" );
      }
    }
  };
  //
  //@formatter:on
  //
  /**
   * Frage, ob BR erlaubt werden sollte Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 20.12.2012
   */
  private void askEnableBT()
  {
    Log.v( TAG, "askEnableBT()..." );
    Intent enableIntent = new Intent( BluetoothAdapter.ACTION_REQUEST_ENABLE );
    startActivityForResult( enableIntent, ProjectConst.REQUEST_ENABLE_BT );
    Log.v( TAG, "askEnableBT()...OK" );
  }

  /**
   * 
   * Ist die Activity mit zwei Anzeigeflächen? (Tablett)
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 07.01.2013
   * @return ist zweigeteilt oder nicht
   */
  public boolean istActivityTwoPane()
  {
    return( mTwoPane );
  }

  @Override
  public void finishFromChild( Activity child )
  {
    Log.i( TAG, "child process called finish()..." );
    //
    // wenn eine Clientactivity mit finish() beendet
    // wurde, ist hier auch schluss
    //
    finish();
  }

  @Override
  public void onDestroy()
  {
    super.onDestroy();
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
        // Wenn BT eingeschaltet wurde
        if( resultCode == Activity.RESULT_OK )
        {
          Log.v( TAG, "REQUEST_ENABLE_BT => BT Device ENABLED" );
          Toast.makeText( this, R.string.toast_bt_enabled, Toast.LENGTH_SHORT ).show();
          // Service starten und binden (bei onResume())
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

  public static final int getAppStyle()
  {
    return( currentStyleId );
  }

  /**
   * Wenn die Activity erzeugt wird, u.A. herausfinden ob ein- oder zwei-Flächen Mode
   * 
   * @see android.app.Activity#onCreate(android.os.Bundle)
   * @author Dirk Marciniak 28.12.2012
   * @param savedInstanceState
   */
  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
    Log.v( TAG, "onCreate..." );
    serviceListener = this;
    // den defaultadapter lesen
    mBtAdapter = BluetoothAdapter.getDefaultAdapter();
    Log.v( TAG, "onCreate: setContentView..." );
    //
    // Das gewünschte Thema setzen
    //
    SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences( this );
    if( sPref.contains( "keyProgOthersThemeIsDark" ) )
    {
      boolean whishedTheme = sPref.getBoolean( "keyProgOthersThemeIsDark", false );
      if( whishedTheme )
      {
        if( BuildConfig.DEBUG ) Log.d( TAG, "onCreate: select DARK theme while preference was set" );
        currentStyleId = R.style.AppDarkTheme;
        setTheme( R.style.AppDarkTheme );
      }
      else
      {
        if( BuildConfig.DEBUG ) Log.d( TAG, "onCreate: select Blue theme while preference was set" );
        currentStyleId = R.style.AppLightTheme;
        setTheme( R.style.AppLightTheme );
      }
    }
    //
    setContentView( R.layout.activity_area_list );
    //
    // finde raus, ob es ein Restart für ein neues Theme war
    //
    if( getIntent().getExtras() != null && getIntent().getExtras().containsKey( ProjectConst.ARG_ITEM_ID ) )
    {
      Log.v( TAG, "onCreate: it was an bundle there..." );
      if( getIntent().getExtras().getInt( ProjectConst.ARG_ITEM_ID, 0 ) == R.string.progitem_progpref )
      {
        // ja, jetzt muss ich auch drauf reagieren und die Preferenzen aufbauen
        Log.i( TAG, "onCreate: set program preferences after switch theme..." );
        Bundle arg = new Bundle();
        arg.putString( ProjectConst.ARG_ITEM_ID, getResources().getString( R.string.progitem_progpref ) );
        ProgramPreferencesFragment ppFragment = new ProgramPreferencesFragment();
        ppFragment.setArguments( arg );
        getActionBar().setTitle( R.string.conf_prog_headline );
        getActionBar().setLogo( R.drawable.properties );
        getFragmentManager().beginTransaction().replace( R.id.area_detail_container, ppFragment ).commit();
      }
    }
  }

  /**
   * 
   * Service binden, ggf starten
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 23.02.2013
   */
  private void doBindService()
  {
    //
    // Service starten, wenn er nicht schon läuft
    //
    final ActivityManager activityManager = ( ActivityManager )getSystemService( ACTIVITY_SERVICE );
    final List<RunningServiceInfo> services = activityManager.getRunningServices( Integer.MAX_VALUE );
    boolean isServiceFound = false;
    //
    for( int i = 0; i < services.size(); i++ )
    {
      // if( BuildConfig.DEBUG ) Log.d( TAG, "Service Nr." + i + ":" + services.get( i ).service );
      if( "de.dmarcini.submatix.android4.comm".equals( services.get( i ).service.getPackageName() ) )
      {
        if( "BlueThoothComService".equals( services.get( i ).service.getClassName() ) )
        {
          isServiceFound = true;
        }
      }
    }
    if( !isServiceFound )
    {
      if( BuildConfig.DEBUG ) Log.d( TAG, "Starting Service..." );
      Intent service = new Intent( this, BlueThoothComService.class );
      startService( service );
    }
    //
    // binde Service
    //
    Log.i( TAG, "bind  BT service..." );
    Intent intent = new Intent( this, BlueThoothComService.class );
    bindService( intent, mConnection, Context.BIND_AUTO_CREATE );
    mIsBound = true;
  }

  /**
   * 
   * Service unbinden
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 23.02.2013
   */
  private void doUnbindService()
  {
    if( mIsBound )
    {
      // If we have received the service, and hence registered with it, then now is the time to unregister.
      if( mService != null )
      {
        Log.v( TAG, "doUnbindService..." );
        if( mService != null )
        {
          if( mService != null && binder != null )
          {
            if( BuildConfig.DEBUG ) Log.d( TAG, "doUnbindService...unregister Handler..." );
            binder.unregisterServiceHandler( mHandler );
          }
          mService = null;
          binder = null;
          unbindService( mConnection );
        }
        mIsBound = false;
        Log.v( TAG, "doUnbindService...OK" );
      }
    }
  }

  /**
   * 
   * Verbinde Blutethooth Gerät
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 04.03.2013
   * @param device
   */
  public void doConnectBtDevice( String device )
  {
    if( BuildConfig.DEBUG ) Log.d( TAG, String.format( "recived do connect device <%s>", device ) );
    if( mIsBound )
    {
      mService.connect( device );
    }
  }

  /**
   * 
   * Trenne Bluethooth Gerät
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 04.03.2013
   */
  public void doDisconnectBtDevice()
  {
    if( BuildConfig.DEBUG ) Log.d( TAG, "recived do disconnect device " );
    if( mIsBound )
    {
      mService.disconnect();
    }
  }

  /**
   * Wird ein Dialog negativ beendet (nein oder Abbruch)
   * 
   * @see de.dmarcini.submatix.android4.gui.AreYouSureDialogFragment.NoticeDialogListener#onDialogNegativeClick(android.app.DialogFragment)
   * @author Dirk Marciniak 28.12.2012
   * @param dialog
   */
  @Override
  public void onDialogNegativeClick( DialogFragment dialog )
  {
    Log.v( TAG, "Negative dialog click!" );
  }

  /**
   * Wird ein dialog Positiv beendet (ja oder Ok...)
   * 
   * @see de.dmarcini.submatix.android4.gui.AreYouSureDialogFragment.NoticeDialogListener#onDialogPositiveClick(android.app.DialogFragment)
   * @author Dirk Marciniak 28.12.2012
   * @param dialog
   */
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
        Log.i( TAG, "User will close app..." );
        Toast.makeText( this, R.string.toast_exit, Toast.LENGTH_SHORT ).show();
        if( BluetoothAdapter.getDefaultAdapter() != null )
        {
          // TODO: Preferences -> Programmeinstellungen soll das automatisch passieren?
          BluetoothAdapter.getDefaultAdapter().disable();
        }
        finish();
      }
    }
  }

  /**
   * Wird ein Eintrag der Auswahlliste angeklickt Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 22.12.2012
   * @param listView
   * @param view
   * @param position
   * @param id
   */
  public void onListItemClick( ListView listView, View view, int position, long id )
  {
    ContentSwitcher.ProgItem mItem = null;
    Bundle arguments = new Bundle();
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
    arguments.putString( ProjectConst.ARG_ITEM_CONTENT, mItem.content );
    arguments.putInt( ProjectConst.ARG_ITEM_ID, mItem.nId );
    Log.v( TAG, "onListItemClick: item content was: " + mItem.content );
    Log.v( TAG, "onListItemClick: item id was: " + mItem.nId );
    //
    // wenn EXIT angeordnet wurde
    //
    switch ( mItem.nId )
    {
      case R.string.progitem_exit:
        // ENDE
        Log.v( TAG, "onListItemClick: make dialog for USER..." );
        AreYouSureDialogFragment sureDial = new AreYouSureDialogFragment( getString( R.string.dialog_sure_exit ) );
        sureDial.show( getFragmentManager().beginTransaction(), "programexit" );
        return;
    }
    // ////////////////////////////////////////////////////////////////////////
    // jetzt noch zwischen Tablett mit Schirmsplitt und Smartphone unterscheiden
    // ////////////////////////////////////////////////////////////////////////
    if( mTwoPane )
    {
      //
      // zweischirmbetrieb
      //
      Log.i( TAG, "onListItemClick: towPane mode!" );
      switch ( mItem.nId )
      {
        case R.string.progitem_config:
          //
          // Der Benutzer wählt den Konfigurationseintrag für den SPX
          //
          Log.v( TAG, "onListItemClick: create SPX42PreferencesFragment..." );
          SPX42PreferencesFragment cFragment = new SPX42PreferencesFragment( isIndividual );
          cFragment.setArguments( arguments );
          getActionBar().setTitle( R.string.conf_headline );
          getActionBar().setLogo( mItem.resId );
          getFragmentManager().beginTransaction().replace( R.id.area_detail_container, cFragment ).commit();
          break;
        //
        case R.string.progitem_progpref:
          //
          // der Benutzer will Programmeinstellungen setzen
          //
          Log.v( TAG, "onListItemClick: set program preferences..." );
          ProgramPreferencesFragment ppFragment = new ProgramPreferencesFragment();
          ppFragment.setArguments( arguments );
          getActionBar().setTitle( R.string.conf_prog_headline );
          getActionBar().setLogo( mItem.resId );
          getFragmentManager().beginTransaction().replace( R.id.area_detail_container, ppFragment ).commit();
          break;
        case R.string.progitem_gaslist:
          //
          // der Benutzer wählt den Gaslisten Editmode
          //
          Log.v( TAG, "onListItemClick: set gas preferences..." );
          SPX42GaslistPreferencesFragment glFragment = new SPX42GaslistPreferencesFragment( isTrimix );
          glFragment.setArguments( arguments );
          getActionBar().setTitle( R.string.gaslist_headline );
          getActionBar().setLogo( mItem.resId );
          getFragmentManager().beginTransaction().replace( R.id.area_detail_container, glFragment ).commit();
          break;
        //
        default:
          Log.w( TAG, "Not programitem found for <" + mItem.nId + ">" );
        case R.string.progitem_connect:
          //
          // keine passende ID gefunden oder
          // der Benutzer wählt den Verbindungseintrag
          //
          connectFragment connFragment = new connectFragment();
          getActionBar().setTitle( R.string.connect_headline );
          getActionBar().setLogo( mItem.resId );
          connFragment.setArguments( arguments );
          getFragmentManager().beginTransaction().replace( R.id.area_detail_container, connFragment ).commit();
          //
      }
    }
    else
    {
      //
      // kleiner Schirm
      // da wird jeder Eintrag als einzelne activity ausgeführt
      //
      Log.i( TAG, "onListItemClick: onePane modus! Call intent DetailActivity fur itenid<" + mItem.nId + ">" );
      Intent detailIntent = new Intent( this, areaDetailActivity.class );
      detailIntent.putExtras( arguments );
      startActivity( detailIntent );
    }
  }

  @Override
  public boolean onOptionsItemSelected( MenuItem item )
  {
    switch ( item.getItemId() )
    {
      case android.R.id.home:
        if( BuildConfig.DEBUG ) Log.d( TAG, "onOptionsItemSelected: navigate UP!" );
        // This is called when the Home (Up) button is pressed
        // in the Action Bar.
        Intent parentActivityIntent = new Intent( this, areaListActivity.class );
        parentActivityIntent.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK );
        startActivity( parentActivityIntent );
        finish();
        return true;
    }
    return super.onOptionsItemSelected( item );
  }

  @Override
  public void onPause()
  {
    super.onPause();
    doUnbindService();
  }

  @Override
  public void onResume()
  {
    Log.v( TAG, "onResume..." );
    super.onResume();
    //
    if( mBtAdapter == null )
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
    if( !mBtAdapter.isEnabled() )
    {
      // Eh, kein BT erlaubt!
      askEnableBT();
    }
    else
    {
      // Service wieder anbinden / starten
      doBindService();
    }
  }

  /**
   * 
   * Verbindungsstatus erfragen
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 13.03.2013
   * @return
   */
  public int getConnectionStatus()
  {
    if( mService != null )
    {
      return( mService.getConnectionState() );
    }
    return( ProjectConst.STATE_NONE );
  }

  /**
   * 
   * Wenn das Gerät verbunden wird (beim Verbinden)
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 23.02.2013
   * @param msg
   */
  @Override
  public void msgConnecting( BtServiceMessage msg )
  {
    Log.v( TAG, "connecting..." );
    // in der überschriebenen Funktion befüllen
  }

  /**
   * 
   * Das Gerät wurde verbunden
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 23.02.2013
   * @param msg
   */
  @Override
  public void msgConnected( BtServiceMessage msg )
  {
    Log.v( TAG, "connected..." );
    // in der überschriebenen Funktion befüllen
  }

  /**
   * 
   * Das Gerät wurde getrennt
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 23.02.2013
   * @param msg
   * 
   */
  @Override
  public void msgDisconnected( BtServiceMessage msg )
  {
    Log.v( TAG, "disconnected..." );
    // in der überschriebenen Funktion befüllen
  }

  /**
   * 
   * Wenn ein Fragment die Nachrichten erhalten soll, muß es den listener übergben...
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 24.02.2013
   * @param listener
   */
  public void setServiceListener( IBtServiceListener listener )
  {
    serviceListener = listener;
  }

  /**
   * 
   * Den Listener löschen, d.h. die Activity macht das wieder selber
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 24.02.2013
   */
  public void clearServiceListener()
  {
    serviceListener = this;
  }

  @Override
  public void msgRecivedTick( BtServiceMessage msg )
  {
    if( BuildConfig.DEBUG ) Log.d( TAG, String.format( "recived Tick <%x08x>", msg.getTimeStamp() ) );
  }

  @Override
  public void msgConnectError( BtServiceMessage msg )
  {
    if( BuildConfig.DEBUG ) Log.d( TAG, "connection error (device not online?)" );
  }
}
