/*
 * Klasse als Container für Programmkonstanten Bluethooth service
 */
package de.dmarcini.submatix.android4.utils;

import java.util.UUID;

import android.graphics.Color;

/**
 * 
 * Klasse für die globalen Konstanten und Einstellungen des Programmes
 * 
 * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 * 
 *         Stand: 10.11.2013
 */
//@formatter:off
@SuppressWarnings( "javadoc" )
public final class ProjectConst
{
  // 0 Grad Celsius
  public static final float  KELVIN                     = (float)(273.15);
  
  // UDDF Festlegungen
  public static final String UDDFVERSION                = "2.2.0";
  public static final String CREATORPROGRAM             = "SUBMATIX SPX42 Manager";
  public static final String CREATORNAME                = "SPX42";
  public static final String MANUFACTNAME               = "Dirk Marciniak";
  public static final String MANUFACTMAIL               = "dirk_marciniak@arcor.de";
  public static final String MANUFACTHOME               = "http://www.submatix.com";
  public static final String MANUFACTVERS               = "0.7-BETA";
  public static final String GENYEAR                    = "2013";
  public static final String GENMONTH                   = "12";
  public static final String GENDAY                     = "13";
  public static final boolean CHECK_PHYSICAL_BT         = false; 

  // ANDROID: Preferences Version
  public static int          PREF_VERSION               = 1;

  // ANDROID: Unique UUID für allgemeine (well known) Serielle Schnittstelle
  public static final UUID   SERIAL_DEVICE_UUID         = UUID.fromString( "00001101-0000-1000-8000-00805f9b34fb" );

  // Android: gewünschte BT Geräteklasse
  public static final int SPX_BTDEVICE_CLASS            = 0x1F00;

  // ANDROID: Verbindungsstatus BT
  public static final int    CONN_STATE_NONE            = 0;
  public static final int    CONN_STATE_CONNECTING      = 1;
  public static final int    CONN_STATE_CONNECTED       = 2;
  
  // interne Begrenzung für Empfangspuffer
  public static final int    MAXINBUFFER                = 10 * 1024;
  // wie lange wartet der Watchdog auf Schreiben ins Device
  public static final int    WATCHDOG_FOR_WRITEOPS      = 5;
  
  // Buggy Firmware, Temperatur-Lesen, Gradienten-Bug
  public static final String FIRMWARE_2_6_7_7V          = "V2.6.7.7_V";
  public static final String FIRMWARE_2_7V              = "V2.7_V";
  public static final String FIRMWARE_2_7H              = "V2.7_H";
  public static final String FIRMWARE_2_7H_R83CE        = "V2.7_H r83 ce";
  public static final String FIRMWARE_2_7V_R83CE        = "V2.7_V r83 ce";
  
//  public static final int    FW_NOT_SET                 = -1;
//  public static final int    FW_2_6_7_7V                = 0;
//  public static final int    FW_2_7V                    = 1;
//  public static final int    FW_2_7H                    = 2;
  
  // Lizenzstati des SPX42
  public static final int SPX_LICENSE_NOT_SET           = -1;
  public static final int SPX_LICENSE_NITROX            =  0;
  public static final int SPX_LICENSE_NORMOXICTX        =  1;
  public static final int SPX_LICENSE_FULLTX            =  2;
  
  // ANDROID:Verzeichnis für Datenbanken
  public static final String APPROOTDIR                 = "SUBMATIXDatabase";
  // Verzeichnis für Datenbanken
  public static final String DEFAULTDATADIR             = "database";
  public static final String DEFAULTEXPORTDIR           = "export";
  public static final String DATABASE_NAME              = "submatixDatabase.db";
  public static final int DATABASE_VERSION              = 4;
  
  // Messages für SPX 42
  public static final String STX                        = new String( new byte[] { 0x02 } );
  public static final String ETX                        = new String( new byte[] { 0x03 } );
  public static final String FILLER                     = new String( new byte[] { 0x0d, 0x0a } );
  public static final String FILLERCHAR                 = "[\\n\\r]";                                               // Zeichen zum entfernen
  public static final String LOGSELECTOR                = new String ( new byte[] { 0x09 } );
  
