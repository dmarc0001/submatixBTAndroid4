package de.dmarcini.submatix.android4.full.utils;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.util.Log;

public class SPX42diveSamplesReader
{
  public static final String TAG              = SPX42diveSamplesReader.class.getSimpleName();
  private int                diveTimeCurrent  = 0;
  public static final int    DIVE_TIME        = 0;
  public static final int    DIVE_DEPTH       = 1;
  public static final int    DIVE_TEMPERATURE = 2;
  public static final int    DIVE_PPO2        = 3;
  public static final int    DIVE_ZEROTIME    = 4;
  private Vector<float[]>    sampleVector     = null;

  /**
   * 
   * gesperrter Konstruktor
   * 
   * Project: SubmatixBluethoothLogger Package: de.dmarcini.submatix.logger.utils
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand:01.02.2014
   */
  @SuppressWarnings( "unused" )
  private SPX42diveSamplesReader()
  {}

  /**
   * 
   * Der Konstruktor
   * 
   * Project: SubmatixBluethoothLogger Package: de.dmarcini.submatix.logger.utils
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 01.02.2014
   * @param xmlFile
   * 
   */
  public SPX42diveSamplesReader( File xmlFile )
  {
    sampleVector = new Vector<float[]>();
    makeSamples( xmlFile );
  }

