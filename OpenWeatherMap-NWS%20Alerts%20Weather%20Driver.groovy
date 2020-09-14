/*
   OpenWeatherMap-NWS Alerts Weather Driver
   Import URL: https://raw.githubusercontent.com/HubitatCommunity/OpenWeatherMap-NWS-Alerts-Weather-Driver/master/OpenWeatherMap-NWS%2520Alerts%2520Weather%2520Driver.groovy
   Copyright 2020 @Matthew (Scottma61)

   This driver has morphed many, many times, so the genesis is very blurry now.  It stated as a WeatherUnderground
   driver, then when they restricted their API it morphed into an APIXU driver.  When APIXU ceased it becaome a
   Dark Sky driver .... and now that Dark Sky is going away it is morphing into a OpenWeatherMap driver.

   Many people contributed to the creation of this driver.  Significant contributors include:
   - @Cobra who adapted it from @mattw01's work and I thank them for that!
   - @bangali for his original APIXU.COM base code that much of the early versions of this driver was
     adapted from.
   - @bangali for his the Sunrise-Sunset.org code used to calculate illuminance/lux and the more
     recent adaptations of that code from @csteele in his continuation driver 'wx-ApiXU'.
   - @csteele (and prior versions from @bangali) for the attribute selection code.
   - @csteele for his examples on how to convert to asyncHttp calls to reduce Hub resource utilization.
   - @bangali also contributed the icon work from
     https://github.com/jebbett for new cooler 'Alternative' weather icons with icons courtesy
     of https://www.deviantart.com/vclouds/art/VClouds-Weather-Icons-179152045.
    - @storageanarchy for his Dark Sky Icon mapping and some new icons to compliment the Vclouds set.
    - @nh.schottfam for lots of code clean up and optimizations.

   In addition to all the cloned code from the Hubitat community, I have heavily modified/created new
   code myself @Matthew (Scottma61) with lots of help from the Hubitat community.  If you believe you
   should have been acknowledged or received attribution for a code contribution, I will happily do so.
   While I compiled and orchestrated the driver, very little is actually original work of mine.

   This driver is free to use.  I do not accept donations. Please feel free to contribute to those
   mentioned here if you like this work, as it would not have been possible without them.

   This driver is intended to pull weather data from OpenWeatherMap.org (https://OpenWeatherMap.org). You will need your
   OpenWeatherMap API key to use the data from that site.  It also pulls in weather alerts from the Nation Weather
   Service's API (weather.gov).  At the present time there is no API required for consume Alert data.

   The driver exposes both metric and imperial measurements for you to select from.

   Licensed under the Apache License, Version 2.0 (the 'License'); you may not use this file except
   in compliance with the License. You may obtain a copy of the License at:

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
   on an 'AS IS' BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
   for the specific language governing permissions and limitations under the License.

   Last Update 09/13/2020
  { Left room below to document version changes...}

   V0.1.8   Re-worked Alerts to not be dependent on api.weather.gov returning a valid response code           - 09/13/2020
   V0.1.7   Remove most DB accesses and string cleanup (by @nh.schottfam)                                     - 09/12/2020   
   V0.1.6   Restoring 'certainty' to weather.gov alert poll                                                   - 09/08/2020   
   V0.1.5   Removed 'certainty' from weather.gov alert poll                                                   - 09/08/2020   
   V0.1.4   Bug fix for NullPointerException on line 580                                                      - 09/07/2020
   V0.1.3   Improved Alert handling for dashboard tiles, again, various bug fixes                             - 09/05/2020
   V0.1.2   Bug fix sync MyTile and weatherSummary tiles upon alert update.                                   - 07/02/2020
   V0.1.1   Bug fix to exclude minutely and hourly data in poll.                                              - 06/06/2020
   V0.1.0   Improved Alert handling for dashboard tiles, various bug fixes                                    - 05/07/2020
   V0.0.9   Continue to work on improving null handling, various bug fixes                                    - 04/24/2020
   V0.0.8   Numerous bug fixes, better handling where alerts are not available, handling nulls                - 04/23/2020-2
   V0.0.7   Numerous bug fixes, better handling where alerts are not available                                - 04/23/2020
   V0.0.6   Refactored much of the code, added Hubitat Package Manager compatibility                          - 04/20/2020
   V0.0.5   More code cleanup and optimizations (Thanks @nh.schottfam!)                                       - 04/19/2020
   V0.0.4   Corrected forecast icon to always be 'day' instead of current time                                - 04/18/2020
   V0.0.3   More fixes on Alerts, mapped condition_code, weatherIcon(s)                                       - 04/18/2020
   V0.0.2   Fixed Alerts on myTile and alertTile, Capitalized condition_text                                  - 04/17/2020
   V0.0.1   Initial conversion from Dark Sky to OWM                                                           - 04/17/2020
=========================================================================================================
**ATTRIBUTES CAUTION**
The way the 'optional' attributes work:
 - Initially, only the optional attributes selected will show under 'Current States' and will be available
   in dashboard.
 - Once an attribute has been selected it too will show under 'Current States' and be available in dashboard.
   <*** HOWEVER ***> If you ever de-select the optional attribute, it will still show under 'Current States'
   and will still show as an attribute for dashboards **BUT IT'S DATA WILL NO LONGER BE REFRESHED WITH DATA
   POLLS**.  This means what is shown on the 'Current States' and dashboard tiles for de-selected attributes
   may not be current valid data.
 - To my knowledge, the only way to remove the de-selected attribute from 'Current States' and not show it as
   available in the dashboard is to delete the virtual device and create a new one AND DO NOT SELECT the
   attribute you do not want to show.
*/
public static String version()      {  return '0.1.8'  }
import groovy.transform.Field

metadata {
    definition (name: 'OpenWeatherMap-NWS Alerts Weather Driver',
                namespace: 'Matthew',
                author: 'Scottma61',
                importUrl: 'https://raw.githubusercontent.com/HubitatCommunity/OpenWeatherMap-NWS-Alerts-Weather-Driver/master/OpenWeatherMap-NWS%2520Alerts%2520Weather%2520Driver.groovy') {
        capability 'Sensor'
        capability 'Temperature Measurement'
        capability 'Illuminance Measurement'
        capability 'Relative Humidity Measurement'
 		capability 'Pressure Measurement'
 		capability 'Ultraviolet Index'

        capability 'Refresh'
	
		attributesMap.each
		{
            k, v -> if (v.typeof)        attribute k, v.typeof
		}
//    The following attributes may be needed for dashboards that require these attributes,
//    so they are alway available and shown by default.
        attribute 'city', 'string'              //Hubitat  OpenWeather  SharpTool.io  SmartTiles
        attribute 'feelsLike', 'number'         //SharpTool.io  SmartTiles
        attribute 'forecastIcon', 'string'      //SharpTool.io
        attribute 'localSunrise', 'string'      //SharpTool.io  SmartTiles
        attribute 'localSunset', 'string'       //SharpTool.io  SmartTiles
        attribute 'pressured', 'string'         //UNSURE SharpTool.io  SmartTiles
        attribute 'weather', 'string'           //SharpTool.io  SmartTiles
        attribute 'weatherIcon', 'string'       //SharpTool.io  SmartTiles
        attribute 'weatherIcons', 'string'      //Hubitat  openWeather
        attribute 'wind', 'number'              //SharpTool.io
        attribute 'windDirection', 'number'     //Hubitat  OpenWeather
        attribute 'windSpeed', 'number'         //Hubitat  OpenWeather

//      The attributes below are sub-groups of optional attributes.  They need to be listed here to be available
//alert
        attribute 'alert', 'string'
        attribute 'alertTile', 'string'

//threedayTile
        attribute 'threedayfcstTile', 'string'

//fcstHighLow
		attribute 'forecastHigh', 'number'
		attribute 'forecastLow', 'number'

// controlled with localSunrise
		attribute 'tw_begin', 'string'
		attribute 'sunriseTime', 'string'
		attribute 'noonTime', 'string'
		attribute 'sunsetTime', 'string'
		attribute 'tw_end', 'string'

//obspoll   
		attribute 'last_poll_Forecast', 'string' // time the poll was initiated
		attribute 'last_observation_Forecast', 'string'  // datestamp of the forecast observation
        
//precipExtended
		attribute 'rainDayAfterTomorrow', 'number'
		attribute 'rainTomorrow', 'number'

        command 'pollData'
    }
    String settingDescr = settingEnable ? '<br><i>Hide many of the optional attributes to reduce the clutter, if needed, by turning OFF this toggle.</i><br>' : '<br><i>Many optional attributes are available to you, if needed, by turning ON this toggle.</i><br>'
    preferences() {
		section('Query Inputs'){
			input 'apiKey', 'text', required: true, title: 'Type OpenWeatherMap.org API Key Here', defaultValue: null
            input 'city', 'text', required: true, defaultValue: 'City or Location name forecast area', title: 'City name'
			input 'pollIntervalForecast', 'enum', title: 'External Source Poll Interval (daytime)', required: true, defaultValue: '3 Hours', options: ['Manual Poll Only', '2 Minutes', '5 Minutes', '10 Minutes', '15 Minutes', '30 Minutes', '1 Hour', '3 Hours']
            input 'pollIntervalForecastnight', 'enum', title: 'External Source Poll Interval (nighttime)', required: true, defaultValue: '3 Hours', options: ['Manual Poll Only', '2 Minutes', '5 Minutes', '10 Minutes', '15 Minutes', '30 Minutes', '1 Hour', '3 Hours']
            input 'logSet', 'bool', title: 'Enable extended Logging', description: '<i>Extended logging will turn off automatically after 30 minutes.</i>', required: true, defaultValue: false
	    	input 'tempFormat', 'enum', required: true, defaultValue: 'Fahrenheit (°F)', title: 'Display Unit - Temperature: Fahrenheit (°F) or Celsius (°C)',  options: ['Fahrenheit (°F)', 'Celsius (°C)']
            input 'TWDDecimals', 'enum', required: true, defaultValue: '0', title: 'Display decimals for Temperature & Wind Speed', options: [0:'0', 1:'1', 2:'2', 3:'3', 4:'4']
            input 'RDecimals', 'enum', required: true, defaultValue: '0', title: 'Display decimals for Precipitation', options: [0:'0', 1:'1', 2:'2', 3:'3', 4:'4']
            input 'PDecimals', 'enum', required: true, defaultValue: '0', title: 'Display decimals for Pressure', options: [0:'0', 1:'1', 2:'2', 3:'3', 4:'4']
            input 'datetimeFormat', 'enum', required: true, defaultValue: '1', title: 'Display Unit - Date-Time Format',  options: [1:'m/d/yyyy 12 hour (am|pm)', 2:'m/d/yyyy 24 hour', 3:'mm/dd/yyyy 12 hour (am|pm)', 4:'mm/dd/yyyy 24 hour', 5:'d/m/yyyy 12 hour (am|pm)', 6:'d/m/yyyy 24 hour', 7:'dd/mm/yyyy 12 hour (am|pm)', 8:'dd/mm/yyyy 24 hour', 9:'yyyy/mm/dd 24 hour']
            input 'distanceFormat', 'enum', required: true, defaultValue: 'Miles (mph)', title: 'Display Unit - Distance/Speed: Miles, Kilometers, knots or meters',  options: ['Miles (mph)', 'Kilometers (kph)', 'knots', 'meters (m/s)']
            input 'pressureFormat', 'enum', required: true, defaultValue: 'Inches', title: 'Display Unit - Pressure: Inches or Millibar/Hectopascal',  options: ['Inches', 'Millibar', 'Hectopascal']
            input 'rainFormat', 'enum', required: true, defaultValue: 'Inches', title: 'Display Unit - Precipitation: Inches or Millimeters',  options: ['Inches', 'Millimeters']
            input 'luxjitter', 'bool', title: 'Use lux jitter control (rounding)?', required: true, defaultValue: false
			input 'iconLocation', 'text', required: true, defaultValue: 'https://tinyurl.com/y6xrbhpf/', title: 'Alternative Icon Location:'
            input 'iconType', 'bool', title: 'Condition Icon: On=Current or Off=Forecast', required: true, defaultValue: false
            input 'altCoord', 'bool', required: true, defaultValue: false, title: "Override Hub's location coordinates"
            if (altCoord) {
                input 'altLat', 'string', title: 'Override location Latitude', required: true, defaultValue: location.latitude.toString(), description: '<br>Enter location Latitude<br>'
                input 'altLon', 'string', title: 'Override location Longitude', required: true, defaultValue: location.longitude.toString(), description: '<br>Enter location Longitude<br>'
            }
            input 'settingEnable', 'bool', title: '<b>Display All Optional Attributes</b>', description: settingDescr, defaultValue: true
	// build a Selector for each mapped Attribute or group of attributes
	    	attributesMap.each
		    {
	    		keyname, attribute ->
                if (settingEnable) {
                    input keyname+'Publish', 'bool', title: attribute.title, required: true, defaultValue: attribute.default, description: sBR+attribute.descr+sBR                    
                    if(keyname == 'weatherSummary') input 'summaryType', 'bool', title: 'Full Weather Summary', description: '<br>Full: on or short: off summary?<br>', required: true, defaultValue: false
                }
	    	}
            if (settingEnable) {
                input 'windPublish', 'bool', title: 'Wind Speed', required: true, defaultValue: 'false', description: '<br>Display wind speed<br>'
            }
        }
    }
}

@Field static final String sNULL=(String)null
@Field static final String sAB='<a>'
@Field static final String sACB='</a>'
@Field static final String sCSPAN='</span>'
@Field static final String sBR='<br>'
@Field static final String sBLK=''
@Field static final String sSPC=' '
@Field static final String sICON='iconLocation'
@Field static final String sTMETR='tMetric'
@Field static final String sDMETR='dMetric'
@Field static final String sPMETR='pMetric'
@Field static final String sRMETR='rMetric'
@Field static final String sTEMP='temperature'
@Field static final String sSUMLST='Summary_last_poll_time'

// <<<<<<<<<< Begin Sunrise-Sunset Poll Routines >>>>>>>>>>
void pollSunRiseSet() {
    if(ifreInstalled()) { updated(); return }
    String currDate = new Date().format('yyyy-MM-dd', TimeZone.getDefault())
    LOGINFO('Polling Sunrise-Sunset.org')
    def requestParams = [ uri: 'https://api.sunrise-sunset.org/json?lat=' + altLat + '&lng=' + altLon + '&formatted=0' ]
    if (currDate) {requestParams = [ uri: 'https://api.sunrise-sunset.org/json?lat=' + altLat + '&lng=' + altLon + '&formatted=0&date=' + currDate ]}
    LOGINFO('Poll Sunrise-Sunset: ' + requestParams)
    asynchttpGet('sunRiseSetHandler', requestParams)
}