  // Kommandos für den SPX
  public static final int SPX_MANUFACTURERS             = 0x01;
  public static final int SPX_FACTORY_NUMBER            = 0x02;
  public static final int SPX_ALIVE                     = 0x03;
  public static final int SPX_APPLICATION_ID            = 0x04; //! Firmwareversion des SPX
  public static final int SPX_DEV_IDENTIFIER            = 0x05; 
  public static final int SPX_DEVSOFTVERSION            = 0x06;
  public static final int SPX_SERIAL_NUMBER             = 0x07; //! Seriennummer des SPX
  public static final int SPX_SER1_FROM_SER0            = 0x08;
  public static final int SPX_DATETIME                  = 0x20; //! Datum und Zeit setzen...
  public static final int SPX_DATE_OSOLETE              = 0x21; //! erledigt
  public static final int SPX_TEMPSTICK                 = 0x22;
  public static final int SPX_HUD                       = 0x23; //! HUD Status senden
  public static final int SPX_UBAT                      = 0x24; //! UBAT anfordern auswerten
  public static final int SPX_IO_STATUS                 = 0x25;
  public static final int SPX_CAL_CO2                   = 0x25; //! CO2 Kalibrierung
  public static final int SPX_CAL_CO2_IS_CALIBRATED     = 0x27; //! CO2 Flag ob kalibriert wurde
  public static final int SPX_DEBUG_DEPTH               = 0x28;
  public static final int SPX_SET_SETUP_DEKO            = 0x29; //! Bluetoothkommunikation, setzen der Dekodaten
  public static final int SPX_SET_SETUP_SETPOINT        = 0x30; //! Einstellung des Setpoints (Bluetooth)
  public static final int SPX_SET_SETUP_DISPLAYSETTINGS = 0x31; //! Displayeinstellungen setzen (Bluetooth)
  public static final int SPX_SET_SETUP_UNITS           = 0x32; //! Einheiten setzen (Bluetooth)
  public static final int SPX_SET_SETUP_INDIVIDUAL      = 0x33; //! Individualsettings (Bluetooth)
  public static final int SPX_GET_SETUP_DEKO            = 0x34; //! Dekodaten senden (Bluetooth)
  public static final int SPX_GET_SETUP_SETPOINT        = 0x35; //! Setpointdaten senden (Bluetooth)
  public static final int SPX_GET_SETUP_DISPLAYSETTINGS = 0x36; //! Displayeinstellungen senden (Bluetooth)
  public static final int SPX_GET_SETUP_UNITS           = 0x37; //! Einheiten senden (Bluetooth)
  public static final int SPX_GET_SETUP_INDIVIDUAL      = 0x38; //! Individualeinstellungen senden (Bluetooth)
  public static final int SPX_GET_SETUP_GASLIST         = 0x39; //! Gasliste senden (Bluetooth)
  public static final int SPX_SET_SETUP_GASLIST         = 0x40; //! Gasliste setzen (Bluetooth)
  public static final int SPX_GET_LOG_INDEX             = 0x41; //! Logbuch index senden (Bluetooth)
  public static final int SPX_GET_LOG_NUMBER            = 0x42; //! Logbuch senden (Bluetooth)
  public static final int SPX_GET_LOG_NUMBER_SE         = 0x43; //! Logbuch senden START/ENDE (Bluetooth)
  public static final int SPX_GET_DEVICE_OFF            = 0x44; //! Flag ob Device aus den Syncmode gegangen ist
  public static final int SPX_SEND_FILE                 = 0x45; //! Sende ein File
  public static final int SPX_LICENSE_STATE             = 0x45; //! Lizenz Status zurückgeben!
  public static final int SPX_GET_LIC_STATUS            = 0x46; //! Lizenzstatus senden (Bluetooth)
  public static final int SPX_GET_LOG_NUMBER_DETAIL     = 0x47; //! Logdatei senden
  public static final int SPX_GET_LOG_NUMBER_DETAIL_OK  = 0x48; //! Logdatei senden OK/ENDE
  //
  public static final String IS_END_LOGLISTENTRY        = ":41";
  
  // Temstick Versionen ab Version FIRMWARE_2_7H_R83CE
  public static final int SPX_TEMPSTICK_T1              = 0;
  public static final int SPX_TEMPSTICK_T2              = 1;
  public static final int SPX_TEMPSTICK_T3              = 2;
  
  // Einheiten default(wie gespeichert)/metrisch umrechnen/imperial umrechnen
  public static final int  UNITS_DEFAULT                = 0;
  public static final int  UNITS_METRIC                 = 1;
  public static final int  UNITS_IMPERIAL               = 2;

