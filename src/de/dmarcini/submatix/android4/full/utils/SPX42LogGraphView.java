package de.dmarcini.submatix.android4.full.utils;

import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.View.OnTouchListener;
import de.dmarcini.submatix.android4.full.R;

/**
 * 
 * View, welches die Logdaten eines Tauchganges anzeigt.
 * 
 * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.full.utils
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 * 
 *         Stand: 02.02.2014
 */
public class SPX42LogGraphView extends View implements OnTouchListener
{
  @SuppressWarnings( "javadoc" )
  public static final String       TAG                = SPX42LogGraphView.class.getSimpleName();
  private final Paint              pnt                = new Paint( Paint.ANTI_ALIAS_FLAG );
  private int                      currentAppstyle    = R.style.AppDarkTheme;
  private final SPX42LogColorClass colorClass         = new SPX42LogColorClass();
  private ScaleGestureDetector     scaleDetector      = null;
  private Vector<float[]>          sampleVector       = null;
  private int                      viewWidth          = 1;
  private int                      viewHeight         = 1;
  private final static float       margin_top         = 20;
  private final static float       margin_bottom      = 30;
  private final static float       margin_bottom_ox   = 55;
  private final static float       margin_bottom_temp = 75;
  private final static float       countTimeMark      = 6;
  private float                    masterScale        = 0.0F;
  private float                    sampleOffset       = 0F;
  private float                    startTouchMove     = 0F;

  /**
   * 
   * Private, interne Klasse für die Multitouch-Skalierung
   * 
   * Project: SubmatixBluethoothLogger Package: de.dmarcini.submatix.logger.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 18.02.2012
   */
  private class MyScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener
  {
    @Override
    public boolean onScale( ScaleGestureDetector detector )
    {
      masterScale *= detector.getScaleFactor();
      Log.d( TAG, String.format( "Skalierung: %03.2f", masterScale ) );
      invalidate();
      return true;
    }
  }

  /**
   * 
   * Konstruktor
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.full.gui
   * 
   * Stand: 01.02.2014
   * 
   * @param context
   */
  public SPX42LogGraphView( Context context )
  {
    super( context );
    sampleVector = null;
    initColors();
    initLogView( context );
    setOnTouchListener( this );
  }

  /**
   * 
   * Konstruktor
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.full.gui
   * 
   * Stand: 01.02.2014
   * 
   * @param context
   * @param attrs
   */
  public SPX42LogGraphView( Context context, AttributeSet attrs )
  {
    super( context, attrs );
    initColors();
    initLogView( context );
    setOnTouchListener( this );
  }

  /**
   * 
   * Konstruktor
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.full.gui
   * 
   * Stand: 01.02.2014
   * 
   * @param context
   * @param attrs
   * @param defStyle
   */
  public SPX42LogGraphView( Context context, AttributeSet attrs, int defStyle )
  {
    super( context, attrs, defStyle );
    initColors();
    initLogView( context );
    setOnTouchListener( this );
  }

  /**
   * 
   * Farben initialisieren
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.full.utils
   * 
   * Stand: 02.02.2014
   */
  private void initColors()
  {
    Resources res = getResources();
    if( currentAppstyle == R.style.AppDarkTheme )
    {
      colorClass.spxGraphBackcolor = res.getColor( R.color.spxGraphBackcolorDark );
      colorClass.spxGraphDepthfill = res.getColor( R.color.spxGraphDepthfillDark );
      colorClass.spxGraphDepthborder = res.getColor( R.color.spxGraphDepthborderDark );
      colorClass.spxGraphOuterZoombar = res.getColor( R.color.spxGraphOuterZoombarDark );
      colorClass.spxGraphInnerZoombar = res.getColor( R.color.spxGraphInnerZoombarDark );
      colorClass.spxGraphPpo2Line = res.getColor( R.color.spxGraphPpo2LineDark );
      colorClass.spxGraphPpo2Scale = res.getColor( R.color.spxGraphPpo2ScaleDark );
      colorClass.spxGraphDepthscale = res.getColor( R.color.spxGraphDepthscaleDark );
      colorClass.spxGraphTimeline = res.getColor( R.color.spxGraphTimelineDark );
      colorClass.spxGraphTemperatureLine = res.getColor( R.color.spxGraphTemperatureLineDark );
      colorClass.spxGraphTemperatureScale = res.getColor( R.color.spxGraphTemperatureScaleDark );
    }
    else
    {
      colorClass.spxGraphBackcolor = getResources().getColor( R.color.spxGraphBackcolorLight );
      colorClass.spxGraphDepthfill = res.getColor( R.color.spxGraphDepthfillLight );
      colorClass.spxGraphDepthborder = res.getColor( R.color.spxGraphDepthborderLight );
      colorClass.spxGraphOuterZoombar = res.getColor( R.color.spxGraphOuterZoombarLight );
      colorClass.spxGraphInnerZoombar = res.getColor( R.color.spxGraphInnerZoombarLight );
      colorClass.spxGraphPpo2Line = res.getColor( R.color.spxGraphPpo2LineLight );
      colorClass.spxGraphPpo2Scale = res.getColor( R.color.spxGraphPpo2ScaleLight );
      colorClass.spxGraphDepthscale = res.getColor( R.color.spxGraphDepthscaleLight );
      colorClass.spxGraphTimeline = res.getColor( R.color.spxGraphTimelineLight );
      colorClass.spxGraphTemperatureLine = res.getColor( R.color.spxGraphTemperatureLineLight );
      colorClass.spxGraphTemperatureScale = res.getColor( R.color.spxGraphTemperatureScaleLight );
    }
  }