void sunRiseSetHandler(resp, data) {
	if(ifreInstalled()) { updated(); return }
	if(resp.getStatus() == 200 || resp.getStatus() == 207) {
		sunRiseSet = resp.getJson().results
		myUpdData('sunRiseSet', resp.data)
		LOGINFO('Sunrise-Sunset Data: ' + sunRiseSet)
		String tfmt='yyyy-MM-dd\'T\'HH:mm:ssXXX'
		String tfmt1='HH:mm'
		myUpdData('riseTime', new Date().parse(tfmt, sunRiseSet.sunrise).format(tfmt1, TimeZone.getDefault()))
		myUpdData('noonTime', new Date().parse(tfmt, sunRiseSet.solar_noon).format(tfmt1, TimeZone.getDefault()))
		myUpdData('setTime', new Date().parse(tfmt, sunRiseSet.sunset).format(tfmt1, TimeZone.getDefault()))
		myUpdData('tw_begin', new Date().parse(tfmt, sunRiseSet.civil_twilight_begin).format(tfmt1, TimeZone.getDefault()))
		myUpdData('tw_end', new Date().parse(tfmt, sunRiseSet.civil_twilight_end).format(tfmt1, TimeZone.getDefault()))
		myUpdData('localSunset',new Date().parse(tfmt, sunRiseSet.sunset).format(myGetData('timeFormat'), TimeZone.getDefault()))
		myUpdData('localSunrise', new Date().parse(tfmt, sunRiseSet.sunrise).format(myGetData('timeFormat'), TimeZone.getDefault()))
		myUpdData('riseTime1', new Date().parse(tfmt, sunRiseSet.sunrise + 86400000).format(tfmt1, TimeZone.getDefault()))
		myUpdData('riseTime2', new Date().parse(tfmt, sunRiseSet.sunrise + 86400000 + 86400000).format(tfmt1, TimeZone.getDefault()))
		myUpdData('setTime1', new Date().parse(tfmt, sunRiseSet.sunset + 86400000).format(tfmt1, TimeZone.getDefault()))
		myUpdData('setTime2', new Date().parse(tfmt, sunRiseSet.sunset + 86400000 + 86400000).format(tfmt1, TimeZone.getDefault()))
    } else {
		LOGWARN('Sunrise-Sunset api did not return data.')
    }
}
// >>>>>>>>>> End Sunrise-Sunset Poll Routines <<<<<<<<<<

// <<<<<<<<<< Begin OWM Poll Routines >>>>>>>>>>
void pollOWM() {
    if(ifreInstalled()) { updated(); return }
    if( apiKey == null ) {
        LOGWARN('OpenWeatherMap API Key not found.  Please configure in preferences.')
        return
    }
    def ParamsOWM
    ParamsOWM = [ uri: 'https://api.openweathermap.org/data/2.5/onecall?lat=' + altLat + '&lon=' + altLon + '&exclude=minutely,hourly&mode=json&units=imperial&appid=' + apiKey ]
    LOGINFO('Poll OpenWeatherMap.org: ' + ParamsOWM)
    asynchttpGet('pollOWMHandler', ParamsOWM)
}

void pollOWMHandler(resp, data) {
    if(ifreInstalled()) { updated(); return }
    LOGINFO('Polling OpenWeatherMap.org')
    if(resp.getStatus() != 200 && resp.getStatus() != 207) {
        LOGWARN('Calling https://api.openweathermap.org/data/2.5/onecall?lat=' + altLat + '&lon=' + altLon + '&exclude=minutely,hourly&mode=json&units=imperial&appid=' + apiKey)
        LOGWARN(resp.getStatus() + ':' + resp.getErrorMessage())
	} else {
        def owm = parseJson(resp.data)
        LOGINFO('OpenWeatherMap Data: ' + owm)

        fotime = (owm?.current?.dt==null) ? new Date() : new Date(owm.current.dt * 1000L)
        myUpdData('fotime', fotime.toString())
        futime = new Date()
        myUpdData('futime', futime.toString())
        myUpdData('fotime', fotime.toString())
        myUpdData(sSUMLST, futime.format(myGetData('timeFormat'), TimeZone.getDefault()).toString())
        myUpdData('Summary_last_poll_date', futime.format(myGetData('dateFormat'), TimeZone.getDefault()).toString())

        myUpdData('currDate', new Date().format('yyyy-MM-dd', TimeZone.getDefault()))
        myUpdData('currTime', new Date().format('HH:mm', TimeZone.getDefault()))
        if(myGetData('riseTime') <= myGetData('currTime') && myGetData('setTime') >= myGetData('currTime')) {
            myUpdData('is_day', 'true')
        } else {
            myUpdData('is_day', 'false')
        }
        if(myGetData('currTime') < myGetData('tw_begin') || myGetData('currTime') > myGetData('tw_end')) {
            myUpdData('is_light', 'false')
        } else {
            myUpdData('is_light', 'true')
        }
        if(myGetData('is_light') != myGetData('is_lightOld')) {
            if(myGetData('is_light')=='true') {
                log.info('OpenWeatherMap.org Weather Driver - INFO: Switching to Daytime schedule.')
            }else{
                log.info('OpenWeatherMap.org Weather Driver - INFO: Switching to Nighttime schedule.')
            }
            initialize_poll()
            myUpdData('is_lightOld', myGetData('is_light'))
        }
// >>>>>>>>>> End Setup Global Variables <<<<<<<<<<

// <<<<<<<<<< Begin Process Standard Weather-Station Variables (Regardless of Forecast Selection)  >>>>>>>>>>
	    Integer mult_twd=myGetData('mult_twd').toInteger()
        Integer mult_p=myGetData('mult_p').toInteger()
        Integer mult_r=myGetData('mult_r').toInteger()
        
        BigDecimal t_dew = owm?.current?.dew_point==null ? 0.00 : owm.current.dew_point.toBigDecimal()
        if(myGetData(sTMETR) == '°F') {
            t_dew = Math.round(t_dew * mult_twd) / mult_twd
        } else {
            t_dew = Math.round((t_dew - 32) / 1.8 * mult_twd) / mult_twd
        }

        myUpdData('dewpoint', t_dew.toString())
        myUpdData('humidity', (Math.round((owm?.current?.humidity==null ? 0.00 : owm.current.humidity.toBigDecimal()) * 10) / 10).toString())

        BigDecimal t_press = owm?.current?.pressure==null ? 0.00 : owm.current.pressure.toBigDecimal()
        if(myGetData(sPMETR) == 'inHg') {
            t_press = Math.round(t_press * 0.029529983071445 * mult_p) / mult_p
        } else {
            t_press = Math.round(t_press * mult_p) / mult_p
        }
        myUpdData('pressure', t_press.toString())

        BigDecimal t_temp = owm?.current?.temp=null ? 0.00 : owm.current.temp.toBigDecimal()
        if(myGetData(sTMETR) == '°F') {
            t_temp = Math.round(t_temp * mult_twd) / mult_twd
        } else {
            t_temp = Math.round((t_temp - 32) / 1.8 * mult_twd) / mult_twd
        }
        myUpdData(sTEMP, t_temp.toString())

        String w_string_bft
        String w_bft_icon
        BigDecimal t_ws = owm?.current?.wind_speed==null ? 0.00 : owm.current.wind_speed.toBigDecimal()
        if(t_ws < 1.0) {
            w_string_bft = 'Calm'; w_bft_icon = 'wb0.png'
        }else if(t_ws < 4.0) {
            w_string_bft = 'Light air'; w_bft_icon = 'wb1.png'
        }else if(t_ws < 8.0) {
            w_string_bft = 'Light breeze'; w_bft_icon = 'wb2.png'
        }else if(t_ws < 13.0) {
            w_string_bft = 'Gentle breeze'; w_bft_icon = 'wb3.png'
        }else if(t_ws < 19.0) {
            w_string_bft = 'Moderate breeze'; w_bft_icon = 'wb4.png'
        }else if(t_ws < 25.0) {
            w_string_bft = 'Fresh breeze'; w_bft_icon = 'wb5.png'
        }else if(t_ws < 32.0) {
            w_string_bft = 'Strong breeze'; w_bft_icon = 'wb6.png'
        }else if(t_ws < 39.0) {
            w_string_bft = 'High wind, moderate gale, near gale'; w_bft_icon = 'wb7.png'
        }else if(t_ws < 47.0) {
            w_string_bft = 'Gale, fresh gale'; w_bft_icon = 'wb8.png'
        }else if(t_ws < 55.0) {
            w_string_bft = 'Strong/severe gale'; w_bft_icon = 'wb9.png'
        }else if(t_ws < 64.0) {
            w_string_bft = 'Storm, whole gale'; w_bft_icon = 'wb10.png'
        }else if(t_ws < 73.0) {
            w_string_bft = 'Violent storm'; w_bft_icon = 'wb11.png'
        }else if(t_ws >= 73.0) {
            w_string_bft = 'Hurricane force'; w_bft_icon = 'wb12.png'
        }
	    myUpdData('wind_string_bft', w_string_bft)
        myUpdData('wind_bft_icon', w_bft_icon)

        BigDecimal t_wd = owm?.current?.wind_speed==null ? 0.00 : owm.current.wind_speed.toBigDecimal()
        BigDecimal t_wg = owm?.current?.wind_gust==null ? t_wd : owm.current.wind_gust.toBigDecimal()
        if(myGetData(sDMETR) == 'MPH') {
            t_wd = Math.round(t_wd * mult_twd) / mult_twd
            t_wg = Math.round(t_wg * mult_twd) / mult_twd
        } else if(myGetData(sDMETR) == 'KPH') {
            t_wd = Math.round(t_wd * 1.609344 * mult_twd) / mult_twd
            t_wg = Math.round(t_wg * 1.609344 * mult_twd) / mult_twd
        } else if(myGetData(sDMETR) == 'knots') {
            t_wd = Math.round(t_wd * 0.868976 * mult_twd) / mult_twd
            t_wg = Math.round(t_wg * 0.868976 * mult_twd) / mult_twd
        } else {  //  this leave only m/s
            t_wd = Math.round(t_wd * 0.44704 * mult_twd) / mult_twd
            t_wg = Math.round(t_wg * 0.44704 * mult_twd) / mult_twd
        }
        myUpdData('wind', t_wd.toString())
        myUpdData('wind_gust', t_wg.toString())

        myUpdData('wind_degree', owm.current.wind_deg.toInteger().toString())	
        String w_cardinal
        String w_direction
        BigDecimal twb = owm?.current?.wind_deg==null ? 0.00 : owm.current.wind_deg.toBigDecimal()
        if(twb < 11.25) {
            w_cardinal = 'N'; w_direction = 'North'
        }else if(twb < 33.75) {
            w_cardinal = 'NNE'; w_direction = 'North-Northeast'
        }else if(twb < 56.25) {
            w_cardinal = 'NE';  w_direction = 'Northeast'
        }else if(twb < 56.25) {
            w_cardinal = 'ENE'; w_direction = 'East-Northeast'
        }else if(twb < 101.25) {
            w_cardinal = 'E'; w_direction = 'East'
        }else if(twb < 123.75) {
            w_cardinal = 'ESE'; w_direction = 'East-Southeast'
        }else if(twb < 146.25) {
            w_cardinal = 'SE'; w_direction = 'Southeast'
        }else if(twb < 168.75) {
            w_cardinal = 'SSE'; w_direction = 'South-Southeast'
        }else if(twb < 191.25) {
            w_cardinal = 'S'; w_direction = 'South'
        }else if(twb < 213.75) {
            w_cardinal = 'SSW'; w_direction = 'South-Southwest'
        }else if(twb < 236.25) {
            w_cardinal = 'SW'; w_direction = 'Southwest'
        }else if(twb < 258.75) {
            w_cardinal = 'WSW'; w_direction = 'West-Southwest'
        }else if(twb < 281.25) {
            w_cardinal = 'W'; w_direction = 'West'
        }else if(twb < 303.75) {
            w_cardinal = 'WNW'; w_direction = 'West-Northwest'
        }else if(twb < 326.25) {
            w_cardinal = 'NW'; w_direction = 'Northwest'
        }else if(twb < 348.75) {
            w_cardinal = 'NNW'; w_direction = 'North-Northwest'
        }else if(twb >= 348.75) {
            w_cardinal = 'N'; w_direction = 'North'
        }
        myUpdData('wind_direction', w_direction)
        myUpdData('wind_cardinal', w_cardinal)	
        myUpdData('wind_string', w_string_bft + ' from the ' + myGetData('wind_direction') + (myGetData('wind').toBigDecimal() < 1.0 ? sBLK: ' at ' + String.format(myGetData('ddisp_twd'), myGetData('wind').toBigDecimal()) + sSPC + myGetData(sDMETR)))
        String s_cardinal
        String s_direction
// >>>>>>>>>> End Process Standard Weather-Station Variables (Regardless of Forecast Selection)  <<<<<<<<<<	
        
	    Integer cloudCover = owm?.current?.clouds==null ? 1 : owm.current.clouds <= 1 ? 1 : owm.current.clouds
        myUpdData('cloud', cloudCover.toString())
        myUpdData('vis', (myGetData(sDMETR)!='MPH' ? Math.round(owm?.current?.visibility==null ? 0.01 : owm.current.visibility.toBigDecimal() * 0.001 * mult_twd) / mult_twd : Math.round(owm?.current?.visibility==null ? 0.00 : owm.current.visibility.toBigDecimal() * 0.0006213712 * mult_twd) / mult_twd).toString())

        List owmCweat = owm?.current?.weather
        myUpdData('condition_id', owmCweat==null || owmCweat[0]?.id==null ? '999' : owmCweat[0].id.toString())
        myUpdData('condition_code', getCondCode(myGetData('condition_id').toInteger(), myGetData('is_day')))
        myUpdData('condition_text', owmCweat==null || owmCweat[0]?.description==null ? 'Unknown' : owmCweat[0].description.capitalize())
        myUpdData('OWN_icon', owmCweat == null || owmCweat[0]?.icon==null ? (myGetData('is_day')=='true' ? '50d' : '50n') : owmCweat[0].icon)

        List owmDaily = owm?.daily != null && ((List)owm.daily)[0]?.weather != null ? ((List)owm?.daily)[0].weather : null
        myUpdData('forecast_id', owmDaily==null || owmDaily[0]?.id==null ? '999' : owmDaily[0].id.toString())
        myUpdData('forecast_code', getCondCode(myGetData('forecast_id').toInteger(), 'true'))
        myUpdData('forecast_text', owmDaily==null || owmDaily[0]?.description==null ? 'Unknown' : owmDaily[0].description.capitalize())

        owmDaily = owm?.daily != null ? owm.daily : null
        BigDecimal t_p0 = (owmDaily==null || owmDaily[0]?.rain==null ? 0.00 : owmDaily[0].rain) + (owmDaily==null || owmDaily[0]?.snow==null ? 0.00 : owmDaily[0].snow)

        myUpdData('rainToday', (Math.round((myGetData(sRMETR) == 'in' ? t_p0 * 0.03937008 : t_p0) * mult_r) / mult_r).toString())

        if(owmDaily && (threedayTilePublish || precipExtendedPublish || myTile2Publish)) {
            BigDecimal t_p1 = (owmDaily[1]?.rain==null ? 0.00 : owmDaily[1].rain) + (owmDaily[1]?.snow==null ? 0.00 : owmDaily[1].snow)
            BigDecimal t_p2 = (owmDaily[2]?.rain==null ? 0.00 : owmDaily[2].rain) + (owmDaily[2]?.snow==null ? 0.00 : owmDaily[2].snow)
            myUpdData('Precip0', (Math.round((myGetData(sRMETR) == 'in' ? t_p0 * 0.03937008 : t_p0) * mult_r) / mult_r).toString())
            myUpdData('Precip1', (Math.round((myGetData(sRMETR) == 'in' ? t_p1 * 0.03937008 : t_p1) * mult_r) / mult_r).toString())
            myUpdData('Precip2', (Math.round((myGetData(sRMETR) == 'in' ? t_p2 * 0.03937008 : t_p2) * mult_r) / mult_r).toString())
        }

        if(owmDaily && (threedayTilePublish || myTile2Publish)) {
            myUpdData('day1', owmDaily[1]?.dt==null ? sBLK : new Date(owmDaily[1].dt * 1000L).format('EEEE'))
            myUpdData('day2', owmDaily[2]?.dt==null ? sBLK : new Date(owmDaily[2].dt * 1000L).format('EEEE'))

            myUpdData('is_day1', 'true')
            myUpdData('is_day2', 'true')
            myUpdData('forecast_id1', owm?.daily[1]?.weather[0]?.id==null ? '999' : owm.daily[1].weather[0].id.toString())
            myUpdData('forecast_code1', getCondCode(myGetData('forecast_id1').toInteger(), 'true'))
            myUpdData('forecast_text1', owm?.daily[1]?.weather[0]?.description==null ? 'Unknown' : owm.daily[1].weather[0].description.capitalize())

            myUpdData('forecast_id2', owm?.daily[2]?.weather[0]?.id==null ? '999' : owm.daily[2].weather[0].id.toString())
            myUpdData('forecast_code2', getCondCode(myGetData('forecast_id2').toInteger(), 'true'))
            myUpdData('forecast_text2', owm?.daily[2]?.weather[0]?.description==null ? 'Unknown' : owm.daily[2].weather[0].description.capitalize())

            myUpdData('forecastHigh1', (myGetData(sTMETR)=='°F' ? (Math.round(owm?.daily[1]?.temp?.max==null ? 0.00 : owm.daily[1].temp.max.toBigDecimal() * mult_twd) / mult_twd) : (Math.round((owm?.daily[1]?.temp?.max==null ? 0.00 : owm.daily[1].temp.max.toBigDecimal() - 32) / 1.8 * mult_twd) / mult_twd)).toString())
            myUpdData('forecastHigh2', (myGetData(sTMETR)=='°F' ? (Math.round(owm?.daily[2]?.temp?.max==null ? 0.00 : owm.daily[2].temp.max.toBigDecimal() * mult_twd) / mult_twd) : (Math.round((owm?.daily[2]?.temp?.max==null ? 0.00 : owm.daily[2].temp.max.toBigDecimal() - 32) / 1.8 * mult_twd) / mult_twd)).toString())

            myUpdData('forecastLow1', (myGetData(sTMETR)=='°F' ? (Math.round(owm?.daily[1]?.temp?.min==null ? 0.00 : owm.daily[1].temp.min.toBigDecimal() * mult_twd) / mult_twd) : (Math.round((owm?.daily[1]?.temp?.min==null ? 0.00 : owm.daily[1].temp.min.toBigDecimal() - 32) / 1.8 * mult_twd) / mult_twd)).toString())
            myUpdData('forecastLow2', (myGetData(sTMETR)=='°F' ? (Math.round(owm?.daily[2]?.temp?.min==null ? 0.00 : owm.daily[2].temp.min.toBigDecimal() * mult_twd) / mult_twd) : (Math.round((owm?.daily[2]?.temp?.min==null ? 0.00 : owm.daily[2].temp.min.toBigDecimal() - 32) / 1.8 * mult_twd) / mult_twd)).toString())

            myUpdData('imgName0', '<img class="centerImage" src=' + myGetData(sICON) + getImgName(myGetData('condition_id').toInteger(), myGetData('is_day')) + (((myGetData(sICON).toLowerCase().contains('://github.com/')) && (myGetData(sICON).toLowerCase().contains('/blob/master/'))) ? '?raw=true' : sBLK) + '>')
            myUpdData('imgName1', '<img class="centerImage" src=' + myGetData(sICON) + getImgName(owm?.daily[1]?.weather[0]?.id==null ? 999 : owm.daily[1].weather[0].id, 'true') + (((myGetData(sICON).toLowerCase().contains('://github.com/')) && (myGetData(sICON).toLowerCase().contains('/blob/master/'))) ? '?raw=true' : sBLK) + '>')
            myUpdData('imgName2', '<img class="centerImage" src=' + myGetData(sICON) + getImgName(owm?.daily[2]?.weather[0]?.id==null ? 999 : owm.daily[2].weather[0].id, 'true') + (((myGetData(sICON).toLowerCase().contains('://github.com/')) && (myGetData(sICON).toLowerCase().contains('/blob/master/'))) ? '?raw=true' : sBLK) + '>')
        }

        myUpdData('forecastHigh', (myGetData(sTMETR)=='°F' ? (Math.round(owm?.daily[0]?.temp?.max==null ? 0.00 : owm.daily[0].temp.max.toBigDecimal() * mult_twd) / mult_twd) : (Math.round((owm?.daily[0]?.temp?.max==null ? 0.00 : owm.daily[0].temp.max.toBigDecimal() - 32) / 1.8 * mult_twd) / mult_twd)).toString())
        myUpdData('forecastLow', (myGetData(sTMETR)=='°F' ? (Math.round(owm?.daily[0]?.temp?.min==null ? 0.00 : owm.daily[0].temp.min.toBigDecimal() * mult_twd) / mult_twd) : (Math.round((owm?.daily[0]?.temp?.min==null ? 0.00 : owm.daily[0].temp.min.toBigDecimal() - 32) / 1.8 * mult_twd) / mult_twd)).toString())

        if(precipExtendedPublish){
            myUpdData('rainTomorrow', myGetData('Precip1'))
            myUpdData('rainDayAfterTomorrow', myGetData('Precip2'))
        }

        updateLux(false)
        myUpdData('ultravioletIndex', (owm?.current?.uvi==null ? 0.00 : owm.current.uvi.toBigDecimal()).toString())

        BigDecimal t_fl
        if(myGetData(sTMETR) == '°F') {
            t_fl = Math.round(owm?.current?.feels_like==null ? 0.00 : owm.current.feels_like.toBigDecimal() * mult_twd) / mult_twd
        } else {
            t_fl = Math.round((owm?.current?.feels_like==null ? 0.00 : owm.current.feels_like.toBigDecimal() - 32) / 1.8 * mult_twd) / mult_twd
        }
        myUpdData('feelsLike', t_fl.toString())

        if(alertPublish) {
            pollAlerts()
        } else {
            clearAlerts()
        }
// >>>>>>>>>> End Setup Forecast Variables <<<<<<<<<<

	    // <<<<<<<<<< Begin Icon Processing  >>>>>>>>>>
        String imgName = (myGetData('iconType')== 'true' ? getImgName(myGetData('condition_id').toInteger(), myGetData('is_day')) : getImgName(myGetData('forecast_id').toInteger(), myGetData('is_day')))
        sendEventPublish(name: 'condition_icon', value: '<img src=' + myGetData(sICON) + imgName + (((myGetData(sICON).toLowerCase().contains('://github.com/')) && (myGetData(sICON).toLowerCase().contains('/blob/master/'))) ? '?raw=true' : sBLK) + '>')
        sendEventPublish(name: 'condition_iconWithText', value: '<img src=' + myGetData(sICON) + imgName + (((myGetData(sICON).toLowerCase().contains('://github.com/')) && (myGetData(sICON).toLowerCase().contains('/blob/master/'))) ? '?raw=true' : sBLK) + '><br>' + (myGetData('iconType')== 'true' ? myGetData('condition_text') : myGetData('forecast_text')))
        sendEventPublish(name: 'condition_icon_url', value: myGetData(sICON) + imgName + (((myGetData(sICON).toLowerCase().contains('://github.com/')) && (myGetData(sICON).toLowerCase().contains('/blob/master/'))) ? '?raw=true' : sBLK))
        myUpdData('condition_icon_url', myGetData(sICON) + imgName + (((myGetData(sICON).toLowerCase().contains('://github.com/')) && (myGetData(sICON).toLowerCase().contains('/blob/master/'))) ? '?raw=true' : sBLK))
        sendEventPublish(name: 'condition_icon_only', value: imgName.split('/')[-1].replaceFirst('\\?raw=true',sBLK))
        // >>>>>>>>>> End Icon Processing <<<<<<<<<<
        PostPoll()
    }
}
// >>>>>>>>>> End OpenWeatherMap Poll Routine <<<<<<<<<<

