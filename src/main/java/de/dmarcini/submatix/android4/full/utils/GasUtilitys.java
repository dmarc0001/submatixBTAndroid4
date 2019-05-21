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
package de.dmarcini.submatix.android4.full.utils;

import android.annotation.SuppressLint;

/**
 * Klasse, die für Gasberechnungen verwendet werden soll
 * <p/>
 * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
 *
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 *         <p/>
 *         Stand: 10.11.2013
 */
public class GasUtilitys
{
  private static double barOffset          = 1.0D; // Oberflächendruck Meereshöhe
  // private static double barConstClearWater = 0.0980665D; // Bar per 1 Meter
  private static double barConstClearWater = 1.0D; // Bar per 1 Meter

  /**
   * Gib den Namen des Gaases zurück
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 02.01.2013
   *
   * @param o2
   * @param he
   * @return Gasname
   */
  @SuppressLint( "DefaultLocale" )
  public static String getNameForGas(final int o2, final int he)
  {
    //
    // Wieviel Stickstoff?
    //
    int n2 = 100 - o2 - he;
    //
    // Mal sondieren
    //
    if( n2 == 0 )
    {
      //
      // heliox oder O2
      //
      if( o2 == 100 )
      {
        return ("O2");
      }
      // Es gibt Helium und O2.... == Heliox
      return (String.format("HX%d/%d", o2, he));
    }
    if( he == 0 )
    {
      // eindeutig Nitrox
      if( o2 == 21 )
      {
        return ("AIR");
      }
      return (String.format("NX%02d", o2));
    }
    else
    {
      // das ist dan wohl Trimix/Triox
      return (String.format("TX%d/%d", o2, he));
    }
  }

  /**
   * Gib das idealisierte MOD für das Gas aus
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 26.07.2013
   *
   * @param o2
   * @return Maximum Oxigen Depth
   */
  public static double getMODForGasMetric(final int o2)
  {
    // Also, gegeben O2 in Prozent, PPOMax (meist wohl 1.6 Bar)
    double pEnv, mod;
    // errechne den Umgebungsdruck für ppOMax (1.6 Bar) und Sauerstoffanteil
    pEnv = (1.6D * 100.0D) / o2;
    //
    // ziehe ein Bar für Oberflächendruck ab,
    // teile durch die Konstante für Süsswasser
    // ergibt die Tiefe für den Umgebungsdruck in Süßwasser
    //
    mod = ((pEnv - barOffset) / barConstClearWater) * (10D * barConstClearWater);
    if( mod < 0 )
    {
      return (0);
    }
    return (mod);
  }

  /**
   * Gib die EAD für das Diluent aus
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 26.07.2013
   *
   * @param o2
   * @param he
   * @param ppo2Setpoint
   * @param depth
   * @return Equralent Air Depth
   */
  public static double getEADForDilMetric(final int o2, final int he, final double ppo2Setpoint, final double depth)
  {
    // Gegeben n2 in Prozent, ich will wissen, wie die equivalente Tiefe ist
    double ead, o2Result, pEnv;
    double restgas;
    double n2FromRest, n2ForEAD;
    //
    // wieviel Prozent vom Restgas fällt auf die Bestandteile?
    //
    restgas = 100D - o2;
    n2FromRest = (restgas * (100 - he - o2)) / 100D;
    //
    // Umgebungsdruck
    //
    pEnv = (depth * barConstClearWater) + barOffset;
    // Sauerstoff im Diluent
    // o2Dil = ( o2 / 100D );
    //
    // wieviel Sauerstoff braucht es für den Setpoint?
    //
    o2Result = (ppo2Setpoint * 100.0D) / pEnv;
    //
    // wie hoch ist dann der Anteil vom Restgas?
    //
    restgas = 100D - o2Result;
    //
    // dann ist also restgas Prozent aufgeteilt in N2 und he
    // und Stickstoff in Prozenten läßt sich jetzt ausrechnen
    //
    n2ForEAD = ((restgas / 100D) * n2FromRest) / 100D;
    ead = (((n2ForEAD / .79D) * (barOffset + (depth * barConstClearWater))) - 1D) * 10.0D;
    if( ead < 0 )
    {
      return (0);
    }
    return (ead);
  }
}
