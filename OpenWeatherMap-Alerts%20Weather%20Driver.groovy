/*
	OpenWeatherMap-Alerts Weather Driver
	Import URL: https://raw.githubusercontent.com/HubitatCommunity/OpenWeatherMap-Alerts-Weather-Driver/master/OpenWeatherMap-Alerts%2520Weather%2520Driver.groovy
	Copyright 2023 @Matthew (Scottma61)

	This driver has morphed many, many times, so the genesis is very blurry now.  It stated as a WeatherUnderground
	driver, then when they restricted their API it morphed into an APIXU driver.  When APIXU ceased it became a
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

	Last Update 04/17/2024
	{ Left room below to document version changes...}

    V0.6.9	04/17/2024	Added moonrise, moonset and moon_phase attributes.
    V0.6.8	08/31/2023	Added pull request from @nh.schottfam to display sun 'altitude' & 'azimuth' as stand-alone optional attributes. Code cleanups.
    V0.6.7	06/07/2023	Added pull request from @nh.schottfam to display 'City' as a stand-alone optional attribute.
    V0.6.6	06/03/2023	Code clean-up & corrections from @nh.schottfam (Thanks!).
    V0.6.5	05/30/2023	Changes to prevent errors and better report in situations where there is no sunrise or sunset.
    V0.6.4	01/23/2023	Bug fix for wind_cardinal that is creating a "No Data" response w/ 3rd party tile apps.
    V0.6.3	01/05/2023	Bug fix for myTile not showing icon when neither the 'Three day Forecast Tile' nor the 'Forecast High/Low Temperatures' Optional attributes are selected.
    V0.6.2	08/23/2022	Added user selection of 2.5 or 3.5 OWM API Key; Moved Schedule Change notice to Extended Logging.
    V0.6.1	08/22/2022	Removed the sunrise-sunset.org poll.
    V0.6.0	08/20/2022	More corrections to sunrise/sunset data when when there is a Sunrise-Sunset.org failure.
    V0.5.9	07/28/2022	Code clean-up and optimization (Thanks @nh.schottfam).
    V0.5.8	07/26/2022	Fallback to hub location defaults and estimates for Sunrise-Sunset.org failure.
    V0.5.7  06/12/2022  Both MyTile and the Three day Forecast Tile use the Icon and Text selected in the 'Condition Icon/Text for current day on MyTile & Three Day Forecast Tile' option.
    V0.5.6	06/11/2022	Corrected 3 day tile icon to honor user's selection of Current or Forecast icon.
    V0.5.5	06/10/2022	Corrected PoP1 & PoP2 from not displaying when extended precipitation forcast was selected.
    V0.5.4	04/17/2022	Fallback for Sunrise-Sunset.org failure.
	V0.5.3	08/11/2021	Exposed cloud coverage forecasts.
	V0.5.2	01/26/2021	Corrected a display issue on Alerts.
	V0.5.1	12/12/2020	Changes to dahboard tile logo/hyperlinks when using weather.gov for alerts and there is an alert.
	V0.5.0	12/08/2020	Bug fix for 'forecast_textn' optional attributes.
	V0.4.9	12/03/2020	New tinyurl for icons.  Added tinyurl for weather.gov alert poll.
	V0.4.8	12/01/2020	Added ability to select Weather Alert source (none/OWM/Weather.gov {US Only}).
	V0.4.7	11/26/2020	Bug fixes.  Fix timeouts on http calls (by @nh.schottfam).
	V0.4.6	11/06/2020	Refactored the dashboard tiles.
	V0.4.5	10/31/2020	Tweaked threedayfcstTile for small screens.
	V0.4.4	10/30/2020	More code cleanups/reductions/optimizations by @nh.schottfam.
	V0.4.3	10/29/2020	Bug fixes and the usual code cleanup/reduction/optimizations by @nh.schottfam.
	V0.4.2	10/29/2020	Yet another Precip bux fix.
	V0.4.1	10/29/2020	Move today's precip back to 'Daily'.  More bux fixes.
	V0.4.0	10/28/2020	More Bux fixes for new Probability of Precipitation (PoP) from OWM.
	V0.3.9	10/28/2020	Bux fixes for new Probability of Precipitation (PoP) from OWM.
	V0.3.8	10/28/2020	Added Probability of Precipitation (PoP) from OWM.  Bug fixes and code and string reductions by @nh.schottfam).
	V0.3.7	10/27/2020	Bug fixes.
	V0.3.6	10/27/2020	Removed '+' from attribute names.  Three Day Tile now has optional 'Low/High' or 'High/Low' setting.
	V0.3.5	10/25/2020	Bug fixes for null JSON returns.
	V0.3.4	10/24/2020	Added indicator of multiple alerts in tiles. Minor bug fixes (by @nh.schottfam).
	V0.3.3	10/23/2020	Code optimizations and minor bug fixes (by @nh.schottfam).
	V0.3.2	10/22/2020	Removed 'NWS' from driver name, minor bug fixes.
	V0.3.1	10/21/2020	Improved OWM URLs in the dashboard tiles to pull in location's city code (if available).
	V0.3.0	10/21/2020	Better OWM URLs in the dashboard tiles.
	V0.2.9	10/20/2020	Correcting some Tile displays from the last update.
	V0.2.8	10/20/2020	Pulling Alerts from OWM instead of NWS.
	V0.2.7	10/19/2020	Added forecast 'Morn', 'Day', 'Eve' and 'Night' temperatures for current day and tomorrow.
	V0.2.6	10/07/2020	Change to use asynchttp for NWS alerts (by @nh.schottfam).
	V0.2.5	10/02/2020	More string constant optimizations (by @nh.schottfam)
	V0.2.4	09/27/2020	Fix to allow for use of multiple virtual devices, More string constant optimizations (by @nh.schottfam)
	V0.2.3	09/24/2020	More string constant optimizations, and removal of white space characters (by @nh.schottfam)
	V0.2.2	09/23/2020	Removing 'urgency' restrictions from alerts poll
	V0.2.1	09/22/2020	Added forecast icon url attributes for tomorrow and day-after-tomorrow
	V0.2.0	09/21/2020	Added forecast High/Low temp attributes for tomorrow and day-after-tomorrow
	V0.1.9	09/16/2020	Removing 'severity' and 'certainty' restrictions from alerts poll
	V0.1.8	09/13/2020	Re-worked Alerts to not be dependent on api.weather.gov returning a valid response code
	V0.1.7	09/12/2020	Remove most DB accesses and string cleanup (by @nh.schottfam)
	V0.1.6	09/08/2020	Restoring 'certainty' to weather.gov alert poll
	V0.1.5	09/08/2020	Removed 'certainty' from weather.gov alert poll
	V0.1.4	09/07/2020	Bug fix for NullPointerException on line 580
	V0.1.3	09/05/2020	Improved Alert handling for dashboard tiles, again, various bug fixes
	V0.1.2	07/02/2020	Bug fix sync MyTile and weatherSummary tiles upon alert update
	V0.1.1	06/06/2020	Bug fix to exclude minutely and hourly data in poll
	V0.1.0	05/07/2020	Improved Alert handling for dashboard tiles, various bug fixes
	V0.0.9	04/24/2020	Continue to work on improving null handling, various bug fixes
	V0.0.8	4/23/2020-2	Numerous bug fixes, better handling where alerts are not available, handling nulls
	V0.0.7	04/23/2020	Numerous bug fixes, better handling where alerts are not available
	V0.0.6	04/20/2020	Refactored much of the code, added Hubitat Package Manager compatibility
	V0.0.5	04/19/2020	More code cleanup and optimizations (Thanks @nh.schottfam!)
	V0.0.4	04/18/2020	Corrected forecast icon to always be 'day' instead of current time
	V0.0.3	04/18/2020	More fixes on Alerts, mapped condition_code, weatherIcon(s)
	V0.0.2	04/17/2020	Fixed Alerts on myTile and alertTile, Capitalized condition_text
	V0.0.1	04/17/2020	Initial conversion from Dark Sky to OWM
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

//file:noinspection GroovyUnusedAssignment
//file:noinspection SpellCheckingInspection
//file:noinspection unused
//file:noinspection GroovyAssignabilityCheck
//file:noinspection GrDeprecatedAPIUsage

static String version()	{  return '0.6.9'  }
import groovy.transform.Field

metadata {
	definition (name: 'OpenWeatherMap-Alerts Weather Driver',
		namespace: 'Matthew',
		author: 'Scottma61',
		importUrl: 'https://raw.githubusercontent.com/HubitatCommunity/OpenWeatherMap-Alerts-Weather-Driver/master/OpenWeatherMap-Alerts%2520Weather%2520Driver.groovy') {

		capability 'Sensor'
		capability 'Temperature Measurement'
		capability 'Illuminance Measurement'
		capability 'Relative Humidity Measurement'
		capability 'Pressure Measurement'
		capability 'Ultraviolet Index'

		capability 'Refresh'

		attributesMap.each {
			k, v -> if (v.ty)	attribute k, v.ty
		}

//	The following attributes may be needed for dashboards that require these attributes,
//	so they are alway available and shown by default.
		attribute 'city', sSTR			//Hubitat  OpenWeather  SharpTool.io  SmartTiles
		attribute 'feelsLike', sNUM		//SharpTool.io  SmartTiles
		attribute 'forecastIcon', sSTR	//SharpTool.io
		attribute 'localSunrise', sSTR	//SharpTool.io  SmartTiles
		attribute 'localSunset', sSTR	//SharpTool.io  SmartTiles
		attribute 'percentPrecip', sNUM	//SharpTool.io  SmartTiles
		attribute 'pressured', sSTR		//UNSURE SharpTool.io  SmartTiles
		attribute 'weather', sSTR		//SharpTool.io  SmartTiles
		attribute 'weatherIcon', sSTR	//SharpTool.io  SmartTiles
		attribute 'weatherIcons', sSTR	//Hubitat  openWeather
		attribute 'wind', sNUM			//SharpTool.io
		attribute 'windDirection', sNUM	//Hubitat  OpenWeather
		attribute 'windSpeed', sNUM		//Hubitat  OpenWeather

//	The attributes below are sub-groups of optional attributes.  They need to be listed here to be available
//alert
		attribute 'alert', sSTR
		attribute 'alertTile', sSTR
		attribute 'alertDescr', sSTR
		attribute 'alertSender', sSTR

//threedayTile
		attribute 'threedayfcstTile', sSTR

//fcstHighLow
		attribute 'forecastHigh', sNUM
		attribute 'forecastHigh1', sNUM
		attribute 'forecastHigh2', sNUM
		attribute 'forecastLow', sNUM
		attribute 'forecastLow1', sNUM
		attribute 'forecastLow2', sNUM
		attribute 'forecastMorn', sNUM
		attribute 'forecastDay', sNUM
		attribute 'forecastEve', sNUM
		attribute 'forecastNight', sNUM
		attribute 'forecastMorn1', sNUM
		attribute 'forecastDay1', sNUM
		attribute 'forecastEve1', sNUM
		attribute 'forecastNight1', sNUM
		attribute 'forecast_text1', sSTR
		attribute 'forecast_text2', sSTR
		attribute 'condition_icon_url1', sSTR
		attribute 'condition_icon_url2', sSTR

//controlled with localSunrise
		attribute 'tw_begin', sSTR
		attribute 'sunriseTime', sSTR
		attribute 'noonTime', sSTR
		attribute 'sunsetTime', sSTR
		attribute 'tw_end', sSTR

//suncalc
		attribute 'altitude', sNUM // sun angle up from the horizon (0 on your horizon, 90 straight up)
		attribute 'azimuth', sNUM  // sun angle along the horizon (0 is N, 90 East, etc..)

//obspoll
		attribute 'last_poll_Forecast', sSTR // time the poll was initiated
		attribute 'last_observation_Forecast', sSTR  // datestamp of the forecast observation

//precipExtended
		attribute 'rainTomorrow', sNUM
		attribute 'rainDayAfterTomorrow', sNUM
		attribute 'Precip0', sNUM
		attribute 'Precip1', sNUM
		attribute 'Precip2', sNUM
		attribute 'PoP1', sNUM
		attribute 'PoP2', sNUM

//cloudExtended
		attribute 'cloudToday', sNUM
		attribute 'cloudTomorrow', sNUM
		attribute 'cloudDayAfterTomorrow', sNUM

		command 'pollData'
	}

	preferences() {
		String settingDescr = settingEnable ? '<br><i>Hide many of the optional attributes to reduce the clutter, if needed, by turning OFF this toggle.</i><br>' : '<br><i>Many optional attributes are available to you, if needed, by turning ON this toggle.</i><br>'
		section('Query Inputs'){
			input 'apiKey', 'text', required: true, title: 'Type OpenWeatherMap.org API Key Here', defaultValue: null
			input 'apiVer', 'bool', required: true, title: 'API Key Version (2.5 = OFF;  3.0 = ON)', defaultValue: false
			input 'city', 'text', required: true, defaultValue: 'City or Location name forecast area', title: 'City name'
			input 'pollIntervalForecast', 'enum', title: 'External Source Poll Interval (daytime)', required: true, defaultValue: '3 Hours', options: ['Manual Poll Only', '2 Minutes', '5 Minutes', '10 Minutes', '15 Minutes', '30 Minutes', '1 Hour', '3 Hours']
			input 'pollIntervalForecastnight', 'enum', title: 'External Source Poll Interval (nighttime)', required: true, defaultValue: '3 Hours', options: ['Manual Poll Only', '2 Minutes', '5 Minutes', '10 Minutes', '15 Minutes', '30 Minutes', '1 Hour', '3 Hours']
			input 'txtEnable', 'bool', title: 'Enable Extended Logging', description: '<i>Extended logging will turn off automatically after 30 minutes.</i>', required: true, defaultValue: false
			input 'alertSource', 'enum', required: true, defaultValue: sONE, title: 'Weather Alert Source<br>0=None 1=OWM or 2=Weather.gov (US only)', options: [0:sZERO, 1:sONE, 2:sTWO]
			input 'tempFormat', 'enum', required: true, defaultValue: 'Fahrenheit (°F)', title: 'Display Unit - Temperature: Fahrenheit (°F) or Celsius (°C)',  options: ['Fahrenheit (°F)', 'Celsius (°C)']
			input 'TWDDecimals', 'enum', required: true, defaultValue: sZERO, title: 'Display decimals for Temperature & Wind Speed', options: [0:sZERO, 1:sONE, 2:sTWO, 3:'3', 4:'4']
			input 'RDecimals', 'enum', required: true, defaultValue: sZERO, title: 'Display decimals for Precipitation', options: [0:sZERO, 1:sONE, 2:sTWO, 3:'3', 4:'4']
			input 'PDecimals', 'enum', required: true, defaultValue: sZERO, title: 'Display decimals for Pressure', options: [0:sZERO, 1:sONE, 2:sTWO, 3:'3', 4:'4']
			input 'datetimeFormat', 'enum', required: true, defaultValue: sONE, title: 'Display Unit - Date-Time Format',  options: [1:'m/d/yyyy 12 hour (am|pm)', 2:'m/d/yyyy 24 hour', 3:'mm/dd/yyyy 12 hour (am|pm)', 4:'mm/dd/yyyy 24 hour', 5:'d/m/yyyy 12 hour (am|pm)', 6:'d/m/yyyy 24 hour', 7:'dd/mm/yyyy 12 hour (am|pm)', 8:'dd/mm/yyyy 24 hour', 9:'yyyy/mm/dd 24 hour']
			input 'distanceFormat', 'enum', required: true, defaultValue: 'Miles (mph)', title: 'Display Unit - Distance/Speed: Miles, Kilometers, knots or meters',  options: ['Miles (mph)', 'Kilometers (kph)', 'knots', 'meters (m/s)']
			input 'pressureFormat', 'enum', required: true, defaultValue: 'Inches', title: 'Display Unit - Pressure: Inches or Millibar/Hectopascal',  options: ['Inches', 'Millibar', 'Hectopascal']
			input 'rainFormat', 'enum', required: true, defaultValue: 'Inches', title: 'Display Unit - Precipitation: Inches or Millimeters',  options: ['Inches', 'Millimeters']
			input 'luxjitter', 'bool', title: 'Use lux jitter control (rounding)?', required: true, defaultValue: false
//	https://tinyurl.com/icnqz/ points to https://raw.githubusercontent.com/HubitatCommunity/WeatherIcons/master/
			input 'iconLocation', 'text', required: true, defaultValue: 'https://tinyurl.com/icnqz/', title: 'Alternative Icon Location:'
			input 'iconType', 'bool', title: 'Condition Icon/Text for current day on MyTile & Three Day Forecast Tile: On=Current or Off=Forecast', required: true, defaultValue: false
			input 'altCoord', 'bool', required: true, defaultValue: false, title: "Override Hub's location coordinates"
			if (altCoord) {
				input 'altLat', sSTR, title: 'Override location Latitude', required: true, defaultValue: location.latitude.toString(), description: '<br>Enter location Latitude<br>'
				input 'altLon', sSTR, title: 'Override location Longitude', required: true, defaultValue: location.longitude.toString(), description: '<br>Enter location Longitude<br>'
			}
			input 'settingEnable', 'bool', title: '<b>Display All Optional Attributes</b>', description: settingDescr, defaultValue: true
	//build a Selector for each mapped Attribute or group of attributes
			attributesMap.each {
				keyname, attribute ->
				if (settingEnable) {
					input keyname+'Publish', 'bool', title: attribute.t, required: true, defaultValue: attribute.defa, description: sBR+(String)attribute.d+sBR
					if(keyname == 'threedayTile') input 'threedayLH', 'bool', title: 'Three Day Temp Display', description: '<br>High/Low: On or Low/High: Off<br>', required: true, defaultValue: false
					if(keyname == 'weatherSummary') input 'summaryType', 'bool', title: 'Full Weather Summary', description: '<br>Full: on or short: off summary?<br>', required: true, defaultValue: false
				}
			}
			if (settingEnable) {
				input 'windPublish', 'bool', title: 'Wind Speed', required: true, defaultValue: sFLS, description: '<br>Display wind speed<br>'
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
@Field static final String sRB='>'
@Field static final String sCOMMA=','
@Field static final String sMINUS='-'
@Field static final String sCOLON=':'
@Field static final String sZERO='0'
@Field static final String sONE='1'
@Field static final String sTWO='2'
@Field static final String sDOT='.'
@Field static final String sICON='iconLocation'
@Field static final String sTMETR='tMetric'
@Field static final String sDMETR='dMetric'
@Field static final String sPMETR='pMetric'
@Field static final String sRMETR='rMetric'
@Field static final String sTEMP='temperature'
@Field static final String sSUMLST='Summary_last_poll_time'
@Field static final String sTRU='true'
@Field static final String sFLS='false'
@Field static final String sNPNG='na.png'
@Field static final String s11D='11d.png'
@Field static final String s11N='11n.png'
@Field static final String sCTS='chancetstorms'
@Field static final String sNCTS='nt_chancetstorms'
@Field static final String sRAIN='rain'
@Field static final String sNRAIN='nt_rain'
@Field static final String sPCLDY='partlycloudy'
@Field static final String sNPCLDY='nt_partlycloudy'
@Field static final String s23='23.png'
@Field static final String s9='9.png'
@Field static final String s39='39.png'
@Field static final String sDF='°F'
@Field static final String sIMGS5='<img class="cI" src='
@Field static final String sIMGS8='<img class="cIb" src='
@Field static final String sTD='<td>'
@Field static final String sTR='<tr><td>'
@Field static final String sSTR='string'
@Field static final String sNUM='number'
@Field static final String sNCWA='No current weather alerts for this area'

// <<<<<<<<<< Begin Sunrise-Sunset Poll Routines >>>>>>>>>>
void pollSunRiseSet() {
	if(ifreInstalled()) { updated(); return }
	TimeZone tZ= TimeZone.getDefault()

	Date dnow= new Date()
	String currDate = dnow.format('yyyy-MM-dd', tZ)

	String tfmt1='HH:mm'
	Date tSunrise; tSunrise = (Date)todaysSunrise
	tSunrise = (!tSunrise || tSunrise == null) ? Date.parse("yyyy-MM-dd hh:mm:ss", currDate + " 00:00:00") : tSunrise

	Date tSunset; tSunset = (Date)todaysSunset
	if(!tSunset || tSunset == null){
		String currYear = dnow.format('yyyy', tZ)
		Date mar21= Date.parse("yyyy-MM-dd", currYear + '-03-21')
		Date sep21= Date.parse("yyyy-MM-dd", currYear + '-09-21')
		Boolean isBtwn= (dnow >= mar21 && dnow < sep21)
		Date twelve59= Date.parse("yyyy-MM-dd hh:mm:ss", currDate + " 23:59:59")
		Date mid01= Date.parse("yyyy-MM-dd hh:mm:ss", currDate + " 00:00:01")
		if(altLat.toDouble() > 0.0D) {
			tSunset = isBtwn ? twelve59 : mid01
		} else {
			tSunset = !isBtwn ? twelve59 : mid01
		}
	}
	myUpdData('riseTime', tSunrise.format(tfmt1, tZ))
	myUpdData('noonTime', new Date(tSunrise.getTime() + ((tSunset.getTime() - tSunrise.getTime()).intdiv(2))).format(tfmt1, tZ))
	myUpdData('setTime', tSunset.format(tfmt1, tZ))
	myUpdData('tw_begin', new Date(tSunrise.getTime() - (25*60*1000)).format(tfmt1, tZ)) // 25 minutes before sunrise
	myUpdData('tw_end', new Date(tSunset.getTime() + (25*60*1000)).format(tfmt1, tZ)) // 25 minutes after sunset
	myUpdData('localSunset', tSunset.format(myGetData('timeFormat'), tZ))
	myUpdData('localSunrise', tSunrise.format(myGetData('timeFormat'), tZ))
	myUpdData('riseTime1', new Date(tSunrise.getTime() + (60*60*24*1000)).format(tfmt1, tZ))
	myUpdData('riseTime2', new Date(tSunrise.getTime() + (60*60*24*1000*2)).format(tfmt1, tZ))
	myUpdData('setTime1', new Date(tSunset.getTime() + (60*60*24*1000)).format(tfmt1, tZ))
	myUpdData('setTime2', new Date(tSunset.getTime() + (60*60*24*1000*2)).format(tfmt1, tZ))
}
// >>>>>>>>>> End Sunrise-Sunset Routines <<<<<<<<<<

// <<<<<<<<<< Begin OWM Poll Routines >>>>>>>>>>
void pollOWM() {
	if(ifreInstalled()) { updated(); return }
	if( apiKey == null ) {
		LOGWARN('OpenWeatherMap API Key not found.  Please configure in preferences.')
		return
	}

/*  for testing a different Lat/Lon location uncommnent the two lines below */
//	String altLat = "44.809122" //"41.5051613" // "40.6" //"38.627003" //"30.6953657"
//	String altLon = "-68.735892" //"-81.6934446" // "-75.43" //"-90.199402" //-88.0398912"

	Map ParamsOWM
	ParamsOWM = [ uri: 'https://api.openweathermap.org/data/' + (apiVer==true ? '3.0' : '2.5') + '/onecall?lat=' + (String)altLat + '&lon=' + (String)altLon + '&exclude=minutely,hourly&mode=json&units=imperial&appid=' + (String)apiKey, timeout: 20 ]
	LOGINFO('Poll OpenWeatherMap.org: ' + ParamsOWM)
	asynchttpGet('pollOWMHandler', ParamsOWM)
}