void clearAlerts(){
	myUpdData('noAlert','true')
	myUpdData('alert', 'No current weather alerts for this area')
	myUpdData('alertTileLink', '<a href="https://forecast.weather.gov/MapClick.php?lat=' + altLat + '&lon=' + altLon + '" target=\"_blank\">No current weather alerts for this area.</a>')
	myUpdData('alertLink', sAB + myGetData('condition_text') + sACB)
	myUpdData('alertLink2', sAB + myGetData('condition_text') + sACB)
	myUpdData('alertLink3', sAB + myGetData('condition_text') + sACB)
	myUpdData('possAlert', 'false')
}

void alertErr(String msg){
	myUpdData('alertFails', (myGetData('alertFails').toInteger() + 1).toString())
       	if(myGetData('alertFails').toInteger() < 3) {
       		runIn(5, pollAlerts)
		LOGINFO('NWS Alert Data Poll Failed, Will try again in 5 seconds.')
	} else {
		clearAlerts()
		myUpdData('alert', 'Weather alerts are not available')
		myUpdData('alertTileLink', '<a href="https://forecast.weather.gov/MapClick.php?lat=' + altLat + '&lon=' + altLon + '" target=\"_blank\">Weather alerts are not available.</a>')
		LOGWARN(msg)
		myUpdData('alertFails', '0')
	}
}

// <<<<<<<<<< Begin NWS Active Alert Poll Routines >>>>>>>>>>
void pollAlerts() {
    if(ifreInstalled()) { updated(); return }
    if(myGetData('alertFails')==null) {myUpdData('alertFails','0')}
    Integer pollTimeout = settings.pollIntervalStation == '1 Minute' ? 15 : 30
    Map result = null
/*  for testing weather alerts in a different area
    altLat = 30.6941667
    altLon = -88.0430556 
*/
    Map ParamsAlerts = [ uri: 'https://api.weather.gov/alerts/active?status=actual&message_type=alert,update&point=' + altLat + ',' + altLon + '&urgency=unknown,future,expected,immediate&severity=unknown,moderate,severe,extreme&certainty=unknown,possible,likely,observed',
                    requestContentType:'application/json',
                    contentType:'application/json',
                    timeout: pollTimeout
                   ]
    LOGINFO('Poll api.weather.gov/alerts/active: ' + ParamsAlerts)
    try {
        httpGet(ParamsAlerts) { response -> result = response.data }
    }
        catch (SocketTimeoutException e) {
            alertErr('NWS Alerts - Connection to weather.gov API timed out. This is a NWS API website issue, the website is busy.')
        }
    
        catch (e) {
            alertErr('NWS Alerts - Connection to weather.gov API failed. This is a NWS API website issue, the website is down or not responding as expected.')
        }

    if(result!=null) {
//    if(response?.status == 200) {
        String curAl = result.features[0]?.properties?.event==null ? null: result.features[0].properties.event.replaceAll('[{}\\[\\]]', sBLK).split(/,/)[0]
        LOGINFO('NWS Alert - response: ' + result + '; Alert: ' + curAl)
        myUpdData('alertFails', '0')
        try {
            if(curAl==null) {
                clearAlerts()
            } else {
                myUpdData('noAlert','false')
                myUpdData('alert', curAl)
                myUpdData('alertTileLink', '<a style="font-style:italic;color:red;" href="https://forecast.weather.gov/MapClick.php?lat=' + altLat + '&lon=' + altLon +'" target=\'_blank\'>'+myGetData('alert')+sACB)
                myUpdData('alertLink', '<a style="font-style:italic;color:red;" href="https://forecast.weather.gov/MapClick.php?lat=' + altLat + '&lon=' + altLon + '" target=\'_blank\'>'+myGetData('alert')+sACB)
                String al3 = '<a style="font-style:italic;color:red;" href="https://forecast.weather.gov/MapClick.php?lat=' + altLat + '&lon=' + altLon + '" target="_blank">'
                myUpdData('alertLink2', al3 + myGetData('alert')+sACB)
                myUpdData('alertLink3', '<a style="font-style:italic;color:red;" target=\'_blank\'>' + myGetData('alert')+sACB)
                myUpdData('possAlert', 'true')
            }
            myUpdData('alertFails', '0')
            
        } catch (e) {
            alertErr('NWS Alert Poll Failed Three Times. This is a NWS API website issue.')
        }
    } else {
        alertErr('NWS Alert Poll Failed Three Times. This is a NWS API website issue.')
    }

    
    //  <<<<<<<<<< Begin Built alertTile >>>>>>>>>>
    String alertTile = 'Weather Alerts for ' + '<a href="https://forecast.weather.gov/MapClick.php?lat=' + altLat + '&lon=' + altLon + '" target="_blank">' + myGetData('city') + '</a><br>updated at ' + myGetData(sSUMLST) + ' on ' + myGetData('Summary_last_poll_date') + '.<br>'
    alertTile+= myGetData('alertTileLink') + sBR
    alertTile+= '<a href=\"https://forecast.weather.gov/MapClick.php?lat=' + altLat + '&lon=' + altLon + '\" target=\'_blank\'><img src=' + myGetData(sICON) + 'NWS_240px.png' + ' style=\"height:2.0em;display:inline;\"></a>'
    myUpdData('alertTile', alertTile)
    sendEvent(name: 'alert', value: myGetData('alert'))
    sendEvent(name: 'alertTile', value: myGetData('alertTile'))
    //  >>>>>>>>>> End Built alertTile <<<<<<<<<<   
}
// >>>>>>>>>> End NWS Active Alert Poll Routines <<<<<<<<<<

