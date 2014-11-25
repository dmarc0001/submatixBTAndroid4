//@formatter:off
/*
    programm: SubmatixBTLoggerAndroid
    purpose:  configuration and read logs from SUBMATIX SPX42 divecomputer via Bluethooth    
    Copyright (C) 2012  Dirk Marciniak

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/
*/
//@formatter:on
package de.dmarcini.submatix.android4.full.gui;

import android.app.Activity;
import android.app.Fragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import de.dmarcini.submatix.android4.full.ApplicationDEBUG;
import de.dmarcini.submatix.android4.full.R;
import de.dmarcini.submatix.android4.full.comm.BtServiceMessage;
import de.dmarcini.submatix.android4.full.interfaces.IBtServiceListener;
import de.dmarcini.submatix.android4.full.utils.ProjectConst;

/**
 * 
 * Ein Fragment zur Anzeige der Stati des SPX42
 * 
 * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 * 
 *         Stand: 26.01.2014
 */
public class SPX42HealthFragment extends Fragment implements IBtServiceListener
{
  @SuppressWarnings( "javadoc" )
  public static final String TAG                       = SPX42HealthFragment.class.getSimpleName();
  private MainActivity       runningActivity           = null;
  private TextView           ackuVoltageTextView       = null;
  private TextView           serialNumberTextView      = null;
  private TextView           firmwareVersionTextView   = null;
  private TextView           licenseNitroxTextView     = null;
  private TextView           licenseNTMXTextView       = null;
  private TextView           licenseTMXTextView        = null;
  private TextView           licenseIndividualTextView = null;

  @Override
  public void handleMessages( int what, BtServiceMessage msg )
  {
    switch ( what )
    {
      case ProjectConst.MESSAGE_TICK:
        break;
      // ################################################################
      // OnConnect Meldung
      // ################################################################
      case ProjectConst.MESSAGE_CONNECTED:
        msgConnected( msg );
        break;
      // ################################################################
      // OnAlive
      // ################################################################
      case ProjectConst.MESSAGE_SPXALIVE:
        msgRecivedAlive( msg );
        break;
      // ################################################################
      // DEFAULT
      // ################################################################
      default:
        if( ApplicationDEBUG.DEBUG ) Log.i( TAG, "unknown messsage with id <" + what + "> recived!" );
    }
  }