void pollOWMHandler(resp, data) {
	LOGINFO('Polling OpenWeatherMap.org')
	if(resp.getStatus() != 200 && resp.getStatus() != 207) {
		LOGWARN('Calling https://api.openweathermap.org/data/' + (apiVer==true ? '3.0' : '2.5') + '/onecall?lat=' + (String)altLat + '&lon=' + (String)altLon + '&exclude=minutely,hourly&mode=json&units=imperial&appid=' + (String)apiKey)
		LOGWARN(resp.getStatus() + sCOLON + resp.getErrorMessage())
	}else{
		Map owm = parseJson(resp.data)
		LOGINFO('OpenWeatherMap Data: ' + owm.toString())
		if(ifreInstalled()) { updated(); return }
		if(owm.toString()==sNULL) {
			pauseExecution(1000)
			pollOWM()
			return
		}
		Date fotime = (owm?.current?.dt==null) ? new Date() : new Date((Long)owm.current.dt * 1000L)
		myUpdData('fotime', fotime.toString())
		Date futime = new Date()
		myUpdData('futime', futime.toString())
		TimeZone tZ= TimeZone.getDefault()
		myUpdData(sSUMLST, futime.format(myGetData('timeFormat'), tZ).toString())
		myUpdData('Summary_last_poll_date', futime.format(myGetData('dateFormat'), tZ).toString())
		myUpdData('currDate', new Date().format('yyyy-MM-dd', tZ))
		myUpdData('currTime', new Date().format('HH:mm', tZ))
		if(myGetData('riseTime') <= myGetData('currTime') && myGetData('setTime') >= myGetData('currTime')) {
			myUpdData('is_day', sTRU)
		}else{
			myUpdData('is_day', sFLS)
		}
		if(myGetData('currTime') < myGetData('tw_begin') || myGetData('currTime') > myGetData('tw_end')) {
			myUpdData('is_light', sFLS)
		}else{
			myUpdData('is_light', sTRU)
		}
		if(myGetData('is_light') != myGetData('is_lightOld')) {
			if(myGetData('is_light')==sTRU) {
				LOGINFO(' Switching to Daytime schedule.')
			}else{
				LOGINFO(' Switching to Nighttime schedule.')
			}
			initialize_poll()
			myUpdData('is_lightOld', myGetData('is_light'))
		}
// >>>>>>>>>> End Setup Global Variables <<<<<<<<<<

// <<<<<<<<<< Begin Process Standard Weather-Station Variables (Regardless of Forecast Selection)  >>>>>>>>>>
		Integer mult_twd = myGetData('mult_twd')==sNULL ? 1 : myGetData('mult_twd').toInteger()
		Integer mult_p = myGetData('mult_p')==sNULL ? 1 : myGetData('mult_p').toInteger()
		Integer mult_r = myGetData('mult_r')==sNULL ? 1 : myGetData('mult_r').toInteger()
		String ddisp_twd = myGetData('ddisp_twd')==sNULL ? '%3.0f' : myGetData('ddisp_twd')

		Boolean isF = myGetData(sTMETR) == sDF

		BigDecimal t_dew = owm?.current?.dew_point
		myUpdData('dewpoint', adjTemp(t_dew, isF, mult_twd))
		myUpdData('humidity', (Math.round((owm?.current?.humidity==null ? 0.00 : owm.current.humidity.toBigDecimal()) * 10) / 10).toString())

		BigDecimal t_press; t_press = owm?.current?.pressure==null ? 0.00 : owm.current.pressure.toBigDecimal()
		if(myGetData(sPMETR) == 'inHg') {
			t_press = Math.round(t_press * 0.029529983071445 * mult_p) / mult_p
		}else{
			t_press = Math.round(t_press * mult_p) / mult_p
		}
		myUpdData('pressure', t_press.toString())

		myUpdData(sTEMP, adjTemp(owm?.current?.temp, isF, mult_twd))

		String w_string_bft,w_bft_icon
		w_string_bft=sNULL
		w_bft_icon=sNULL
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

		BigDecimal t_wd,t_wg
		t_wd = owm?.current?.wind_speed==null ? 0.00 : owm.current.wind_speed.toBigDecimal()
		t_wg = owm?.current?.wind_gust==null ? t_wd : owm.current.wind_gust.toBigDecimal()
		if(myGetData(sDMETR) == 'MPH') {
			t_wd = Math.round(t_wd * mult_twd) / mult_twd
			t_wg = Math.round(t_wg * mult_twd) / mult_twd
		} else if(myGetData(sDMETR) == 'KPH') {
			t_wd = Math.round(t_wd * 1.609344 * mult_twd) / mult_twd
			t_wg = Math.round(t_wg * 1.609344 * mult_twd) / mult_twd
		} else if(myGetData(sDMETR) == 'knots') {
			t_wd = Math.round(t_wd * 0.868976 * mult_twd) / mult_twd
			t_wg = Math.round(t_wg * 0.868976 * mult_twd) / mult_twd
		}else{  //  this leave only m/s
			t_wd = Math.round(t_wd * 0.44704 * mult_twd) / mult_twd
			t_wg = Math.round(t_wg * 0.44704 * mult_twd) / mult_twd
		}
		myUpdData('wind', t_wd.toString())
		myUpdData('wind_gust', t_wg.toString())

		BigDecimal twb = owm?.current?.wind_deg==null ? 0.00 : owm.current.wind_deg.toBigDecimal()
		myUpdData('wind_degree', twb.toInteger().toString())
		String w_cardinal,w_direction
		w_cardinal=sNULL
		w_direction=sNULL
		if(twb < 11.25) {
			w_cardinal = 'N'; w_direction = 'North'
		}else if(twb < 33.75) {
			w_cardinal = 'NNE'; w_direction = 'North-Northeast'
		}else if(twb < 56.25) {
			w_cardinal = 'NE';  w_direction = 'Northeast'
		}else if(twb < 78.75) {
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
		myUpdData('wind_string', w_string_bft + ' from the ' + myGetData('wind_direction') + (myGetDataBD('wind') < 1.0 ? sBLK: ' at ' + String.format(ddisp_twd, myGetDataBD('wind')) + sSPC + myGetData(sDMETR)))
// >>>>>>>>>> End Process Standard Weather-Station Variables (Regardless of Forecast Selection)  <<<<<<<<<<

		Integer cloudCover = owm?.current?.clouds==null ? 1 : owm.current.clouds <= 1 ? 1 : owm.current.clouds
		myUpdData('cloud', cloudCover.toString())
		myUpdData('vis', (myGetData(sDMETR)!='MPH' ? Math.round(owm?.current?.visibility==null ? 0.01 : owm.current.visibility.toBigDecimal() * 0.001 * mult_twd) / mult_twd : Math.round(owm?.current?.visibility==null ? 0.00 : owm.current.visibility.toBigDecimal() * 0.0006213712 * mult_twd) / mult_twd).toString())

		List owmCweat = owm?.current?.weather
		myUpdData('condition_id', owmCweat==null || owmCweat[0]?.id==null ? '999' : owmCweat[0].id.toString())
		myUpdData('condition_code', getCondCode(myGetData('condition_id').toInteger(), myGetData('is_day')))
		myUpdData('OWN_icon', owmCweat == null || owmCweat[0]?.icon==null ? (myGetData('is_day')==sTRU ? '50d' : '50n') : owmCweat[0].icon)

		List<Map> owmDaily
		owmDaily = owm?.daily != null && ((List)owm.daily)[0]?.weather != null ? ((List)owm?.daily)[0].weather : null
		myUpdData('forecast_id', owmDaily==null || owmDaily[0]?.id==null ? '999' : owmDaily[0].id.toString())
		myUpdData('forecast_code', getCondCode(myGetData('forecast_id').toInteger(), sTRU))
		myUpdData('forecast_text', owmDaily==null || owmDaily[0]?.description==null ? 'Unknown' : owmDaily[0].description.capitalize())

		myUpdData('condition_text', myGetData('iconType')== sTRU ? (owmCweat==null || owmCweat[0]?.description==null ? 'Unknown' : owmCweat[0].description.capitalize()): (owm?.daily==null || owm?.daily[0]?.weather[0]?.description==null ? 'Unknown' : owm.daily[0].weather[0].description.capitalize()))

		owmDaily = owm?.daily != null ? (List)owm.daily : null
		BigDecimal t_p0 = (owmDaily==null || !owmDaily[0]?.rain ? 0.00 : owmDaily[0].rain.toBigDecimal()) + (owmDaily==null || !owmDaily[0]?.snow ? 0.00 : owmDaily[0].snow.toBigDecimal())
		myUpdData('rainToday', (Math.round((myGetData(sRMETR) == 'in' ? t_p0 * 0.03937008 : t_p0) * mult_r) / mult_r).toString())
		myUpdData('PoP', (!owmDaily[0].pop ? 0 : Math.round(owmDaily[0].pop.toBigDecimal() * 100.toInteger())).toString())
		myUpdData('percentPrecip', myGetData('PoP'))

        owmDaily = owm?.daily != null ? (List)owm.daily : null
        Date moonrise = (owmDaily[0].moonrise==null) ? new Date() : new Date((Long)owmDaily[0].moonrise * 1000L)
		myUpdData('moonrise', moonrise.toString())
        Date moonset = (owmDaily[0].moonset==null) ? new Date() : new Date((Long)owmDaily[0].moonset * 1000L)
		myUpdData('moonset', moonset.toString())
        String mPhase
	    BigDecimal tma = !owmDaily[0]?.moon_phase ? 0.00 : owmDaily[0].moon_phase.toBigDecimal()
	    if (tma >= 0.00 && tma < 0.0625) {mPhase = 'New Moon'}
	    else if (tma >= 0.0625 && tma < 0.125) {mPhase = 'Waxing Crescent'}
	    else if (tma >= 0.125 && tma < 0.25) {mPhase = 'First Quarter'}
	    else if (tma >= 0.25 && tma < 0.375) {mPhase = 'Waxing Gibbous'}
	    else if (tma >= 0.375 && tma < 0.5) {mPhase = 'Full Moon'}
	    else if (tma >= 0.5 && tma < 0.625) {mPhase = 'Waning Gibbous'}
	    else if (tma >= 0.625 && tma < 0.75) {mPhase = 'Last Quarter'}
	    else if (tma >= 0.075 && tma < 0.875) {mPhase = 'Waxing Gibbous'}
        else if (tma >= 0.875) {mPhase = 'New Moon'}
        myUpdData('moon_phase', mPhase)
        
		if(owmDaily && (threedayTilePublish || precipExtendedPublish || myTilePublish)) {
			BigDecimal t_p1 = (owmDaily==null || !owmDaily[1]?.rain ? 0.00 : owmDaily[1].rain.toBigDecimal()) + (owmDaily==null || !owmDaily[1]?.snow ? 0.00 : owmDaily[1].snow.toBigDecimal())
			BigDecimal t_p2 = (owmDaily==null || !owmDaily[2]?.rain ? 0.00 : owmDaily[2].rain.toBigDecimal()) + (owmDaily==null || !owmDaily[2]?.snow ? 0.00 : owmDaily[2].snow.toBigDecimal())
			myUpdData('Precip0', (Math.round((myGetData(sRMETR) == 'in' ? t_p0 * 0.03937008 : t_p0) * mult_r) / mult_r).toString())
			myUpdData('Precip1', (Math.round((myGetData(sRMETR) == 'in' ? t_p1 * 0.03937008 : t_p1) * mult_r) / mult_r).toString())
			myUpdData('Precip2', (Math.round((myGetData(sRMETR) == 'in' ? t_p2 * 0.03937008 : t_p2) * mult_r) / mult_r).toString())
			myUpdData('PoP1', (!owmDaily[1].pop ? 0 : Math.round(owmDaily[1].pop.toBigDecimal() * 100.toInteger())).toString())
			myUpdData('PoP2', (!owmDaily[2].pop ? 0 : Math.round(owmDaily[2].pop.toBigDecimal() * 100.toInteger())).toString())
		}

		if(owmDaily && cloudExtendedPublish) {
			myUpdData('Cloud0', (owmDaily[0].clouds==null ? 1 : owmDaily[0].clouds <= 1 ? 1 : owmDaily[0].clouds).toString())
			myUpdData('Cloud1', (owmDaily[1].clouds==null ? 1 : owmDaily[1].clouds <= 1 ? 1 : owmDaily[1].clouds).toString())
			myUpdData('Cloud2', (owmDaily[2].clouds==null ? 1 : owmDaily[2].clouds <= 1 ? 1 : owmDaily[2].clouds).toString())
		}

		String imgT1=(myGetData(sICON).toLowerCase().contains('://github.com/') && myGetData(sICON).toLowerCase().contains('/blob/master/') ? '?raw=true' : sBLK)
		if(owmDaily && owmDaily[1] && owmDaily[2]) {
			String tmpImg0= myGetData(sICON) + (myGetData('iconType')== sTRU ? getImgName(myGetData('condition_id').toInteger(), myGetData('is_day')) : getImgName(myGetData('forecast_id').toInteger(), myGetData('is_day'))) + imgT1
//			String tmpImg0= myGetData(sICON) + getImgName((!owmDaily[0].weather[0].id ? 999 : owmDaily[0].weather[0].id.toInteger()), sTRU) + imgT1
			String tmpImg1= myGetData(sICON) + getImgName((!owmDaily[1].weather[0].id ? 999 : owmDaily[1].weather[0].id.toInteger()), sTRU) + imgT1
			String tmpImg2= myGetData(sICON) + getImgName((!owmDaily[2].weather[0].id ? 999 : owmDaily[2].weather[0].id.toInteger()), sTRU) + imgT1


			if(threedayTilePublish || myTilePublish || fcstHighLowPublish) {
				myUpdData('day1', owmDaily[1]?.dt==null ? sBLK : new Date((Long)owmDaily[1].dt * 1000L).format('EEEE'))
				myUpdData('day2', owmDaily[2]?.dt==null ? sBLK : new Date((Long)owmDaily[2].dt * 1000L).format('EEEE'))
				myUpdData('is_day1', sTRU)
				myUpdData('is_day2', sTRU)
				myUpdData('forecast_id1', owmDaily[1]?.weather[0]?.id==null ? '999' : owmDaily[1].weather[0].id.toString())
				myUpdData('forecast_code1', getCondCode(myGetData('forecast_id1').toInteger(), sTRU))
				myUpdData('forecast_text1', owmDaily[1]?.weather[0]?.description==null ? 'Unknown' : owmDaily[1].weather[0].description.capitalize())

				myUpdData('forecast_id2', owmDaily[2]?.weather[0]?.id==null ? '999' : owmDaily[2].weather[0].id.toString())
				myUpdData('forecast_code2', getCondCode(myGetData('forecast_id2').toInteger(), sTRU))
				myUpdData('forecast_text2', owmDaily[2]?.weather[0]?.description==null ? 'Unknown' : owmDaily[2].weather[0].description.capitalize())

				myUpdData('forecastHigh1', adjTemp(owmDaily[1]?.temp?.max, isF, mult_twd))
				myUpdData('forecastHigh2', adjTemp(owmDaily[2]?.temp?.max, isF, mult_twd))

				myUpdData('forecastLow1', adjTemp(owmDaily[1]?.temp?.min, isF, mult_twd))
				myUpdData('forecastLow2', adjTemp(owmDaily[2]?.temp?.min, isF, mult_twd))
				myUpdData('forecastMorn', adjTemp(owmDaily[0]?.temp?.morn, isF, mult_twd))
				myUpdData('forecastDay', adjTemp(owmDaily[0]?.temp?.day, isF, mult_twd))
				myUpdData('forecastEve', adjTemp(owmDaily[0]?.temp?.eve, isF, mult_twd))
				myUpdData('forecastNight', adjTemp(owmDaily[0]?.temp?.night, isF, mult_twd))

				myUpdData('forecastMorn1', adjTemp(owmDaily[1]?.temp?.morn, isF, mult_twd))
				myUpdData('forecastDay1', adjTemp(owmDaily[1]?.temp?.day, isF, mult_twd))
				myUpdData('forecastEve1', adjTemp(owmDaily[1]?.temp?.eve, isF, mult_twd))
				myUpdData('forecastNight1', adjTemp(owmDaily[1]?.temp?.night, isF, mult_twd))

				myUpdData('imgName0', sIMGS5 + tmpImg0 + sRB) // For daily forecasted condition text for 'Today' 
				myUpdData('imgName1', sIMGS5 + tmpImg1 + sRB)
				myUpdData('imgName2', sIMGS5 + tmpImg2 + sRB)
			}
			if(condition_icon_urlPublish) {
				sendEvent(name: 'condition_icon_url1', value: tmpImg1)
				sendEvent(name: 'condition_icon_url2', value: tmpImg2)
			}else{
				device.deleteCurrentState('condition_icon_url1')
				device.deleteCurrentState('condition_icon_url2')
			}

		}
		myUpdData('forecastHigh', adjTemp(owmDaily[0]?.temp?.max, isF, mult_twd))
		myUpdData('forecastLow', adjTemp(owmDaily[0]?.temp?.min, isF, mult_twd))
		if(precipExtendedPublish){
			myUpdData('rainTomorrow', myGetData('Precip1'))
			myUpdData('rainDayAfterTomorrow', myGetData('Precip2'))
		}else{
			device.deleteCurrentState('rainTomorrow')
			device.deleteCurrentState('rainDayAfterTomorrow')
		}

		if(cloudExtendedPublish){
			myUpdData('cloudToday', myGetData('Cloud0'))
			myUpdData('cloudTomorrow', myGetData('Cloud1'))
			myUpdData('cloudDayAfterTomorrow', myGetData('Cloud2'))
		}else{
			device.deleteCurrentState('cloudToday')
			device.deleteCurrentState('cloudTomorrow')
			device.deleteCurrentState('cloudDayAfterTomorrrow')
		}

		updateLux(false)
		myUpdData('ultravioletIndex', (owm?.current?.uvi==null ? 0.00 : owm.current.uvi.toBigDecimal()).toString())

		myUpdData('feelsLike', adjTemp(owm?.current?.feels_like, isF, mult_twd))

		if(alertPublish) {
			if(alertSource==sTWO) {
/*  for testing a different Lat/Lon location uncommnent the two lines below */
//	String altLat = "44.809122" //"41.5051613" // "40.6" //"38.627003" //"30.6953657"
//	String altLon = "-68.735892" //"-81.6934446" // "-75.43" //"-90.199402" //-88.0398912"
				pollWDG()
			}
			if((alertSource==sZERO) || (!owm.alerts && alertSource==sONE) || (myGetData('curAl')==sNCWA && alertSource==sTWO)) {
				clearAlerts()
			}else{
				if(alertSource==sONE) {
					Map owmAlerts0= owm?.alerts ? owm.alerts[0] : null
					String curAl = owmAlerts0?.event==null ? sNCWA : owmAlerts0.event.replaceAll('\n', sSPC).replaceAll('[{}\\[\\]]', sBLK)
					String curAlSender = owmAlerts0?.sender_name==null ? sNULL : owmAlerts0.sender_name.replaceAll('\n',sSPC).replaceAll('[{}\\[\\]]', sBLK)
					String curAlDescr = owmAlerts0?.description==null ? sNULL : owmAlerts0.description.replaceAll('\n',sSPC).replaceAll('[{}\\[\\]]', sBLK).take(1024)
					if(curAl==sNCWA) {
						clearAlerts()
					}else{
						Integer alertCnt; alertCnt = 0
						Integer i
						for(i = 1;i<10;i++) {
							if(owm?.alerts[i]?.event!=null) {
								alertCnt++
							}
						}
						myUpdData('alertCnt', alertCnt.toString())
					}
					myUpdData('alert', curAl + (myGetData('alertCnt') != sZERO ? ' +' + myGetData('alertCnt') : sBLK))
					myUpdData('curAlSender', curAlSender)
					myUpdData('curAlDescr', curAlDescr)
					LOGINFO('OWM Weather Alert: ' + curAl + '; Description: ' + curAlDescr.length() + ' ' +curAlDescr)
					myUpdData('alertTileLink', '<a style="font-style:italic;color:red" href="https://openweathermap.org/city/' + myGetData('OWML') + '" target="_blank">'+myGetData('alert')+sACB)
					myUpdData('alertLink',  '<a style="font-style:italic;color:red">'+myGetData('alert')+sACB)
				}else{
/*  for testing a different Lat/Lon location uncommnent the two lines below */
//	String altLat = "44.809122" //"41.5051613" // "40.6" //"38.627003" //"30.6953657"
//	String altLon = "-68.735892" //"-81.6934446" // "-75.43" //"-90.199402" //-88.0398912"
					myUpdData('alert', myGetData('curAl') + (myGetData('alertCnt') != sZERO ? ' +' + myGetData('alertCnt') : sBLK))
// https://tinyurl.com/zznws points to https://forecast.weather.gov/MapClick.php
					myUpdData('alertTileLink', '<a style="font-style:italic;color:red" href="https://tinyurl.com/zznws?lat=' + altLat + '&lon=' + altLon +'" target=\'_blank\'>'+myGetData('alert')+sACB)
					myUpdData('alertLink',  '<a style="font-style:italic;color:red">'+myGetData('alert')+sACB)
					if(myGetData('curAl')==sNCWA) {
						clearAlerts()
					}
				}
				myUpdData('noAlert',sFLS)
				myUpdData('alertDescr', myGetData('curAlDescr'))
				myUpdData('alertSender', myGetData('curAlSender'))
				myUpdData('possAlert', sTRU)
			}
			//  <<<<<<<<<< Begin Built alertTile >>>>>>>>>>
			String alertTile
			alertTile = (myGetData('alert')== sNCWA ? 'No Weather Alerts for ' : 'Weather Alert for ') + myGetData('city') + (myGetData('alertSender')==null || myGetData('alertSender')==sSPC ? '' : ' issued by ' + myGetData('alertSender')) + sBR
			alertTile+= myGetData('alertTileLink') + sBR
			if(alertSource==sONE) {
				alertTile+= '<a href="https://openweathermap.org/city/' + myGetData('OWML') + '" target="_blank">' + sIMGS5 + myGetData(sICON) + 'OWM.png style="height:2em"></a> @ ' + myGetData(sSUMLST)
			}else{
				if(alertSource==sTWO) {
					alertTile+= '<a href=https://tinyurl.com/zznws?lat=' + altLat + '&lon=' + altLon + '" target="_blank">' + sIMGS5 + myGetData(sICON) + 'NWS_240px.png style="height:2em"></a> @ ' + myGetData(sSUMLST)
				}
			}
			myUpdData('alertTile', alertTile)
			sendEvent(name: 'alert', value: myGetData('alert'))
			sendEvent(name: 'alertDescr', value: myGetData('alertDescr'))
			sendEvent(name: 'alertSender', value: myGetData('alertSender'))
			sendEvent(name: 'alertTile', value: myGetData('alertTile'))
			//  >>>>>>>>>> End Built alertTile <<<<<<<<<<
		}
// >>>>>>>>>> End Setup Forecast Variables <<<<<<<<<<

		// <<<<<<<<<< Begin Icon Processing  >>>>>>>>>>
		String imgName = (myGetData('iconType')== sTRU ? getImgName(myGetData('condition_id').toInteger(), myGetData('is_day')) : getImgName(myGetData('forecast_id').toInteger(), myGetData('is_day')))
		sendEventPublish(name: 'condition_icon', value: sIMGS5 + myGetData(sICON) + imgName + imgT1 + sRB)
		sendEventPublish(name: 'condition_iconWithText', value: sIMGS5 + myGetData(sICON) + imgName + imgT1 + sRB+ sBR + myGetData('condition_text'))
		sendEventPublish(name: 'condition_icon_url', value: myGetData(sICON) + imgName + imgT1)
		myUpdData('condition_icon_url', myGetData(sICON) + imgName + imgT1)
		sendEventPublish(name: 'condition_icon_only', value: imgName.split('/')[-1].replaceFirst('\\?raw=true',sBLK))
	// >>>>>>>>>> End Icon Processing <<<<<<<<<<
		PostPoll()
	}
}
// >>>>>>>>>> End OpenWeatherMap Poll Routine <<<<<<<<<<

// <<<<<<<<<< Begin polling weather.gov for Alerts >>>>>>>>>>
void pollWDG() {
/*  for testing a different Lat/Lon location uncommnent the two lines below */
//	String altLat = "44.809122" //"41.5051613" // "40.6" //"38.627003" //"30.6953657"
//	String altLon = "-68.735892" //"-81.6934446" // "-75.43" //"-90.199402" //-88.0398912"
	Map wdgParams = [ uri: 'https://api.weather.gov/alerts/active?status=actual&message_type=alert,update&point=' + altLat + ',' + altLon,
		requestContentType:'application/json',
		contentType:'application/json',
		timeout: 20
	]
	LOGINFO('Poll api.weather.gov/alerts/active: ' + wdgParams)
	asynchttpGet('pollWDGHandler', wdgParams)
}

void pollWDGHandler(resp, data) {
	LOGINFO('Polling weather.gov')
	if(resp.getStatus() != 200 && resp.getStatus() != 207) {
		LOGWARN('Calling https://api.weather.gov/alerts/active?status=actual&message_type=alert,update&point=' + altLat + ',' + altLon)
		LOGWARN(resp.getStatus() + sCOLON + resp.getErrorMessage())
	}else{
		Map wdg = parseJson(resp.data)
		myUpdData('wdg', wdg.toString())
		LOGINFO('weather.gov Data: ' + wdg.toString())
		if(wdg.toString()==sNULL) {
			pauseExecution(1000)
			pollWDG()
			return
		}
		myUpdData('curAl', wdg?.features[0]?.properties?.event == null ? sNCWA : wdg.features[0].properties.event.replaceAll('\n', sSPC).replaceAll('[{}\\[\\]]', sBLK))
		myUpdData('curAlSender', wdg?.features[0]?.properties?.senderName==null ? sNULL : ((String)wdg.features[0].properties.senderName).replaceAll('\n',sSPC).replaceAll('[{}\\[\\]]', sBLK))
		myUpdData('curAlDescr', wdg?.features[0]?.properties?.description==null ? sNULL : ((String)wdg.features[0].properties.description).replaceAll('\n',sSPC).replaceAll('[{}\\[\\]]', sBLK).take(1024))
		Integer alertCnt; alertCnt = 0
		Integer i
		for(i = 1;i<10;i++) {
			if(wdg?.features[i]?.properties?.event!=null) {
				alertCnt++
			}
		}
		myUpdData('alertCnt', alertCnt.toString())
	}
}
// >>>>>>>>>> End polling weather.gov for Alerts <<<<<<<<<<

static String adjTemp(temp, Boolean isF, Integer mult_twd){
	BigDecimal t_fl
	t_fl = temp==null ? 0.00 : temp.toBigDecimal()
	if(!isF) t_fl = (t_fl - 32.0) / 1.8
	t_fl = Math.round(t_fl * mult_twd) / mult_twd
	return t_fl.toString()
}

void clearAlerts(){
	myUpdData('noAlert',sTRU)
	myUpdData('alert', sNCWA)
	myUpdData('alertDescr', sNCWA)
	myUpdData('alertSender', sSPC)
	String al3 = '<a style="font-style:italic">'
	myUpdData('alertTileLink', al3+myGetData('alert')+sACB)
	myUpdData('alertLink', sAB + myGetData('condition_text') + sACB)
	myUpdData('possAlert', sFLS)
}


@Field static Map<String,Map> dataStoreFLD=[:]

void myUpdData(String key, String val){
	String mc=device.id.toString()
	Map<String,String> myV
	myV=dataStoreFLD[mc]
	myV= myV!=null ? myV : [:]
	myV[key]=val
	dataStoreFLD[mc]=myV
	//removeDataValue(key) // THIS SHOULD BE REMOVED AT SOME POINT
}

BigDecimal myGetDataBD(String key){
	String ret= myGetData(key)
	return ret!=sNULL ? ret.toBigDecimal() : 0.0
}

String myGetData(String key){
	String mc=device.id.toString()
	Map<String,String> myV
	myV=dataStoreFLD[mc]
	myV= myV!=null ? myV : [:]
	if(myV[key]) return (String)myV[key]
	else return sNULL
}

static String dumpListDesc(data, Integer level, List<Boolean> lastLevel, String listLabel, Boolean html=false){
	String str
	str=sBLK
	Integer cnt
	cnt=1
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
			String lineStrt; lineStrt='\n'
			Integer i
			for(i=0; i<level; i++){
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
	String str
	str=sBLK
	Integer cnt
	cnt=1
	Integer sz=data?.size()
	data?.each{ par ->
		String lineStrt
		List<Boolean> newLevel=lastLevel
		Boolean thisIsLast= cnt==sz && !listCall
		if(level>0){
			newLevel[(level-1)]=thisIsLast
		}
		Boolean theLast
		theLast=thisIsLast
		if(level==0){
			lineStrt='\n\n • '
		}else{
			theLast= theLast && thisIsLast
			lineStrt='\n'
			Integer i
			for(i=0; i<level; i++){
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
	if(obj instanceof String){return sSTR}
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
	String mc=device.id.toString()
	Map myV
	myV=dataStoreFLD[mc]
	myV= myV!=null ? myV : [:]
	String message=getMapDescStr(myV)
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
			newLight =  sFLS
		}else{
			newLight =  sTRU
		}
		if(newLight != myGetData('is_lightOld') || myGetData('condition_id')==sNULL || myGetData('cloud')==sNULL) {
			pollOWM()
			return
		}
	}
	def (Long lux, String bwn) = estimateLux(myGetData('condition_id').toInteger(), myGetData('cloud').toInteger())
	myUpdData('illuminance', (!lux) ? sZERO : lux.toString())
	myUpdData('illuminated', String.format('%,4d', (!lux) ? 0 : lux).toString())
	myUpdData('bwn', bwn)
	if(pollAgain) PostPoll()
}
// >>>>>>>>>> End Lux Processing <<<<<<<<<<

// <<<<<<<<<< Begin Post-Poll Routines >>>>>>>>>>
void PostPoll() {
	if(ifreInstalled()) { updated(); return }
	Integer mult_twd = myGetData('mult_twd')==sNULL ? 1 : myGetData('mult_twd').toInteger()
	String ddisp_twd = myGetData('ddisp_twd')==sNULL ? '%3.0f' : myGetData('ddisp_twd')
	String ddisp_p = myGetData('ddisp_p')==sNULL ? '%4.0f' : myGetData('ddisp_p')
	String ddisp_r = myGetData('ddisp_r')==sNULL ? '%2.0f' : myGetData('ddisp_r')

	String tfmt='yyyy-MM-dd\'T\'HH:mm:ssXXX'
	String tfmt1=myGetData('timeFormat')
	if(localSunrisePublish){  // don't bother setting these values if it's not enabled
		sendEvent(name: 'tw_begin', value: myGetData('tw_begin'))
		sendEvent(name: 'sunriseTime', value: myGetData('riseTime'))
		sendEvent(name: 'noonTime', value: myGetData('noonTime'))
		sendEvent(name: 'sunsetTime', value: myGetData('setTime'))
		sendEvent(name: 'tw_end', value: myGetData('tw_end'))
	}else{
		device.deleteCurrentState('tw_begin')
		device.deleteCurrentState('sunriseTime')
		device.deleteCurrentState('noonTime')
		device.deleteCurrentState('sunsetTime')
		device.deleteCurrentState('tw_end')
	}
	if(dashSharpToolsPublish || dashSmartTilesPublish || localSunrisePublish) {
		sendEvent(name: 'localSunset', value: myGetData('localSunset')) // only needed for certain dashboards
		sendEvent(name: 'localSunrise', value: myGetData('localSunrise')) // only needed for certain dashboards
	}else{
		device.deleteCurrentState('localSunset')
		device.deleteCurrentState('localSunrise')
	}

/*  Capability Data Elements */
	sendEvent(name: 'humidity', value: myGetDataBD('humidity'), unit: '%')
	sendEvent(name: 'illuminance', value: myGetData('illuminance').toInteger(), unit: 'lx')
	sendEvent(name: 'pressure', value: myGetDataBD('pressure'), unit: myGetData(sPMETR))
	if(dashSharpToolsPublish || dashSmartTilesPublish){
		sendEvent(name: 'pressured', value: String.format(ddisp_p, myGetDataBD('pressure')), unit: myGetData(sPMETR))
	}else{
		device.deleteCurrentState('pressured')
	}
	String tmetr= myGetData(sTMETR)
	sendEvent(name: sTEMP, value: myGetDataBD(sTEMP), unit: tmetr)
	sendEvent(name: 'ultravioletIndex', value: myGetDataBD('ultravioletIndex'), unit: 'uvi')
	sendEvent(name: 'feelsLike', value: myGetDataBD('feelsLike'), unit: tmetr)

/*  'Required for Dashboards' Data Elements */
	if(dashHubitatOWMPublish || dashSharpToolsPublish || dashSmartTilesPublish || cityPublish) {
		sendEvent(name: 'city', value: myGetData('city'))
	}else{
		device.deleteCurrentState('city')
	}
	if(dashSharpToolsPublish) {
		sendEvent(name: 'forecastIcon', value: getCondCode(myGetData('condition_id').toInteger(), myGetData('is_day')))
	}else{
		device.deleteCurrentState('forecastIcon')
	}
	if(dashSharpToolsPublish || dashSmartTilesPublish || rainTodayPublish) {
		sendEvent(name: 'rainToday', value: myGetDataBD('rainToday'), unit: myGetData(sRMETR))
	}else{
		device.deleteCurrentState('rainToday')
	}
	if(dashSharpToolsPublish || dashSmartTilesPublish || percentPrecipPublish) {
		sendEvent(name: 'percentPrecip', value: myGetData('percentPrecip').toInteger())
	}else{
		device.deleteCurrentState('percentPrecip')
	}
	if(dashSharpToolsPublish || dashSmartTilesPublish) {
		sendEvent(name: 'weather', value: myGetData('condition_text'))
	}else{
		device.deleteCurrentState('weather')
	}
	if(dashSharpToolsPublish || dashSmartTilesPublish) {
		sendEvent(name: 'weatherIcon', value: getCondCode(myGetData('condition_id').toInteger(), myGetData('is_day')))
	}else{
		device.deleteCurrentState('weatherIcon')
	}
	if(dashHubitatOWMPublish) {
		sendEvent(name: "weatherIcons", value: myGetData('OWN_icon'))
	}else{
		device.deleteCurrentState('weatherIcons')
	}
	if(dashHubitatOWMPublish || dashSharpToolsPublish || windPublish) {
		sendEvent(name: 'wind', value: myGetDataBD('wind'), unit: myGetData(sDMETR))
	}else{
		device.deleteCurrentState('wind')
	}
	if(dashHubitatOWMPublish) {
		sendEvent(name: 'windSpeed', value: myGetDataBD('wind'), unit: myGetData(sDMETR))
	}else{
		device.deleteCurrentState('windSpeed')
	}
	if(dashHubitatOWMPublish) {
		sendEvent(name: 'windDirection', value: myGetData('wind_degree').toInteger(), unit: 'DEGREE')
	}else{
		device.deleteCurrentState('windDirection')
	}

/*  Selected optional Data Elements */
	sendEventPublish(name: 'betwixt', value: myGetData('bwn'))
	sendEventPublish(name: 'cloud', value: myGetData('cloud').toInteger(), unit: '%')
	sendEventPublish(name: 'condition_code', value: myGetData('condition_code'))
	sendEventPublish(name: 'condition_text', value: myGetData('condition_text'))
	sendEventPublish(name: 'dewpoint', value: myGetDataBD('dewpoint'), unit: tmetr)

	sendEventPublish(name: 'forecast_code', value: myGetData('forecast_code'))
	if(forecast_textPublish) {
		sendEventPublish(name: 'forecast_text', value: myGetData('forecast_text'))
		sendEvent(name: 'forecast_text1', value: myGetData('forecast_text1'))
		sendEvent(name: 'forecast_text2', value: myGetData('forecast_text2'))
	}else{
		device.deleteCurrentState('forecast_text1')
		device.deleteCurrentState('forecast_text2')
	}
	if(fcstHighLowPublish){ // don't bother setting these values if it's not enabled
		sendEvent(name: 'forecastHigh', value: myGetDataBD('forecastHigh'), unit: tmetr)
		sendEvent(name: 'forecastHigh1', value: myGetDataBD('forecastHigh1'), unit: tmetr)
		sendEvent(name: 'forecastHigh2', value: myGetDataBD('forecastHigh2'), unit: tmetr)
		sendEvent(name: 'forecastLow', value: myGetDataBD('forecastLow'), unit: tmetr)
		sendEvent(name: 'forecastLow1', value: myGetDataBD('forecastLow1'), unit: tmetr)
		sendEvent(name: 'forecastLow2', value: myGetDataBD('forecastLow2'), unit: tmetr)
		sendEvent(name: 'forecastMorn', value: myGetDataBD('forecastMorn'), unit: tmetr)
		sendEvent(name: 'forecastDay', value: myGetDataBD('forecastDay'), unit: tmetr)
		sendEvent(name: 'forecastEve', value: myGetDataBD('forecastEve'), unit: tmetr)
		sendEvent(name: 'forecastNight', value: myGetDataBD('forecastNight'), unit: tmetr)
		sendEvent(name: 'forecastMorn1', value: myGetDataBD('forecastMorn1'), unit: tmetr)
		sendEvent(name: 'forecastDay1', value: myGetDataBD('forecastDay1'), unit: tmetr)
		sendEvent(name: 'forecastEve1', value: myGetDataBD('forecastEve1'), unit: tmetr)
		sendEvent(name: 'forecastNight1', value: myGetDataBD('forecastNight1'), unit: tmetr)
	}else{
		device.deleteCurrentState('forecastHigh')
		device.deleteCurrentState('forecastHigh1')
		device.deleteCurrentState('forecastHigh2')
		device.deleteCurrentState('forecastLow')
		device.deleteCurrentState('forecastLow1')
		device.deleteCurrentState('forecastLow2')
		device.deleteCurrentState('forecastMorn')
		device.deleteCurrentState('forecastDay')
		device.deleteCurrentState('forecastEve')
		device.deleteCurrentState('forecastNight')
		device.deleteCurrentState('forecastMorn1')
		device.deleteCurrentState('forecastDay1')
		device.deleteCurrentState('forecastEve1')
		device.deleteCurrentState('forecastNight1')
	}
	sendEventPublish(name: 'illuminated', value: myGetData('illuminated') + ' lx')
	sendEventPublish(name: 'is_day', value: myGetData('is_day'))

	//suncalc
	if(suncalcPublish){  // don't bother setting these values if it's not enabled
		LinkedHashMap<String,Double> coords = getPosition()
		sendEvent(name: 'altitude', value: coords.altitude)
		sendEvent(name: 'azimuth', value: coords.azimuth)
	}else{
		device.deleteCurrentState('altitude')
		device.deleteCurrentState('azimuth')
	}

    sendEvent(name: 'moonrise', value: myGetData('moonrise'))
    sendEvent(name: 'moonset', value: myGetData('moonset'))
    sendEvent(name: 'moon_phase', value: myGetData('moon_phase'))
    
	if(obspollPublish){  // don't bother setting these values if it's not enabled
		TimeZone tZ= TimeZone.getDefault()
		sendEvent(name: 'last_poll_Forecast', value: new Date().parse('EEE MMM dd HH:mm:ss z yyyy', myGetData('futime')).format(myGetData('dateFormat'), tZ) + ', ' + new Date().parse('EEE MMM dd HH:mm:ss z yyyy', myGetData('futime')).format(tfmt1, tZ))
		sendEvent(name: 'last_observation_Forecast', value: new Date().parse('EEE MMM dd HH:mm:ss z yyyy', myGetData('fotime')).format(myGetData('dateFormat'), tZ) + ', ' + new Date().parse('EEE MMM dd HH:mm:ss z yyyy', myGetData('fotime')).format(tfmt1, tZ))
	}else{
		device.deleteCurrentState('last_poll_Forecast')
		device.deleteCurrentState('last_observation_Forecast')
	}

	if(precipExtendedPublish){ // don't bother setting these values if it's not enabled
		sendEvent(name: 'rainTomorrow', value: myGetDataBD('rainTomorrow'), unit: myGetData(sRMETR))
		sendEvent(name: 'rainDayAfterTomorrow', value: myGetDataBD('rainDayAfterTomorrow'), unit: myGetData(sRMETR))
		sendEvent(name: 'PoP1', value: myGetData('PoP1').toInteger())
		sendEvent(name: 'PoP2', value: myGetData('PoP2').toInteger())
	}else{
		device.deleteCurrentState('rainTomorrow')
		device.deleteCurrentState('rainDayAfterTomorrow')
		device.deleteCurrentState('PoP1')
		device.deleteCurrentState('PoP2')
	}
	if(cloudExtendedPublish){ // don't bother setting these values if it's not enabled
		sendEvent(name: 'cloudToday', value: myGetData('cloudToday').toInteger(), unit: '%')
		sendEvent(name: 'cloudTomorrow', value: myGetData('cloudTomorrow').toInteger(), unit: '%')
		sendEvent(name: 'cloudDayAfterTomorrow', value: myGetData('cloudDayAfterTomorrow').toInteger(), unit: '%')
	}else{
		device.deleteCurrentState('cloudToday')
		device.deleteCurrentState('cloudTomorrow')
		device.deleteCurrentState('cloudDayAfterTomorrrow')
	}

	sendEventPublish(name: 'vis', value: Math.round(myGetDataBD('vis') * mult_twd) / mult_twd, unit: (myGetData(sDMETR)=='MPH' ? 'miles' : 'kilometers'))
	sendEventPublish(name: 'wind_degree', value: myGetData('wind_degree').toInteger(), unit: 'DEGREE')
	sendEventPublish(name: 'wind_direction', value: myGetData('wind_direction'))
	sendEventPublish(name: 'wind_cardinal', value: myGetData('wind_cardinal'))
	sendEventPublish(name: 'wind_gust', value: myGetDataBD('wind_gust'), unit: myGetData(sDMETR))
	sendEventPublish(name: 'wind_string', value: myGetData('wind_string'))

	buildweatherSummary()

	String OWMIcon
	String OWMText
	if((alertSource==sZERO) || (alertSource==sONE) || (myGetData('curAl')==sNCWA && alertSource==sTWO)) {
		OWMIcon = '<a href="https://openweathermap.org/city/' + myGetData('OWML') + '" target="_blank">' + sIMGS5 + myGetData(sICON) + 'OWM.png style="height:2em"></a> @ ' + myGetData(sSUMLST)
		OWMText = '<a href="https://openweathermap.org" target="_blank">OpenWeatherMap.org</a> @ ' + myGetData(sSUMLST)
	}else{
		OWMIcon = '<a href="https://tinyurl.com/zznws?lat=' + altLat + '&lon=' + altLon + '" target="_blank">' + sIMGS5 + myGetData(sICON) + 'NWS_240px.png style="height:2em"></a> @ ' + myGetData(sSUMLST)
		OWMText = '<a href="https://tinyurl.com/zznws?lat=' + altLat + '&lon=' + altLon + '" target="_blank">Weather.gov</a> @ ' + myGetData(sSUMLST)
	}
//  <<<<<<<<<< Begin Built 3dayfcstTile >>>>>>>>>>
	if(threedayTilePublish) {
		Boolean gitclose = (myGetData(sICON).toLowerCase().contains('://github.com/')) && (myGetData(sICON).toLowerCase().contains('/blob/master/'))
		String iconClose = (gitclose ? '?raw=true>' : sRB)
		String my3day
		my3day = '<style type="text/css">.cI{height:45%}.cIb{height:80%}</style>'
		my3day += '<table style="text-align:center;display:inline">'
		my3day += sTR
		my3day += '<B>' + myGetData('city') +'</B>'
		my3day += sTD+'Today'
		my3day += sTD + myGetData('day1')
		my3day += sTD + myGetData('day2')
		my3day += sTR
		my3day += 'Now' + String.format(ddisp_twd, myGetDataBD(sTEMP)) + tmetr + sBR + 'Feels' + String.format(ddisp_twd, myGetDataBD('feelsLike')) + tmetr
		my3day += sTD + myGetData('imgName0')
		my3day += sTD + myGetData('imgName1')
		my3day += sTD + myGetData('imgName2')
		my3day += sTR
		my3day += sTD + myGetData('condition_text')
		my3day += sTD + myGetData('forecast_text1')
		my3day += sTD + myGetData('forecast_text2')
		my3day += sTR
		if(myGetData('threedayLH')==sFLS){
			my3day += 'Low High'
			my3day += sTD + String.format(ddisp_twd, myGetDataBD('forecastLow')) + tmetr + sSPC + String.format(ddisp_twd, myGetDataBD('forecastHigh')) + tmetr
			my3day += sTD + String.format(ddisp_twd, myGetDataBD('forecastLow1')) + tmetr + sSPC + String.format(ddisp_twd, myGetDataBD('forecastHigh1')) + tmetr
			my3day += sTD + String.format(ddisp_twd, myGetDataBD('forecastLow2')) + tmetr + sSPC + String.format(ddisp_twd, myGetDataBD('forecastHigh2')) + tmetr
		}else{
			my3day += 'High Low'
			my3day += sTD + String.format(ddisp_twd, myGetDataBD('forecastHigh')) + tmetr + sSPC + String.format(ddisp_twd, myGetDataBD('forecastLow')) + tmetr
			my3day += sTD + String.format(ddisp_twd, myGetDataBD('forecastHigh1')) + tmetr + sSPC + String.format(ddisp_twd, myGetDataBD('forecastLow1')) + tmetr
			my3day += sTD + String.format(ddisp_twd, myGetDataBD('forecastHigh2')) + tmetr + sSPC + String.format(ddisp_twd, myGetDataBD('forecastLow2')) + tmetr
		}
		my3day += sTR
		my3day += 'PoP Precip'
		my3day += sTD + myGetData('PoP') + '% ' + (myGetDataBD('Precip0') > 0 ? String.format(ddisp_r, myGetDataBD('Precip0')) + myGetData(sRMETR) : 'None')
		my3day += sTD + myGetData('PoP1') + '% ' + (myGetDataBD('Precip1') > 0 ? String.format(ddisp_r, myGetDataBD('Precip1')) + myGetData(sRMETR) : 'None')
		my3day += sTD + myGetData('PoP2') + '% ' + (myGetDataBD('Precip2') > 0 ? String.format(ddisp_r, myGetDataBD('Precip2')) + myGetData(sRMETR) : 'None')
		my3day += '<tr style="font-size:85%">' + '<td  colspan="4">'
		my3day += sIMGS8 + myGetData(sICON) + 'wsr.png' + iconClose + myGetData('localSunrise') + sSPC + sIMGS8 + myGetData(sICON) + 'wss.png' + iconClose + myGetData('localSunset')

		if((my3day.length() + OWMIcon.length()+8) < 1025) {
			my3day += OWMIcon
		}else if((my3day.length() + OWMText.length()+8) < 1025) {
			my3day += OWMText
		}else{
			my3day += 'OpenWeatherMap.org'
		}
		my3day += '</table>'
		if(my3day.length() > 1024) {
			LOGWARN('Too much data to display.</br></br>Current threedayfcstTile length (' + my3day.length() + ') exceeds maximum tile length by ' + (my3day.length() - 1024).toString()  + ' characters.')
		}
		sendEvent(name: 'threedayfcstTile', value: my3day.take(1024))
	}else{
		device.deleteCurrentState('threedayfcstTile')
	}
//  >>>>>>>>>> End Built 3dayfcstTile <<<<<<<<<<
	buildMyText()
}

void buildweatherSummary() {
	//  <<<<<<<<<< Begin Built Weather Summary text >>>>>>>>>>
	String ddisp_twd = myGetData('ddisp_twd')==sNULL ? '%3.0f' : myGetData('ddisp_twd')

	if(weatherSummaryPublish){ // don't bother setting these values if it's not enabled
		String Summary_forecastTemp = ' with a high of ' + String.format(ddisp_twd, myGetDataBD('forecastHigh')) + myGetData(sTMETR) + ' and a low of ' + String.format(ddisp_twd, myGetDataBD('forecastLow')) + myGetData(sTMETR) + '. '
		String Summary_precip = 'There is a ' + myGetData('percentPrecip') + '% chance of precipitation. '
		LOGINFO('Summary_precip: ' + Summary_precip)
		String Summary_vis = 'Visibility is around ' + String.format(ddisp_twd, myGetDataBD('vis')) + (myGetData(sDMETR)=='MPH' ? ' miles.' : ' kilometers.')
		SummaryMessage((Boolean)settings.summaryType, myGetData('Summary_last_poll_date'), myGetData(sSUMLST), Summary_forecastTemp, Summary_precip, Summary_vis)
	}
//  >>>>>>>>>> End Built Weather Summary text <<<<<<<<<<
}
// >>>>>>>>>> End Post-Poll Routines <<<<<<<<<<
void buildMyText() {
	String ddisp_twd = myGetData('ddisp_twd')==sNULL ? '%3.0f' : myGetData('ddisp_twd')
	String ddisp_p = myGetData('ddisp_p')==sNULL ? '%4.0f' : myGetData('ddisp_p')
	String ddisp_r = myGetData('ddisp_r')==sNULL ? '%2.0f' : myGetData('ddisp_r')
//  <<<<<<<<<< Begin Built mytext >>>>>>>>>>
	String OWMIcon
	String OWMText
	if((alertSource==sZERO) || (alertSource==sONE) || (myGetData('curAl')==sNCWA && alertSource==sTWO)) {
		OWMIcon = '<a href="https://openweathermap.org/city/' + myGetData('OWML') + '" target="_blank">' + sIMGS5 + myGetData(sICON) + 'OWM.png style="height:2em"></a> @ ' + myGetData(sSUMLST)
		OWMText = '<a href="https://openweathermap.org" target="_blank">OpenWeatherMap.org</a> @ ' + myGetData(sSUMLST)
	}else{
		OWMIcon = '<a href="https://tinyurl.com/zznws?lat=' + altLat + '&lon=' + altLon + '" target="_blank">' + sIMGS5 + myGetData(sICON) + 'NWS_240px.png style="height:2em"></a> @ ' + myGetData(sSUMLST)
		OWMText = '<a href="https://tinyurl.com/zznws?lat=' + altLat + '&lon=' + altLon + '" target="_blank">Weather.gov</a> @ ' + myGetData(sSUMLST)
	}
	if(myTilePublish) { // don't bother setting these values if it's not enabled
		Boolean gitclose = (myGetData(sICON).toLowerCase().contains('://github.com/')) && (myGetData(sICON).toLowerCase().contains('/blob/master/'))
		String iconClose = (gitclose ? '?raw=true>' : sRB)
		Boolean noAlert = (!alertPublish) ? true : (!myGetData('possAlert') || myGetData('possAlert') == sBLK || myGetData('possAlert') == sFLS)
		String alertStyleOpen = (noAlert ? sBLK : '<span>')
		String alertStyleClose = (noAlert ? sBLK : sCSPAN)

		BigDecimal wgust
		if (myGetDataBD('wind_gust') < 1.0) {
			wgust = 0.0g
		} else {
			wgust = myGetDataBD('wind_gust')
		}
		String mytext
		mytext = '<style type="text/css">.cI{height:45%}.cIb{height:80%}</style>'
		mytext += '<table style="text-align:center;display:inline">'
		mytext += sTR + '<B>' + myGetData('city') + '</B>'
		mytext += sTR + myGetData('condition_text') + (noAlert ? sBLK : ' | ') + alertStyleOpen + (noAlert ? sBLK : myGetData('alertLink')) + alertStyleClose
		LOGINFO("myGetData('imgName0') is " + myGetData('imgName0'))
		mytext += sTR + String.format(ddisp_twd, myGetDataBD(sTEMP)) + myGetData(sTMETR) + myGetData('imgName0')
		mytext += 'Feels like ' + String.format(ddisp_twd, myGetDataBD('feelsLike')) + myGetData(sTMETR)
		mytext += '<tr style="font-size:85%">' + sTD + sIMGS8 + myGetData(sICON) + myGetData('wind_bft_icon') + iconClose + myGetData('wind_direction') + sSPC
		mytext += (myGetDataBD('wind') < 1.0 ? 'calm' : '@ ' + String.format(ddisp_twd, myGetDataBD('wind')) + sSPC + myGetData(sDMETR))
		mytext += ', gusts ' + ((wgust < 1.0) ? 'calm' : '@ ' + String.format(ddisp_twd, wgust) + sSPC + myGetData(sDMETR))

		String mytexte
		mytexte = '<tr style="font-size:80%">' + sTD + sIMGS8 + myGetData(sICON) + 'wb.png' + iconClose + String.format(ddisp_p, myGetDataBD('pressure')) + sSPC + myGetData(sPMETR) + sSPC + sIMGS8 + myGetData(sICON) + 'wh.png' + iconClose
		mytexte += myGetData('humidity') + '%' + sSPC + sIMGS8 + myGetData(sICON) + 'wu.png' + iconClose + myGetData('percentPrecip') + '%' + sSPC + sIMGS8 + myGetData(sICON) + 'wr.png' + iconClose + (myGetDataBD('rainToday') > 0 ? String.format(ddisp_r, myGetDataBD('rainToday')) + sSPC + myGetData(sRMETR) : 'None') + sBR
		mytexte += sIMGS8 + myGetData(sICON) + 'wsr.png' + iconClose + myGetData('localSunrise') + sSPC + sIMGS8 + myGetData(sICON) + 'wss.png' + iconClose + myGetData('localSunset')
		if ((mytext.length() + mytexte.length() + OWMIcon.length() + 8) < 1025) {
			mytext += mytexte + OWMIcon
		} else {
			mytexte = '<tr style="font-size:80%">' + sTD + '<B>B:</B> ' + String.format(ddisp_p, myGetDataBD('pressure')) + sSPC + myGetData(sPMETR) + sSPC + '<B>H:</B> '
			mytexte += myGetData('humidity') + '%' + sSPC + '<B>PoP:</B> ' + myGetData('percentPrecip') + '%' + sSPC + '<B>Precip:</B> ' + (myGetDataBD('rainToday') > 0 ? String.format(ddisp_r, myGetDataBD('rainToday')) + sSPC + myGetData(sRMETR) : 'None') + sBR
			mytexte += '<B>SRise:</B> ' + myGetData('localSunrise') + sSPC + '<B>SSet:</B> ' + myGetData('localSunset')
			mytext += mytexte
			if ((mytext.length() + OWMIcon.length() + 8) < 1025) {
				mytext += OWMIcon
			} else if ((mytext.length() + OWMText.length() + 8) < 1025) {
				mytext += OWMText
			} else {
				mytext += 'OpenWeatherMap.org'
			}
		}
		mytext += '</table>'
		if (mytext.length() > 1024) {
			LOGWARN('Too much data to display.</br></br>Current myTile length (' + mytext.length() + ') exceeds maximum tile length by ' + (mytext.length() - 1024).toString() + ' characters.')
		}
		sendEvent(name: 'myTile', value: mytext.take(1024))
	}else{
		device.deleteCurrentState('myTile')
	}
//  >>>>>>>>>> End Built mytext <<<<<<<<<<
}

void refresh() {
	updateLux(true)
}

void installed() {
}

@Field static Map<String,String> verFLD=[:]

Boolean ifreInstalled(){
	String mc=device.id.toString()
	if(verFLD[mc]!=version()) return true
	return false
}

void updated(){
	LOGINFO("running updated()")
	unschedule()
	String mc=device.id.toString()
	verFLD[mc]=version()
	initMe()
	updateCheck()
	runIn(5,finishSched)
}

Long wnow(){ return (Long)now() }

void finishSched() {
	pollSunRiseSet()
	initialize_poll()
	runEvery10Minutes(updateLux, [Data: [true]])
	Random rand = new Random(wnow())
	Integer ssseconds = rand.nextInt(60)
	schedule("${ssseconds} 20 0/8 ? * * *", pollSunRiseSet)
	runIn(5, pollData)
	if(settingEnable) runIn(2100,settingsOff)// 'roll up' (hide) the condition selectors after 35 min
	if(settings.txtEnable) runIn(1800,logsOff)// turns off extended logging after 30 min
	Integer r_minutes = rand.nextInt(60)
	schedule("0 ${r_minutes} 8 ? * FRI *", updateCheck)
}

void initMe() {
	myUpdData('is_light', sTRU)
	myUpdData('is_lightOld', myGetData('is_light')) //avoid startup oscillation
	String city = (settings.city ?: sBLK)
	myUpdData('city', city)
	myUpdData('threedayLH', settings.threedayLH ? sTRU : sFLS)
	Boolean altCoord = (settings.altCoord ?: false)
	String valtLat; valtLat = location.latitude.toString().replace(sSPC, sBLK)
	String valtLon; valtLon = location.longitude.toString().replace(sSPC, sBLK)
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
				LOGERR('The Override Coordinates feature is selected but Both Hub & the Override Latitude are null.')
			}else{
				device.updateSetting('altLat', [value:valtLat,type:'text'])
			}
			if ((valtLon == null) || (valtLon = sBLK)) {
				LOGERR('The Override Coordinates feature is selected but Both Hub & the Override Longitude are null.')
			}else{
				device.updateSetting('altLon', [value:valtLon,type:'text'])
			}
		}
	}else{
		device.updateSetting('altLat', [value:valtLat,type:'text'])
		device.updateSetting('altLon', [value:valtLon,type:'text'])
		if (altLat == null || altLon == null) {
			if ((valtLat == null) || (valtLat = sBLK)) {
				LOGERR('The Hub\'s latitude is not set. Please set it, or use the Override Coordinates feature.')
			}else{
				device.updateSetting('altLat', [value:valtLat,type:'text'])
			}
			if ((valtLon == null) || (valtLon = sBLK)) {
				LOGERR('The Hub\'s longitude is not set. Please set it, or use the Override Coordinates feature.')
			}else{
				device.updateSetting('altLon', [value:valtLon,type:'text'])
			}
		}
	}
	Boolean iconType = (settings.iconType ?: false)
	myUpdData('iconType', iconType ? sTRU : sFLS)
//	https://tinyurl.com/icnqz/ points to https://raw.githubusercontent.com/HubitatCommunity/WeatherIcons/master/
	String iconLocation = (settings.iconLocation ?: 'https://tinyurl.com/icnqz/')
	myUpdData(sICON, iconLocation)
	state.OWM = '<a href="https://openweathermap.org" target="_blank">'+sIMGS5 + myGetData(sICON) + 'OWM.png style="height:2em"></a>'
	setDateTimeFormats((String)settings.datetimeFormat)
	String distanceFormat = (settings.distanceFormat ?: 'Miles (mph)')
	String pressureFormat = (settings.pressureFormat ?: 'Inches')
	String rainFormat = (settings.rainFormat ?: 'Inches')
	String tempFormat = (settings.tempFormat ?: 'Fahrenheit (°F)')
	setMeasurementMetrics(distanceFormat, pressureFormat, rainFormat, tempFormat)
	String TWDDecimals = (settings.TWDDecimals ?: sZERO)
	String PDecimals = (settings.PDecimals ?: sZERO)
	String RDecimals = (settings.RDecimals ?: sZERO)
	setDisplayDecimals(TWDDecimals, PDecimals, RDecimals)
	pollOWMl()
	if(settings.alertSource==sTWO) {pollWDG()}
}
void pollOWMl() {
/*  for testing a different Lat/Lon location uncommnent the two lines below */
//	String altLat = "44.809122" //"41.5051613" // "40.6" //"38.627003" //"30.6953657"
//	String altLon = "-68.735892" //"-81.6934446" // "-75.43" //"-90.199402" //-88.0398912"
	Map ParamsOWMl = [ uri: 'https://api.openweathermap.org/data/2.5/find?lat=' + (String)altLat + '&lon=' + (String)altLon + '&cnt=1&appid=' + (String)apiKey, timeout: 20 ]
	LOGINFO('Poll OpenWeatherMap.org Location: ' + ParamsOWMl)
	asynchttpGet('pollOWMlHandler', ParamsOWMl)
}
void pollOWMlHandler(resp, data) {
	LOGINFO('Polling OpenWeatherMap.org Location')
	if(resp.getStatus() != 200 && resp.getStatus() != 207) {
		LOGWARN('Calling https://api.openweathermap.org/data/2.5/find?lat=' + (String)altLat + '&lon=' + (String)altLon + '&cnt=1&appid=' + (String)apiKey)
		LOGWARN(resp.getStatus() + sCOLON + resp.getErrorMessage())
		myUpdData('OWML',sSPC)
	}else{
		Map owml = parseJson(resp.data)
		if(owml.toString()==sNULL) {
			pauseExecution(1000)
			pollOWMl()
			return
		}
		LOGINFO('OpenWeatherMap Location Data: ' + owml.toString())
		myUpdData('OWML',(owml?.list[0]?.id==null ? sSPC : owml.list[0].id.toString()))
		LOGINFO('OWM Location City Code: ' + myGetData('OWML'))
	}
}