@Field static Map<String,String> dataStoreFLD=[:]

void myUpdData(String key, String val){
	dataStoreFLD[key]=val
	removeDataValue(key) // THIS SHOULD BE REMOVED AT SOME POINT
}

static String myGetData(String key){
	if(dataStoreFLD[key]) return (String)dataStoreFLD[key]
	else return sNULL
}

static String dumpListDesc(data, Integer level, List<Boolean> lastLevel, String listLabel, Boolean html=false){
	String str=sBLK
	Integer cnt=1
	List<Boolean> newLevel=lastLevel

	List list1=data?.collect{it}
	Integer sz=(Integer)list1.size()
	list1?.each{ par ->
		Integer t0=cnt-1
		String myStr="${listLabel}[${t0}]".toString()
		if(par instanceof Map){
			Map newmap=[:]
			newmap[myStr]=(Map)par
			Boolean t1= cnt==sz
			newLevel[level]=t1
			str += dumpMapDesc(newmap, level, newLevel, !t1, html)
		}else if(par instanceof List || par instanceof ArrayList){
			Map newmap=[:]
			newmap[myStr]=par
			Boolean t1= cnt==sz
			newLevel[level]=t1
			str += dumpMapDesc(newmap, level, newLevel, !t1, html)
		}else{
			String lineStrt='\n'
			for(Integer i=0; i<level; i++){
				lineStrt += (i+1<level)? (!lastLevel[i] ? '     │' : '      '):'      '
			}
			lineStrt += (cnt==1 && sz>1)? '┌─ ':(cnt<sz ? '├─ ' : '└─ ')
			if(html)str += '<span>'
			str += "${lineStrt}${listLabel}[${t0}]: ${par} (${getObjType(par)})".toString()
			if(html)str += sCSPAN
		}
		cnt=cnt+1
	}
	return str
}

static String dumpMapDesc(data, Integer level, List<Boolean> lastLevel, Boolean listCall=false, Boolean html=false){
	String str=sBLK
	Integer cnt=1
	Integer sz=data?.size()
	data?.each{ par ->
		String lineStrt
		List<Boolean> newLevel=lastLevel
		Boolean thisIsLast= cnt==sz && !listCall
		if(level>0){
			newLevel[(level-1)]=thisIsLast
		}
		Boolean theLast=thisIsLast
		if(level==0){
			lineStrt='\n\n • '
		}else{
			theLast= theLast && thisIsLast
			lineStrt='\n'
			for(Integer i=0; i<level; i++){
				lineStrt += (i+1<level)? (!newLevel[i] ? '     │' : '      '):'      '
			}
			lineStrt += ((cnt<sz || listCall) && !thisIsLast) ? '├─ ' : '└─ '
		}
		String objType=getObjType(par.value)
		if(par.value instanceof Map){
			if(html)str += '<span>'
			str += "${lineStrt}${(String)par.key}: (${objType})".toString()
			if(html)str += sCSPAN
			newLevel[(level+1)]=theLast
			str += dumpMapDesc((Map)par.value, level+1, newLevel, false, html)
		}
		else if(par.value instanceof List || par.value instanceof ArrayList){
			if(html)str += '<span>'
			str += "${lineStrt}${(String)par.key}: [${objType}]".toString()
			if(html)str += sCSPAN
			newLevel[(level+1)]=theLast
			str += dumpListDesc(par.value, level+1, newLevel, sBLK, html)
		}
		else{
			if(html)str += '<span>'
			str += "${lineStrt}${(String)par.key}: (${par.value}) (${objType})".toString()
			if(html)str += sCSPAN
		}
		cnt=cnt+1
	}
	return str
}

static String myObj(obj){
	if(obj instanceof String){return 'String'}
	else if(obj instanceof Map){return 'Map'}
	else if(obj instanceof List){return 'List'}
	else if(obj instanceof ArrayList){return 'ArrayList'}
	else if(obj instanceof Integer){return 'Int'}
	else if(obj instanceof BigInteger){return 'BigInt'}
	else if(obj instanceof Long){return 'Long'}
	else if(obj instanceof Boolean){return 'Bool'}
	else if(obj instanceof BigDecimal){return 'BigDec'}
	else if(obj instanceof Float){return 'Float'}
	else if(obj instanceof Byte){return 'Byte'}
	else{ return 'unknown'}
}

static String getObjType(obj){
	return "<span style='color:orange'>"+myObj(obj)+sCSPAN
}

static String getMapDescStr(data){
	String str
	List<Boolean> lastLevel=[true]
	str=dumpMapDesc(data, 0, lastLevel, false, true)
	return str!=sBLK ? str:'No Data was returned'
}

def pageDump(){
	String message=getMapDescStr(dataStoreFLD)
	log.info message
}


// >>>>>>>>>> Begin Lux Processing <<<<<<<<<<
void updateLux(Boolean pollAgain=true) {
	if(ifreInstalled()) { updated(); return }
	LOGINFO('Calling UpdateLux(' + pollAgain + ')')
	if(pollAgain) {
		String curTime = new Date().format('HH:mm', TimeZone.getDefault())
		String newLight
		if(curTime < myGetData('tw_begin') || curTime > myGetData('tw_end')) {
			newLight =  'false'
		} else {
			newLight =  'true'
		}
		if(newLight != myGetData('is_lightOld')) {
			pollOWM()
			return
		}
	}
    def (lux, bwn) = estimateLux(myGetData('condition_id').toInteger(), myGetData('cloud').toInteger())
        myUpdData('illuminance', (!lux) ? '0' : lux.toString())
        myUpdData('illuminated', String.format('%,4d', (!lux) ? 0 : lux).toString())
        myUpdData('bwn', bwn)
        if(pollAgain) PostPoll()
}
// >>>>>>>>>> End Lux Processing <<<<<<<<<<

// <<<<<<<<<< Begin Post-Poll Routines >>>>>>>>>>
void PostPoll() {
    if(ifreInstalled()) { updated(); return }
    Integer mult_twd=myGetData('mult_twd').toInteger()
    Integer mult_p=myGetData('mult_p').toInteger()
    Integer mult_r=myGetData('mult_r').toInteger()
    def sunRiseSet = parseJson(myGetData('sunRiseSet')).results
/*  SunriseSunset Data Eements */
    String tfmt='yyyy-MM-dd\'T\'HH:mm:ssXXX'
    String tfmt1=myGetData('timeFormat')
    if(localSunrisePublish){  // don't bother setting these values if it's not enabled
        sendEvent(name: tw_begin, value: new Date().parse(tfmt, sunRiseSet.civil_twilight_begin).format(tfmt1, TimeZone.getDefault()))
        sendEvent(name: sunriseTime, value: new Date().parse(tfmt, sunRiseSet.sunrise).format(tfmt1, TimeZone.getDefault()))
        sendEvent(name: noonTime, value: new Date().parse(tfmt, sunRiseSet.solar_noon).format(tfmt1, TimeZone.getDefault()))
        sendEvent(name: sunsetTime, value: new Date().parse(tfmt, sunRiseSet.sunset).format(tfmt1, TimeZone.getDefault()))
        sendEvent(name: tw_end, value: new Date().parse(tfmt, sunRiseSet.civil_twilight_end).format(tfmt1, TimeZone.getDefault()))
    }
    if(dashSharpToolsPublish || dashSmartTilesPublish || localSunrisePublish) {
        sendEvent(name: 'localSunset', value: new Date().parse(tfmt, sunRiseSet.sunset).format(tfmt1, TimeZone.getDefault())) // only needed for certain dashboards
        sendEvent(name: 'localSunrise', value: new Date().parse(tfmt, sunRiseSet.sunrise).format(tfmt1, TimeZone.getDefault())) // only needed for certain dashboards
    }

/*  Capability Data Elements */
	sendEvent(name: 'humidity', value: myGetData('humidity').toBigDecimal(), unit: '%')
    sendEvent(name: 'illuminance', value: myGetData('illuminance').toInteger(), unit: 'lx')
    sendEvent(name: 'pressure', value: myGetData('pressure').toBigDecimal(), unit: myGetData(sPMETR))
	sendEvent(name: 'pressured', value: String.format(myGetData('ddisp_p'), myGetData('pressure').toBigDecimal()), unit: myGetData(sPMETR))
	sendEvent(name: sTEMP, value: myGetData(sTEMP).toBigDecimal(), unit: myGetData(sTMETR))
    sendEvent(name: 'ultravioletIndex', value: myGetData('ultravioletIndex').toBigDecimal(), unit: 'uvi')
    sendEvent(name: 'feelsLike', value: myGetData('feelsLike').toBigDecimal(), unit: myGetData(sTMETR))

/*  'Required for Dashboards' Data Elements */
    if(dashHubitatOWMPublish || dashSharpToolsPublish || dashSmartTilesPublish) { sendEvent(name: 'city', value: myGetData('city')) }
    if(dashSharpToolsPublish) { sendEvent(name: 'forecastIcon', value: getCondCode(myGetData('condition_id').toInteger(), myGetData('is_day'))) }
    if(dashSharpToolsPublish || dashSmartTilesPublish || rainTodayPublish) { sendEvent(name: 'rainToday', value: myGetData('rainToday').toBigDecimal(), unit: myGetData(sRMETR)) }
    if(dashSharpToolsPublish || dashSmartTilesPublish) { sendEvent(name: 'weather', value: myGetData('condition_text')) }
    if(dashSharpToolsPublish || dashSmartTilesPublish) { sendEvent(name: 'weatherIcon', value: getCondCode(myGetData('condition_id').toInteger(), myGetData('is_day'))) }
    if(dashHubitatOWMPublish) { sendEvent(name: "weatherIcons", value: myGetData('OWN_icon')) }
    if(dashHubitatOWMPublish || dashSharpToolsPublish || windPublish) { sendEvent(name: 'wind', value: myGetData('wind').toBigDecimal(), unit: myGetData(sDMETR)) }
    if(dashHubitatOWMPublish) { sendEvent(name: 'windSpeed', value: myGetData('wind').toBigDecimal(), unit: myGetData(sDMETR)) }
    if(dashHubitatOWMPublish) { sendEvent(name: 'windDirection', value: myGetData('wind_degree').toInteger(), unit: 'DEGREE')   }

/*  Selected optional Data Elements */
    sendEventPublish(name: 'betwixt', value: myGetData('bwn'))
    sendEventPublish(name: 'cloud', value: myGetData('cloud').toInteger(), unit: '%')
    sendEventPublish(name: 'condition_code', value: myGetData('condition_code'))
    sendEventPublish(name: 'condition_text', value: myGetData('condition_text'))
    sendEventPublish(name: 'dewpoint', value: myGetData('dewpoint').toBigDecimal(), unit: myGetData(sTMETR))

    sendEventPublish(name: 'forecast_code', value: myGetData('forecast_code'))
    sendEventPublish(name: 'forecast_text', value: myGetData('forecast_text'))
    if(fcstHighLowPublish){ // don't bother setting these values if it's not enabled
        sendEvent(name: 'forecastHigh', value: myGetData('forecastHigh').toBigDecimal(), unit: myGetData(sTMETR))
    	sendEvent(name: 'forecastLow', value: myGetData('forecastLow').toBigDecimal(), unit: myGetData(sTMETR))
    }
    sendEventPublish(name: 'illuminated', value: myGetData('illuminated') + ' lx')
    sendEventPublish(name: 'is_day', value: myGetData('is_day'))

    if(obspollPublish){  // don't bother setting these values if it's not enabled
    	sendEvent(name: 'last_poll_Forecast', value: new Date().parse('EEE MMM dd HH:mm:ss z yyyy', myGetData('futime')).format(myGetData('dateFormat'), TimeZone.getDefault()) + ', ' + new Date().parse('EEE MMM dd HH:mm:ss z yyyy', myGetData('futime')).format(tfmt1, TimeZone.getDefault()))
        sendEvent(name: 'last_observation_Forecast', value: new Date().parse('EEE MMM dd HH:mm:ss z yyyy', myGetData('fotime')).format(myGetData('dateFormat'), TimeZone.getDefault()) + ', ' + new Date().parse('EEE MMM dd HH:mm:ss z yyyy', myGetData('fotime')).format(tfmt1, TimeZone.getDefault()))
    }

    if(precipExtendedPublish){ // don't bother setting these values if it's not enabled
        sendEvent(name: 'rainDayAfterTomorrow', value: myGetData('rainDayAfterTomorrow').toBigDecimal(), unit: myGetData(sRMETR))	
    	sendEvent(name: 'rainTomorrow', value: myGetData('rainTomorrow').toBigDecimal(), unit: myGetData(sRMETR))
    }
    sendEventPublish(name: 'vis', value: Math.round(myGetData('vis').toBigDecimal() * mult_twd) / mult_twd, unit: (myGetData(sDMETR)=='MPH' ? 'miles' : 'kilometers'))
    sendEventPublish(name: 'wind_degree', value: myGetData('wind_degree').toInteger(), unit: 'DEGREE')
    sendEventPublish(name: 'wind_direction', value: myGetData('wind_direction'))
    sendEventPublish(name: 'wind_cardinal', value: myGetData('wind_cardinal'))
    sendEventPublish(name: 'wind_gust', value: myGetData('wind_gust').toBigDecimal(), unit: myGetData(sDMETR))
    sendEventPublish(name: 'wind_string', value: myGetData('wind_string'))
    
    buildweatherSummary()
    
//    https://tinyurl.com/h7pp5yn points to https://openweathermap.org/weathermap
    String OWMIcon = '<a href="https://tinyurl.com/h7pp5yn?lat=' + altLat + '&lon=' + altLon + '&zoom=12" target="_blank"><img src=' + myGetData(sICON) + 'OWM.png style="height:2em"></a>'
    String OWMIcon2 = '<a href="https://openweathermap.org" target="_blank"><img src=' + myGetData(sICON) + 'OWM.png style="height:2em"></a>'
    String OWMText = '<a href="https://openweathermap.org" target="_blank">OpenWeatherMap.org</a>'
//  <<<<<<<<<< Begin Built 3dayfcstTile >>>>>>>>>>
    if(threedayTilePublish) {
        String ddisp_twd = myGetData('ddisp_twd')
        String ddisp_r = myGetData('ddisp_r')
        Boolean gitclose = (myGetData(sICON).toLowerCase().contains('://github.com/')) && (myGetData(sICON).toLowerCase().contains('/blob/master/'))
        String iconClose = (gitclose ? '?raw=true' : sBLK)
        String my3day = '<style type="text/css">'
        my3day += '.centerImage'
        my3day += '{text-align:center;display:inline;height:50%}'
        my3day += '</style>'
        my3day += '<table align="center">'
        my3day += '<tr>'
        my3day += '<td></td>'
        my3day += '<td>Today</td>'
	    my3day += '<td>' + myGetData('day1') + '</td>'
	    my3day += '<td>' + myGetData('day2') + '</td>'
        my3day += '</tr>'
        my3day += '<tr>'
        my3day += '<td>Now:' + String.format(ddisp_twd, myGetData(sTEMP).toBigDecimal()) + myGetData(sTMETR) + sBR + 'Feels:' + String.format(ddisp_twd, myGetData('feelsLike').toBigDecimal()) + myGetData(sTMETR) + '</td>'
        my3day += '<td>' + myGetData('imgName0') + '</td>'
	    my3day += '<td>' + myGetData('imgName1') + '</td>'
	    my3day += '<td>' + myGetData('imgName2') + '</td>'
        my3day += '</tr>'
        my3day += '<tr>'
        my3day += '<td></td>'
        my3day += '<td>' + myGetData('condition_text') + '</td>'
        my3day += '<td>' + myGetData('forecast_text1') + '</td>'
	    my3day += '<td>' + myGetData('forecast_text2') + '</td>'
        my3day += '</tr>'
        my3day += '<tr>'
        my3day += '<td>Low/High:</td>'
        my3day += '<td>' + String.format(ddisp_twd, myGetData('forecastLow').toBigDecimal()) + myGetData(sTMETR) + '/' + String.format(ddisp_twd, myGetData('forecastHigh').toBigDecimal()) + myGetData(sTMETR) + '</td>'
        my3day += '<td>' + String.format(ddisp_twd, myGetData('forecastLow1').toBigDecimal()) + myGetData(sTMETR) + '/' + String.format(ddisp_twd, myGetData('forecastHigh1').toBigDecimal()) + myGetData(sTMETR)  + '</td>'
        my3day += '<td>' + String.format(ddisp_twd, myGetData('forecastLow2').toBigDecimal()) + myGetData(sTMETR) + '/' + String.format(ddisp_twd, myGetData('forecastHigh2').toBigDecimal()) + myGetData(sTMETR) + '</td>'
        my3day += '</tr>'
        my3day += '<tr>'
        my3day += '<td>Precip:</td>'
        my3day += '<td>' + (myGetData('Precip0').toBigDecimal() > 0 ? String.format(ddisp_r, myGetData('Precip0').toBigDecimal()) + sSPC + myGetData(sRMETR) : 'None') + '</td>'
        my3day += '<td>' + (myGetData('Precip1').toBigDecimal() > 0 ? String.format(ddisp_r, myGetData('Precip1').toBigDecimal()) + sSPC + myGetData(sRMETR) : 'None') + '</td>'
        my3day += '<td>' + (myGetData('Precip2').toBigDecimal() > 0 ? String.format(ddisp_r, myGetData('Precip2').toBigDecimal()) + sSPC + myGetData(sRMETR) : 'None') + '</td>'
        my3day += '</tr>'
        my3day += '</table>'
        my3day += '<table align="center">'
        my3day += '<tr>'
        my3day += '<td><img src=' + myGetData(sICON) + 'wsr.png' + iconClose + '>' + myGetData('localSunrise') + '</td>'
        my3day += '<td><img src=' + myGetData(sICON) + 'wss.png' + iconClose + '>' + myGetData('localSunset') + '</td>'
        my3day += '</tr></table>'

        if(my3day.length() + 27 > 1024) {
            my3day = 'Too much data to display.</br></br>Current length (' + mywtile.length() + ') exceeds maximum tile length by ' + 1024 - my3day.length() - 27 + ' characters.'
        }else if((my3day.length() + OWMIcon.length()) < 1025) {
            my3day += OWMIcon
        }else if((my3day.length() + OWMIcon2.length()) < 1025) {
            my3day += OWMIcon2
        }else if((my3day.length() + OWMText.length()) < 1025) {
            my3day += OWMText
        }else{
            my3day += 'OpenWeatherMap.org'
        }
        my3day += '@' + myGetData(sSUMLST)
        LOGINFO('my3day character length: ' + my3day.length() + '; OWMIcon length: ' + OWMIcon.length() + '; OWMIcon2 length: ' + OWMIcon2.length() + '; OWMText length: ' + OWMText.length())
        sendEvent(name: 'threedayfcstTile', value: my3day.take(1024))
    }
//  >>>>>>>>>> End Built 3dayfcstTile <<<<<<<<<<
    buildMyText()

}    