  @Override
  public void msgConnected( BtServiceMessage msg )
  {
    Resources res = runningActivity.getResources();
    float ackuValue = MainActivity.ackuValue;
    //
    // Fülle die Textfelder mit aktuellen Werten
    //
    ackuVoltageTextView.setText( String.format( runningActivity.getResources().getString( R.string.health_acku_volatage ), ackuValue ) );
    //
    // Ackuspannung mit Farbe hinterlegen (grün SUPI, gelb, NAJA, rot, sollte nicht mehr tauchen gehen
    // grün ( oberhalb 3.6 Volt ) ROT 2,5 Volt
    // grün ggrößer 3,6 Volt
    // gelb 3,1 bis 3,6 volt
    // rot kleiner 3,6 Volt
    if( ackuValue > 3.6F )
    {
      ackuVoltageTextView.setTextColor( res.getColor( R.color.acku_good_value ) );
    }
    else if( ackuValue > 3.1F )
    {
      ackuVoltageTextView.setTextColor( res.getColor( R.color.acku_mid_value ) );
    }
    else
    {
      ackuVoltageTextView.setTextColor( res.getColor( R.color.acku_low_value ) );
    }
    // Beschriftungen
    serialNumberTextView.setText( MainActivity.spxConfig.getSerial() );
    firmwareVersionTextView.setText( MainActivity.spxConfig.getFirmwareVersion() );
    switch ( MainActivity.spxConfig.getLicenseState() )
    {
      case ProjectConst.SPX_LICENSE_FULLTX:
        if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "SPX MIX License : FULL TRIMIX licensed" );
        licenseTMXTextView.setText( res.getString( R.string.health_license_enable ) );
        licenseTMXTextView.setTextColor( res.getColor( R.color.licenseEnabled ) );
      case ProjectConst.SPX_LICENSE_NORMOXICTX:
        if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "SPX MIX License : NORMOXIX TRIMIX licensed" );
        licenseNTMXTextView.setText( res.getString( R.string.health_license_enable ) );
        licenseNTMXTextView.setTextColor( res.getColor( R.color.licenseEnabled ) );
      case ProjectConst.SPX_LICENSE_NITROX:
        if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "SPX MIX License : NITROX licensed" );
        licenseNitroxTextView.setText( res.getString( R.string.health_license_enable ) );
        licenseNitroxTextView.setTextColor( res.getColor( R.color.licenseEnabled ) );
        break;
      default:
        if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "SPX MIX License : UNKNOWN" );
    }
    //
    // jetzt noch den Individual-Lizenzsatatus erfragen
    //
    if( 1 == MainActivity.spxConfig.getCustomEnabled() )
    {
      if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "SPX CUSTOM License : licensed" );
      licenseIndividualTextView.setText( res.getString( R.string.health_license_enable ) );
      licenseIndividualTextView.setTextColor( res.getColor( R.color.licenseEnabled ) );
    }
  }

  @Override
  public void msgConnectError( BtServiceMessage msg )
  {
    // TODO Automatisch generierter Methodenstub
  }

  @Override
  public void msgConnecting( BtServiceMessage msg )
  {
    // TODO Automatisch generierter Methodenstub
  }

  @Override
  public void msgDisconnected( BtServiceMessage msg )
  {
    // TODO Automatisch generierter Methodenstub
  }

  @Override
  public void msgRecivedAlive( BtServiceMessage msg )
  {
    ackuVoltageTextView.setText( String.format( runningActivity.getResources().getString( R.string.health_acku_volatage ), MainActivity.ackuValue ) );
  }

  @Override
  public void msgRecivedTick( BtServiceMessage msg )
  {
    // TODO Automatisch generierter Methodenstub
  }

  @Override
  public void msgReciveWriteTmeout( BtServiceMessage msg )
  {
    // TODO Automatisch generierter Methodenstub
  }

  @Override
  public void onActivityCreated( Bundle bundle )
  {
    super.onActivityCreated( bundle );
    runningActivity = ( MainActivity )getActivity();
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onActivityCreated: ACTIVITY ATTACH" );
    Bundle arguments = getArguments();
    if( arguments != null && arguments.containsKey( ProjectConst.ARG_ITEM_CONTENT ) )
    {
      runningActivity.onSectionAttached( arguments.getString( ProjectConst.ARG_ITEM_CONTENT ) );
    }
    else
    {
      Log.w( TAG, "onActivityCreated: TITLE NOT SET!" );
    }
  }

  @Override
  public void onAttach( Activity activity )
  {
    super.onAttach( activity );
    runningActivity = ( MainActivity )activity;
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onAttach: ATTACH" );
  }

  /**
   * Nach dem Erzeugen des Objektes noch Einstellungen....
   */
  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onCreate..." );
  }

  /**
   * Wenn das View erzeugt wird (nach onCreate), noch ein paar Sachen erledigen
   */
  @Override
  public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
  {
    View rootView;
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onCreateView..." );
    //
    // wenn kein Container vorhanden ist, dann gibts auch keinen View
    //
    if( container == null )
    {
      Log.e( TAG, "onCreateView: container is NULL ..." );
      return( null );
    }
    //
    // wenn die laufende Activity eine AreaDetailActivity ist, dann gibts das View schon
    //
    // if( runningActivity instanceof AreaDetailActivity )
    // {
    // if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onCreateView: running from AreaDetailActivity ..." );
    // return( null );
    // }
    //
    // Verbindungsseite via twoPane ausgewählt
    //
    //
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "makeConnectionView..." );
    //
    // View aus Resource laden
    //
    rootView = inflater.inflate( R.layout.fragment_spx42_health, container, false );
    //
    // die Adressen der gesuchten Objekte rausuchen
    //
    ackuVoltageTextView = ( TextView )rootView.findViewById( R.id.ackuVoltageTextView );
    serialNumberTextView = ( TextView )rootView.findViewById( R.id.serialNumberTextView );
    firmwareVersionTextView = ( TextView )rootView.findViewById( R.id.firmwareVersionTextView );
    licenseNitroxTextView = ( TextView )rootView.findViewById( R.id.licenseNitroxTextView );
    licenseNTMXTextView = ( TextView )rootView.findViewById( R.id.licenseNTMXTextView );
    licenseTMXTextView = ( TextView )rootView.findViewById( R.id.licenseTMXTextView );
    licenseIndividualTextView = ( TextView )rootView.findViewById( R.id.licenseIndividualTextView );
    //
    return( rootView );
  }

  @Override
  public void onDestroy()
  {
    super.onDestroy();
  }

  @Override
  public void onDetach()
  {
    super.onDetach();
    Bundle arguments = getArguments();
    //
    if( arguments != null && arguments.containsKey( ProjectConst.ARG_ITEM_ID ) )
    {
      // Es gibt einen Eintrag für den Gewählten Menüpunkt
      if( arguments.getBoolean( ProjectConst.ARG_TOSTACK_ONDETACH, false ) )
      {
        // wenn das Fragment NICHT über Back aufgerufen wurde, dann im Stack verewigen
        // und kennzeichnen
        arguments.putBoolean( ProjectConst.ARG_TOSTACK_ONDETACH, false );
        runningActivity.fillCallStack( arguments.getInt( ProjectConst.ARG_ITEM_ID ), arguments );
      }
    }
  }

  @Override
  public void onPause()
  {
    super.onPause();
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onPause..." );
    //
    // die abgeleiteten Objekte führen das auch aus
    //
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onPause: clear service listener for preferences fragment..." );
    runningActivity.removeServiceListener( this );
  }

  /**
   * Wenn das View wieder nach vorn kommt / reaktiviert wurde
   */
  @Override
  public synchronized void onResume()
  {
    super.onResume();
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onResume..." );
    runningActivity.addServiceListener( this );
    // if( runningActivity instanceof AreaDetailActivity )
    // {
    // //
    // // die Adressen der gesuchten Objekte rausuchen
    // //
    // ackuVoltageTextView = ( TextView )runningActivity.findViewById( R.id.ackuVoltageTextView );
    // serialNumberTextView = ( TextView )runningActivity.findViewById( R.id.serialNumberTextView );
    // firmwareVersionTextView = ( TextView )runningActivity.findViewById( R.id.firmwareVersionTextView );
    // licenseNitroxTextView = ( TextView )runningActivity.findViewById( R.id.licenseNitroxTextView );
    // licenseNTMXTextView = ( TextView )runningActivity.findViewById( R.id.licenseNTMXTextView );
    // licenseTMXTextView = ( TextView )runningActivity.findViewById( R.id.licenseTMXTextView );
    // licenseIndividualTextView = ( TextView )runningActivity.findViewById( R.id.licenseIndividualTextView );
    // //
    // }
  }
}