  /**
   * 
   * Das Element initialisieren
   * 
   * Project: CanvasPrintSample Package: de.dmarcini.android.canvas_sample
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 01.02.2014
   */
  private void initLogView( Context context )
  {
    scaleDetector = new ScaleGestureDetector( context, new MyScaleListener() );
    masterScale = 0.0F;
    sampleOffset = -20.0F;
  }

  /**
   * Wird immer aufgerufen, wenn das System meint, es muss was nachgemalt werden
   */
  @Override
  public void onDraw( Canvas canvas )
  {
    float[] depthProfil = null;
    float[] oneProfil = null;
    super.onDraw( canvas );
    //
    // Hintergrund zeichnen
    //
    pnt.setColor( Color.BLACK );
    pnt.setStyle( Style.FILL );
    canvas.drawColor( colorClass.spxGraphBackcolor );
    //
    // gibt es ein Profil?
    //
    if( sampleVector == null ) return;
    //
    // muß die Skalierung initialisiert werden?
    //
    if( masterScale == 0.0F )
    {
      masterScale = viewWidth / sampleVector.size();
      sampleOffset = -20.0F;
    }
    //
    // Profildaten holen
    //
    depthProfil = getIndexProfil( sampleVector, SPX42diveSamplesReader.DIVE_DEPTH );
    if( sampleOffset < -10.0F ) sampleOffset = -10.0F;
    if( sampleOffset > depthProfil.length )
    {
      sampleOffset = depthProfil.length - ( ( depthProfil.length / 100 ) * 10 );
    }
    //
    // Zoombar als Orientierung zeichnen
    //
    drawZoomBar( canvas, depthProfil );
    //
    // Tiefenprofil zeichnen
    //
    drawDepthPath( canvas, depthProfil );
    depthProfil = null;
    //
    // PPO2 zeigen
    //
    oneProfil = getIndexProfil( sampleVector, SPX42diveSamplesReader.DIVE_PPO2 );
    drawOxigenLine( canvas, oneProfil );
    oneProfil = null;
    //
    // Temperaturkurve zeichnen
    //
    oneProfil = getIndexProfil( sampleVector, SPX42diveSamplesReader.DIVE_TEMPERATURE );
    drawTemperatureLine( canvas, oneProfil );
    oneProfil = null;
    //
    // Zeitleiste zeichnen
    //
    oneProfil = getIndexProfil( sampleVector, SPX42diveSamplesReader.DIVE_TIME );
    drawTimeLine( canvas, oneProfil );
  }

