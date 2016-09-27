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
package de.dmarcini.submatix.android4.full;

/**
 * Klasse mit statischem Flag zum Debuggen
 * <p>
 * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4
 *
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 *         <p>
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