  // Zeitformat Voreinstelling
  public static final int TIMEFORMAT_ISO                = 1;  // ISO Date = 'YYYY-MM-DD hh:mm:ss'
  public static final int TIMEFORMAT_DE                 = 2;
  public static final int TIMEFORMAT_EN                 = 2;  //EnglishDate ='MM/DD/YYYY hh:mm:ss';
  
  // Android: Farben der Gase
  public static final int GASNAMECOLOR_NORMAL           = Color.blue( 0x88 ); 
  public static final int GASNAMECOLOR_DANGEROUS        = Color.RED;
  public static final int GASNAMECOLOR_NONORMOXIC       = Color.MAGENTA;
  
  // Android: Parameterbezeichnug für Programmmenü-Id
  public static final String ARG_ITEM_ID                = "de.dmarcini.submatix.progitem_item_id";
  public static final String ARG_ITEM_CONTENT           = "de.dmarcini.submatix.progitem_item_content";
  
  // Android: ID für Intentrequests
  public static final int REQUEST_ENABLE_BT             = 1;
  public static final int REQUEST_SPX_PREFS             = 2;
  
  // Android (für PC MUSS die Klasse auskommentiert werden )
  public static final class ActionEvent
  {
    static final int RESERVED_ID_MAX = 0;
  }
  
  // Message Bezeichnungen
  public static final int    MESSAGE_NONE               = ActionEvent.RESERVED_ID_MAX + 1;
  public static final int    MESSAGE_TICK               = ActionEvent.RESERVED_ID_MAX + 2;
  public static final int    MESSAGE_DIALOG_POSITIVE    = ActionEvent.RESERVED_ID_MAX + 3;
  public static final int    MESSAGE_DIALOG_NEGATIVE    = ActionEvent.RESERVED_ID_MAX + 4;
  public static final int    MESSAGE_CONNECTING         = ActionEvent.RESERVED_ID_MAX + 5;
  public static final int    MESSAGE_CONNECTED          = ActionEvent.RESERVED_ID_MAX + 6;
  public static final int    MESSAGE_DISCONNECTED       = ActionEvent.RESERVED_ID_MAX + 7;
  public static final int    MESSAGE_CONNECTERROR       = ActionEvent.RESERVED_ID_MAX + 8;
  public static final int    MESSAGE_SERIAL_READ        = ActionEvent.RESERVED_ID_MAX + 9;
  public static final int    MESSAGE_MANUFACTURER_READ  = ActionEvent.RESERVED_ID_MAX + 10;
  public static final int    MESSAGE_SPXALIVE           = ActionEvent.RESERVED_ID_MAX + 11;
  public static final int    MESSAGE_COMMTIMEOUT        = ActionEvent.RESERVED_ID_MAX + 12;
  public static final int    MESSAGE_FWVERSION_READ     = ActionEvent.RESERVED_ID_MAX + 13;
  public static final int    MESSAGE_SETPOINT_READ      = ActionEvent.RESERVED_ID_MAX + 14;
  public static final int    MESSAGE_SETPOINT_ACK       = ActionEvent.RESERVED_ID_MAX + 15;
  public static final int    MESSAGE_LICENSE_STATE_READ = ActionEvent.RESERVED_ID_MAX + 16;
  public static final int    MESSAGE_DECO_READ          = ActionEvent.RESERVED_ID_MAX + 17;
  public static final int    MESSAGE_DECO_ACK           = ActionEvent.RESERVED_ID_MAX + 18;
  public static final int    MESSAGE_DISPLAY_READ       = ActionEvent.RESERVED_ID_MAX + 19;
  public static final int    MESSAGE_DISPLAY_ACK        = ActionEvent.RESERVED_ID_MAX + 20;
  public static final int    MESSAGE_UNITS_READ         = ActionEvent.RESERVED_ID_MAX + 21;
  public static final int    MESSAGE_UNITS_ACK          = ActionEvent.RESERVED_ID_MAX + 22;
  public static final int    MESSAGE_INDIVID_READ       = ActionEvent.RESERVED_ID_MAX + 23;
  public static final int    MESSAGE_INDIVID_ACK        = ActionEvent.RESERVED_ID_MAX + 24;
  public static final int    MESSAGE_GAS_READ           = ActionEvent.RESERVED_ID_MAX + 25;
  public static final int    MESSAGE_GAS_ACK            = ActionEvent.RESERVED_ID_MAX + 26;
  public static final int    MESSAGE_DEVALIAS_SET       = ActionEvent.RESERVED_ID_MAX + 27;
  public static final int    MESSAGE_DIRENTRY_READ      = ActionEvent.RESERVED_ID_MAX + 28;
  public static final int    MESSAGE_DIRENTRY_END       = ActionEvent.RESERVED_ID_MAX + 29;
  public static final int    MESSAGE_LOGENTRY_START     = ActionEvent.RESERVED_ID_MAX + 30;
  public static final int    MESSAGE_LOGENTRY_LINE      = ActionEvent.RESERVED_ID_MAX + 31;
  public static final int    MESSAGE_LOGENTRY_STOP      = ActionEvent.RESERVED_ID_MAX + 32;
  
