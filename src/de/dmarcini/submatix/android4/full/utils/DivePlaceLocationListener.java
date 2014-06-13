package de.dmarcini.submatix.android4.full.utils;

import java.util.Locale;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;

/**
 * Klasse für Benachrichtigungen über Locationupdates
 * 
 * Project: Android GPS Testapp Package: de.dmarcini.submatix.android4.full.utils
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 * 
 *         Stand: 12.06.2014
 */
public class DivePlaceLocationListener implements LocationListener
{
  private static final String TAG = DivePlaceLocationListener.class.getSimpleName();

  @Override
  public void onLocationChanged( Location location )
  {
    // Called when a new location is found by the network location provider.
    makeUseOfNewLocation( location );
  }

  /**
   * 
   * Mache etwas mit der neuen Ortsangabe
   * 
   * Project: Android GPS Testapp Package: de.dmarcini.android4.gpstest
   * 
   * Stand: 12.06.2014
   * 
   * @param location
   */
  private void makeUseOfNewLocation( Location location )
  {
    String out = null;
    //
    Log.d( TAG, "new Location..." );
    if( location.hasAccuracy() )
    {
      out = String.format( Locale.ENGLISH, "accuracy: %2.1f m LAT: %2.6f, LON: %2.6f", location.getAccuracy(), location.getLatitude(), location.getLongitude() );
    }
    else
    {
      out = "accuracy: NONE LAT: NONE, LON: NONE";
    }
    Log.d( TAG, out );
  }

  @Override
  public void onStatusChanged( String provider, int status, Bundle extras )
  {
    String stat = "unknown";
    String out = null;
    //
    Log.d( TAG, "status changed..." );
    if( status == LocationProvider.OUT_OF_SERVICE )
    {
      stat = "OUT_OF_SERVICE";
    }
    else if( status == LocationProvider.AVAILABLE )
    {
      stat = "AVAVIBLE";
    }
    else if( status == LocationProvider.TEMPORARILY_UNAVAILABLE )
    {
      stat = "TEMPORARILY_UNAVAILABLE";
    }
    out = String.format( "Provider: %s, Status: %s", provider, stat );
    Log.d( TAG, out );
  }

  @Override
  public void onProviderEnabled( String provider )
  {
    String out = null;
    out = String.format( "provider %s enabled...", provider );
    Log.d( TAG, out );
  }

  @Override
  public void onProviderDisabled( String provider )
  {
    String out = null;
    out = String.format( "provider %s disabled...", provider );
    Log.d( TAG, out );
  }
}