void buildweatherSummary() {
    //  <<<<<<<<<< Begin Built Weather Summary text >>>>>>>>>>
    if(weatherSummaryPublish){ // don't bother setting these values if it's not enabled
		String Summary_forecastTemp = ' with a high of ' + String.format(myGetData('ddisp_twd'), myGetData('forecastHigh').toBigDecimal()) + myGetData(sTMETR) + ' and a low of ' + String.format(myGetData('ddisp_twd'), myGetData('forecastLow').toBigDecimal()) + myGetData(sTMETR) + '. '
		String Summary_precip = 'There has been ' + (myGetData('rainToday').toBigDecimal() > 0 ? String.format(myGetData('ddisp_r'), myGetData('rainToday').toBigDecimal()) + (myGetData(sRMETR) == 'in' ? ' inches' : ' millimeters') + ' of ' : ' no ') + 'precipitation today. '
        LOGINFO('Summary_precip: ' + Summary_precip)
		String Summary_vis = 'Visibility is around ' + String.format(myGetData('ddisp_twd'), myGetData('vis').toBigDecimal()) + (myGetData(sDMETR)=='MPH' ? ' miles.' : ' kilometers.')
        SummaryMessage((Boolean)settings.summaryType, myGetData('Summary_last_poll_date'), myGetData(sSUMLST), Summary_forecastTemp, Summary_precip, Summary_vis)
    }
//  >>>>>>>>>> End Built Weather Summary text <<<<<<<<<<
}
// >>>>>>>>>> End Post-Poll Routines <<<<<<<<<<
void buildMyText() {
    //  <<<<<<<<<< Begin Built mytext >>>>>>>>>>
    String OWMIcon = '<a href="https://openweathermap.org/weathermap?basemap=map&cities=true&layer=temperature&lat=' + altLat + '&lon=' + altLon + '&zoom=12" target="_blank"><img src=' + myGetData(sICON) + 'OWM.png style="height:2em;"></a>'
    String OWMIcon2 = '<a href="https://openweathermap.org" target="_blank"><img src=' + myGetData(sICON) + 'OWM.png style="height:2em;"></a>'
    String OWMText = '<a href="https://openweathermap.org" target="_blank">OpenWeatherMap.org</a>'

    if(myTilePublish){ // don't bother setting these values if it's not enabled
        Boolean gitclose = (myGetData(sICON).toLowerCase().contains('://github.com/')) && (myGetData(sICON).toLowerCase().contains('/blob/master/'))
        String iconClose = (gitclose ? '?raw=true' : sBLK)
        String iconCloseStyled = iconClose + '>'
        Boolean noAlert = (!alertPublish) ? true : (!myGetData('possAlert') || myGetData('possAlert')==sBLK || myGetData('possAlert')=='false')
        String alertStyleOpen = (noAlert ? sBLK :  '<span>')
        String alertStyleClose = (noAlert ? sBR : sCSPAN+sBR)

        BigDecimal wgust
        if(myGetData('wind_gust').toBigDecimal() < 1.0 ) {
            wgust = 0.0g
        } else {
            wgust = myGetData('wind_gust').toBigDecimal()
        }

        String mytextb = '<span style="display:inline;"><a href="https://openweathermap.org/weathermap?basemap=map&cities=true&layer=temperature&lat=' + altLat + '&lon=' + altLon + '&zoom=12" target="_blank">' + myGetData('city') + '</a><br>'
        String mytextm1 = myGetData('condition_text') + (noAlert ? sBLK : ' | ') + alertStyleOpen + (noAlert ? sBLK : myGetData('alertLink')) + alertStyleClose
        String mytextm2 = myGetData('condition_text') + (noAlert ? sBLK : ' | ') + alertStyleOpen + (noAlert ? sBLK : myGetData('alertLink2')) + alertStyleClose
        String mytexte = String.format(myGetData('ddisp_twd'), myGetData(sTEMP).toBigDecimal()) + myGetData(sTMETR) + '<img src=' + myGetData('condition_icon_url') + iconClose + ' style="height:2.2em;display:inline;">'
        mytexte+= ' Feels like ' + String.format(myGetData('ddisp_twd'), myGetData('feelsLike').toBigDecimal()) + myGetData(sTMETR) + sBR+sCSPAN
        mytexte+= '<span style="font-size:.9em;"><img src=' + myGetData(sICON) + myGetData('wind_bft_icon') + iconCloseStyled + myGetData('wind_direction') + sSPC
        mytexte+= (myGetData('wind').toBigDecimal() < 1.0 ? 'calm' : '@ ' + String.format(myGetData('ddisp_twd'), myGetData('wind').toBigDecimal()) + sSPC + myGetData(sDMETR))
        mytexte+= ', gusts ' + ((wgust < 1.0) ? 'calm' :  '@ ' + String.format(myGetData('ddisp_twd'), wgust) + sSPC + myGetData(sDMETR)) + sBR
        mytexte+= '<img src=' + myGetData(sICON) + 'wb.png' + iconCloseStyled + String.format(myGetData('ddisp_p'), myGetData('pressure').toBigDecimal()) + sSPC + myGetData(sPMETR) + '     <img src=' + myGetData(sICON) + 'wh.png' + iconCloseStyled
        mytexte+= myGetData('humidity') + '%     ' + '<img src=' + myGetData(sICON) + 'wu.png' + iconCloseStyled + (myGetData('rainToday').toBigDecimal() > 0 ? String.format(myGetData('ddisp_r'), myGetData('rainToday').toBigDecimal()) + sSPC + myGetData(sRMETR) : 'None') + sBR
        mytexte+= '<img src=' + myGetData(sICON) + 'wsr.png' + iconCloseStyled + myGetData('localSunrise') + '     <img src=' + myGetData(sICON) + 'wss.png' + iconCloseStyled
        mytexte+= myGetData('localSunset') + '     Updated: ' + myGetData(sSUMLST)

        String mytext = mytextb + mytextm1 + mytexte
        if((mytext.length() + OWMIcon.length() + 10) < 1025) {
            mytext+= sBR + OWMIcon + sCSPAN
        } else if((mytext.length() + OWMIcon2.length() + 10) < 1025) {
            mytext+= sBR + OWMIcon2 + sCSPAN
        } else if((mytext.length() + OWMText.length() + 10) < 1025) {
            mytext+= sBR + OWMText + sCSPAN
        } else {
            mytext = mytextb + mytextm2 + mytexte
            if((mytext.length() + OWMIcon.length() + 10) < 1025) {
                mytext+= sBR + OWMIcon + sCSPAN
            }else if((mytext.length() + OWMIcon2.length() + 10) < 1025) {
                mytext+= sBR + OWMIcon2 + sCSPAN
            }else if((mytext.length() + OWMText.length() + 10) < 1025) {
                mytext+= sBR + OWMText + sCSPAN
            }else{
                mytext+= sBR+'Forecast by OpenWeatherMap.org'+sCSPAN
            }
        }
        if(mytext.length() > 1024) {
            Integer iconfilepath = ('<img src=' + myGetData(sICON) + myGetData('wind_bft_icon') + iconCloseStyled).length()
            Integer excess = (mytext.length() - 1024)
            Integer removeicons    // = 0
            Integer ics = iconfilepath + iconCloseStyled.length()
            if((excess - ics + 11) < 0) {
                removeicons = 1  //Remove sunset
            }else if((excess - (ics * 2) + 20) < 0) {
                removeicons = 2 //remove sunset and sunrise
            }else if((excess - (ics * 3) + 31) < 0) {
                removeicons = 3 //remove sunset, sunrise, Precip
            }else if((excess - (ics * 4) + 38) < 0) {
                removeicons = 4 //remove sunset, sunrise, Precip, Humidity
            }else if((excess - (ics * 5) + 42) < 0) {
                removeicons = 5 //remove sunset, sunrise, Precip, Humidity, Pressure
            }else if((excess - (ics * 6) + 42) < 0) {
                removeicons = 6 //remove sunset, sunrise, Precip, Humidity, Pressure, Wind
            }else if((excess - (ics * 7) + 42) < 0) {
                removeicons = 7 //remove sunset, sunrise, Precip, Humidity, Pressure, Wind, condition
            }else{
                removeicons = 8 // still need to remove html formatting
            }
            if(removeicons < 8) {
                LOGDEBUG('myTile exceeds 1,024 characters (' + mytext.length() + ') ... removing last ' + (removeicons + 1).toString() + ' icons.')
                mytext = '<span>' + myGetData('city') + sBR
                mytext+= myGetData('condition_text') + (noAlert ? sBLK : ' | ') + alertStyleOpen + (noAlert ? sBLK : myGetData('alert')) + alertStyleClose + sBR
                mytext+= String.format(myGetData('ddisp_twd'), myGetData(sTEMP).toBigDecimal()) + myGetData(sTMETR) + (removeicons < 7 ? '<img src=' + myGetData('condition_icon_url') + iconClose + ' style=\"height:2.0em;display:inline;\">' : sBLK)
                mytext+= ' Feels like ' + String.format(myGetData('ddisp_twd'), myGetData('feelsLike').toBigDecimal()) + myGetData(sTMETR) + sBR+sCSPAN
                mytext+= '<span style="font-size:.8em;">' + (removeicons < (raintoday ? 7 : 6) ? '<img src=' + myGetData(sICON) + myGetData('wind_bft_icon') + iconCloseStyled : sBLK) + myGetData('wind_direction') + sSPC
                mytext+= (removeicons < 6 ? '<img src=' + myGetData(sICON) + myGetData('wind_bft_icon') + iconCloseStyled : sBLK) + myGetData('wind_direction') + sSPC
                mytext+= (myGetData('wind').toBigDecimal() < 1.0 ? 'calm' : '@ ' + String.format(myGetData('ddisp_twd'), myGetData('wind').toBigDecimal()) + sSPC + myGetData(sDMETR))
                mytext+= ', gusts ' + ((wgust < 1.0) ? 'calm' :  '@ ' + String.format(myGetData('ddisp_twd'), wgust) + sSPC + myGetData(sDMETR)) + sBR
                mytext+= (removeicons < 5 ? '<img src=' + myGetData(sICON) + 'wb.png' + iconCloseStyled : 'Bar: ') + String.format(myGetData('ddisp_p'), myGetData('pressure').toBigDecimal()) + sSPC + myGetData(sPMETR) + '  '
                mytext+= (removeicons < 4 ? '<img src=' + myGetData(sICON) + 'wh.png' + iconCloseStyled : ' | Hum: ') + myGetData('humidity') + '%  '
                mytext+= (removeicons < 3 ? '<img src=' + myGetData(sICON) + 'wu.png' + iconCloseStyled : ' | Precip: ') + (myGetData('rainToday').toBigDecimal() > 0 ? String.format(myGetData('ddisp_r'), myGetData('rainToday').toBigDecimal()) + sSPC + myGetData(sRMETR) : sBLK) + sBR
                mytext+= (removeicons < 2 ? '<img src=' + myGetData(sICON) + 'wsr.png' + iconCloseStyled : 'Sunrise: ') + myGetData('localSunrise') + '  '
                mytext+= (removeicons < 1 ? '<img src=' + myGetData(sICON) + 'wss.png' + iconCloseStyled : ' | Sunset: ') + myGetData('localSunset')
                mytext+= '     Updated ' + myGetData(sSUMLST) + sCSPAN
            }else{
                LOGINFO('myTile still exceeds 1,024 characters (' + mytext.length() + ') ... removing all formatting.')
                mytext = myGetData('city') + sBR
                mytext+= myGetData('condition_text') + (noAlert ? sBLK : ' | ') + (noAlert ? sBLK : myGetData('alert')) + sBR
                mytext+= String.format(myGetData('ddisp_twd'), myGetData(sTEMP).toBigDecimal()) + myGetData(sTMETR) + ' Feels like ' + String.format(myGetData('ddisp_twd'), myGetData('feelsLike').toBigDecimal()) + myGetData(sTMETR) + sBR
                mytext+= myGetData('wind_direction') + sSPC
                mytext+= myGetData('wind').toBigDecimal() < 1.0 ? 'calm' : '@ ' + String.format(myGetData('ddisp_twd'), myGetData('wind').toBigDecimal()) + sSPC + myGetData(sDMETR)
                mytext+= ', gusts ' + ((wgust < 1.0) ? 'calm' :  '@ ' + String.format(myGetData('ddisp_twd'), wgust) + sSPC + myGetData(sDMETR)) + sBR
                mytext+= 'Bar: ' + String.format(myGetData('ddisp_p'), myGetData('pressure').toBigDecimal()) + sSPC + myGetData(sPMETR)
                mytext+= ' | Hum: ' + myGetData('humidity') + '%  ' + ' | Precip: ' + (myGetData('rainToday').toBigDecimal() > 0 ? String.format(myGetData('ddisp_r'), myGetData('rainToday').toBigDecimal()) + sSPC + myGetData(sRMETR) : sBLK) + sBR
                mytext+= 'Sunrise: ' + myGetData('localSunrise') + ' | Sunset:' + myGetData('localSunset') + ' |  Updated:' + myGetData(sSUMLST)
                if(mytext.length() > 1024) {
                    LOGINFO('myTile even still exceeds 1,024 characters (' + mytext.length() + ') ... truncating.')
                }
            }
        }
        LOGINFO('mytext: ' + mytext)
        sendEvent(name: 'myTile', value: mytext.take(1024))
    }
//  >>>>>>>>>> End Built mytext <<<<<<<<<<
}
public void refresh() {
    updateLux(true)
}

