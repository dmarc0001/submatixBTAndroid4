package de.dmarcini.submatix.android4.full.utils;

/**
 * 
 * Klasse als Container für ein Gas-Update
 * 
 * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 * 
 *         Stand: 10.11.2013
 */
public class GasUpdateEntity
{
  private int           gasNr    = 0;
  private SPX42GasParms gasParms = null;

  /**
   * 
   * Konstruktor mit Parametern
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * 
   * Stand: 03.12.2013
   * 
   * @param nr
   * @param parms
   */
  public GasUpdateEntity( int nr, SPX42GasParms parms )
  {
    gasNr = nr;
    gasParms = parms;
  }

  /**
   * @return gasNr
   */
  public int getGasNr()
  {
    return gasNr;
  }

  /**
   * @return gasParms
   */
  public SPX42GasParms getGasParms()
  {
    return gasParms;
  }
}