void initialize_poll() {
	unschedule(pollOWM)
	Random rand = new Random(wnow())
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
	}else{
		dsseconds = ssseconds - 60 + 4
	}
	String pollIntervalFcst = (settings.pollIntervalForecast ?: '3 Hours')
	String pollIntervalFcstnight = (settings.pollIntervalForecastnight ?: '3 Hours')
	if(myGetData('is_light')==sTRU) {
		myPoll = pollIntervalFcst
	}else{
		myPoll = pollIntervalFcstnight
	}
	if(myPoll == 'Manual Poll Only'){
		LOGINFO('MANUAL FORECAST POLLING ONLY')
	}else{
		myPoll = myPoll.replace(sSPC,sBLK)
		String mySched
		LOGINFO('pollInterval: ' + myPoll)
		switch(myPoll) {
			case '2Minutes':
				mySched = "${dsseconds} ${minutes2}/2 * * * ? *"
				break
			case '5Minutes':
				mySched = "${dsseconds} ${minutes5}/5 * * * ? *"
				break
			case '10Minutes':
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
			default:
				mySched = "${dsseconds} ${minutes60} ${hours3}/3 * * ? *"
		}
		schedule(mySched, pollOWM)
	}
}

void pollData() {
	pollOWM()
}

// ************************************************************************************************