  /**
   * 
   * Die Übersicht zeichnen
   * 
   * Project: SubmatixBluethoothLogger Package: de.dmarcini.submatix.logger.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 01.02.2014
   * @param canvas
   * @param origProfil
   * 
   */
  private void drawZoomBar( Canvas canvas, float[] origProfil )
  {
    Path bar_outer = new Path();
    Path bar_inner = new Path();
    float barStart;
    float barLength;
    Paint zbPnt = new Paint( Paint.ANTI_ALIAS_FLAG );
    // Berechne, wo der gezeigte Bereich anfängt
    if( sampleOffset <= 0.0F )
    {
      barStart = 0.0F;
    }
    {
      barStart = ( viewWidth * sampleOffset ) / origProfil.length;
      if( barStart < 0.0F )
      {
        barStart = 0.0F;
      }
    }
    // an rechten Rand trotzdem nicht kleiner als 15 Einheiten werden lassen!
    if( barStart > viewWidth - 15.0F )
    {
      barStart = viewWidth - 15.0F;
    }
    // Berechne, wie lang die Bar sein muß
    barLength = ( ( viewWidth * viewWidth ) / ( origProfil.length * masterScale ) );
    // Die Bar soll nicht zu kurz werden!
    if( barLength < 10.0F )
    {
      barLength = 10.0F;
    }
    // Wird der darzustelende Bereich überschritten, abschneiden...
    if( barStart + barLength > viewWidth )
    {
      barLength = viewWidth - barStart;
    }
    // hier gehts los, Ausgangspunkt erstellen und Hintergrund malen
    bar_outer.moveTo( 0, 0 );
    bar_outer.lineTo( viewWidth, 0 );
    bar_outer.lineTo( viewWidth, margin_top );
    bar_outer.lineTo( 0, margin_top );
    bar_outer.close();
    // Bar (Indikator) zeichnen
    bar_inner.moveTo( barStart, 1 );
    bar_inner.lineTo( barStart + barLength, 1 );
    bar_inner.lineTo( barStart + barLength, margin_top - 1 );
    bar_inner.lineTo( barStart, margin_top - 1 );
    bar_inner.close();
    // Zeichne den Mist
    zbPnt.setColor( colorClass.spxGraphOuterZoombar );
    zbPnt.setStyle( Style.FILL );
    canvas.drawPath( bar_outer, zbPnt );
    zbPnt.setColor( colorClass.spxGraphInnerZoombar );
    zbPnt.setStrokeWidth( 2.0F );
    zbPnt.setStyle( Style.FILL );
    canvas.drawPath( bar_inner, zbPnt );
  }

  /**
   * 
   * Das Tiefenprofil zeichnen
   * 
   * Project: CanvasPrintSample Package: de.dmarcini.android.canvas_sample
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 01.02.2014
   * @param canvas
   * @param origProfil
   */
  private void drawDepthPath( Canvas canvas, float[] origProfil )
  {
    float currX, currY;
    float depthFactor;
    float maxValue;
    float maxTextPosY;
    int samplePos = 0;
    //
    maxValue = getMaxValue( origProfil );
    // Die Tiefe muß ich nun noch skalieren, passend auf den Schirm
    depthFactor = ( viewHeight - margin_top - margin_bottom - 30 ) / maxValue;
    Log.v( TAG, String.format( "drawDepthPath() -> max depth value: <%03.1f>, depthFactor: <%03.2f>", maxValue, depthFactor ) );
    // wo kommt die Legende für max-tiefe hin?
    maxTextPosY = ( maxValue * depthFactor ) + margin_top;
    // Pfad erstellen
    Path pathDepth = new Path();
    // hier gehts los, Ausgangspunkt erstellen
    pathDepth.moveTo( 0, margin_top );
    currX = 0;
    samplePos = ( int )sampleOffset;
    while( ( currX <= viewWidth ) && samplePos < 0 )
    {
      pathDepth.lineTo( currX, margin_top );
      // nächster Schritt
      currX += masterScale;
      samplePos++;
    }
    pathDepth.lineTo( currX, margin_top );
    while( ( currX < viewWidth ) && ( samplePos < origProfil.length ) )
    {
      // Y ist Wert in Metern, skaliert auf Display
      currY = ( depthFactor * origProfil[samplePos] ) + margin_top;
      pathDepth.lineTo( currX, currY );
      // nächster Schritt
      currX += masterScale;
      samplePos++;
    }
    if( samplePos < origProfil.length )
    {
      try
      {
        currY = ( depthFactor * origProfil[samplePos] ) + margin_top;
        pathDepth.lineTo( currX, currY );
      }
      catch( ArrayIndexOutOfBoundsException ex )
      {}
    }
    pathDepth.lineTo( currX, margin_top );
    // Pfad schließen
    pathDepth.lineTo( 0, margin_top );
    pathDepth.close();
    //
    // Pfade zeichnen
    //
    pnt.setColor( colorClass.spxGraphDepthborder );
    pnt.setStyle( Style.STROKE );
    pnt.setStrokeWidth( 2.0F );
    canvas.drawPath( pathDepth, pnt );
    pnt.setStrokeWidth( 1.0F );
    pnt.setColor( colorClass.spxGraphDepthfill );
    pnt.setStyle( Style.FILL );
    canvas.drawPath( pathDepth, pnt );
    //
    // Tiefenskala machen
    //
    Path pathDepthScale = new Path();
    // Tiefenlinien machen
    pnt.setStrokeWidth( 0.5F );
    pnt.setColor( colorClass.spxGraphDepthscale );
    pnt.setTextSize( 20 );
    pnt.setStyle( Style.FILL );
    pnt.setTextAlign( Align.LEFT );
    if( maxValue < 5.0F )
    {
      float dLine = 0.5F;
      while( dLine < maxValue )
      {
        currY = ( dLine * depthFactor ) + margin_top;
        canvas.drawLine( 0, currY, viewWidth, currY, pnt );
        if( currY < ( maxTextPosY - 20 ) )
        {
          canvas.drawText( String.format( "%02.1fm", dLine ), 30, currY, pnt );
        }
        dLine += 0.5F;
      }
    }
    else if( maxValue < 10.0F )
    {
      float dLine = 1.0F;
      while( dLine < maxValue )
      {
        currY = ( dLine * depthFactor ) + margin_top;
        canvas.drawLine( 0, currY, viewWidth, currY, pnt );
        if( currY < ( maxTextPosY - 20 ) )
        {
          canvas.drawText( String.format( "%02.0fm", dLine ), 30, currY, pnt );
        }
        dLine += 1.0F;
      }
    }
    else if( maxValue < 25.0F )
    {
      float dLine = 5.0F;
      while( dLine < maxValue )
      {
        currY = ( dLine * depthFactor ) + margin_top;
        canvas.drawLine( 0, currY, viewWidth, currY, pnt );
        if( currY < ( maxTextPosY - 20 ) )
        {
          canvas.drawText( String.format( "%02.0fm", dLine ), 30, currY, pnt );
        }
        dLine += 5.0F;
      }
    }
    else
    {
      float dLine = 10.0F;
      while( dLine < maxValue )
      {
        currY = ( dLine * depthFactor ) + margin_top;
        canvas.drawLine( 0, currY, viewWidth, currY, pnt );
        if( currY < ( maxTextPosY - 20 ) )
        {
          canvas.drawText( String.format( "%02.0fm", dLine ), 30, currY, pnt );
        }
        dLine += 10.0F;
      }
    }
    // Grundlinie
    pathDepthScale.moveTo( 13, margin_top + 2 );
    pathDepthScale.lineTo( 27, margin_top + 2 );
    // Skala
    pathDepthScale.moveTo( 20, margin_top + 2 );
    pathDepthScale.lineTo( 20, viewHeight - margin_bottom - margin_top + 10 );
    // Endpfeil
    pathDepthScale.lineTo( 15, viewHeight - margin_bottom - margin_top + 10 - 15 );
    pathDepthScale.moveTo( 20, viewHeight - margin_bottom - margin_top + 10 );
    pathDepthScale.lineTo( 25, viewHeight - margin_bottom - margin_top + 10 - 15 );
    // Max-Markierung
    pathDepthScale.moveTo( 13, ( maxValue * depthFactor ) + margin_top );
    pathDepthScale.lineTo( 27, ( maxValue * depthFactor ) + margin_top );
    // Zeichnen
    pnt.setStrokeWidth( 2.5F );
    pnt.setColor( colorClass.spxGraphDepthscale );
    pnt.setStyle( Style.STROKE );
    canvas.drawPath( pathDepthScale, pnt );
    // skalenbezeichnung
    pnt.setTextSize( 20 );
    pnt.setStyle( Style.FILL );
    canvas.drawText( "0", 30, margin_top + pnt.getTextSize(), pnt );
    canvas.drawText( String.format( "%02.1fm", maxValue ), 30, ( maxValue * depthFactor ) + margin_top, pnt );
  }