void installed() {
}

@Field static String verFLD=''

static Boolean ifreInstalled(){
	if(verFLD!=version()) return true
	return false
}

void updated(){
	LOGINFO("running updated()")
	unschedule()
	verFLD=version()
	initMe()
	updateCheck()
	runIn(5,finishSched)
}

void finishSched() {
    pollSunRiseSet()
    initialize_poll()
    runEvery10Minutes(updateLux, [Data: [true]])
	Random rand = new Random(now())
	Integer ssseconds = rand.nextInt(60)
    schedule("${ssseconds} 20 0/8 ? * * *", pollSunRiseSet)
	runIn(5, pollData)
	if(settingEnable) runIn(2100,settingsOff)// 'roll up' (hide) the condition selectors after 35 min
    if(settings.logSet) runIn(1800,logsOff)// turns off extended logging after 30 min
	Integer r_minutes = rand.nextInt(60)
    schedule("0 ${r_minutes} 8 ? * FRI *", updateCheck)
}
                    
void initMe() {
    myUpdData('is_light', 'true')
    myUpdData('is_lightOld', myGetData('is_light')) //avoid startup oscilation
    String city = (settings.city ?: sBLK)
    myUpdData('city', city)
    Boolean altCoord = (settings.altCoord ?: false)
    String valtLat = location.latitude.toString().replace(sSPC, sBLK)
    String valtLon = location.longitude.toString().replace(sSPC, sBLK)
    String altLat = settings.altLat ?: valtLat
    String altLon = settings.altLon ?: valtLon
    if (altCoord) {
        if (altLat == null) {
            device.updateSetting('altLat', [value:valtLat,type:'text'])
        }
        if (altLon == null) {
            device.updateSetting('altLon', [value:valtLon,type:'text'])
        }
        if (altLat == null || altLon == null) {
            if ((valtLat == null) || (valtLat = sBLK)) {
                LOGERR('The Override Coorinates feature is selected but Both Hub & the Override Latitude are null.')
            } else {
                device.updateSetting('altLat', [value:valtLat,type:'text'])
            }
            if ((valtLon == null) || (valtLon = sBLK)) {
                LOGERR('The Override Coorinates feature is selected but Both Hub & the Override Longitude are null.')
            } else {
                device.updateSetting('altLon', [value:valtLon,type:'text'])
            }
        }
    } else {
        device.updateSetting('altLat', [value:valtLat,type:'text'])
        device.updateSetting('altLon', [value:valtLon,type:'text'])
        if (altLat == null || altLon == null) {
            if ((valtLat == null) || (valtLat = sBLK)) {
                LOGERR('The Hub\'s latitude is not set. Please set it, or use the Override Coorinates feature.')
            } else {
                device.updateSetting('altLat', [value:valtLat,type:'text'])
            }
            if ((valtLon == null) || (valtLon = sBLK)) {
                LOGERR('The Hub\'s longitude is not set. Please set it, or use the Override Coorinates feature.')
            } else {
                device.updateSetting('altLon', [value:valtLon,type:'text'])
            }
        }
    }
    Boolean iconType = (settings.iconType ?: false)
    myUpdData('iconType', iconType ? 'true' : 'false')
    String iconLocation = (settings.iconLocation ?: 'https://tinyurl.com/y6xrbhpf/')
    myUpdData(sICON, iconLocation)
    state.OWM = '<a href="https://openweathermap.org" target="_blank"><img src=' + myGetData(sICON) + 'OWM.png style="height:2em;"></a>'
    setDateTimeFormats((String)settings.datetimeFormat)
    String distanceFormat = (settings.distanceFormat ?: 'Miles (mph)')
    String pressureFormat = (settings.pressureFormat ?: 'Inches')
    String rainFormat = (settings.rainFormat ?: 'Inches')
    String tempFormat = (settings.tempFormat ?: 'Fahrenheit (°F)')
    setMeasurementMetrics(distanceFormat, pressureFormat, rainFormat, tempFormat)    
    String TWDDecimals = (settings.TWDDecimals ?: '0')
    String PDecimals = (settings.PDecimals ?: '0')
    String RDecimals = (settings.RDecimals ?: '0')
    setDisplayDecimals(TWDDecimals, PDecimals, RDecimals)
}

void initialize_poll() {
    unschedule(pollOWM)
	Random rand = new Random(now())
	Integer ssseconds = rand.nextInt(60)
	Integer minutes2 = rand.nextInt(2)
	Integer minutes5 = rand.nextInt(5)
	Integer minutes10 = rand.nextInt(10)
	Integer minutes15 = rand.nextInt(15)
	Integer minutes30 = rand.nextInt(30)
	Integer minutes60 = rand.nextInt(60)
	Integer hours3 = rand.nextInt(3)
	Integer dsseconds
	if(ssseconds < 56 ){
		dsseconds = ssseconds + 4
	} else {
		dsseconds = ssseconds - 60 + 4
	}
    String pollIntervalFcst = (settings.pollIntervalForecast ?: '3 Hours')
    String pollIntervalFcstnight = (settings.pollIntervalForecastnight ?: '3 Hours')
	if(myGetData('is_light')=='true') {
        myPoll = pollIntervalFcst
	} else {
        myPoll = pollIntervalFcstnight
    }
    if(myPoll == 'Manual Poll Only'){
			LOGINFO('MANUAL FORECAST POLLING ONLY')
    } else {
        myPoll = myPoll.replace(sSPC,sBLK)
        String mySched = "${dsseconds} ${minutes60} ${hours3/3} * * ? *"
        LOGINFO('pollInterval: ' + myPoll)
        switch(myPoll) {
            case '2Minutes':
                mySched = "${dsseconds} ${minutes2}/2 * * * ? *"
                break
            case '5Minutes':
                mySched = "${dsseconds} ${minutes5}/5 * * * ? *"
                break
            case '10 Minutes':
                mySched = "${dsseconds} ${minutes10}/10 * * * ? *"
                break
            case '15Minutes':
                mySched = "${dsseconds} ${minutes15}/15 * * * ? *"
                break
            case '30Minutes':
                mySched = "${dsseconds} ${minutes30}/30 * * * ? *"
                break
            case '1Hour':
                mySched = "${dsseconds} ${minutes60} * * * ? *"
                break
            case '3Hours':
                mySched = "${dsseconds} ${minutes60} ${hours3}/3 * * ? *"
	}
        schedule(mySched, pollOWM)
    }
}

public void pollData() {
	pollOWM()
}

// ************************************************************************************************

public void setDateTimeFormats(String formatselector){
    String mSel = formatselector ?: '1'
    String DTFormat
    String dateFormat
    String timeFormat
    switch(mSel) {
        case '1': DTFormat = 'M/d/yyyy h:mm a';   dateFormat = 'M/d/yyyy';   timeFormat = 'h:mm a'; break
        case '2': DTFormat = 'M/d/yyyy HH:mm';    dateFormat = 'M/d/yyyy';   timeFormat = 'HH:mm';  break
        case '3': DTFormat = 'MM/dd/yyyy h:mm a'; dateFormat = 'MM/dd/yyyy'; timeFormat = 'h:mm a'; break
        case '4': DTFormat = 'MM/dd/yyyy HH:mm';  dateFormat = 'MM/dd/yyyy'; timeFormat = 'HH:mm';  break
        case '5': DTFormat = 'd/M/yyyy h:mm a';   dateFormat = 'd/M/yyyy';   timeFormat = 'h:mm a'; break
        case '6': DTFormat = 'd/M/yyyy HH:mm';    dateFormat = 'd/M/yyyy';   timeFormat = 'HH:mm';  break
        case '7': DTFormat = 'dd/MM/yyyy h:mm a'; dateFormat = 'dd/MM/yyyy'; timeFormat = 'h:mm a'; break
        case '8': DTFormat = 'dd/MM/yyyy HH:mm';  dateFormat = 'dd/MM/yyyy'; timeFormat = 'HH:mm';  break
        case '9': DTFormat = 'yyyy/MM/dd HH:mm';  dateFormat = 'yyyy/MM/dd'; timeFormat = 'HH:mm';  break
        default: DTFormat = 'M/d/yyyy h:mm a';  dateFormat = 'M/d/yyyy';   timeFormat = 'h:mm a'; break
	}
    myUpdData('DTFormat', DTFormat)
    myUpdData('dateFormat', dateFormat)
    myUpdData('timeFormat', timeFormat)
}

public void setMeasurementMetrics(distFormat, pressFormat, precipFormat, temptFormat){
    String dMetric
    String pMetric
    String rMetric
    String tMetric 
    if(distFormat == 'Miles (mph)') {
        dMetric = 'MPH'
    } else if(distFormat == 'knots') {
        dMetric = 'knots'
    } else if(distFormat == 'Kilometers (kph)') {
        dMetric = 'KPH'
    } else {
        dMetric = 'm/s'
    }
    myUpdData(sDMETR, dMetric)

    if(pressFormat == 'Millibar') {
        pMetric = 'MBAR'
    } else if(pressFormat == 'Inches') {
        pMetric = 'inHg'
    } else {
        pMetric = 'hPa'
    }
    myUpdData(sPMETR, pMetric)

    if(precipFormat == 'Millimeters') {
        rMetric = 'mm'
    } else {
        rMetric = 'in'
    }
    myUpdData(sRMETR, rMetric)

    if(temptFormat == 'Fahrenheit (°F)') {
        tMetric = '°F'
    } else {
        tMetric = '°C'
    }
    myUpdData(sTMETR, tMetric)
}

public void setDisplayDecimals(TWDDisp, PressDisp, RainDisp) {
    String ddisp_twd
    String mult_twd
    String ddisp_p
    String mult_p
    String ddisp_r
    String mult_r
    switch(TWDDisp) {
        case '0': ddisp_twd = '%3.0f'; mult_twd = '1'; break
        case '1': ddisp_twd = '%3.1f'; mult_twd = '10'; break
        case '2': ddisp_twd = '%3.2f'; mult_twd = '100'; break
        case '3': ddisp_twd = '%3.3f'; mult_twd = '1000'; break
        case '4': ddisp_twd = '%3.4f'; mult_twd = '10000'; break
        default: ddisp_twd = '%3.0f'; mult_twd = '1'; break
    }
    myUpdData('ddisp_twd', ddisp_twd)
    myUpdData('mult_twd', mult_twd)
    switch(PressDisp) {
        case '0': ddisp_p = '%,4.0f'; mult_p = '1'; break
        case '1': ddisp_p = '%,4.1f'; mult_p = '10'; break
        case '2': ddisp_p = '%,4.2f'; mult_p = '100'; break
        case '3': ddisp_p = '%,4.3f'; mult_p = '1000'; break
        case '4': ddisp_p = '%,4.4f'; mult_p = '10000'; break
        default: ddisp_p = '%,4.0f'; mult_p = '1'; break
    }
    myUpdData('ddisp_p', ddisp_p)
    myUpdData('mult_p', mult_p)
    switch(RainDisp) {
        case '0': ddisp_r = '%2.0f'; mult_r = '1'; break
        case '1': ddisp_r = '%2.1f'; mult_r = '10'; break
        case '2': ddisp_r = '%2.2f'; mult_r = '100'; break
        case '3': ddisp_r = '%2.3f'; mult_r = '1000'; break
        case '4': ddisp_r = '%2.4f'; mult_r = '10000'; break
        default: ddisp_r = '%2.0f'; mult_r = '1'; break
    }
    myUpdData('ddisp_r', ddisp_r)
    myUpdData('mult_r', mult_r)
}