void setDateTimeFormats(String formatselector){
	String mSel = formatselector ?: sONE
	String DTFormat
	String dateFormat
	String timeFormat
	switch(mSel) {
		case sONE: DTFormat = 'M/d/yyyy h:mm a';  dateFormat = 'M/d/yyyy';  timeFormat = 'h:mm a'; break
		case sTWO: DTFormat = 'M/d/yyyy HH:mm';	dateFormat = 'M/d/yyyy';  timeFormat = 'HH:mm';  break
		case '3': DTFormat = 'MM/dd/yyyy h:mm a'; dateFormat = 'MM/dd/yyyy'; timeFormat = 'h:mm a'; break
		case '4': DTFormat = 'MM/dd/yyyy HH:mm';  dateFormat = 'MM/dd/yyyy'; timeFormat = 'HH:mm';  break
		case '5': DTFormat = 'd/M/yyyy h:mm a';  dateFormat = 'd/M/yyyy';  timeFormat = 'h:mm a'; break
		case '6': DTFormat = 'd/M/yyyy HH:mm';	dateFormat = 'd/M/yyyy';  timeFormat = 'HH:mm';  break
		case '7': DTFormat = 'dd/MM/yyyy h:mm a'; dateFormat = 'dd/MM/yyyy'; timeFormat = 'h:mm a'; break
		case '8': DTFormat = 'dd/MM/yyyy HH:mm';  dateFormat = 'dd/MM/yyyy'; timeFormat = 'HH:mm';  break
		case '9': DTFormat = 'yyyy/MM/dd HH:mm';  dateFormat = 'yyyy/MM/dd'; timeFormat = 'HH:mm';  break
		default: DTFormat = 'M/d/yyyy h:mm a';  dateFormat = 'M/d/yyyy';  timeFormat = 'h:mm a'; break
	}
	myUpdData('DTFormat', DTFormat)
	myUpdData('dateFormat', dateFormat)
	myUpdData('timeFormat', timeFormat)
}