  /**
   * 
   * Zeichne die Zeitleiste
   * 
   * Project: SubmatixBluethoothLogger Package: de.dmarcini.submatix.logger.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 22.02.2012
   * @param canvas
   * @param oneProfil
   *          01.02.2014
   */
  private void drawTimeLine( Canvas canvas, float[] oneProfil )
  {
    int timeStep = ( int )oneProfil[0];
    int diveLength = getDiveLength( oneProfil );
    float timeFactor = ( ( 100 * viewWidth ) / oneProfil.length ) * masterScale;
    int timeLineTimeMark;
    float markScale;
    float currX, currY;
    int samplePos;
    //
    markScale = viewWidth / countTimeMark;
    Log.v( TAG, String.format( "drawTimeLine() -> divelength: <%d> secounds, timeStep: <%d> secounds, timeFactor: <%03.2f>", diveLength, timeStep, timeFactor ) );
    pnt.setColor( colorClass.spxGraphTimeline );
    pnt.setTextSize( 20 );
    pnt.setTextAlign( Align.LEFT );
    pnt.setStyle( Style.STROKE );
    pnt.setStrokeWidth( 2.5F );
    Path pathTimeLine = new Path();
    //
    // Zeitstrahl/Grundlinie
    //
    currY = viewHeight - margin_bottom + 15;
    pathTimeLine.moveTo( 10, currY - 7 );
    pathTimeLine.lineTo( 10, currY + 7 );
    // Strahl
    pathTimeLine.moveTo( 10, currY );
    pathTimeLine.lineTo( viewWidth - 10, currY );
    // Pfeil
    pathTimeLine.lineTo( viewWidth - 25, currY - 7 );
    pathTimeLine.moveTo( viewWidth - 10, currY );
    pathTimeLine.lineTo( viewWidth - 25, currY + 7 );
    canvas.drawPath( pathTimeLine, pnt );
    //
    // Skala anbringen
    //
    // Anfang bei
    pnt.setStyle( Style.FILL );
    pnt.setTextAlign( Align.LEFT );
    currX = 0;
    samplePos = ( int )sampleOffset;
    if( samplePos >= 0 )
    {
      // den Start der Timeline kennzeichnen, wenn der > 0 ist
      timeLineTimeMark = ( int )( sampleOffset * timeStep );
      canvas.drawText( getTimeString( timeLineTimeMark ), 12, currY - 6, pnt );
    }
    pnt.setTextAlign( Align.CENTER );
    //
    // Vorarbeiten bis zum Anfang, falls Offset im Minus...
    //
    float markScaleCount = 0;
    while( ( currX <= viewWidth ) && samplePos < 0 )
    {
      currX += masterScale;
      samplePos++;
    }
    if( samplePos == 0 )
    {
      canvas.drawLine( currX, currY - 3, currX, currY + 3, pnt );
      canvas.drawText( "0", currX, currY - 6, pnt );
    }
    // Weiter im Bereich des Tauchganges
    while( ( currX < viewWidth ) && ( samplePos < oneProfil.length ) )
    {
      // nächster Schritt
      currX += masterScale;
      // zähle den abstand mit
      markScaleCount += masterScale;
      if( markScaleCount >= markScale )
      {
        // Neuer Start...
        markScaleCount = 0;
        canvas.drawLine( currX, currY - 3, currX, currY + 3, pnt );
        timeLineTimeMark = ( samplePos * timeStep );
        canvas.drawText( getTimeString( timeLineTimeMark ), currX, currY - 6, pnt );
      }
      samplePos++;
    }
  }

