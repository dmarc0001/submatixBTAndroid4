package de.dmarcini.submatix.android4.full;

/**
 * Klasse mit statischem Flag zum Debuggen
 * 
 * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 * 
 *         Stand: 18.11.2013
 */
public class ApplicationDEBUG
{
  //
  // dies ist ein Workarround, da BuildConfig.DEBUG nicht funktioniert :-(
  //
  @SuppressWarnings( "javadoc" )
  public static final boolean DEBUG = true;
}