  //
//  public static final int    MESSAGE_STATE_CHANGE       = ActionEvent.RESERVED_ID_MAX + 2;
//  public static final int    MESSAGE_READ               = ActionEvent.RESERVED_ID_MAX + 3;
//  public static final int    MESSAGE_WRITE              = ActionEvent.RESERVED_ID_MAX + 4;
//  public static final int    MESSAGE_DEVICE_NAME        = ActionEvent.RESERVED_ID_MAX + 5;
//  public static final int    MESSAGE_TOAST              = ActionEvent.RESERVED_ID_MAX + 6;
//  public static final int    MESSAGE_KDO45_READ         = ActionEvent.RESERVED_ID_MAX + 13;
//  public static final int    MESSAGE_LOGDIRFROMCACHE    = ActionEvent.RESERVED_ID_MAX + 20;
//  public static final int    MESSAGE_DB_SUCCESS         = ActionEvent.RESERVED_ID_MAX + 21;
//  public static final int    MESSAGE_DB_FAIL            = ActionEvent.RESERVED_ID_MAX + 22;
//  public static final int    MESSAGE_DB_WRITE_WAIT      = ActionEvent.RESERVED_ID_MAX + 23;
//  public static final int    MESSAGE_DB_DELETE_WAIT     = ActionEvent.RESERVED_ID_MAX + 24;
//  public static final int    MESSAGE_BTWAITFOR          = ActionEvent.RESERVED_ID_MAX + 34;
//  public static final int    MESSAGE_BTNODEVCONN        = ActionEvent.RESERVED_ID_MAX + 35;
//  public static final int    MESSAGE_BTAUTHREQEST       = ActionEvent.RESERVED_ID_MAX + 36;
//  public static final int    MESSAGE_BTMESSAGE          = ActionEvent.RESERVED_ID_MAX + 37;
//  public static final int    MESSAGE_PROCESS_NEXT       = ActionEvent.RESERVED_ID_MAX + 39;
//  public static final int    MESSAGE_PROCESS_END        = ActionEvent.RESERVED_ID_MAX + 40;
//  public static final int    MESSAGE_SYCSTAT_OFF        = ActionEvent.RESERVED_ID_MAX + 41;
//  public static final int    MESSAGE_FWNOTSUPPORTED     = ActionEvent.RESERVED_ID_MAX + 44;
  
  
//  // Interne Messages
//  private static final int   INT_MESSAGE_OFFSET         = 100;
//  public static final int    INT_MESSAGE_READ           = INT_MESSAGE_OFFSET + 1;
//  public static final int    INT_LOGENTRY_LINE          = INT_MESSAGE_OFFSET + 2;
//  public static final int    INT_STARTWAITDIAL          = INT_MESSAGE_OFFSET + 3;
//  public static final int    INT_STOPTWAITDIAL          = INT_MESSAGE_OFFSET + 4;
//  public static final int    INT_SETTITLEDIAL           = INT_MESSAGE_OFFSET + 5;

  // DATENBANK
  // Datenbanktabellen
  //
  // Tabelle für die Versionsnummer der Datenbank (bei Updates evtl gebraucht)
  public static final String V_DBVERSION                = "dbversion";
  public static final String V_VERSION                  = "version";
  //
  // Tabelle für Alias und PIN des Gerätes
  public static final String A_TABLE_ALIASES            = "aliases";
  public static final String A_DEVICEID                 = "devid";
  public static final String A_DEVNAME                  = "devname";
  public static final String A_ALIAS                    = "alias";
  public static final String A_MAC                      = "mac";
  public static final String A_SERIAL                   = "serial";
  