  /**
   * 
   * Zeichne die PPO2-Linie
   * 
   * Project: SubmatixBluethoothLogger Package: de.dmarcini.submatix.logger.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 01.02.2014
   * @param canvas
   * @param oxyProfil
   * 
   */
  private void drawOxigenLine( Canvas canvas, float[] oxyProfil )
  {
    float currX, currY;
    float oxygenFactor;
    float maxOxigen, maxOxigenAbsolute;
    int samplePos = 0;
    int graphColor = colorClass.spxGraphPpo2Line;
    int scaleColor = colorClass.spxGraphPpo2Scale;
    maxOxigen = getMaxValue( oxyProfil );
    // Die PPO2 muß ich nun noch skalieren, passend auf den Schirm und verkleinert
    oxygenFactor = ( viewHeight - margin_bottom_ox - ( viewHeight / 4 ) ) / maxOxigen;
    Log.v( TAG, String.format( "drawOxigenLine() -> max oxy value: <%03.1f>, oxigenFactor: <%03.2f>", maxOxigen, oxygenFactor ) );
    // Pfad erstellen
    Path pathOx = new Path();
    // hier gehts los, Ausgangspunkt erstellen
    currX = 0;
    samplePos = ( int )sampleOffset;
    while( ( currX <= viewWidth ) && samplePos < 0 )
    {
      // nächster Schritt
      currX += masterScale;
      samplePos++;
    }
    try
    {
      pathOx.moveTo( currX, viewHeight - ( oxygenFactor * oxyProfil[samplePos] ) - margin_bottom_ox );
    }
    catch( ArrayIndexOutOfBoundsException ex )
    {}
    ;
    while( ( currX <= viewWidth ) && ( samplePos < oxyProfil.length ) )
    {
      // Y ist Wert in Metern, skaliert auf Display
      currY = viewHeight - ( oxygenFactor * oxyProfil[samplePos] ) - margin_bottom_ox;
      pathOx.lineTo( currX, currY );
      // nächster Schritt
      currX += masterScale;
      samplePos++;
    }
    if( samplePos < oxyProfil.length )
    {
      try
      {
        currY = viewHeight - ( oxygenFactor * oxyProfil[samplePos] ) - margin_bottom_ox;
        pathOx.lineTo( currX, currY );
      }
      catch( ArrayIndexOutOfBoundsException ex )
      {}
    }
    // PPO2 Pfad zeichnen
    pnt.setColor( graphColor );
    pnt.setStyle( Style.STROKE );
    pnt.setStrokeWidth( 4.0F );
    canvas.drawPath( pathOx, pnt );
    //
    // PPO2 Skala machen
    //
    pnt.setStrokeWidth( 1.0F );
    Path pathPpo2Scale = new Path();
    // Grundlinie
    pathPpo2Scale.moveTo( viewWidth - 13, viewHeight - margin_bottom_ox );
    pathPpo2Scale.lineTo( viewWidth - 27, viewHeight - margin_bottom_ox );
    // Skala
    maxOxigenAbsolute = viewHeight - ( maxOxigen * oxygenFactor ) - margin_bottom_ox;
    pathPpo2Scale.moveTo( viewWidth - 20, viewHeight - margin_bottom_ox );
    pathPpo2Scale.lineTo( viewWidth - 20, maxOxigenAbsolute - 25 );
    // Endpfeil
    pathPpo2Scale.lineTo( viewWidth - 15, maxOxigenAbsolute - 25 + 15 );
    pathPpo2Scale.moveTo( viewWidth - 20, maxOxigenAbsolute - 25 );
    pathPpo2Scale.lineTo( viewWidth - 25, maxOxigenAbsolute - 25 + 15 );
    // Max-Markierung
    pathPpo2Scale.moveTo( viewWidth - 13, maxOxigenAbsolute );
    pathPpo2Scale.lineTo( viewWidth - 27, maxOxigenAbsolute );
    // Zeichnen
    pnt.setStrokeWidth( 2.5F );
    pnt.setStyle( Style.STROKE );
    canvas.drawPath( pathPpo2Scale, pnt );
    //
    float dLine = 0.4F;
    pnt.setTextSize( 20 );
    pnt.setTextAlign( Align.RIGHT );
    pnt.setStyle( Style.FILL );
    while( dLine < maxOxigen )
    {
      currY = viewHeight - ( dLine * oxygenFactor ) - margin_bottom_ox;
      pnt.setColor( graphColor );
      canvas.drawLine( viewWidth - 27, currY, viewWidth - 13, currY, pnt );
      // if( currY > ( maxOxigenAbsolute + 20 ) )
      {
        pnt.setColor( scaleColor );
        canvas.drawText( String.format( "%02.1f bar", dLine ), viewWidth - 30, currY, pnt );
      }
      dLine += 0.2F;
    }
    // skalenbezeichnung
    pnt.setColor( scaleColor );
    pnt.setTextSize( 20 );
    pnt.setTextAlign( Align.RIGHT );
    pnt.setStyle( Style.FILL );
    canvas.drawText( "0 bar", viewWidth - 30, viewHeight - margin_bottom_ox, pnt );
    // Maximum schreiben
    canvas.drawText( String.format( "%02.1f bar", maxOxigen ), viewWidth - 30, maxOxigenAbsolute, pnt );
    pnt.setTextAlign( Align.LEFT );
  }