  /**
   * Erzeuge das eigentliche Profil für den Tauchgang
   * 
   * Project: SubmatixXMLTest Package: de.dmarcini.bluethooth.submatix.xml
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 01.02.2014
   * @param doc
   *          Document Objekt
   * @param diveNumber
   *          Nummer des Tauchgangs in der Datenbank
   * @param dAttr
   *          Objekt für Tauchgangseigenschaften
   */
  private void makeSamples( File xmlFile )
  {
    SAXParserFactory spf = null;
    SAXParser sp = null;
    ContentHandler myHandler = null;
    XMLReader xr = null;
    // Liste des Tauchganges machen
    Log.v( TAG, "getDiveList()...scan XML <" + xmlFile.getName() + ">..." );
    // versuchen wir es...
    // SAX Parser erzeugen
    spf = SAXParserFactory.newInstance();
    try
    {
      Log.v( TAG, "getDiveList()...NEW SAX Parser..." );
      sp = spf.newSAXParser();
      xr = sp.getXMLReader();
    }
    catch( ParserConfigurationException ex )
    {
      Log.e( TAG, "getDiveList() => " + ex.getLocalizedMessage() );
      return;
    }
    catch( SAXException ex )
    {
      Log.e( TAG, "getDiveList() => " + ex.getLocalizedMessage() );
      return;
    }
    // einen nagenleuen Handler erzeugen
    // als implizite Klasse
    Log.v( TAG, "getDiveList()...create ContentHandler..." );
    //
    // Lokaler Handler
    //
    myHandler = new ContentHandler() {
      private boolean             readBetween;
      private boolean             scanActive    = false;
      private String              stringBetween = null;
      private final static String rootTag       = "spx42Log";
      private SPX42LogEntryObj    entry         = null;

      @Override
      public void startDocument() throws SAXException
      {
        /* Wird aufgerufen am Anfang des Dokumentes */
        readBetween = false;
        scanActive = false;
        stringBetween = "";
      }

      @Override
      public void endDocument() throws SAXException
      {
        /* Wird aufgerufen am Ende des Dokumentes */
      }

      @Override
      public void startElement( String namespaceURI, String localName, String qName, Attributes atts ) throws SAXException
      {
        // Wird aufgerufen bei einem Start-Tag
        if( localName.equalsIgnoreCase( rootTag ) )
        {
          Log.d( TAG, "PARSER: rootElement <" + localName + "> Opened!" );
          scanActive = true;
          return;
        }
        if( scanActive == false )
        {
          // nix zu tun hier!
          return;
        }
        if( localName.equals( "logEntry" ) )
        {
          // Logeintrag ANFANG
          entry = new SPX42LogEntryObj();
          return;
        }
        if( entry == null )
        {
          Log.e( TAG, "PARSER startElement(): object for entry is null!" );
          return;
        }
        if( localName.equals( "presure" ) )
        {
          readBetween = true;
          return;
        }
        if( localName.equals( "step" ) )
        {
          readBetween = true;
          return;
        }
        if( localName.equals( "depth" ) )
        {
          readBetween = true;
          return;
        }
        if( localName.equals( "temp" ) )
        {
          readBetween = true;
          return;
        }
        if( localName.equals( "acku" ) )
        {
          readBetween = true;
          return;
        }
        if( localName.equals( "ppo2" ) )
        {
          readBetween = true;
          return;
        }
        if( localName.equals( "n2" ) )
        {
          readBetween = true;
          return;
        }
        if( localName.equals( "he" ) )
        {
          readBetween = true;
          return;
        }
        if( localName.equals( "setpoint" ) )
        {
          readBetween = true;
          return;
        }
        if( localName.equals( "zerotime" ) )
        {
          readBetween = true;
          return;
        }
      }

      @Override
      public void endElement( String uri, String localName, String qName ) throws SAXException
      {
        // root Tag geschlossen? Dann ist Finito
        if( localName.equalsIgnoreCase( rootTag ) )
        {
          Log.d( TAG, "PARSER: rootElement <" + localName + "> Closed!" );
          scanActive = false;
          return;
        }
        if( entry == null )
        {
          Log.e( TAG, "PARSER endElement(): object for entry is null!" );
          return;
        }
        if( localName.equals( "logEntry" ) )
        {
          // Logeintrag ENDE
          if( entry.whereAlDataThere() )
          {
            // Jetz einen Eintrag in das Vector-Teil
            Log.v( TAG, "create new LogSample..." );
            float[] entArr = new float[5];
            entArr[DIVE_TIME] = entry.time;
            entArr[DIVE_DEPTH] = ( float )entry.depth;
            entArr[DIVE_TEMPERATURE] = ( float )entry.temp;
            entArr[DIVE_PPO2] = ( float )entry.ppo2;
            entArr[DIVE_ZEROTIME] = entry.zerotime;
            // und papp den dran
            sampleVector.add( entArr );
          }
          else
          {
            Log.e( TAG, "PARSER endElement(): entry is FAILED. END" );
          }
          return;
        }
        try
        {
          if( localName.equals( "presure" ) )
          {
            readBetween = false;
            entry.presure = Integer.parseInt( stringBetween );
            stringBetween = "";
            return;
          }
          if( localName.equals( "step" ) )
          {
            readBetween = false;
            diveTimeCurrent += Integer.parseInt( stringBetween );
            entry.time = diveTimeCurrent;
            stringBetween = "";
            return;
          }
          if( localName.equals( "depth" ) )
          {
            readBetween = false;
            entry.depth = ( Float.parseFloat( stringBetween ) / 10 );
            stringBetween = "";
            return;
          }
          if( localName.equals( "temp" ) )
          {
            readBetween = false;
            entry.temp = ( Float.parseFloat( stringBetween ) );
            stringBetween = "";
            return;
          }
          if( localName.equals( "acku" ) )
          {
            readBetween = false;
            entry.acku = Float.parseFloat( stringBetween );
            stringBetween = "";
            return;
          }
          if( localName.equals( "ppo2" ) )
          {
            readBetween = false;
            entry.ppo2 = Float.parseFloat( stringBetween );
            stringBetween = "";
            return;
          }
          if( localName.equals( "n2" ) )
          {
            readBetween = false;
            entry.n2 = ( Float.parseFloat( stringBetween ) / 100.0 );
            stringBetween = "";
            return;
          }
          if( localName.equals( "he" ) )
          {
            readBetween = false;
            entry.he = ( Float.parseFloat( stringBetween ) / 100.0 );
            stringBetween = "";
            return;
          }
          if( localName.equals( "setpoint" ) )
          {
            readBetween = false;
            entry.setpoint = ( Float.parseFloat( stringBetween ) / 10.0 );
            stringBetween = "";
            return;
          }
          if( localName.equals( "zerotime" ) )
          {
            readBetween = false;
            entry.zerotime = Integer.parseInt( stringBetween );
            stringBetween = "";
            return;
          }
        }
        catch( NumberFormatException ex )
        {
          Log.e( TAG, "PARSER: <" + ex.getLocalizedMessage() + ">" );
          return;
        }
      }

      @Override
      public void characters( char ch[], int start, int length ) throws SAXException
      {
        // Wird aufgerufen für den Content zwischen Start und End-Tag
        String text = new String( ch, start, length );
        if( readBetween )
        {
          stringBetween += text;
        }
      }

      @Override
      public void endPrefixMapping( String prefix ) throws SAXException
      {}

      @Override
      public void ignorableWhitespace( char[] ch, int start, int length ) throws SAXException
      {}

      @Override
      public void processingInstruction( String target, String data ) throws SAXException
      {}

      @Override
      public void setDocumentLocator( Locator locator )
      {}

      @Override
      public void skippedEntity( String name ) throws SAXException
      {}

      @Override
      public void startPrefixMapping( String prefix, String uri ) throws SAXException
      {}
    };
    // Jetzt parse mal lustig
    try
    {
      xr.setContentHandler( myHandler );
      xr.parse( new InputSource( "file:" + xmlFile.getAbsolutePath() ) );
    }
    catch( SAXException ex )
    {
      Log.e( TAG, "getDiveList() => : " + ex.getLocalizedMessage() );
      return;
    }
    catch( IOException ex )
    {
      Log.e( TAG, "getDiveList() => : " + ex.getLocalizedMessage() );
      return;
    }
    return;
  }

  /**
   * 
   * Gib das Tauchprofil für den Tauchgang zurück
   * 
   * Project: SubmatixBluethoothLogger Package: de.dmarcini.submatix.logger.utils
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 21.02.2012
   * @return 01.02.2014
   */
  public Vector<float[]> getDiveSamples()
  {
    return( sampleVector );
  }
}