def estimateLux(Integer condition_id, Integer cloud)     {	
	Long lux // = 0L
	Boolean aFCC = true
	Double l
	String bwn
	def sunRiseSet            = parseJson(myGetData('sunRiseSet')).results
	def tZ                    = TimeZone.getDefault() //TimeZone.getTimeZone(tz_id)
	String lT                 = new Date().format('yyyy-MM-dd\'T\'HH:mm:ssXXX', tZ)
	Long localeMillis         = getEpoch(lT)
	Long twilight_beginMillis = getEpoch(sunRiseSet.civil_twilight_begin)
	Long sunriseTimeMillis    = getEpoch(sunRiseSet.sunrise)
	Long noonTimeMillis       = getEpoch(sunRiseSet.solar_noon)
	Long sunsetTimeMillis     = getEpoch(sunRiseSet.sunset)
	Long twilight_endMillis   = getEpoch(sunRiseSet.civil_twilight_end)
	Long twiStartNextMillis   = twilight_beginMillis + 86400000L // = 24*60*60*1000 --> one day in milliseconds
	Long sunriseNextMillis    = sunriseTimeMillis + 86400000L
	Long noonTimeNextMillis   = noonTimeMillis + 86400000L
	Long sunsetNextMillis     = sunsetTimeMillis + 86400000L
	Long twiEndNextMillis     = twilight_endMillis + 86400000L

	switch(localeMillis) { 
		case { it < twilight_beginMillis}: 
			bwn = 'Fully Night Time'
			lux = 5l
            aFCC = false
			break
		case { it < sunriseTimeMillis}:
			bwn = 'between twilight and sunrise'
			l = (((localeMillis - twilight_beginMillis) * 50f) / (sunriseTimeMillis - twilight_beginMillis))
			lux = (l < 10f ? 10l : l.trunc(0) as Long)
			break
		case { it < noonTimeMillis}:
			bwn = 'between sunrise and noon'
			l = (((localeMillis - sunriseTimeMillis) * 10000f) / (noonTimeMillis - sunriseTimeMillis))
			lux = (l < 50f ? 50l : l.trunc(0) as Long)
			break
		case { it < sunsetTimeMillis}:
			bwn = 'between noon and sunset'
			l = (((sunsetTimeMillis - localeMillis) * 10000f) / (sunsetTimeMillis - noonTimeMillis))
			lux = (l < 50f ? 50l : l.trunc(0) as Long)
			break
		case { it < twilight_endMillis}:
			bwn = 'between sunset and twilight'
			l = (((twilight_endMillis - localeMillis) * 50f) / (twilight_endMillis - sunsetTimeMillis))
			lux = (l < 10f ? 10l : l.trunc(0) as Long)
			break
		case { it < twiStartNextMillis}:
			bwn = 'Fully Night Time'
			lux = 5l
            aFCC = false
			break
		case { it < sunriseNextMillis}:
			bwn = 'between twilight and sunrise'
			l = (((localeMillis - twiStartNextMillis) * 50f) / (sunriseNextMillis - twiStartNextMillis))
			lux = (l < 10f ? 10l : l.trunc(0) as Long)
			break
		case { it < noonTimeNextMillis}:
			bwn = 'between sunrise and noon'
			l = (((localeMillis - sunriseNextMillis) * 10000f) / (noonTimeNextMillis - sunriseNextMillis))
			lux = (l < 50f ? 50l : l.trunc(0) as Long)
			break
		case { it < sunsetNextMillis}:
			bwn = 'between noon and sunset'
			l = (((sunsetNextMillis - localeMillis) * 10000f) / (sunsetNextMillis - noonTimeNextMillis))
			lux = (l < 50f ? 50l : l.trunc(0) as Long)
			break
		case { it < twiEndNextMillis}:
			bwn = 'between sunset and twilight'
			l = (((twiEndNextMillis - localeMillis) * 50f) / (twiEndNextMillis - sunsetNextMillis))
			lux = (l < 10f ? 10l : l.trunc(0) as Long)
			break
		default:
			bwn = 'Fully Night Time'
			lux = 5l
            aFCC = false
			break
	}
    String cC = condition_id.toString()
	String cCT = ' using cloud cover from API'
    Double cCF = (!cloud || cloud==sBLK) ? 0.998d : (1 - (cloud/100 / 3d))
    if(aFCC){
        if(!cloud){
            LUitem = LUTable.find{ it.id == condition_id }
			if (LUitem)    {
				cCF = (LUitem ? (LUitem.luxpercent / 3d) : 0.998d)
				cCT = ' using estimated cloud cover based on condition.'
            } else {
                cCF = 1.0
		        cCT = ' cloud coverage not available now.'
			}
        }
    }
	lux = (lux * cCF) as Long
    Boolean t_jitter = (!settings.luxjitter) ? false : settings.luxjitter
    if(t_jitter){
        // reduce event variability  code from @nh.schottfam
        if(lux > 1100) {
            Long t0 = (lux/800)
            lux = t0 * 800
        } else if(lux <= 1100 && lux > 400) {
            Long t0 = (lux/400)
            lux = t0 * 400
        } else {
            lux = 5
        }
    }
    lux = Math.max(lux, 5)
	LOGINFO('estimateLux results: condition: ' + cC + ' | condition factor: ' + cCF + ' | condition text: ' + cCT + '| lux: ' + lux)
	return [lux, bwn]
}

private Long getEpoch (String aTime) {
	def tZ = TimeZone.getDefault()
	def localeTime = new Date().parse('yyyy-MM-dd\'T\'HH:mm:ssXXX', aTime, tZ)
	Long localeMillis = localeTime.getTime()
	return (localeMillis)
}

void SummaryMessage(Boolean SType, String Slast_poll_date, String Slast_poll_time, String SforecastTemp, String Sprecip, String Svis){
    BigDecimal windgust
    if(myGetData('wind_gust') == sBLK || myGetData('wind_gust').toBigDecimal() < 1.0 || myGetData('wind_gust')==null) {
        windgust = 0.00g
    } else {
        windgust = myGetData('wind_gust').toBigDecimal()
    }
    String wSum // = (String)null
    if(SType){
        wSum = 'Weather summary for ' + myGetData('city') + ' updated at ' + Slast_poll_time + ' on ' + Slast_poll_date + '. '
        wSum+= myGetData('condition_text')
        wSum+= (!SforecastTemp || SforecastTemp==sBLK) ? '. ' : SforecastTemp
        wSum+= 'Humidity is ' + myGetData('humidity') + '% and the temperature is ' + String.format(myGetData('ddisp_twd'), myGetData(sTEMP).toBigDecimal()) + myGetData(sTMETR) + '. '
        wSum+= 'The temperature feels like it is ' + String.format(myGetData('ddisp_twd'), myGetData('feelsLike').toBigDecimal()) + myGetData(sTMETR) + '. '
        wSum+= 'Wind: ' + myGetData('wind_string') + ', gusts: ' + ((windgust < 1.00) ? 'calm. ' : 'up to ' + windgust.toString() + sSPC + myGetData(sDMETR) + '. ')
        wSum+= Sprecip
        wSum+= Svis
        wSum+= alertPublish ? ((!myGetData('alert') || myGetData('alert')==null) ? sBLK : sSPC + myGetData('alert') + '.') : sBLK
    } else {
        wSum = myGetData('condition_text') + sSPC
        wSum+= ((!SforecastTemp || SforecastTemp==sBLK) ? '. ' : SforecastTemp)
        wSum+= ' Humidity: ' + myGetData('humidity') + '%. Temperature: ' + String.format(myGetData('ddisp_twd'), myGetData(sTEMP).toBigDecimal()) + myGetData(sTMETR) + '. '
        wSum+= myGetData('wind_string') + ', gusts: ' + ((windgust == 0.00) ? 'calm. ' : 'up to ' + windgust + myGetData(sDMETR) + '.')
	}
    wSum = wSum.take(1024)
    sendEvent(name: 'weatherSummary', value: wSum)
}

String getImgName(Integer wCode, String iconTOD){
    LUitem = LUTable.find{ it.id == wCode }
	LOGINFO('getImgName Inputs: ' + wCode.toString() + ', ' + iconTOD + ';  Result: ' + (iconTOD=='true' ? (LUitem ? LUitem.Icond : 'na.png') : (LUitem ? LUitem.Iconn : 'na.png')))
    return (iconTOD=='true' ? (LUitem ? LUitem.Icond : 'na.png') : (LUitem ? LUitem.Iconn : 'na.png'))
}

String getCondCode(Integer cid, String iconTOD){
    LUitem = LUTable.find{ it.id == cid }
    LOGINFO('getCondCode Inputs: ' + cid.toString() + ', ' + iconTOD + ';  Result: ' + (iconTOD=='true' ? (LUitem ? LUitem.stdIcond : 'na.png') : (LUitem ? LUitem.stdIconn : 'na.png')))
    return (iconTOD=='true' ? (LUitem ? LUitem.stdIcond : 'na.png') : (LUitem ? LUitem.stdIconn : 'na.png'))
}

void logCheck(){
    if(settings.logSet){
        log.info 'OpenWeatherMap.org Weather Driver - INFO:  All Logging Enabled'
    } else {
        log.info 'OpenWeatherMap.org Weather Driver - INFO:  Further Logging Disabled'
    }
}

void LOGDEBUG(txt){
    if(settings.logSet){ log.debug('OpenWeatherMap.org Weather Driver - DEBUG:  ' + txt) }
}

void LOGINFO(txt){
    if(settings.logSet){log.info('OpenWeatherMap.org Weather Driver - INFO:  ' + txt) }
}

void LOGWARN(txt){
    if(settings.logSet){log.warn('OpenWeatherMap.org Weather Driver - WARNING:  ' + txt) }
}

void LOGERR(txt){
    if(settings.logSet){log.error('OpenWeatherMap.org Weather Driver - ERROR:  ' + txt) }
}

void logsOff(){
	log.info 'OpenWeatherMap.org Weather Driver - INFO:  extended logging disabled...'
	device.updateSetting('logSet',[value:'false',type:'bool'])
}

void settingsOff(){
	log.info 'OpenWeatherMap.org Weather Driver - INFO: Settings disabled...'
	device.updateSetting('settingEnable',[value:'false',type:'bool'])
}

void sendEventPublish(evt)	{
// 	Purpose: Attribute sent to DB if selected	
    if (settings."${evt.name + 'Publish'}") {
		sendEvent(name: evt.name, value: evt.value, descriptionText: evt.descriptionText, unit: evt.unit, displayed: evt.displayed)
		LOGINFO('Will publish: ' + evt.name) //: evt.name, evt.value evt.unit'
    }
}