  /**
   * 
   * Zeichne die Temperaturkurve für den Tauchgang
   * 
   * Project: SubmatixBluethoothLogger Package: de.dmarcini.submatix.logger.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 01.02.2014
   * @param canvas
   * @param oneProfil
   */
  private void drawTemperatureLine( Canvas canvas, float[] tempProfil )
  {
    float currX, currY;
    float temperatureFactor;
    float maxTemperature, maxTemperatureAbsolute, minTemperature;
    int samplePos = 0;
    int graphColor = colorClass.spxGraphTemperatureLine;
    int scaleColor = colorClass.spxGraphTemperatureScale;
    maxTemperature = getMaxValue( tempProfil );
    minTemperature = getMinValue( tempProfil );
    if( ( maxTemperature - minTemperature ) < 2 )
    {
      maxTemperature += 1.5;
      minTemperature -= 0.5;
    }
    // Die Temperatur muß ich nun noch skalieren, passend auf den Schirm und verkleinert
    temperatureFactor = ( viewHeight - margin_bottom_temp - ( viewHeight / 3 ) ) / ( maxTemperature - minTemperature );
    Log.v( TAG,
            String.format( "drawTemperatureLine() -> max temp value: <%03.1f>, min temp value: <%03.2f>, tempFactor: <%03.2f>", maxTemperature, minTemperature, temperatureFactor ) );
    // Pfad erstellen
    Path pathTemp = new Path();
    // hier gehts los, Ausgangspunkt erstellen
    currX = 0;
    samplePos = ( int )sampleOffset;
    while( ( currX <= viewWidth ) && samplePos < 0 )
    {
      // nächster Schritt
      currX += masterScale;
      samplePos++;
    }
    try
    {
      pathTemp.moveTo( currX, viewHeight - ( temperatureFactor * ( tempProfil[samplePos] - minTemperature ) ) - margin_bottom_temp );
    }
    catch( ArrayIndexOutOfBoundsException ex )
    {}
    ;
    while( ( currX <= viewWidth ) && ( samplePos < tempProfil.length ) )
    {
      // Y ist Wert in Grad, skaliert auf Display
      currY = viewHeight - ( temperatureFactor * ( tempProfil[samplePos] - minTemperature ) ) - margin_bottom_temp;
      pathTemp.lineTo( currX, currY );
      // nächster Schritt
      currX += masterScale;
      samplePos++;
    }
    if( samplePos < tempProfil.length )
    {
      try
      {
        currY = viewHeight - ( temperatureFactor * ( tempProfil[samplePos] - minTemperature ) ) - margin_bottom_temp;
        pathTemp.lineTo( currX, currY );
      }
      catch( ArrayIndexOutOfBoundsException ex )
      {}
    }
    // Temperatur Pfad zeichnen
    pnt.setColor( graphColor );
    pnt.setStyle( Style.STROKE );
    pnt.setStrokeWidth( 1.2F );
    canvas.drawPath( pathTemp, pnt );
    //
    // Abstand nach links wegen Tiefenskala
    //
    Rect bounds = new Rect();
    pnt.getTextBounds( "000.2 m", 0, 6, bounds );
    int pathLeft = 30 + bounds.width();
    //
    // Temperatur Skala machen
    //
    Path pathTemperatureScale = new Path();
    // Grundlinie
    pathTemperatureScale.moveTo( pathLeft - 7, viewHeight - margin_bottom_temp );
    pathTemperatureScale.lineTo( pathLeft + 7, viewHeight - margin_bottom_temp );
    // Skala
    maxTemperatureAbsolute = viewHeight - ( ( maxTemperature - minTemperature ) * temperatureFactor ) - margin_bottom_temp;
    pathTemperatureScale.moveTo( pathLeft, viewHeight - margin_bottom_temp );
    pathTemperatureScale.lineTo( pathLeft, maxTemperatureAbsolute - 30 );
    // Endpfeil
    pathTemperatureScale.lineTo( pathLeft - 5, maxTemperatureAbsolute - 30 + 15 );
    pathTemperatureScale.moveTo( pathLeft, maxTemperatureAbsolute - 30 );
    pathTemperatureScale.lineTo( pathLeft + 5, maxTemperatureAbsolute - 30 + 15 );
    // Max-Markierung
    pathTemperatureScale.moveTo( pathLeft - 7, maxTemperatureAbsolute );
    pathTemperatureScale.lineTo( pathLeft + 7, maxTemperatureAbsolute );
    // Zeichnen
    pnt.setStrokeWidth( 1.5F );
    pnt.setStyle( Style.STROKE );
    canvas.drawPath( pathTemperatureScale, pnt );
    //
    // Skaleneinteilung
    //
    float dLine = minTemperature;
    pnt.setTextSize( 20 );
    pnt.setTextAlign( Align.LEFT );
    pnt.setStyle( Style.FILL );
    //
    while( dLine <= maxTemperature )
    {
      currY = viewHeight - ( ( dLine - minTemperature ) * temperatureFactor ) - margin_bottom_temp;
      pnt.setColor( graphColor );
      canvas.drawLine( pathLeft - 7, currY, pathLeft + 7, currY, pnt );
      {
        pnt.setColor( scaleColor );
        canvas.drawText( String.format( "%02d°C", ( int )dLine ), pathLeft + 17, currY, pnt );
      }
      dLine += 1F;
    }
  }

