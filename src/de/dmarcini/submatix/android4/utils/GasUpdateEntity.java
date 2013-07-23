package de.dmarcini.submatix.android4.utils;

public class GasUpdateEntity
{
  private int           gasNr    = 0;
  private SPX42GasParms gasParms = null;

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
