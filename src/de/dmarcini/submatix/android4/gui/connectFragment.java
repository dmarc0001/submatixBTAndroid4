package de.dmarcini.submatix.android4.gui;

import android.app.Fragment;
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
import android.widget.Spinner;
import de.dmarcini.submatix.android4.R;
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
public class connectFragment extends Fragment
{
  public static final String          TAG            = connectFragment.class.getSimpleName();
  // private final DisplayMetrics displayMetrics = new DisplayMetrics();
  private View                        rootView       = null;
  private final BluetoothAdapter      mBtAdapter     = null;
  private BluetoothDeviceArrayAdapter btArrayAdapter = null;
  private Button                      discoverButton = null;
  private Spinner                     devSpinner     = null;

  /**
   * Nach dem Erzeugen des Objektes noch Einstellungen....
   */
  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
    Log.v( TAG, "onCreate()..." );
  }

  /**
   * Wenn das View erzeugt wird (nach onCreate), noch ein paar Sachen erledigen
   */
  @Override
  public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
  {
    Log.v( TAG, "onCreateView()..." );
    // Höhe des Views feststellen
    // getActivity().getWindowManager().getDefaultDisplay().getMetrics( displayMetrics );
    // Verbindungsseite ausgewählt
    Log.v( TAG, "onCreateView: item for connect device selected!" );
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
    btArrayAdapter = new BluetoothDeviceArrayAdapter( getActivity(), R.layout.device_name_textview );
    //
    // eine Liste der bereits gepaarten Devices
    //
    // Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
    //
    // Sind dort einige vorhanden, dann ab in den adapter...
    //
    // if( pairedDevices.size() > 0 )
    // {
    // // Alle gepaarten Geräte durch
    // for( BluetoothDevice device : pairedDevices )
    // {
    // // Ist es ein gerät vom gewünschten Typ?
    // if( ( device.getBluetoothClass().getDeviceClass() == ProjectConst.SPX_BTDEVICE_CLASS ) && ( device.getName() != null ) )
    // {
    // btArrayAdapter.add( device.getName() + "\n" + device.getAddress() + "\n" + device.getName() + "\n0" );
    // }
    // }
    // }
    // else
    // {
    // btArrayAdapter.add( getActivity().getString( R.string.no_device ) );
    // }
    // devSpinner.setAdapter( btArrayAdapter );
  }

  @Override
  public void onDestroy()
  {
    super.onDestroy();
    // Make sure we're not doing discovery anymore
    if( mBtAdapter != null )
    {
      mBtAdapter.cancelDiscovery();
    }
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
    // ToggleButton tgButton = null;
    int height, width;
    //
    Log.v( TAG, "makeConnectionView()..." );
    //
    // View aus Resource laden
    //
    rootView = inflater.inflate( R.layout.fragment_connect, container, false );
    // Dimensionen checken
    // height = displayMetrics.heightPixels;
    // width = displayMetrics.widthPixels;
    // Log.v( TAG, String.format( "screen has height: %d pixels and width: %d pixels", height, width ) );
    // Objekte lokalisieren
    // tgButton = ( ToggleButton )rootView.findViewById( R.id.connectTroggleButton );
    discoverButton = ( Button )rootView.findViewById( R.id.connectDiscoverButton );
    devSpinner = ( Spinner )rootView.findViewById( R.id.connectBlueToothDeviceSpinner );
    //
    // den Discoverbutton mit Funktion versehen
    //
    discoverButton.setOnClickListener( new OnClickListener() {
      @Override
      public void onClick( View v )
      {
        Log.v( TAG, "start discovering for BT Devices..." );
        // ist da nur die Kennzeichnung für LEER?
        if( btArrayAdapter.isEmpty() || btArrayAdapter.getItem( 0 ).startsWith( getActivity().getString( R.string.no_device ).substring( 0, 5 ) ) )
        {
          Log.v( TAG, "not devices in Adapter yet..." );
          btArrayAdapter.clear();
        }
        Log.v( TAG, "start discovering for BT Devices...ArrayAdapter created" );
        startDiscoverBt();
        v.setEnabled( false );
      }
    } );
    return( rootView );
  }

  private void startDiscoverBt()
  {
    // If we're already discovering, stop it
    Log.v( TAG, "start discovering..." );
    if( mBtAdapter.isDiscovering() )
    {
      mBtAdapter.cancelDiscovery();
    }
    // Request discover from BluetoothAdapter
    mBtAdapter.startDiscovery();
  }

  // The BroadcastReceiver that listens for discovered devices and
  // changes the title when discovery is finished
  private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
                                              @Override
                                              public void onReceive( Context context, Intent intent )
                                              {
                                                String action = intent.getAction();
                                                // When discovery finds a device
                                                if( BluetoothDevice.ACTION_FOUND.equals( action ) )
                                                {
                                                  // Get the BluetoothDevice object from the Intent
                                                  BluetoothDevice device = intent.getParcelableExtra( BluetoothDevice.EXTRA_DEVICE );
                                                  Log.v( TAG, String.format( "Name: %s, MAC: %s", device.getName(), device.getAddress() ) );
                                                  // If it's already paired, skip it, because it's been listed already
                                                  if( ( device.getBondState() != BluetoothDevice.BOND_BONDED ) && ( device.getName() != null )
                                                          && ( device.getBluetoothClass().getMajorDeviceClass() == ProjectConst.SPX_BTDEVICE_CLASS ) )
                                                  {
                                                    Log.v( TAG, "found device..." );
                                                    // Feld 0 = Geräte Alias / Gerätename
                                                    // Feld 1 = Geräte-MAC
                                                    // Feld 2 = Geräte-Name
                                                    // Feld 3 = Datenbank-Id (wenn vorhanden) sonst 0
                                                    btArrayAdapter.add( device.getName() + "\n" + device.getAddress() + "\n" + device.getName() + "\n0" );
                                                  }
                                                  //
                                                  // When discovery is finished, change the Activity title
                                                }
                                                else if( BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals( action ) )
                                                {
                                                  Log.v( TAG, "discover finished, enable button." );
                                                  discoverButton.setEnabled( true );
                                                  devSpinner.setAdapter( btArrayAdapter );
                                                }
                                              }
                                            };
}