  //
  // Tabelle für die Kopfdaten des Tauchgangs
  // Tabelle dive_logs
  // speichert "Kopfdaten" der Logs
  public static final String H_TABLE_DIVELOGS           = "dive_logs";
  public static final String H_DEVICEID                 = "devid";
  public static final String H_DIVEID                   = "dive_id";
  public static final String H_FILEONMOBILE             = "xml_file";
  public static final String H_DIVENUMBERONSPX          = "dive_number";
  public static final String H_FILEONSPX                = "filename";
  public static final String H_DEVICESERIAL             = "device_serial";
  public static final String H_STARTTIME                = "starttime";
  public static final String H_HADSEND                  = "had_send";
  public static final String H_FIRSTTEMP                = "airtemp";
  public static final String H_LOWTEMP                  = "lowesttemp";
  public static final String H_MAXDEPTH                 = "maxdepth";
  public static final String H_SAMPLES                  = "samples";
  public static final String H_DIVELENGTH               = "length";
  public static final String H_UNITS                    = "units";
  public static final String H_NOTES                    = "notes";
  public static final String H_GEO_LON                  = "longitude";
  public static final String H_GEO_LAT                  = "latitude";
  
//  //
//  // Tabelle für die relevanten Daten des Tauchganges
//  // Tabelle logdata
//  public static final String D_TABLE_DIVEDETAIL         = "logdata";
//  public static final String D_DBID                     = "id";
//  public static final String D_DIVEID                   = "dive_id";
//  public static final String D_DEPTH                    = "depth";
//  public static final String D_TEMPERATURE              = "temperature";
//  public static final String D_PPO                      = "ppo";
//  public static final String D_PPO_1                    = "ppo1";
//  public static final String D_PPO_2                    = "ppo2";
//  public static final String D_PPO_3                    = "ppo3";
//  public static final String D_SETPOINT                 = "setpoint";
//  public static final String D_N2                       = "n2";
//  public static final String D_HE                       = "he";
//  public static final String D_NULLTIME                 = "nulltime";
//  public static final String D_DELTATIME                = "deltatime";
//  public static final String D_PRESURE                  = "presure";
//  public static final String D_ACKU                     = "acku";
//  //
//  // Tabelle für Gas-Presets
//  // Tabelle presets
//  public static final String P_TABLE_PRESETS            = "presets";
//  public static final String P_DBID                     = "id";
//  public static final String P_SETNAME                  = "setname";
//  //
//  // Tablelle preset_details
//  public static final String PD_TABLE_PRESETDETAIL      = "presets_details";
//  public static final String PD_DBID                    = "id";
//  public static final String PD_SETID                   = "setid";
//  public static final String PD_GASNR                   = "gasnr";
//  public static final String PD_O2                      = "o2";
//  public static final String PD_HE                      = "he";
//  public static final String PD_DILUENT1                = "dil1";
//  public static final String PD_DILUENT2                = "dil2";
//  public static final String PD_BAILOUT                 = "bailout";
  
  //
  // ANDROID: XML-Dateiversion
  //
  public static final String XML_FILEVERSION            = "V 1.0";  
  //
  // Default Farben für die Graphen in der Loganzeige
  //
  public static int          GRAPH_TEMPERATURE_ACOLOR   = 0xFF0000; // Axenfarbe (Beschriftungen)
  public static int          GRAPH_TEMPERATURE_RCOLOR   = 0xFF0000; // Farbe des Graphen
  public static int          GRAPH_PPO2ALL_ACOLOR       = 0xB300FF;
  public static int          GRAPH_PPO2ALL_RCOLOR       = 0xB300FF;
  public static int          GRAPH_PPO2_01_RCOLOR       = 0x0000FF;
  public static int          GRAPH_PPO2_02_RCOLOR       = 0x0080FF;
  public static int          GRAPH_PPO2_03_RCOLOR       = 0x7ABDFF;
  public static int          GRAPH_SETPOINT_ACOLOR      = 0x000033;
  public static int          GRAPH_SETPOINT_RCOLOR      = 0x000033;
  public static int          GRAPH_INNERTGAS_ACOLOR     = 0x1A2000;
  public static int          GRAPH_HE_RCOLOR            = 0x1A9900;
  public static int          GRAPH_N2_RCOLOR            = 0xFF3DD8;
  public static int          GRAPH_NULLTIME_ACOLOR      = 0x006600;
  public static int          GRAPH_DEPTH_ACOLOR         = 0x4775FF;
  public static int          GRAPH_DEPTH_RCOLOR         = 0x4775FF;
  
  

  //@formatter:on
}