  /**
   * 
   * Größten wert bei allen Samlpes finden
   * 
   * Project: CanvasPrintSample Package: de.dmarcini.android.canvas_sample
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 01.02.2014
   * @param origProfil
   * @return größter Wert
   */
  private float getMaxValue( float[] origProfil )
  {
    float maxVal = 0;
    for( float val : origProfil )
    {
      if( val > maxVal ) maxVal = val;
    }
    return maxVal;
  }

  /**
   * 
   * Kleinsten Wert bei allen Samples finden
   * 
   * Project: SubmatixBluethoothLogger Package: de.dmarcini.submatix.logger.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 01.02.2014
   * @param origProfil
   * @return
   */
  private float getMinValue( float[] origProfil )
  {
    float minVal = 0xFFFF;
    for( float val : origProfil )
    {
      if( val < minVal ) minVal = val;
    }
    return minVal;
  }

  /**
   * 
   * Die Tauchgangslänge in Sekunden berechnen
   * 
   * Project: SubmatixBluethoothLogger Package: de.dmarcini.submatix.logger.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 01.02.2014
   * @param oneProfil
   * @return
   */
  private int getDiveLength( float[] oneProfil )
  {
    float length = 0;
    for( float sampleLength : oneProfil )
    {
      length += sampleLength;
    }
    return( ( int )length );
  }