void setMeasurementMetrics(String distFormat, String pressFormat, String precipFormat, String temptFormat){
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
	}else{
		dMetric = 'm/s'
	}
	myUpdData(sDMETR, dMetric)

	if(pressFormat == 'Millibar') {
		pMetric = 'MBAR'
	} else if(pressFormat == 'Inches') {
		pMetric = 'inHg'
	}else{
		pMetric = 'hPa'
	}
	myUpdData(sPMETR, pMetric)

	if(precipFormat == 'Millimeters') {
		rMetric = 'mm'
	}else{
		rMetric = 'in'
	}
	myUpdData(sRMETR, rMetric)

	if(temptFormat == 'Fahrenheit (°F)') {
		tMetric = sDF
	}else{
		tMetric = '°C'
	}
	myUpdData(sTMETR, tMetric)
}

void setDisplayDecimals(String TWDDisp, String PressDisp, String RainDisp) {
	String ddisp_twd
	String mult_twd
	String ddisp_p
	String mult_p
	String ddisp_r
	String mult_r
	switch(TWDDisp) {
		case sZERO: ddisp_twd = '%3.0f'; mult_twd = sONE; break
		case sONE: ddisp_twd = '%3.1f'; mult_twd = '10'; break
		case sTWO: ddisp_twd = '%3.2f'; mult_twd = '100'; break
		case '3': ddisp_twd = '%3.3f'; mult_twd = '1000'; break
		case '4': ddisp_twd = '%3.4f'; mult_twd = '10000'; break
		default: ddisp_twd = '%3.0f'; mult_twd = sONE; break
	}
	myUpdData('ddisp_twd', ddisp_twd)
	myUpdData('mult_twd', mult_twd)
	switch(PressDisp) {
		case sZERO: ddisp_p = '%,4.0f'; mult_p = sONE; break
		case sONE: ddisp_p = '%,4.1f'; mult_p = '10'; break
		case sTWO: ddisp_p = '%,4.2f'; mult_p = '100'; break
		case '3': ddisp_p = '%,4.3f'; mult_p = '1000'; break
		case '4': ddisp_p = '%,4.4f'; mult_p = '10000'; break
		default: ddisp_p = '%,4.0f'; mult_p = sONE; break
	}
	myUpdData('ddisp_p', ddisp_p)
	myUpdData('mult_p', mult_p)
	switch(RainDisp) {
		case sZERO: ddisp_r = '%2.0f'; mult_r = sONE; break
		case sONE: ddisp_r = '%2.1f'; mult_r = '10'; break
		case sTWO: ddisp_r = '%2.2f'; mult_r = '100'; break
		case '3': ddisp_r = '%2.3f'; mult_r = '1000'; break
		case '4': ddisp_r = '%2.4f'; mult_r = '10000'; break
		default: ddisp_r = '%2.0f'; mult_r = sONE; break
	}
	myUpdData('ddisp_r', ddisp_r)
	myUpdData('mult_r', mult_r)
}