@Field final List    LUTable =     [
[id: 200, OWMd: '11d.png', OWMn: '11n.png', Icond: '38.png', Iconn: '47.png', luxpercent: 0.2, stdIcond: 'chancetstorms', stdIconn: 'nt_chancetstorms'],
[id: 201, OWMd: '11d.png', OWMn: '11n.png', Icond: '38.png', Iconn: '47.png', luxpercent: 0.2, stdIcond: 'chancetstorms', stdIconn: 'nt_chancetstorms'],
[id: 202, OWMd: '11d.png', OWMn: '11n.png', Icond: '38.png', Iconn: '47.png', luxpercent: 0.2, stdIcond: 'chancetstorms', stdIconn: 'nt_chancetstorms'],
[id: 210, OWMd: '11d.png', OWMn: '11n.png', Icond: '38.png', Iconn: '47.png', luxpercent: 0.2, stdIcond: 'chancetstorms', stdIconn: 'nt_chancetstorms'],
[id: 211, OWMd: '11d.png', OWMn: '11n.png', Icond: '38.png', Iconn: '47.png', luxpercent: 0.2, stdIcond: 'chancetstorms', stdIconn: 'nt_chancetstorms'],
[id: 212, OWMd: '11d.png', OWMn: '11n.png', Icond: '38.png', Iconn: '47.png', luxpercent: 0.2, stdIcond: 'chancetstorms', stdIconn: 'nt_chancetstorms'],
[id: 221, OWMd: '11d.png', OWMn: '11n.png', Icond: '38.png', Iconn: '47.png', luxpercent: 0.2, stdIcond: 'chancetstorms', stdIconn: 'nt_chancetstorms'],
[id: 230, OWMd: '11d.png', OWMn: '11n.png', Icond: '38.png', Iconn: '47.png', luxpercent: 0.2, stdIcond: 'chancetstorms', stdIconn: 'nt_chancetstorms'],
[id: 231, OWMd: '11d.png', OWMn: '11n.png', Icond: '38.png', Iconn: '47.png', luxpercent: 0.2, stdIcond: 'chancetstorms', stdIconn: 'nt_chancetstorms'],
[id: 232, OWMd: '11d.png', OWMn: '11n.png', Icond: '38.png', Iconn: '47.png', luxpercent: 0.2, stdIcond: 'chancetstorms', stdIconn: 'nt_chancetstorms'],
[id: 300, OWMd: '09d.png', OWMn: '09n.png', Icond: '9.png', Iconn: '9.png', luxpercent: 0.5, stdIcond: 'rain', stdIconn: 'nt_rain'],
[id: 301, OWMd: '09d.png', OWMn: '09n.png', Icond: '9.png', Iconn: '9.png', luxpercent: 0.5, stdIcond: 'rain', stdIconn: 'nt_rain'],
[id: 302, OWMd: '09d.png', OWMn: '09n.png', Icond: '9.png', Iconn: '9.png', luxpercent: 0.5, stdIcond: 'rain', stdIconn: 'nt_rain'],
[id: 310, OWMd: '09d.png', OWMn: '09n.png', Icond: '9.png', Iconn: '9.png', luxpercent: 0.5, stdIcond: 'rain', stdIconn: 'nt_rain'],
[id: 311, OWMd: '09d.png', OWMn: '09n.png', Icond: '9.png', Iconn: '9.png', luxpercent: 0.5, stdIcond: 'rain', stdIconn: 'nt_rain'],
[id: 312, OWMd: '09d.png', OWMn: '09n.png', Icond: '9.png', Iconn: '9.png', luxpercent: 0.5, stdIcond: 'rain', stdIconn: 'nt_rain'],
[id: 313, OWMd: '09d.png', OWMn: '09n.png', Icond: '9.png', Iconn: '9.png', luxpercent: 0.5, stdIcond: 'rain', stdIconn: 'nt_rain'],
[id: 314, OWMd: '09d.png', OWMn: '09n.png', Icond: '9.png', Iconn: '9.png', luxpercent: 0.5, stdIcond: 'rain', stdIconn: 'nt_rain'],
[id: 321, OWMd: '09d.png', OWMn: '09n.png', Icond: '9.png', Iconn: '9.png', luxpercent: 0.5, stdIcond: 'rain', stdIconn: 'nt_rain'],
[id: 500, OWMd: '10d.png', OWMn: '09n.png', Icond: '39.png', Iconn: '9.png', luxpercent: 0.5, stdIcond: 'rain', stdIconn: 'nt_rain'],
[id: 501, OWMd: '10d.png', OWMn: '10n.png', Icond: '39.png', Iconn: '11.png', luxpercent: 0.5, stdIcond: 'rain', stdIconn: 'nt_rain'],
[id: 502, OWMd: '10d.png', OWMn: '10n.png', Icond: '39.png', Iconn: '11.png', luxpercent: 0.5, stdIcond: 'rain', stdIconn: 'nt_rain'],
[id: 503, OWMd: '10d.png', OWMn: '10n.png', Icond: '39.png', Iconn: '11.png', luxpercent: 0.5, stdIcond: 'rain', stdIconn: 'nt_rain'],
[id: 504, OWMd: '10d.png', OWMn: '10n.png', Icond: '39.png', Iconn: '11.png', luxpercent: 0.5, stdIcond: 'rain', stdIconn: 'nt_rain'],
[id: 511, OWMd: '10d.png', OWMn: '10n.png', Icond: '39.png', Iconn: '11.png', luxpercent: 0.5, stdIcond: 'rain', stdIconn: 'nt_rain'],
[id: 520, OWMd: '10d.png', OWMn: '09n.png', Icond: '39.png', Iconn: '9.png', luxpercent: 0.5, stdIcond: 'rain', stdIconn: 'nt_rain'],
[id: 521, OWMd: '10d.png', OWMn: '10n.png', Icond: '39.png', Iconn: '11.png', luxpercent: 0.5, stdIcond: 'rain', stdIconn: 'nt_rain'],
[id: 522, OWMd: '10d.png', OWMn: '10n.png', Icond: '39.png', Iconn: '11.png', luxpercent: 0.5, stdIcond: 'rain', stdIconn: 'nt_rain'],
[id: 531, OWMd: '10d.png', OWMn: '09n.png', Icond: '39.png', Iconn: '9.png', luxpercent: 0.5, stdIcond: 'rain', stdIconn: 'nt_rain'],
[id: 600, OWMd: '13d.png', OWMn: '13n.png', Icond: '13.png', Iconn: '46.png', luxpercent: 0.4, stdIcond: 'flurries', stdIconn: 'nt_snow'],
[id: 601, OWMd: '13d.png', OWMn: '13n.png', Icond: '14.png', Iconn: '46.png', luxpercent: 0.3, stdIcond: 'snow', stdIconn: 'nt_snow'],
[id: 602, OWMd: '13d.png', OWMn: '13n.png', Icond: '16.png', Iconn: '46.png', luxpercent: 0.3, stdIcond: 'snow', stdIconn: 'nt_snow'],
[id: 611, OWMd: '13d.png', OWMn: '13n.png', Icond: '9.png', Iconn: '46.png', luxpercent: 0.5, stdIcond: 'rain', stdIconn: 'nt_snow'],
[id: 612, OWMd: '13d.png', OWMn: '13n.png', Icond: '8.png', Iconn: '46.png', luxpercent: 0.5, stdIcond: 'sleet', stdIconn: 'nt_snow'],
[id: 613, OWMd: '13d.png', OWMn: '13n.png', Icond: '9.png', Iconn: '46.png', luxpercent: 0.5, stdIcond: 'rain', stdIconn: 'nt_snow'],
[id: 615, OWMd: '13d.png', OWMn: '13n.png', Icond: '39.png', Iconn: '45.png', luxpercent: 0.5, stdIcond: 'rain', stdIconn: 'nt_rain'],
[id: 616, OWMd: '13d.png', OWMn: '13n.png', Icond: '39.png', Iconn: '45.png', luxpercent: 0.5, stdIcond: 'rain', stdIconn: 'nt_rain'],
[id: 620, OWMd: '13d.png', OWMn: '13n.png', Icond: '13.png', Iconn: '46.png', luxpercent: 0.4, stdIcond: 'flurries', stdIconn: 'nt_snow'],
[id: 621, OWMd: '13d.png', OWMn: '13n.png', Icond: '16.png', Iconn: '46.png', luxpercent: 0.3, stdIcond: 'snow', stdIconn: 'nt_snow'],
[id: 622, OWMd: '13d.png', OWMn: '13n.png', Icond: '42.png', Iconn: '42.png', luxpercent: 0.6, stdIcond: 'snow', stdIconn: 'nt_snow'],
[id: 701, OWMd: '50d.png', OWMn: '50n.png', Icond: '23.png', Iconn: '23.png', luxpercent: 0.8, stdIcond: 'partlycloudy', stdIconn: 'nt_partlycloudy'],
[id: 711, OWMd: '50d.png', OWMn: '50n.png', Icond: '23.png', Iconn: '23.png', luxpercent: 0.8, stdIcond: 'partlycloudy', stdIconn: 'nt_partlycloudy'],
[id: 721, OWMd: '50d.png', OWMn: '50n.png', Icond: '23.png', Iconn: '23.png', luxpercent: 0.8, stdIcond: 'partlycloudy', stdIconn: 'nt_partlycloudy'],
[id: 731, OWMd: '50d.png', OWMn: '50n.png', Icond: '23.png', Iconn: '23.png', luxpercent: 0.8, stdIcond: 'partlycloudy', stdIconn: 'nt_partlycloudy'],
[id: 741, OWMd: '50d.png', OWMn: '50n.png', Icond: '23.png', Iconn: '23.png', luxpercent: 0.8, stdIcond: 'partlycloudy', stdIconn: 'nt_partlycloudy'],
[id: 751, OWMd: '50d.png', OWMn: '50n.png', Icond: '23.png', Iconn: '23.png', luxpercent: 0.8, stdIcond: 'partlycloudy', stdIconn: 'nt_partlycloudy'],
[id: 761, OWMd: '50d.png', OWMn: '50n.png', Icond: '23.png', Iconn: '23.png', luxpercent: 0.8, stdIcond: 'partlycloudy', stdIconn: 'nt_partlycloudy'],
[id: 762, OWMd: '50d.png', OWMn: '50n.png', Icond: '23.png', Iconn: '23.png', luxpercent: 0.8, stdIcond: 'partlycloudy', stdIconn: 'nt_partlycloudy'],
[id: 771, OWMd: '50d.png', OWMn: '50n.png', Icond: '23.png', Iconn: '23.png', luxpercent: 0.8, stdIcond: 'partlycloudy', stdIconn: 'nt_partlycloudy'],
[id: 781, OWMd: '50d.png', OWMn: '50n.png', Icond: '23.png', Iconn: '23.png', luxpercent: 0.8, stdIcond: 'partlycloudy', stdIconn: 'nt_partlycloudy'],
[id: 800, OWMd: '01d.png', OWMn: '01n.png', Icond: '32.png', Iconn: '31.png', luxpercent: 1, stdIcond: 'clear', stdIconn: 'nt_clear'],
[id: 801, OWMd: '02d.png', OWMn: '02n.png', Icond: '38.png', Iconn: '33.png', luxpercent: 0.9, stdIcond: 'partlycloudy', stdIconn: 'nt_partlycloudy'],
[id: 802, OWMd: '03d.png', OWMn: '03n.png', Icond: '30.png', Iconn: '29.png', luxpercent: 0.8, stdIcond: 'partlycloudy', stdIconn: 'nt_partlycloudy'],
[id: 803, OWMd: '04d.png', OWMn: '04n.png', Icond: '28.png', Iconn: '27.png', luxpercent: 0.6, stdIcond: 'mostlycloudy', stdIconn: 'nt_mostlycloudy'],
[id: 804, OWMd: '04d.png', OWMn: '04n.png', Icond: '26.png', Iconn: '26.png', luxpercent: 0.6, stdIcond: 'cloudy', stdIconn: 'nt_cloudy'],
[id: 999, OWMd: '50d.png', OWMn: '50n.png', Icond: 'na.png', Iconn: 'na.png', luxpercent: 1.0, stdIcond: 'unknown', stdIconn: 'unknown'],
    ]

@Field static attributesMap = [
    'threedayTile':             [title: 'Three Day Forecast Tile', descr: 'Display Three Day Forecast Tile?', typeof: false, default: 'false'],
	'alert':				    [title: 'Weather Alert', descr: 'Display any weather alert?', typeof: false, default: 'false'],
    'betwixt':				    [title: 'Slice of Day', descr: 'Display the slice-of-day?', typeof: 'string', default: 'false'],
	'cloud':			    	[title: 'Cloud', descr: 'Display cloud coverage %?', typeof: 'number', default: 'false'],
	'condition_code':			[title: 'Condition Code', descr: 'Display condition_code?', typeof: 'string', default: 'false'],
	'condition_icon_only':		[title: 'Condition Icon Only', descr: 'Display condition_code_only?', typeof: 'string', default: 'false'],
	'condition_icon_url':		[title: 'Condition Icon URL', descr: 'Display condition_code_url?', typeof: 'string', default: 'false'],
	'condition_icon':			[title: 'Condition Icon', descr: 'Display condition_icon?', typeof: 'string', default: 'false'],
    'condition_iconWithText':   [title: 'Condition Icon With Text', descr: 'Display condition_iconWithText?', typeof: 'string', default: 'false'],
	'condition_text':			[title: 'Condition Text', descr: 'Display condition_text?', typeof: 'string', default: 'false'],
    'dashHubitatOWM':           [title: 'Dash - Hubitat and OpenWeatherMap', descr: 'Display attributes required by Hubitat and OpenWeatherMap dashboards?', typeof: false, default: 'false'],
    'dashSmartTiles':           [title: 'Dash - SmartTiles', descr: 'Display attributes required by SmartTiles dashboards?', typeof: false, default: 'false'],
    'dashSharpTools':           [title: 'Dash - SharpTools.io', descr: 'Display attributes required by SharpTools.io?', typeof: false, default: 'false'],
    'dewpoint':                 [title: 'Dewpoint (in default unit)', descr: 'Display the dewpoint?', typeof: 'number', default: 'false'],
    'fcstHighLow':              [title: 'Forecast High/Low Temperatures:', descr: 'Display forecast High/Low temperatures?', typeof: false, default: 'false'],
	'forecast_code':		    [title: 'Forecast Code', descr: 'Display forecast_code?', typeof: 'string', default: 'false'],
	'forecast_text':		    [title: 'Forecast Text', descr: 'Display forecast_text?', typeof: 'string', default: 'false'],
	'illuminated':			    [title: 'Illuminated', descr: 'Display illuminated (with lux added for use on a Dashboard)?', typeof: 'string', default: 'false'],
	'is_day':				    [title: 'Is daytime', descr: 'Display is_day?', typeof: 'number', default: 'false'],
	'localSunrise':			    [title: 'Local SunRise and SunSet', descr: 'Display the Group of Time of Local Sunrise and Sunset, with and without Dashboard text?', typeof: false, default: 'false'],
	'myTile':				    [title: 'myTile for dashboard', descr: 'Display myTile?', typeof: 'string', default: 'false'],
    'rainToday':			    [title: 'Today\'s Precipitation', descr: 'Display today\'s precipitation?', typeof: 'number', default: 'false'],
	'precipExtended':			[title: 'Precipitation Forecast', descr: 'Display precipitation forecast?', typeof: false, default: 'false'],
    'obspoll':			        [title: 'Observation time', descr: 'Display Observation and Poll times?', typeof: false, default: 'false'],
	'vis':				        [title: 'Visibility (in default unit)', descr: 'Display visibility distance?', typeof: 'number', default: 'false'],
    'weatherSummary':			[title: 'Weather Summary Message', descr: 'Display the Weather Summary?', typeof: 'string', default: 'false'],
	'wind_cardinal':		    [title: 'Wind Cardinal', descr: 'Display the Wind Direction (text initials)?', typeof: 'number', default: 'false'],	
	'wind_degree':			    [title: 'Wind Degree', descr: 'Display the Wind Direction (number)?', typeof: 'number', default: 'false'],
	'wind_direction':			[title: 'Wind direction', descr: 'Display the Wind Direction (text words)?', typeof: 'string', default: 'false'],
	'wind_gust':				[title: 'Wind gust (in default unit)', descr: 'Display the Wind Gust?', typeof: 'number', default: 'false'],
	'wind_string':			    [title: 'Wind string', descr: 'Display the wind string?', typeof: 'string', default: 'false'],
]

// Check Version   ***** with great thanks and acknowledgment to Cobra (CobraVmax) for his original code ****
def updateCheck()
{
	def paramsUD = [uri: 'https://raw.githubusercontent.com/Scottma61/Hubitat/master/docs/version2.json'] //https://hubitatcommunity.github.io/???/version2.json"]
	
 	asynchttpGet('updateCheckHandler', paramsUD)
}

void updateCheckHandler(resp, data) {

	state.InternalName = 'OpenWeatherMap-NWS Alerts Weather Driver'
	Boolean descTextEnable = settings.logSet ?: false

	if (resp.getStatus() == 200 || resp.getStatus() == 207) {
		def respUD = parseJson(resp.data)
		// log.warn ' Version Checking - Response Data: $respUD'   // Troubleshooting Debug Code - Uncommenting this line should show the JSON response from your webserver
		state.Copyright = respUD.copyright
		// uses reformattted 'version2.json'
		String newVer = padVer(respUD.driver.(state.InternalName).ver)
		String currentVer = padVer(version())
		state.UpdateInfo = (respUD.driver.(state.InternalName).updated)
            // log.debug 'updateCheck: ${respUD.driver.(state.InternalName).ver}, $state.UpdateInfo, ${respUD.author}'

		switch(newVer) {
			case { it == 'NLS'}:
				state.Status = '<b>** This Driver is no longer supported by ${respUD.author}  **</b>'
				if (descTextEnable) log.warn '** This Driver is no longer supported by ${respUD.author} **'
				break
			case { it > currentVer}:
			      state.Status = '<b>New Version Available (Version: ' + respUD.driver.(state.InternalName).ver + ')</b>'
			      if (descTextEnable) log.warn '** There is a newer version of this Driver available  (Version: ' + respUD.driver.(state.InternalName).ver + ') **'
			      if (descTextEnable) log.warn '** ' + state.UpdateInfo + ' **'
				break
			case { it < currentVer}:
			      state.Status = '<b>You are using a Test version of this Driver (Expecting: ' + respUD.driver.(state.InternalName).ver + ')</b>'
			      if (descTextEnable) log.warn 'You are using a Test version of this Driver (Expecting: ' + respUD.driver.(state.InternalName).ver + ')'
				break
			default:
				state.Status = 'Current Version: ' + respUD.driver.(state.InternalName).ver
				if (descTextEnable) log.info 'You are using the current version of this driver'
				break
		}

    } else {
        log.error 'Something went wrong: CHECK THE JSON FILE AND IT\'S URI'
    }
}

/*
	padVer
	Version progression of 1.4.9 to 1.4.10 would mis-compare unless each duple is padded first.
*/
static String padVer(String ver) {
	String pad = sBLK
	ver.replaceAll( '[vV]', sBLK ).split( /\./ ).each { pad += it.padLeft( 2, '0' ) }
	return pad
}

static String getThisCopyright(){'&copy; 2020 Matthew (scottma61) '}
