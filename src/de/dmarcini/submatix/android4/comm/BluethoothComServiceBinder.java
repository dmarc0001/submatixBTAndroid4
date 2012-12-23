package de.dmarcini.submatix.android4.comm;

import java.util.Set;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Binder;
import android.os.Handler;
import android.util.Log;

/**
 * 
 * Binder zwischen Service und Activity
 * 
 * Project: SubmatixBTLog-Service Package: de.dmarcini.bluethooth.submatix.btService
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 * 
 *         Stand: 19.10.2011
 */
public class BluethoothComServiceBinder extends Binder
{
  private static final String  TAG = BluethoothComServiceBinder.class.getSimpleName();
  private BluethoothComService srv = null;

  public void setService( BluethoothComService sv )
  {
    this.srv = sv;
  }

  /**
   * 
   * Lösche den Servicehandler
   * 
   * Project: SubmatixBluethoothLoggerAndroid4Tablet Package: de.dmarcini.submatix.android4.comm
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 07.11.2012
   */
  public void clearServiceHandler()
  {
    if( srv.mIsBusy ) return;
    Log.v( TAG, "BINDER: LOESCHE HANDLER" );
    BluethoothComService.mHandler = null;
  }

  /**
   * 
   * Gib den BT Adapter zurück, wenn vorhanden
   * 
   * Project: SubmatixBluethoothLoggerAndroid4Tablet Package: de.dmarcini.submatix.android4.comm
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 07.11.2012
   * @return BT Adapter
   */
  public BluetoothAdapter getBTADapter()
  {
    return BluethoothComService.mAdapter;
  }

  /**
   * 
   * Gib die beim Gerät bereits gepaarten BT-Geräte zurück
   * 
   * Project: SubmatixBluethoothLoggerAndroid4Tablet Package: de.dmarcini.submatix.android4.comm
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 07.11.2012
   * @return ArrayList mit bereits gepaarten Geräten
   */
  public Set<BluetoothDevice> getPairedDevices()
  {
    if( srv.mIsBusy ) return( null );
    return( srv.getPairedBTDevices() );
  }

  /**
   * 
   * Gib die eigene Instanz zurück
   * 
   * Project: SubmatixBluethoothLoggerAndroid4Tablet Package: de.dmarcini.submatix.android4.comm
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 07.11.2012
   * @return Service selbst
   */
  public BluethoothComServiceBinder getService()
  {
    Log.v( TAG, "getService()..." );
    return( this );
  }

  /**
   * 
   * Gib den Handler mal zurück
   * 
   * Project: SubmatixBluethoothLoggerAndroid4Tablet Package: de.dmarcini.submatix.android4.comm
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 07.11.2012
   * @return Handler
   */
  public Handler getServiceHandler()
  {
    return BluethoothComService.mHandler;
  }

  /**
   * 
   * Ist der BT Adapter eingeschaltet
   * 
   * Project: SubmatixBluethoothLoggerAndroid4Tablet Package: de.dmarcini.submatix.android4.comm
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 07.11.2012
   * @return Ist der BT Adapter angeschaltet?
   */
  public boolean isEnabledBTAdapter()
  {
    if( ( BluethoothComService.mAdapter != null ) && BluethoothComService.mAdapter.isEnabled() ) return( true );
    return false;
  }

  /**
   * 
   * Setze den Handler für Nachrichten an die App
   * 
   * Project: SubmatixBluethoothLoggerAndroid4Tablet Package: de.dmarcini.submatix.android4.comm
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 07.11.2012
   * @param mH
   */
  public void setServiceHandler( Handler mH )
  {
    Log.v( TAG, "BINDER: setze HANDLER" );
    BluethoothComService.mHandler = mH;
    // srv.sendState();
  }
}