def estimateLux(Integer condition_id, Integer cloud) {
	Long lux
	Boolean aFCC
	aFCC = true
	Double l
	String bwn
	TimeZone tZ					= TimeZone.getDefault() //TimeZone.getTimeZone(tz_id)
	String lT					= new Date().format('yyyy-MM-dd\'T\'HH:mm:ssXXX', tZ)
	Long localeMillis			= getEpoch(lT)
	Long twilight_beginMillis
	Long sunriseTimeMillis
	Long noonTimeMillis
	Long sunsetTimeMillis
	Long twilight_endMillis

	Date dnow= new Date()
	String currDate = dnow.format('yyyy-MM-dd', tZ)
	Date tSunrise, tSunset
	tSunrise = (Date)todaysSunrise
	tSunrise = (!tSunrise || tSunrise == null) ? Date.parse("yyyy-MM-dd hh:mm:ss", currDate + " 00:00:00") : tSunrise

	tSunset = (Date)todaysSunset
	if(!tSunset || tSunset == null){
		String currYear = dnow.format('yyyy', tZ)
		Date mar21= Date.parse("yyyy-MM-dd", currYear + '-03-21')
		Date sep21= Date.parse("yyyy-MM-dd", currYear + '-09-21')
		Boolean isBtwn= (dnow >= mar21 && dnow < sep21)
		Date twelve59= Date.parse("yyyy-MM-dd hh:mm:ss", currDate + " 23:59:59")
		Date mid01= Date.parse("yyyy-MM-dd hh:mm:ss", currDate + " 00:00:01")
		if(altLat.toDouble() > 0.0D) {
			tSunset = isBtwn ? twelve59 : mid01
		} else {
			tSunset = !isBtwn ? twelve59 : mid01
		}
	}

	twilight_beginMillis	= tSunrise.getTime() - 1500000L // (25*60*1000) // 25 minutes before sunrise
	sunriseTimeMillis	= tSunrise.getTime()
	noonTimeMillis		= tSunrise.getTime() + (tSunset.getTime() - tSunrise.getTime()).intdiv(2)
	sunsetTimeMillis	= tSunset.getTime()
	twilight_endMillis	= tSunset.getTime() + 1500000L // (25*60*1000) // 25 minutes after sunset

	Long twiStartNextMillis		= twilight_beginMillis + 86400000L // = 24*60*60*1000 --> one day in milliseconds
	Long sunriseNextMillis		= sunriseTimeMillis + 86400000L
	Long noonTimeNextMillis		= noonTimeMillis + 86400000L
	Long sunsetNextMillis		= sunsetTimeMillis + 86400000L
	Long twiEndNextMillis		= twilight_endMillis + 86400000L

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
	String cCT; cCT = ' using cloud cover from API'
	Double cCF; cCF = (!cloud || cloud==sBLK) ? 0.998d : (1 - (cloud/100 / 3d))
	if(aFCC){
		if(!cloud){
			Map LUitem = LUTable.find{ Map it -> (Integer)it.id == condition_id }
			if (LUitem)	{
				cCF = LUitem.luxp
				cCT = ' using estimated cloud cover based on condition.'
			}else{
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
			Long t0 = Math.round(lux/800)
			lux = t0 * 800
		} else if(lux <= 1100 && lux > 400) {
			Long t0 = Math.round(lux/400)
			lux = t0 * 400
		}else{
			lux = 5
		}
	}
	lux = Math.max(lux, 5)
	LOGINFO('estimateLux results: condition: ' + cC + ' | condition factor: ' + cCF + ' | condition text: ' + cCT + '| lux: ' + lux)
	return [lux, bwn]
}

private static Long getEpoch (String aTime) {
	TimeZone tZ = TimeZone.getDefault()
	Date localeTime = new Date().parse('yyyy-MM-dd\'T\'HH:mm:ssXXX', aTime, tZ)
	Long localeMillis = localeTime.getTime()
	return (localeMillis)
}

void SummaryMessage(Boolean SType, String Slast_poll_date, String Slast_poll_time, String SforecastTemp, String Sprecip, String Svis){
	BigDecimal windgust
	if(myGetData('wind_gust') == sBLK || myGetDataBD('wind_gust') < 1.0 || myGetData('wind_gust')==sNULL) {
		windgust = 0.00g
	}else{
		windgust = myGetDataBD('wind_gust')
	}
	String wSum // = (String)null
	if(SType){
		wSum = 'Weather summary for ' + myGetData('city') + ' updated at ' + Slast_poll_time + ' on ' + Slast_poll_date + '. '
		wSum+= myGetData('condition_text')
		wSum+= (!SforecastTemp || SforecastTemp==sBLK) ? '. ' : SforecastTemp
		wSum+= 'Humidity is ' + myGetData('humidity') + '% and the temperature is ' + String.format(myGetData('ddisp_twd'), myGetDataBD(sTEMP)) + myGetData(sTMETR) + '. '
		wSum+= 'The temperature feels like it is ' + String.format(myGetData('ddisp_twd'), myGetDataBD('feelsLike')) + myGetData(sTMETR) + '. '
		wSum+= 'Wind: ' + myGetData('wind_string') + ', gusts: ' + ((windgust < 1.00) ? 'calm. ' : 'up to ' + windgust.toString() + sSPC + myGetData(sDMETR) + '. ')
		wSum+= Sprecip
		wSum+= Svis
		wSum+= alertPublish ? ((!myGetData('alert') || myGetData('alert')==sNULL) ? sBLK : sSPC + myGetData('alert') + sDOT) : sBLK
	}else{
		wSum = myGetData('condition_text') + sSPC
		wSum+= ((!SforecastTemp || SforecastTemp==sBLK) ? '. ' : SforecastTemp)
		wSum+= ' Humidity: ' + myGetData('humidity') + '%. Temperature: ' + String.format(myGetData('ddisp_twd'), myGetDataBD(sTEMP)) + myGetData(sTMETR) + '. '
		wSum+= myGetData('wind_string') + ', gusts: ' + ((windgust == 0.00) ? 'calm. ' : 'up to ' + windgust + sSPC + myGetData(sDMETR) + sDOT)
	}
	wSum = wSum.take(1024)
	sendEvent(name: 'weatherSummary', value: wSum)
}

String getImgName(Integer wCode, String iconTOD){
	Map LUitem = LUTable.find{ (Integer)it.id == wCode }
	LOGINFO('getImgName Inputs: ' + wCode.toString() + ', ' + iconTOD + ';  Result: ' + (iconTOD==sTRU ? (LUitem ? (String)LUitem.Icd : sNPNG) : (LUitem ? (String)LUitem.Icn : sNPNG)))
	return (iconTOD==sTRU ? (LUitem ? (String)LUitem.Icd : sNPNG) : (LUitem ? (String)LUitem.Icn : sNPNG))
}

String getCondCode(Integer cid, String iconTOD){
	Map LUitem = LUTable.find{ (Integer)it.id == cid }
	LOGINFO('getCondCode Inputs: ' + cid.toString() + ', ' + iconTOD + ';  Result: ' + (iconTOD==sTRU ? (LUitem ? (String)LUitem.sId : sNPNG) : (LUitem ? (String)LUitem.sIn : sNPNG)))
	return (iconTOD==sTRU ? (LUitem ? (String)LUitem.sId : sNPNG) : (LUitem ? (String)LUitem.sIn : sNPNG))
}

void logCheck(){
	if(settings.txtEnable){
		log.info 'OpenWeatherMap.org Weather Driver - INFO:  All Logging Enabled'
	}else{
		log.info 'OpenWeatherMap.org Weather Driver - INFO:  Further Logging Disabled'
	}
}

void LOGDEBUG(String txt){
	if(settings.txtEnable){ log.debug('OpenWeatherMap.org Weather Driver - DEBUG:  ' + txt) }
}

void LOGINFO(String txt){
	if(settings.txtEnable){log.info('OpenWeatherMap.org Weather Driver - INFO:  ' + txt) }
}

void LOGWARN(String txt){
	if(settings.txtEnable){log.warn('OpenWeatherMap.org Weather Driver - WARNING:  ' + txt) }
}

void LOGERR(String txt){
	if(settings.txtEnable){log.error('OpenWeatherMap.org Weather Driver - ERROR:  ' + txt) }
}

void logsOff(){
	log.info 'OpenWeatherMap.org Weather Driver - INFO:  extended logging disabled...'
	device.updateSetting('txtEnable',[value:sFLS,type:'bool'])
}

void settingsOff(){
	log.info 'OpenWeatherMap.org Weather Driver - INFO: Settings disabled...'
	device.updateSetting('settingEnable',[value:sFLS,type:'bool'])
}

/**
 * send event if enabled for publishing, otherwise delete current state
 */
void sendEventPublish(evt)	{
//	Purpose: Attribute sent to DB if selected
	if (settings."${evt.name + 'Publish'}") {
		sendEvent(name: evt.name, value: evt.value, descriptionText: evt.descriptionText, unit: evt.unit, displayed: evt.displayed)
		LOGINFO('Will publish: ' + (String)evt.name) //: evt.name, evt.value evt.unit'
	} else {
		device.deleteCurrentState((String)evt.name)
	}
}

///
/// Calculations
// based on SunCalc by Justin Walker
///

// date/time constants and conversions
static Integer dayMs() { return 1000 * 60 * 60 * 24 }

static Long J1970() { return 2440588L }

static Long J2000() { return 2451545L }

static Double rad() { return  Math.PI / 180.0D }

static Double e() { return  rad() * 23.4397D } // obliquity of the Earth

static Double toJulian() {
	Date date = new Date()
	Double l = date.getTime() / dayMs() - 0.5D + J1970()
	return l
}

static Date fromJulian(Double j)  { return new Date(Math.round((j + 0.5D - J1970()) * dayMs()) ) }
static Integer toDays(){ return toJulian() - J2000() }

// general calculations for position

static Double rightAscension(Double l, Double b) { return Math.atan2(Math.sin(l) * Math.cos(e()) - Math.tan(b) * Math.sin(e()), Math.cos(l)) }
static Double declination(Double l, Double b)    { return Math.asin(Math.sin(b) * Math.cos(e()) + Math.cos(b) * Math.sin(e()) * Math.sin(l)) }

static Double azimuth(Double H, Double phi, Double dec)  { return Math.atan2(Math.sin(H), Math.cos(H) * Math.sin(phi) - Math.tan(dec) * Math.cos(phi)) }
static Double altitude(Double H, Double phi, Double dec) { return Math.asin(Math.sin(phi) * Math.sin(dec) + Math.cos(phi) * Math.cos(dec) * Math.cos(H)) }

static Double siderealTime(Double d, Double lw) { return rad() * (280.16D + 360.9856235D * d) - lw }

// general sun calculations

static Double solarMeanAnomaly(Double d) { return rad() * (357.5291D + 0.98560028D * d) }

static Double eclipticLongitude(Double M) {

	Double C = rad() * (1.9148D * Math.sin(M) + 0.02D * Math.sin(2.0D * M) + 0.0003D * Math.sin(3.0D * M)) // equation of center
	Double P = rad() * 102.9372D // perihelion of the Earth

	return M + C + P + Math.PI
}


static LinkedHashMap<String,Double> sunCoords(Double d) {

	Double M = solarMeanAnomaly(d)
	Double L = eclipticLongitude(M)

	return [dec: declination(L, 0D), ra: rightAscension(L, 0D)]
}

// calculates sun position for a given date and latitude/longitude

LinkedHashMap<String,Double> getPosition() {

	Double lng = ((String)altLon).toDouble()
	Double lat = ((String)altLat).toDouble()

	Double lw  = rad() * -lng
	Double phi = rad() * lat
	Double d   = toDays()
	LinkedHashMap<String,Double> c  = sunCoords(d)
	Double H  = siderealTime(d, lw) - c.ra

	Double az; az = azimuth(H, phi, c.dec)
	az = (az * 180.0D / Math.PI) + 180.0D

	Double al; al = altitude(H, phi, c.dec)
	al = al * 180.0D / Math.PI

	return [
			azimuth: az,
			altitude: al,
	]
}



@Field final List<Map>	LUTable =	[
[id: 200, OWMd: s11D, OWMn: s11N, Icd: '38.png', Icn: '47.png', luxp: 0.2, sId: sCTS, sIn: sNCTS],
[id: 201, OWMd: s11D, OWMn: s11N, Icd: '38.png', Icn: '47.png', luxp: 0.2, sId: sCTS, sIn: sNCTS],
[id: 202, OWMd: s11D, OWMn: s11N, Icd: '38.png', Icn: '47.png', luxp: 0.2, sId: sCTS, sIn: sNCTS],
[id: 210, OWMd: s11D, OWMn: s11N, Icd: '38.png', Icn: '47.png', luxp: 0.2, sId: sCTS, sIn: sNCTS],
[id: 211, OWMd: s11D, OWMn: s11N, Icd: '38.png', Icn: '47.png', luxp: 0.2, sId: sCTS, sIn: sNCTS],
[id: 212, OWMd: s11D, OWMn: s11N, Icd: '38.png', Icn: '47.png', luxp: 0.2, sId: sCTS, sIn: sNCTS],
[id: 221, OWMd: s11D, OWMn: s11N, Icd: '38.png', Icn: '47.png', luxp: 0.2, sId: sCTS, sIn: sNCTS],
[id: 230, OWMd: s11D, OWMn: s11N, Icd: '38.png', Icn: '47.png', luxp: 0.2, sId: sCTS, sIn: sNCTS],
[id: 231, OWMd: s11D, OWMn: s11N, Icd: '38.png', Icn: '47.png', luxp: 0.2, sId: sCTS, sIn: sNCTS],
[id: 232, OWMd: s11D, OWMn: s11N, Icd: '38.png', Icn: '47.png', luxp: 0.2, sId: sCTS, sIn: sNCTS],
[id: 300, OWMd: '09d.png', OWMn: '09n.png', Icd: s9, Icn: s9, luxp: 0.5, sId: sRAIN, sIn: sNRAIN],
[id: 301, OWMd: '09d.png', OWMn: '09n.png', Icd: s9, Icn: s9, luxp: 0.5, sId: sRAIN, sIn: sNRAIN],
[id: 302, OWMd: '09d.png', OWMn: '09n.png', Icd: s9, Icn: s9, luxp: 0.5, sId: sRAIN, sIn: sNRAIN],
[id: 310, OWMd: '09d.png', OWMn: '09n.png', Icd: s9, Icn: s9, luxp: 0.5, sId: sRAIN, sIn: sNRAIN],
[id: 311, OWMd: '09d.png', OWMn: '09n.png', Icd: s9, Icn: s9, luxp: 0.5, sId: sRAIN, sIn: sNRAIN],
[id: 312, OWMd: '09d.png', OWMn: '09n.png', Icd: s9, Icn: s9, luxp: 0.5, sId: sRAIN, sIn: sNRAIN],
[id: 313, OWMd: '09d.png', OWMn: '09n.png', Icd: s9, Icn: s9, luxp: 0.5, sId: sRAIN, sIn: sNRAIN],
[id: 314, OWMd: '09d.png', OWMn: '09n.png', Icd: s9, Icn: s9, luxp: 0.5, sId: sRAIN, sIn: sNRAIN],
[id: 321, OWMd: '09d.png', OWMn: '09n.png', Icd: s9, Icn: s9, luxp: 0.5, sId: sRAIN, sIn: sNRAIN],
[id: 500, OWMd: '10d.png', OWMn: '09n.png', Icd: s39, Icn: s9, luxp: 0.5, sId: sRAIN, sIn: sNRAIN],
[id: 501, OWMd: '10d.png', OWMn: '10n.png', Icd: s39, Icn: '11.png', luxp: 0.5, sId: sRAIN, sIn: sNRAIN],
[id: 502, OWMd: '10d.png', OWMn: '10n.png', Icd: s39, Icn: '11.png', luxp: 0.5, sId: sRAIN, sIn: sNRAIN],
[id: 503, OWMd: '10d.png', OWMn: '10n.png', Icd: s39, Icn: '11.png', luxp: 0.5, sId: sRAIN, sIn: sNRAIN],
[id: 504, OWMd: '10d.png', OWMn: '10n.png', Icd: s39, Icn: '11.png', luxp: 0.5, sId: sRAIN, sIn: sNRAIN],
[id: 511, OWMd: '10d.png', OWMn: '10n.png', Icd: s39, Icn: '11.png', luxp: 0.5, sId: sRAIN, sIn: sNRAIN],
[id: 520, OWMd: '10d.png', OWMn: '09n.png', Icd: s39, Icn: s9, luxp: 0.5, sId: sRAIN, sIn: sNRAIN],
[id: 521, OWMd: '10d.png', OWMn: '10n.png', Icd: s39, Icn: '11.png', luxp: 0.5, sId: sRAIN, sIn: sNRAIN],
[id: 522, OWMd: '10d.png', OWMn: '10n.png', Icd: s39, Icn: '11.png', luxp: 0.5, sId: sRAIN, sIn: sNRAIN],
[id: 531, OWMd: '10d.png', OWMn: '09n.png', Icd: s39, Icn: s9, luxp: 0.5, sId: sRAIN, sIn: sNRAIN],
[id: 600, OWMd: '13d.png', OWMn: '13n.png', Icd: '13.png', Icn: '46.png', luxp: 0.4, sId: 'flurries', sIn: 'nt_snow'],
[id: 601, OWMd: '13d.png', OWMn: '13n.png', Icd: '14.png', Icn: '46.png', luxp: 0.3, sId: 'snow', sIn: 'nt_snow'],
[id: 602, OWMd: '13d.png', OWMn: '13n.png', Icd: '16.png', Icn: '46.png', luxp: 0.3, sId: 'snow', sIn: 'nt_snow'],
[id: 611, OWMd: '13d.png', OWMn: '13n.png', Icd: s9, Icn: '46.png', luxp: 0.5, sId: sRAIN, sIn: 'nt_snow'],
[id: 612, OWMd: '13d.png', OWMn: '13n.png', Icd: '8.png', Icn: '46.png', luxp: 0.5, sId: 'sleet', sIn: 'nt_snow'],
[id: 613, OWMd: '13d.png', OWMn: '13n.png', Icd: s9, Icn: '46.png', luxp: 0.5, sId: sRAIN, sIn: 'nt_snow'],
[id: 615, OWMd: '13d.png', OWMn: '13n.png', Icd: s39, Icn: '45.png', luxp: 0.5, sId: sRAIN, sIn: sNRAIN],
[id: 616, OWMd: '13d.png', OWMn: '13n.png', Icd: s39, Icn: '45.png', luxp: 0.5, sId: sRAIN, sIn: sNRAIN],
[id: 620, OWMd: '13d.png', OWMn: '13n.png', Icd: '13.png', Icn: '46.png', luxp: 0.4, sId: 'flurries', sIn: 'nt_snow'],
[id: 621, OWMd: '13d.png', OWMn: '13n.png', Icd: '16.png', Icn: '46.png', luxp: 0.3, sId: 'snow', sIn: 'nt_snow'],
[id: 622, OWMd: '13d.png', OWMn: '13n.png', Icd: '42.png', Icn: '42.png', luxp: 0.6, sId: 'snow', sIn: 'nt_snow'],
[id: 701, OWMd: '50d.png', OWMn: '50n.png', Icd: s23, Icn: s23, luxp: 0.8, sId: sPCLDY, sIn: sNPCLDY],
[id: 711, OWMd: '50d.png', OWMn: '50n.png', Icd: s23, Icn: s23, luxp: 0.8, sId: sPCLDY, sIn: sNPCLDY],
[id: 721, OWMd: '50d.png', OWMn: '50n.png', Icd: s23, Icn: s23, luxp: 0.8, sId: sPCLDY, sIn: sNPCLDY],
[id: 731, OWMd: '50d.png', OWMn: '50n.png', Icd: s23, Icn: s23, luxp: 0.8, sId: sPCLDY, sIn: sNPCLDY],
[id: 741, OWMd: '50d.png', OWMn: '50n.png', Icd: s23, Icn: s23, luxp: 0.8, sId: sPCLDY, sIn: sNPCLDY],
[id: 751, OWMd: '50d.png', OWMn: '50n.png', Icd: s23, Icn: s23, luxp: 0.8, sId: sPCLDY, sIn: sNPCLDY],
[id: 761, OWMd: '50d.png', OWMn: '50n.png', Icd: s23, Icn: s23, luxp: 0.8, sId: sPCLDY, sIn: sNPCLDY],
[id: 762, OWMd: '50d.png', OWMn: '50n.png', Icd: s23, Icn: s23, luxp: 0.8, sId: sPCLDY, sIn: sNPCLDY],
[id: 771, OWMd: '50d.png', OWMn: '50n.png', Icd: s23, Icn: s23, luxp: 0.8, sId: sPCLDY, sIn: sNPCLDY],
[id: 781, OWMd: '50d.png', OWMn: '50n.png', Icd: s23, Icn: s23, luxp: 0.8, sId: sPCLDY, sIn: sNPCLDY],
[id: 800, OWMd: '01d.png', OWMn: '01n.png', Icd: '32.png', Icn: '31.png', luxp: 1, sId: 'clear', sIn: 'nt_clear'],
[id: 801, OWMd: '02d.png', OWMn: '02n.png', Icd: '34.png', Icn: '33.png', luxp: 0.9, sId: sPCLDY, sIn: sNPCLDY],
[id: 802, OWMd: '03d.png', OWMn: '03n.png', Icd: '30.png', Icn: '29.png', luxp: 0.8, sId: sPCLDY, sIn: sNPCLDY],
[id: 803, OWMd: '04d.png', OWMn: '04n.png', Icd: '28.png', Icn: '27.png', luxp: 0.6, sId: 'mostlycloudy', sIn: 'nt_mostlycloudy'],
[id: 804, OWMd: '04d.png', OWMn: '04n.png', Icd: '26.png', Icn: '26.png', luxp: 0.6, sId: 'cloudy', sIn: 'nt_cloudy'],
[id: 999, OWMd: '50d.png', OWMn: '50n.png', Icd: sNPNG, Icn: sNPNG, luxp: 1.0, sId: 'unknown', sIn: 'unknown'],
	]

@Field final Map<String,Map> attributesMap = [
	'city':						[t: 'Display City', d: 'Display City name?', ty: false, defa: sFLS],
	'threedayTile':				[t: 'Three Day Forecast Tile', d: 'Display Three Day Forecast Tile?', ty: sSTR, defa: sFLS],
	'alert':					[t: 'Weather Alert', d: 'Display any weather alert?', ty: false, defa: sFLS],
	'betwixt':					[t: 'Slice of Day', d: 'Display the slice-of-day?', ty: sSTR, defa: sFLS],
	'cloud':					[t: 'Cloud', d: 'Display cloud coverage %?', ty: sNUM, defa: sFLS],
	'cloudExtended':			[t: 'Cloud Forecast', d: 'Display cloud coverage forecast?', ty: false, defa: sFLS],
	'condition_code':			[t: 'Condition Code', d: 'Display condition_code?', ty: sSTR, defa: sFLS],
	'condition_icon_only':		[t: 'Condition Icon Only', d: 'Display condition_code_only?', ty: sSTR, defa: sFLS],
	'condition_icon_url':		[t: 'Condition Icon URL', d: 'Display condition_code_url?', ty: sSTR, defa: sFLS],
	'condition_icon':			[t: 'Condition Icon', d: 'Display condition_icon?', ty: sSTR, defa: sFLS],
	'condition_iconWithText':	[t: 'Condition Icon With Text', d: 'Display condition_iconWithText?', ty: sSTR, defa: sFLS],
	'condition_text':			[t: 'Condition Text', d: 'Display condition_text?', ty: sSTR, defa: sFLS],
	'dashHubitatOWM':			[t: 'Dash - Hubitat and OpenWeatherMap', d: 'Display attributes required by Hubitat and OpenWeatherMap dashboards?', ty: false, defa: sFLS],
	'dashSmartTiles':			[t: 'Dash - SmartTiles', d: 'Display attributes required by SmartTiles dashboards?', ty: false, defa: sFLS],
	'dashSharpTools':			[t: 'Dash - SharpTools.io', d: 'Display attributes required by SharpTools.io?', ty: false, defa: sFLS],
	'dewpoint':					[t: 'Dewpoint (in default unit)', d: 'Display the dewpoint?', ty: sNUM, defa: sFLS],
	'fcstHighLow':				[t: 'Forecast High/Low Temperatures:', d: 'Display forecast High/Low temperatures?', ty: false, defa: sFLS],
	'forecast_code':			[t: 'Forecast Code', d: 'Display forecast_code?', ty: sSTR, defa: sFLS],
	'forecast_text':			[t: 'Forecast Text', d: 'Display forecast_text?', ty: sSTR, defa: sFLS],
	'illuminated':				[t: 'Illuminated', d: 'Display illuminated (with lux added for use on a Dashboard)?', ty: sSTR, defa: sFLS],
	'is_day':					[t: 'Is daytime', d: 'Display is_day?', ty: sNUM, defa: sFLS],
	'localSunrise':				[t: 'Local SunRise and SunSet', d: 'Display the Group of Time of Local Sunrise and Sunset, with and without Dashboard text?', ty: false, defa: sFLS],
	'myTile':					[t: 'myTile for dashboard', d: 'Display myTile?', ty: sSTR, defa: sFLS],
	'rainToday':				[t: 'Today\'s Precipitation volume', d: 'Display today\'s precipitation volume?', ty: sNUM, defa: sFLS],
	'percentPrecip':			[t: 'Today\'s Precipitation Probability', d: 'Display today\'s precipitation probability?', ty: sNUM, defa: sFLS],
	'precipExtended':			[t: 'Precipitation Forecast', d: 'Display precipitation forecast?', ty: false, defa: sFLS],
	'obspoll':					[t: 'Observation time', d: 'Display Observation and Poll times?', ty: false, defa: sFLS],
	'suncalc':					[t: 'Sun calculations', d: 'Display Altitude and Azuimuth of Sun?', ty: false, defa: sFLS],
	'vis':						[t: 'Visibility (in default unit)', d: 'Display visibility distance?', ty: sNUM, defa: sFLS],
	'weatherSummary':			[t: 'Weather Summary Message', d: 'Display the Weather Summary?', ty: sSTR, defa: sFLS],
	'wind_cardinal':			[t: 'Wind Cardinal', d: 'Display the Wind Direction (text initials)?', ty: sSTR, defa: sFLS],
	'wind_degree':				[t: 'Wind Degree', d: 'Display the Wind Direction (number)?', ty: sNUM, defa: sFLS],
	'wind_direction':			[t: 'Wind direction', d: 'Display the Wind Direction (text words)?', ty: sSTR, defa: sFLS],
	'wind_gust':				[t: 'Wind gust (in default unit)', d: 'Display the Wind Gust?', ty: sNUM, defa: sFLS],
	'wind_string':				[t: 'Wind string', d: 'Display the wind string?', ty: sSTR, defa: sFLS],
]

// Check Version   ***** with great thanks and acknowledgment to Cobra (CobraVmax) for his original code ****
void updateCheck()
{
	Map paramsUD = [uri: 'https://raw.githubusercontent.com/Scottma61/Hubitat/master/docs/version2.json'] //https://hubitatcommunity.github.io/???/version2.json"]
	asynchttpGet('updateCheckHandler', paramsUD)
}

void updateCheckHandler(resp, data) {
	state.InternalName = 'OpenWeatherMap-Alerts Weather Driver'
	Boolean descTextEnable = settings.txtEnable ?: false
	if (resp.getStatus() == 200 || resp.getStatus() == 207) {
		Map respUD = parseJson(resp.data)
		// log.warn " Version Checking - Response Data: $respUD"   // Troubleshooting Debug Code - Uncommenting this line should show the JSON response from your webserver
		state.Copyright = respUD.copyright
		// uses reformattted 'version2.json'
		String Ver = (String)respUD.driver.(state.InternalName).ver
		String newVer = padVer(Ver)
		String currentVer = padVer(version())
		state.UpdateInfo = (respUD.driver.(state.InternalName).updated)
		// log.debug 'updateCheck: ${respUD.driver.(state.InternalName).ver}, $state.UpdateInfo, ${respUD.author}'
		switch(newVer) {
			case { it == 'NLS'}:
				state.Status = '<b>** This Driver is no longer supported by ' + respUD.author +'  **</b>'
				if (descTextEnable) log.warn '** This Driver is no longer supported by ${respUD.author} **'
				break
			case { it > currentVer}:
				state.Status = '<b>New Version Available (Version: ' + Ver + ')</b>'
				if (descTextEnable) log.warn '** There is a newer version of this Driver available  (Version: ' + Ver + ') **'
				if (descTextEnable) log.warn '** ' + (String)state.UpdateInfo + ' **'
				break
			case { it < currentVer}:
				state.Status = '<b>You are using a Test version of this Driver (Expecting: ' + Ver + ')</b>'
				if (descTextEnable) log.warn 'You are using a Test version of this Driver (Expecting: ' + Ver + ')'
				break
			default:
				state.Status = 'Current Version: ' + Ver
				if (descTextEnable) log.info 'You are using the current version of this driver'
				break
		}
	}else{
		log.error 'Something went wrong: CHECK THE JSON FILE AND IT\'S URI'
	}
}

/*
	padVer
	Version progression of 1.4.9 to 1.4.10 would mis-compare unless each duple is padded first.
*/
static String padVer(String ver) {
	String pad
	pad = sBLK
	ver.replaceAll( '[vV]', sBLK ).split( /\./ ).each { String it -> pad += it.padLeft( 2, sZERO ) }
	return pad
}

static String getThisCopyright(){'&copy; 2023 Matthew (scottma61) '}
