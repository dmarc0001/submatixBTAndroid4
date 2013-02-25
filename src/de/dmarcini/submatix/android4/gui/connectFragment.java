package de.dmarcini.submatix.android4.gui;

import java.util.Set;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import de.dmarcini.submatix.android4.R;
import de.dmarcini.submatix.android4.comm.BtServiceMessage;
import de.dmarcini.submatix.android4.utils.BluetoothDeviceArrayAdapter;
import de.dmarcini.submatix.android4.utils.ProjectConst;

/**
 * 
 * Ein Detsailfragment, welches die Verbindung mit dem SPX Managed
 * 
 * Project: SubmatixBluethoothLoggerAndroid4Tablet Package: de.dmarcini.submatix.android4.gui
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 * 
 *         Stand: 04.11.2012
 */
public class connectFragment extends Fragment implements OnClickListener, IBtServiceListener
{
  public static final String          TAG            = connectFragment.class.getSimpleName();
  private View                        rootView       = null;
  private BluetoothDeviceArrayAdapter btArrayAdapter = null;
  private Button                      discoverButton = null;
  private Spinner                     devSpinner     = null;
  private ImageButton                 connButton     = null;
  protected ProgressDialog            progressDialog = null;

  /**
   * Nach dem Erzeugen des Objektes noch Einstellungen....
   */
  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
    Log.v( TAG, "onCreate()..." );
    ( ( FragmentCommonActivity )getActivity() ).setServiceListener( this );
  }

  /**
   * Wenn das View erzeugt wird (nach onCreate), noch ein paar Sachen erledigen
   */
  @Override
  public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
  {
    Log.v( TAG, "onCreateView()..." );
    // Verbindungsseite ausgewählt
    rootView = makeConnectionView( inflater, container );
    //
    // Register broadcasts während Geräte gesucht werden
    //
    IntentFilter filter = new IntentFilter( BluetoothDevice.ACTION_FOUND );
    getActivity().registerReceiver( mReceiver, filter );
    //
    // Register broadcasts wenn die Suche beendet wurde
    //
    filter = new IntentFilter( BluetoothAdapter.ACTION_DISCOVERY_FINISHED );
    getActivity().registerReceiver( mReceiver, filter );
    return rootView;
  }

  @Override
  public void onPause()
  {
    super.onPause();
    Log.v( TAG, "onPause()..." );
    discoverButton.setOnClickListener( null );
    connButton.setOnClickListener( null );
  }

  /**
   * Wenn das View wieder nach vorn kommt / reaktiviert wurde
   */
  @Override
  public synchronized void onResume()
  {
    super.onResume();
    Log.v( TAG, "onResume()..." );
    //
    // erst mal leere Liste anzeigen, String aus Resource
    //
    btArrayAdapter = new BluetoothDeviceArrayAdapter( getActivity(), R.layout.device_name_textview, ( ( FragmentCommonActivity )getActivity() ).getAppStyle() );
    if( FragmentCommonActivity.mBtAdapter == null ) return;
    //
    // eine Liste der bereits gepaarten Devices
    //
    Set<BluetoothDevice> pairedDevices = FragmentCommonActivity.mBtAdapter.getBondedDevices();
    //
    // Sind dort einige vorhanden, dann ab in den adapter...
    //
    if( pairedDevices.size() > 0 )
    {
      // Alle gepaarten Geräte durch
      for( BluetoothDevice device : pairedDevices )
      {
        // Ist es ein Gerät vom gewünschten Typ?
        if( ( device.getBluetoothClass().getDeviceClass() == ProjectConst.SPX_BTDEVICE_CLASS ) && ( device.getName() != null ) )
        {
          String[] entr = new String[BluetoothDeviceArrayAdapter.BT_DEVAR_COUNT];
          // TODO: ALIAS TESTEN und eintragen!
          entr[BluetoothDeviceArrayAdapter.BT_DEVAR_ALIAS] = device.getName() + "*";
          entr[BluetoothDeviceArrayAdapter.BT_DEVAR_MAC] = device.getAddress();
          entr[BluetoothDeviceArrayAdapter.BT_DEVAR_NAME] = device.getName();
          entr[BluetoothDeviceArrayAdapter.BT_DEVAR_DBID] = "0";
          entr[BluetoothDeviceArrayAdapter.BT_DEVAR_ISPAIRED] = "true";
          btArrayAdapter.add( entr );
        }
      }
    }
    else
    {
      String[] entr = new String[BluetoothDeviceArrayAdapter.BT_DEVAR_COUNT];
      entr[BluetoothDeviceArrayAdapter.BT_DEVAR_ALIAS] = getActivity().getString( R.string.no_device );
      entr[BluetoothDeviceArrayAdapter.BT_DEVAR_MAC] = "";
      entr[BluetoothDeviceArrayAdapter.BT_DEVAR_NAME] = getActivity().getString( R.string.no_device );
      entr[BluetoothDeviceArrayAdapter.BT_DEVAR_DBID] = "0";
      entr[BluetoothDeviceArrayAdapter.BT_DEVAR_ISPAIRED] = "false";
      btArrayAdapter.add( entr );
    }
    Log.v( TAG, "fill List with devices on surcafe..." );
    devSpinner.setAdapter( btArrayAdapter );
  }

  @Override
  public void onDestroy()
  {
    super.onDestroy();
    // Make sure we're not doing discovery anymore
    if( FragmentCommonActivity.mBtAdapter != null )
    {
      FragmentCommonActivity.mBtAdapter.cancelDiscovery();
    }
    ( ( FragmentCommonActivity )getActivity() ).clearServiceListener();
    // Unregister broadcast listeners
    getActivity().unregisterReceiver( mReceiver );
  }

  /**
   * 
   * Die Anzeige der Verbunden/trennen Seite
   * 
   * Project: SubmatixBluethoothLoggerAndroid4Tablet Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 04.11.2012
   * @param inflater
   * @param container
   * @return
   */
  private View makeConnectionView( LayoutInflater inflater, ViewGroup container )
  {
    View rootView;
    //
    Log.v( TAG, "makeConnectionView()..." );
    //
    // View aus Resource laden
    //
    rootView = inflater.inflate( R.layout.fragment_connect, container, false );
    //
    // Objekte lokalisieren
    //
    discoverButton = ( Button )rootView.findViewById( R.id.connectDiscoverButton );
    devSpinner = ( Spinner )rootView.findViewById( R.id.connectBlueToothDeviceSpinner );
    connButton = ( ImageButton )rootView.findViewById( R.id.connectButton );
    if( discoverButton == null || devSpinner == null || connButton == null )
    {
      throw new NullPointerException( "can init GUI (not found an Element)" );
    }
    //
    // den Discoverbutton mit Funktion versehen, wenn ein Adapter da und eingeschaltet ist
    //
    if( FragmentCommonActivity.mBtAdapter != null && FragmentCommonActivity.mBtAdapter.isEnabled() )
    {
      discoverButton.setOnClickListener( this );
      connButton.setOnClickListener( this );
    }
    setToggleButtonTextAndStat( connButton, 0 );
    return( rootView );
  }

  private void setToggleButtonTextAndStat( ImageButton tg, int connState )
  {
    switch ( connState )
    {
      case ProjectConst.STATE_NONE:
      default:
        tg.setImageResource( R.drawable.bluetooth_icon_bw );
        tg.setAlpha( 1.0F );
        break;
      case ProjectConst.STATE_CONNECTING:
        tg.setImageResource( R.drawable.bluetooth_icon_color );
        tg.setAlpha( 0.5F );
        break;
      case ProjectConst.STATE_CONNECTED:
        tg.setImageResource( R.drawable.bluetooth_icon_color );
        tg.setAlpha( 1.0F );
        break;
    }
  }

  /**
   * 
   * Starte das Suchen nach BT-Geräten
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 17.02.2013
   */
  private void startDiscoverBt()
  {
    // If we're already discovering, stop it
    Log.v( TAG, "start discovering..." );
    setItemsEnabled( false );
    if( FragmentCommonActivity.mBtAdapter == null ) return;
    if( FragmentCommonActivity.mBtAdapter.isDiscovering() )
    {
      FragmentCommonActivity.mBtAdapter.cancelDiscovery();
    }
    // Request discover from BluetoothAdapter
    FragmentCommonActivity.mBtAdapter.startDiscovery();
  }

  /**
   * 
   * Lasse die Butons nicht mehr bedienen
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 17.02.2013
   * @param enabled
   */
  private void setItemsEnabled( boolean enabled )
  {
    if( progressDialog != null )
    {
      Log.v( TAG, "dialog dismiss...." );
      progressDialog.dismiss();
      progressDialog = null;
    }
    if( !enabled )
    {
      Log.v( TAG, "dialog show...." );
      progressDialog = new ProgressDialog( getActivity() );
      progressDialog.setTitle( R.string.progress_wait_for_discover_title );
      progressDialog.setIndeterminate( true );
      progressDialog.setMessage( getActivity().getResources().getString( R.string.progress_wait_for_discover_message_start ) );
      progressDialog.show();
      // .show( getActivity().getApplicationContext(), "search", "Loading...", true );
    }
    discoverButton.setEnabled( enabled );
    devSpinner.setEnabled( enabled );
    connButton.setEnabled( enabled );
  }

  //
  // der Broadcast Empfänger der Nachrichten über gefudnene BT Geräte findet
  //
  private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
                                              @Override
                                              public void onReceive( Context context, Intent intent )
                                              {
                                                String action = intent.getAction();
                                                //
                                                // wenn ein Gerät gefunden wurde
                                                //
                                                if( BluetoothDevice.ACTION_FOUND.equals( action ) )
                                                {
                                                  //
                                                  // Das Gerät extraieren
                                                  //
                                                  BluetoothDevice device = intent.getParcelableExtra( BluetoothDevice.EXTRA_DEVICE );
                                                  Log.v( TAG, String.format( "Name: %s, MAC: %s", device.getName(), device.getAddress() ) );
                                                  if( progressDialog != null )
                                                  {
                                                    Log.v( TAG, "device add to progressDialog..." );
                                                    // den Gerätenamen in den String aus der Resource (lokalisiert) einbauen und in die waitbox setzen
                                                    String dispStr = ( ( device.getName() == null ) ? device.getAddress() : device.getName() );
                                                    progressDialog.setMessage( String.format(
                                                            getActivity().getResources().getString( R.string.progress_wait_for_discover_message_continue ), dispStr ) );
                                                  }
                                                  // If it's already paired, skip it, because it's been listed already
                                                  if( ( device.getBondState() != BluetoothDevice.BOND_BONDED ) && ( device.getName() != null )
                                                          && ( device.getBluetoothClass().getMajorDeviceClass() == ProjectConst.SPX_BTDEVICE_CLASS ) )
                                                  {
                                                    // Feld 0 = Geräte Alias / Gerätename
                                                    // Feld 1 = Geräte-MAC
                                                    // Feld 2 = Geräte-Name
                                                    // Feld 3 = Datenbank-Id (wenn vorhanden) sonst 0
                                                    // Feld 4 = Gerät gepart?
                                                    String[] entr = new String[BluetoothDeviceArrayAdapter.BT_DEVAR_COUNT];
                                                    entr[BluetoothDeviceArrayAdapter.BT_DEVAR_ALIAS] = device.getName();
                                                    entr[BluetoothDeviceArrayAdapter.BT_DEVAR_MAC] = device.getAddress();
                                                    entr[BluetoothDeviceArrayAdapter.BT_DEVAR_NAME] = device.getName();
                                                    entr[BluetoothDeviceArrayAdapter.BT_DEVAR_DBID] = "0";
                                                    entr[BluetoothDeviceArrayAdapter.BT_DEVAR_ISPAIRED] = "false";
                                                    // add, wenn nicht schon vorhanden
                                                    Log.v( TAG, "device add to btArrayAdapter..." );
                                                    ( ( BluetoothDeviceArrayAdapter )devSpinner.getAdapter() ).add( entr );
                                                  }
                                                  //
                                                  // When discovery is finished, change the Activity title
                                                }
                                                else if( BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals( action ) )
                                                {
                                                  Log.v( TAG, "discover finished, enable button." );
                                                  setItemsEnabled( true );
                                                  devSpinner.setAdapter( btArrayAdapter );
                                                }
                                              }
                                            };

  @Override
  public void onClick( View cView )
  {
    int connState = ProjectConst.STATE_NONE;
    Log.d( TAG, "ON CLICK!" );
    //
    if( cView instanceof ImageButton )
    {
      ImageButton tb = ( ImageButton )cView;
      if( btArrayAdapter.isEmpty() || btArrayAdapter.getAlias( 0 ).startsWith( getActivity().getString( R.string.no_device ).substring( 0, 5 ) ) )
      {
        Log.v( TAG, "not devices in Adapter yet..." );
        // TODO: setToggleButtonTextAndStat( tb );
        return;
      }
      switch ( connState )
      {
        case ProjectConst.STATE_NONE:
        default:
          Log.v( TAG, "switch connect to ON" );
          //
          // wenn da noch einer werkelt, anhalten und kompostieren
          //
          // TODO: Service stoppen
          //
          // einen neuen Tread machen
          //
          tb.setImageResource( R.drawable.bluetooth_icon_color );
          tb.setAlpha( 0.5F );
          break;
        case ProjectConst.STATE_CONNECTING:
          Log.v( TAG, "cancel connecting.." );
        case ProjectConst.STATE_CONNECTED:
          Log.v( TAG, "switch connect to OFF" );
          //
          // wenn da noch einer werkelt, anhalten und kompostieren
          //
          // TODO: Servive stoppen
          tb.setImageResource( R.drawable.bluetooth_icon_bw );
          tb.setAlpha( 1.0F );
          break;
      }
    }
    //
    if( cView instanceof Button )
    {
      Log.v( TAG, "start discovering for BT Devices..." );
      // ist da nur die Kennzeichnung für LEER?
      if( btArrayAdapter.isEmpty() || btArrayAdapter.getAlias( 0 ).startsWith( getActivity().getString( R.string.no_device ).substring( 0, 5 ) ) )
      {
        Log.v( TAG, "not devices in Adapter yet..." );
        btArrayAdapter.clear();
        // connButton.setChecked( false );
      }
      Log.v( TAG, "start discovering for BT Devices...ArrayAdapter created" );
      startDiscoverBt();
      return;
    }
  }

  //
  // ENDE
  //
  @Override
  public void msgConnecting( BtServiceMessage msg )
  {
    // TODO Automatisch generierter Methodenstub
  }

  @Override
  public void msgConnected( BtServiceMessage msg )
  {
    // TODO Automatisch generierter Methodenstub
  }

  @Override
  public void msgDisconnected( BtServiceMessage msg )
  {
    // TODO Automatisch generierter Methodenstub
  }

  @Override
  public void msgRecivedTick( BtServiceMessage msg )
  {
    Log.d( TAG, String.format( "recived Tick <%x08x>", msg.getTimeStamp() ) );
  }
}