  /**
   * Wenn die Viewgröße verändert wird
   */
  @Override
  protected void onSizeChanged( int w, int h, int oldw, int oldh )
  {
    viewWidth = w;
    viewHeight = h;
  }

  /**
   * @return the viewWidth
   */
  public int getViewWidth()
  {
    return viewWidth;
  }

  /**
   * @param viewWidth
   *          the viewWidth to set
   */
  public void setViewWidth( int viewWidth )
  {
    this.viewWidth = viewWidth;
  }

  /**
   * @return the viewHeight
   */
  public int getViewHeight()
  {
    return viewHeight;
  }

  /**
   * Wenn jemand auf den screen rumtatscht
   */
  @Override
  public boolean onTouchEvent( MotionEvent ev )
  {
    scaleDetector.onTouchEvent( ev );
    // ...
    return true;
  }

  /**
   * @param viewHeight
   *          the viewHeight to set
   */
  public void setViewHeight( int viewHeight )
  {
    this.viewHeight = viewHeight;
  }

  /**
   * Wenn der User was rumfingert
   */
  @Override
  public boolean onTouch( View vi, MotionEvent ev )
  {
    // Hat der Detector das berbeitet?
    if( scaleDetector.onTouchEvent( ev ) )
    {
      // Wenn der gerade am zoomen ist, nix weiter machen
      if( scaleDetector.isInProgress() )
      {
        return( true );
      }
      // Den Finger drauf gepappt? Merke wo
      if( ev.getAction() == MotionEvent.ACTION_DOWN )
      {
        startTouchMove = ev.getX();
      }
      // bewegt?
      if( ev.getAction() == MotionEvent.ACTION_MOVE )
      {
        // weiter als 50 Pixel?
        if( Math.abs( startTouchMove - ev.getX() ) > 50 )
        {
          // Neues Offset unter Berücksichtigung des Zooms
          sampleOffset -= ( ev.getX() - startTouchMove ) / masterScale;
          // Neu zeichnen
          invalidate();
          startTouchMove = ev.getX();
        }
      }
      // Finger weggenommen?
      if( ev.getAction() == MotionEvent.ACTION_UP )
      {
        // Neues Offset unter Berücksichtigung des Zooms
        sampleOffset -= ( ev.getX() - startTouchMove ) / masterScale;
        // neu zeichnen
        invalidate();
      }
    }
    return( true );
  }

  /**
   * 
   * Gibt das Profil des Indexes id zurück
   * 
   * Project: SubmatixBluethoothLogger Package: de.dmarcini.submatix.logger.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 01.02.2014
   * @param sv
   * @param id
   * @return
   */
  private float[] getIndexProfil( Vector<float[]> sv, int id )
  {
    float[] prof = new float[sv.size()];
    Iterator<float[]> svi = sv.iterator();
    int index;
    index = 0;
    // Alle Elemente...
    while( svi.hasNext() )
    {
      prof[index++] = svi.next()[id];
    }
    // das Array zurückgeben
    return( prof );
  }

  /**
   * 
   * Setze Daten zuum Zeichnen des Profils
   * 
   * Project: SubmatixBluethoothLogger Package: de.dmarcini.submatix.logger.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 01.02.2014
   * @param diveProf
   */
  public void setDiveData( Vector<float[]> diveProf )
  {
    sampleVector = diveProf;
    masterScale = 0.0F;
    invalidate();
  }

  /**
   * 
   * Lösche die Daten
   * 
   * Project: SubmatixBluethoothLogger Package: de.dmarcini.submatix.logger.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 01.02.2014
   */
  public void clearDiveData()
  {
    sampleVector.clear();
    sampleVector = null;
    masterScale = 0.0F;
    invalidate();
  }

  /**
   * 
   * Gib für Sekunden die Form Minute:Sekunde zurück
   * 
   * Project: SubmatixBluethoothLogger Package: de.dmarcini.submatix.logger.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 01.02.2014
   * @param tm
   * @return
   */
  private String getTimeString( int tm )
  {
    int minute = tm / 60;
    int sec = tm % 60;
    return( String.format( Locale.ENGLISH, "%02d:%02d m", minute, sec ) );
  }

  /**
   * 
   * Setze das Thema entsprechend der App
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.full.utils
   * 
   * Stand: 02.02.2014
   * 
   * @param appStyle
   */
  public void setTheme( int appStyle )
  {
    currentAppstyle = appStyle;
    initColors();
  }
}
