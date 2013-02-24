package de.dmarcini.submatix.android4.comm;

/**
 * 
 * Schnittstellenbeschreibung für den Service, wie er mit der App Kommunizieren kann
 * 
 * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.comm
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 * 
 *         Stand: 23.02.2013
 */
public interface IComServiceToApp
{
  public void sendMessage( BtServiceMessage msg );
}
