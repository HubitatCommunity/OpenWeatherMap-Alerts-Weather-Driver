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

   Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
   in compliance with the License. You may obtain a copy of the License at:

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
   for the specific language governing permissions and limitations under the License.

   Last Update 04/18/2020
  { Left room below to document version changes...}


   V0.0.1   Initial conversion from Dark Sky to OWM                                           - 04/17/2020
   V0.0.2   Fixed Alerts on myTile and alertTile, Capitalized condition_text                  - 04/17/2020
   V0.0.3   More fixes on Alerts, mapped condition_code, weatherIcon(s)                       - 04/18/2020


















=========================================================================================================
**ATTRIBUTES CAUTION**
The way the 'optional' attributes work:
 - Initially, only the optional attributes selected will show under 'Current States' and will be available
   in dashboarowm.
 - Once an attribute has been selected it too will show under 'Current States' and be available in dashboarowm.
   <*** HOWEVER ***> If you ever de-select the optional attribute, it will still show under 'Current States'
   and will still show as an attribute for dashboards **BUT IT'S DATA WILL NO LONGER BE REFRESHED WITH DATA
   POLLS**.  This means what is shown on the 'Current States' and dashboard tiles for de-selected attributes
   may not be current valid data.
 - To my knowledge, the only way to remove the de-selected attribute from 'Current States' and not show it as
   available in the dashboard is to delete the virtual device and create a new one AND DO NOT SELECT the
   attribute you do not want to show.
*/
public static String version()      {  return "0.0.3"  }
import groovy.transform.Field

metadata {
    definition (name: "OpenWeatherMap-NWS Alerts Weather Driver",
                namespace: "Matthew",
                author: "Scottma61",
                importUrl: "https://raw.githubusercontent.com/HubitatCommunity/OpenWeatherMap-NWS-Alerts-Weather-Driver/master/OpenWeatherMap-NWS%2520Alerts%2520Weather%2520Driver.groovy") {
        capability "Sensor"
        capability "Temperature Measurement"
        capability "Illuminance Measurement"
        capability "Relative Humidity Measurement"
 		capability "Pressure Measurement"
 		capability "Ultraviolet Index"

        capability "Refresh"
	
		attributesMap.each
		{
            k, v -> if (v.typeof)        attribute "${k}" , "${v.typeof}"
		}
//    The following attributes may be needed for dashboards that require these attributes,
//    so they are alway available and shown by default.
        attribute "city", "string"              //Hubitat  OpenWeather  SharpTool.io  SmartTiles
        attribute "feelsLike", "number"         //SharpTool.io  SmartTiles
        attribute "forecastIcon", "string"      //SharpTool.io
        attribute "localSunrise", "string"      //SharpTool.io  SmartTiles
        attribute "localSunset", "string"       //SharpTool.io  SmartTiles
        attribute "rainToday", "number"         //SharpTool.io  SmartTiles                   <<-- Check this  -->>
        attribute "pressured", "string"         //UNSURE SharpTool.io  SmartTiles
        attribute "weather", "string"           //SharpTool.io  SmartTiles
        attribute "weatherIcon", "string"       //SharpTool.io  SmartTiles
        attribute "weatherIcons", "string"      //Hubitat  openWeather
        attribute "wind", "number"              //SharpTool.io
        attribute "windDirection", "number"     //Hubitat  OpenWeather
        attribute "windSpeed", "number"         //Hubitat  OpenWeather

//      The attributes below are sub-groups of optional attributes.  They need to be listed here to be available
//alert
        attribute "alert", "string"
        attribute "alertTile", "string"

//threedayTile
        attribute "threedayfcstTile", "string"

//fcstHighLow
		attribute "forecastHigh", "number"
		attribute "forecastLow", "number"

// controlled with localSunrise
		attribute "tw_begin", "string"
		attribute "sunriseTime", "string"
		attribute "noonTime", "string"
		attribute "sunsetTime", "string"
		attribute "tw_end", "string"

//obspoll   these are the same value...
		attribute "last_poll_Forecast", "string"
		attribute "last_observation_Forecast", "string"

//precipExtended
		attribute "rainDayAfterTomorrow", "number"
		attribute "rainTomorrow", "number"

        command "pollData"
    }
    def settingDescr = settingEnable ? "<br><i>Hide many of the optional attributes to reduce the clutter, if needed, by turning OFF this toggle.</i><br>" : "<br><i>Many optional attributes are available to you, if needed, by turning ON this toggle.</i><br>"
    def logDescr = "<br><i>Extended logging will turn off automatically after 30 minutes.</i><br>"
    preferences() {
		section("Query Inputs"){
			input "apiKey", "text", required: true, title: "Type OpenWeatherMap.org API Key Here", defaultValue: null
            input "city", "text", required: true, defaultValue: "City or Location name forecast area", title: "City name"
			input "pollIntervalForecast", "enum", title: "External Source Poll Interval (daytime)", required: true, defaultValue: "3 Hours", options: ["Manual Poll Only", "2 Minutes", "5 Minutes", "10 Minutes", "15 Minutes", "30 Minutes", "1 Hour", "3 Hours"]
            input "pollIntervalForecastnight", "enum", title: "External Source Poll Interval (nighttime)", required: true, defaultValue: "3 Hours", options: ["Manual Poll Only", "2 Minutes", "5 Minutes", "10 Minutes", "15 Minutes", "30 Minutes", "1 Hour", "3 Hours"]
            input "logSet", "bool", title: "Enable extended Logging", description: "<i>Extended logging will turn off automatically after 30 minutes.</i>", required: true, defaultValue: false
	    	input "tempFormat", "enum", required: true, defaultValue: "Fahrenheit (°F)", title: "Display Unit - Temperature: Fahrenheit (°F) or Celsius (°C)",  options: ["Fahrenheit (°F)", "Celsius (°C)"]
            input "TWDDecimals", "enum", required: true, defaultValue: "0", title: "Display decimals for Temperature & Wind Speed", options: [0:"0", 1:"1", 2:"2", 3:"3", 4:"4"]
            input "RDecimals", "enum", required: true, defaultValue: "0", title: "Display decimals for Precipitation", options: [0:"0", 1:"1", 2:"2", 3:"3", 4:"4"]
            input "PDecimals", "enum", required: true, defaultValue: "0", title: "Display decimals for Pressure", options: [0:"0", 1:"1", 2:"2", 3:"3", 4:"4"]
            input "datetimeFormat", "enum", required: true, defaultValue: "1", title: "Display Unit - Date-Time Format",  options: [1:"m/d/yyyy 12 hour (am|pm)", 2:"m/d/yyyy 24 hour", 3:"mm/dd/yyyy 12 hour (am|pm)", 4:"mm/dd/yyyy 24 hour", 5:"d/m/yyyy 12 hour (am|pm)", 6:"d/m/yyyy 24 hour", 7:"dd/mm/yyyy 12 hour (am|pm)", 8:"dd/mm/yyyy 24 hour", 9:"yyyy/mm/dd 24 hour"]
            input "distanceFormat", "enum", required: true, defaultValue: "Miles (mph)", title: "Display Unit - Distance/Speed: Miles, Kilometers, knots or meters",  options: ["Miles (mph)", "Kilometers (kph)", "knots", "meters (m/s)"]
            input "pressureFormat", "enum", required: true, defaultValue: "Inches", title: "Display Unit - Pressure: Inches or Millibar/Hectopascal",  options: ["Inches", "Millibar", "Hectopascal"]
            input "rainFormat", "enum", required: true, defaultValue: "Inches", title: "Display Unit - Precipitation: Inches or Millimeters",  options: ["Inches", "Millimeters"]
            input "luxjitter", "bool", title: "Use lux jitter control (rounding)?", required: true, defaultValue: false
			input "iconLocation", "text", required: true, defaultValue: "https://tinyurl.com/y6xrbhpf/", title: "Alternative Icon Location:"
            input "iconType", "bool", title: "Condition Icon: On = Current - Off = Forecast", required: true, defaultValue: false
            input "altCoord", "bool", required: true, defaultValue: false, title: "Override Hub's location coordinates"
            if (altCoord) {
                input "altLat", "string", title: "Override location Latitude", required: true, defaultValue: location.latitude.toString(), description: "<br>Enter location Latitude<br>"
                input "altLon", "string", title: "Override location Longitude", required: true, defaultValue: location.longitude.toString(), description: "<br>Enter location Longitude<br>"
            }
            input "settingEnable", "bool", title: "<b>Display All Optional Attributes</b>", description: "$settingDescr", defaultValue: true
	// build a Selector for each mapped Attribute or group of attributes
	    	attributesMap.each
		    {
	    		keyname, attribute ->
                if (settingEnable) {
                    input "${keyname}Publish", "bool", title: "${attribute.title}", required: true, defaultValue: "${attribute.default}", description: "<br>${attribute.descr}<br>"
                    if(keyname == "weatherSummary") input "summaryType", "bool", title: "Full Weather Summary", description: "<br>Full: on or short: off summary?<br>", required: true, defaultValue: false
                }
	    	}
            if (settingEnable) {
                input "windPublish", "bool", title: "Wind Speed", required: true, defaultValue: "false", description: "<br>Display 'wind' speed<br>"
            }
        }
    }
}

// <<<<<<<<<< Begin Sunrise-Sunset Poll Routines >>>>>>>>>>
void pollSunRiseSet() {
    String currDate = new Date().format("yyyy-MM-dd", TimeZone.getDefault())
    LOGINFO("OpenWeatherMap.org Weather Driver - INFO: Polling Sunrise-Sunset.org")
    def requestParams = [ uri: "https://api.sunrise-sunset.org/json?lat=" + altLat + "&lng=" + altLon + "&formatted=0" ]
    if (currDate) {requestParams = [ uri: "https://api.sunrise-sunset.org/json?lat=" + altLat + "&lng=" + altLon + "&formatted=0&date=$currDate" ]}
    LOGINFO("Poll Sunrise-Sunset: $requestParams")
    asynchttpGet("sunRiseSetHandler", requestParams)
    return
}

void sunRiseSetHandler(resp, data) {
	if(resp.getStatus() == 200 || resp.getStatus() == 207) {
		sunRiseSet = resp.getJson().results
		updateDataValue("sunRiseSet", resp.data)
        LOGINFO("Sunrise-Sunset Data: $sunRiseSet")
        setDateTimeFormats(datetimeFormat)
		updateDataValue("riseTime", new Date().parse("yyyy-MM-dd'T'HH:mm:ssXXX", sunRiseSet.sunrise).format("HH:mm", TimeZone.getDefault()))
        updateDataValue("noonTime", new Date().parse("yyyy-MM-dd'T'HH:mm:ssXXX", sunRiseSet.solar_noon).format("HH:mm", TimeZone.getDefault()))
		updateDataValue("setTime", new Date().parse("yyyy-MM-dd'T'HH:mm:ssXXX", sunRiseSet.sunset).format("HH:mm", TimeZone.getDefault()))
        updateDataValue("tw_begin", new Date().parse("yyyy-MM-dd'T'HH:mm:ssXXX", sunRiseSet.civil_twilight_begin).format("HH:mm", TimeZone.getDefault()))
        updateDataValue("tw_end", new Date().parse("yyyy-MM-dd'T'HH:mm:ssXXX", sunRiseSet.civil_twilight_end).format("HH:mm", TimeZone.getDefault()))
		updateDataValue("localSunset",new Date().parse("yyyy-MM-dd'T'HH:mm:ssXXX", sunRiseSet.sunset).format(timeFormat, TimeZone.getDefault()))
		updateDataValue("localSunrise", new Date().parse("yyyy-MM-dd'T'HH:mm:ssXXX", sunRiseSet.sunrise).format(timeFormat, TimeZone.getDefault()))
        updateDataValue("riseTime1", new Date().parse("yyyy-MM-dd'T'HH:mm:ssXXX", sunRiseSet.sunrise + 86400000).format("HH:mm", TimeZone.getDefault()))
        updateDataValue("riseTime2", new Date().parse("yyyy-MM-dd'T'HH:mm:ssXXX", sunRiseSet.sunrise + 86400000 + 86400000).format("HH:mm", TimeZone.getDefault()))
        updateDataValue("setTime1", new Date().parse("yyyy-MM-dd'T'HH:mm:ssXXX", sunRiseSet.sunset + 86400000).format("HH:mm", TimeZone.getDefault()))
        updateDataValue("setTime2", new Date().parse("yyyy-MM-dd'T'HH:mm:ssXXX", sunRiseSet.sunset + 86400000 + 86400000).format("HH:mm", TimeZone.getDefault()))
    } else {
		log.warn "OpenWeatherMap.org Weather Driver - WARNING: Sunrise-Sunset api did not return data"
	}
    return
}
// >>>>>>>>>> End Sunrise-Sunset Poll Routines <<<<<<<<<<

// <<<<<<<<<< Begin OWM Poll Routines >>>>>>>>>>
void pollOWM() {
    if( apiKey == null ) {
        log.warn "OpenWeatherMap.org Weather Driver - WARNING: OpenWeatherMap API Key not found.  Please configure in preferences."
        return
    }
    def ParamsOWM
    ParamsOWM = [ uri: 'https://api.openweathermap.org/data/2.5/onecall?lat=' + altLat + '&lon=' + altLon + '&mode=json&units=imperial&appid=' + apiKey ]
    LOGINFO("Poll OpenWeatherMap.org: $ParamsOWM")
	asynchttpGet("pollOWMHandler", ParamsOWM)
    return
}

void pollOWMHandler(resp, data) {
    LOGINFO("OpenWeatherMap.org Weather Driver - INFO: Polling OpenWeatherMap.org")
    if(resp.getStatus() != 200 && resp.getStatus() != 207) {
        log.warn "Calling https://api.openweathermap.org/data/2.5/onecall?lat=" + altLat + "&lon=" + altLon + "&mode=json&units=imperial&appid=" + apiKey
        log.warn resp.getStatus() + ":" + resp.getErrorMessage()
	} else {
        def owm = parseJson(resp.data)
//        LOGINFO("OpenWeatherMap Data: " + owm)
// <<<<<<<<<< Begin Setup Global Variables >>>>>>>>>>
        setDateTimeFormats(datetimeFormat)
        setMeasurementMetrics(distanceFormat, pressureFormat, rainFormat, tempFormat)
        setDisplayDecimals(TWDDecimals, PDecimals, RDecimals)

        fotime = new Date(owm.current.dt * 1000L)
        updateDataValue("fotime", fotime.toString())
        futime = new Date(owm.current.dt * 1000L)
        updateDataValue("futime", futime.toString())
        updateDataValue("Summary_last_poll_time", futime.format(timeFormat, TimeZone.getDefault()).toString())
        updateDataValue("Summary_last_poll_date", futime.format(dateFormat, TimeZone.getDefault()).toString())

        pollAlerts()
        updateDataValue("currDate", new Date().format("yyyy-MM-dd", TimeZone.getDefault()))
        updateDataValue("currTime", new Date().format("HH:mm", TimeZone.getDefault()))
        if(getDataValue("riseTime") <= getDataValue("currTime") && getDataValue("setTime") >= getDataValue("currTime")) {
            updateDataValue("is_day", "true")
        } else {
            updateDataValue("is_day", "false")
        }
        if(getDataValue("currTime") < getDataValue("tw_begin") || getDataValue("currTime") > getDataValue("tw_end")) {
            updateDataValue("is_light", "false")
        } else {
            updateDataValue("is_light", "true")
        }
        if(getDataValue("is_light") != getDataValue("is_lightOld")) {
            if(getDataValue("is_light")=="true") {
                log.info("OpenWeatherMap.org Weather Driver - INFO: Switching to Daytime schedule.")
            }else{
                log.info("OpenWeatherMap.org Weather Driver - INFO: Switching to Nighttime schedule.")
            }
            initialize()
            updateDataValue("is_lightOld", getDataValue("is_light"))
        }
// >>>>>>>>>> End Setup Global Variables <<<<<<<<<<

// <<<<<<<<<< Begin Process Standard Weather-Station Variables (Regardless of Forecast Selection)  >>>>>>>>>>
        BigDecimal t_dew
        if(tMetric == "°F") {
            t_dew = Math.round(owm.current.dew_point.toBigDecimal() * getDataValue("mult_twd").toInteger()) / getDataValue("mult_twd").toInteger()
        } else {
            t_dew = Math.round((owm.current.dew_point.toBigDecimal() - 32) / 1.8 * getDataValue("mult_twd").toInteger()) / getDataValue("mult_twd").toInteger()
        }

        updateDataValue("dewpoint", t_dew.toString())
        updateDataValue("humidity", (Math.round(owm.current.humidity.toBigDecimal() * 10) / 10).toString())

        BigDecimal t_press
        if(pMetric == "inHg") {
            t_press = Math.round(owm.current.pressure.toBigDecimal() * 0.029529983071445 * getDataValue("mult_p").toInteger()) / getDataValue("mult_p").toInteger()
        } else {
            t_press = Math.round(owm.current.pressure.toBigDecimal() * getDataValue("mult_p").toInteger()) / getDataValue("mult_p").toInteger()
        }
        updateDataValue("pressure", t_press.toString())

        BigDecimal t_temp
        if(tMetric == "°F") {
            t_temp = Math.round(owm.current.temp.toBigDecimal() * getDataValue("mult_twd").toInteger()) / getDataValue("mult_twd").toInteger()
        } else {
            t_temp = Math.round((owm.current.temp.toBigDecimal() - 32) / 1.8 * getDataValue("mult_twd").toInteger()) / getDataValue("mult_twd").toInteger()
        }
        updateDataValue("temperature", t_temp.toString())

        String w_string_bft
        String w_bft_icon
        BigDecimal t_ws = owm.current.wind_speed.toBigDecimal()
        if(t_ws < 1.0) {
            w_string_bft = "Calm"; w_bft_icon = 'wb0.png'
        }else if(t_ws < 4.0) {
            w_string_bft = "Light air"; w_bft_icon = 'wb1.png'
        }else if(t_ws < 8.0) {
            w_string_bft = "Light breeze"; w_bft_icon = 'wb2.png'
        }else if(t_ws < 13.0) {
            w_string_bft = "Gentle breeze"; w_bft_icon = 'wb3.png'
        }else if(t_ws < 19.0) {
            w_string_bft = "Moderate breeze"; w_bft_icon = 'wb4.png'
        }else if(t_ws < 25.0) {
            w_string_bft = "Fresh breeze"; w_bft_icon = 'wb5.png'
        }else if(t_ws < 32.0) {
            w_string_bft = "Strong breeze"; w_bft_icon = 'wb6.png'
        }else if(t_ws < 39.0) {
            w_string_bft = "High wind, moderate gale, near gale"; w_bft_icon = 'wb7.png'
        }else if(t_ws < 47.0) {
            w_string_bft = "Gale, fresh gale"; w_bft_icon = 'wb8.png'
        }else if(t_ws < 55.0) {
            w_string_bft = "Strong/severe gale"; w_bft_icon = 'wb9.png'
        }else if(t_ws < 64.0) {
            w_string_bft = "Storm, whole gale"; w_bft_icon = 'wb10.png'
        }else if(t_ws < 73.0) {
            w_string_bft = "Violent storm"; w_bft_icon = 'wb11.png'
        }else if(t_ws >= 73.0) {
            w_string_bft = "Hurricane force"; w_bft_icon = 'wb12.png'
        }
	    updateDataValue("wind_string_bft", w_string_bft)
        updateDataValue("wind_bft_icon", w_bft_icon)

        BigDecimal t_wd
        BigDecimal t_wg
        if(dMetric == "MPH") {
            t_wd = Math.round(owm.current.wind_speed.toBigDecimal() * getDataValue("mult_twd").toInteger()) / getDataValue("mult_twd").toInteger()
            t_wg = (!owm.current.wind_gust) ? t_wd : Math.round(owm.current.wind_gust.toBigDecimal() * getDataValue("mult_twd").toInteger()) / getDataValue("mult_twd").toInteger()
        } else if(dMetric == "KPH") {
            t_wd = Math.round(owm.current.wind_speed.toBigDecimal() * 1.609344 * getDataValue("mult_twd").toInteger()) / getDataValue("mult_twd").toInteger()
            t_wg = (!owm.current.wind_gust) ? t_wd : Math.round(owm.current.wind_gust.toBigDecimal() * 1.609344 * getDataValue("mult_twd").toInteger()) / getDataValue("mult_twd").toInteger()
        } else if(dMetric == "knots") {
            t_wd = Math.round(owm.currently.wind_speed.toBigDecimal() * 0.868976 * getDataValue("mult_twd").toInteger()) / getDataValue("mult_twd").toInteger()
            t_wg = (!owm.current.wind_gust) ? t_wd : Math.round(owm.currently.wind_gust.toBigDecimal() * 0.868976 * getDataValue("mult_twd").toInteger()) / getDataValue("mult_twd").toInteger()
        } else {  //  this leave only m/s
            t_wd = Math.round(owm.currently.wind_speed.toBigDecimal() * 0.44704 * getDataValue("mult_twd").toInteger()) / getDataValue("mult_twd").toInteger()
            t_wg = (!owm.current.wind_gust) ? t_wd : Math.round(owm.currently.wind_gust.toBigDecimal() * 0.44704 * getDataValue("mult_twd").toInteger()) / getDataValue("mult_twd").toInteger()
        }
        updateDataValue("wind", t_wd.toString())
        updateDataValue("wind_gust", t_wg.toString())

        updateDataValue("wind_degree", owm.current.wind_deg.toInteger().toString())	
        String w_cardinal
        String w_direction
        BigDecimal twb = owm.current.wind_deg.toBigDecimal()
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
        updateDataValue("wind_direction", w_direction)
        updateDataValue("wind_cardinal", w_cardinal)	
        updateDataValue("wind_string", w_string_bft + " from the " + getDataValue("wind_direction") + (getDataValue("wind").toBigDecimal() < 1.0 ? '': " at " + String.format(ddisp_twd, getDataValue("wind").toBigDecimal()) + " " + dMetric))
        String s_cardinal
        String s_direction
// >>>>>>>>>> End Process Standard Weather-Station Variables (Regardless of Forecast Selection)  <<<<<<<<<<	

	    Integer cloudCover
        if (!owm.current.clouds) {
            cloudCover = 1
        } else {
            cloudCover = (owm.current.clouds <= 1) ? 1 : owm.current.clouds
        }
        updateDataValue("cloud", cloudCover.toString())
        updateDataValue("vis", (dMetric!="MPH" ? Math.round(owm.current.visibility.toBigDecimal() * 0.001 * getDataValue("mult_twd").toInteger()) / getDataValue("mult_twd").toInteger() : Math.round(owm.current.visibility.toBigDecimal() * 0.0006213712 * getDataValue("mult_twd").toInteger()) / getDataValue("mult_twd").toInteger()).toString())

        BigDecimal t_p0 = (!owm.daily[0].rain && !owm.daily[0].snow) || (owm.daily[0].rain == null && !owm.daily[0].snow == null) ? 0.0000 : Math.max(!owm.daily[0].rain ? 0.0000 : owm.daily[0].rain.toBigDecimal(),!owm.daily[0].snow ? 0.0000 : owm.daily[0].snow.toBigDecimal())
        BigDecimal t_p1 = (!owm.daily[1].rain && !owm.daily[1].snow) || (owm.daily[1].rain == null && !owm.daily[1].snow == null) ? 0.0000 : Math.max(!owm.daily[1].rain ? 0.0000 : owm.daily[1].rain.toBigDecimal(),!owm.daily[1].snow ? 0.0000 : owm.daily[1].snow.toBigDecimal())
        BigDecimal t_p2 = (!owm.daily[2].rain && !owm.daily[2].snow) || (owm.daily[2].rain == null && !owm.daily[2].snow == null) ? 0.0000 : Math.max(!owm.daily[2].rain ? 0.0000 : owm.daily[2].rain.toBigDecimal(),!owm.daily[2].snow ? 0.0000 : owm.daily[2].snow.toBigDecimal())
        updateDataValue("rainToday", (Math.round(((rainFormat != "Inches" ? t_p0 : t_p0 * 0.03937008) * getDataValue("mult_r").toBigDecimal())) / getDataValue("mult_r").toBigDecimal()).toString())
        updateDataValue("Precip0", (Math.round(((rainFormat != "Inches" ? t_p0 : t_p0 * 0.03937008) * getDataValue("mult_r").toBigDecimal())) / getDataValue("mult_r").toBigDecimal()).toString())
        updateDataValue("Precip1", (Math.round(((rainFormat != "Inches" ? t_p1 : t_p1 * 0.03937008) * getDataValue("mult_r").toBigDecimal())) / getDataValue("mult_r").toBigDecimal()).toString())
        updateDataValue("Precip2", (Math.round(((rainFormat != "Inches" ? t_p2 : t_p2 * 0.03937008) * getDataValue("mult_r").toBigDecimal())) / getDataValue("mult_r").toBigDecimal()).toString())

        updateDataValue("condition_id", owm.current.weather[0].id.toString())
        updateDataValue('condition_code', getCondCode(owm.current.weather[0].id, getDataValue('is_day')))
        updateDataValue("condition_text", owm.current.weather[0].description.capitalize())

        updateDataValue("forecast_id", owm.daily[0].weather[0].id.toString())
        updateDataValue('forecast_code', getCondCode(owm.daily[0].weather[0].id, 'true'))
        updateDataValue("forecast_text", owm.daily[0].weather[0].description.capitalize())

        if(threedayTilePublish) {

            updateDataValue("day1", new Date(owm.daily[1].dt * 1000L).format("EEEE"))
            updateDataValue("day2", new Date(owm.daily[2].dt * 1000L).format("EEEE"))

            updateDataValue("is_day1", "true")
            updateDataValue("is_day2", "true")
            updateDataValue("forecast_id1", owm.daily[1].weather[0].id.toString())
            updateDataValue('forecast_code1', getCondCode(owm.daily[1].weather[0].id, 'true'))
            updateDataValue("forecast_text1", owm.daily[1].weather[0].description.capitalize())

            updateDataValue("forecast_id2", owm.daily[2].weather[0].id.toString())
            updateDataValue('forecast_code2', getCondCode(owm.daily[2].weather[0].id, 'true'))
            updateDataValue("forecast_text2", owm.daily[2].weather[0].description.capitalize())

            updateDataValue("forecastHigh1", (tMetric=="°F" ? (Math.round(owm.daily[1].temp.max.toBigDecimal() * getDataValue("mult_twd").toInteger()) / getDataValue("mult_twd").toInteger()) : (Math.round((owm.daily[1].temp.max.toBigDecimal() - 32) / 1.8 * getDataValue("mult_twd").toInteger()) / getDataValue("mult_twd").toInteger())).toString())
            updateDataValue("forecastHigh2", (tMetric=="°F" ? (Math.round(owm.daily[2].temp.max.toBigDecimal() * getDataValue("mult_twd").toInteger()) / getDataValue("mult_twd").toInteger()) : (Math.round((owm.daily[2].temp.max.toBigDecimal() - 32) / 1.8 * getDataValue("mult_twd").toInteger()) / getDataValue("mult_twd").toInteger())).toString())

            updateDataValue("forecastLow1", (tMetric=="°F" ? (Math.round(owm.daily[1].temp.min.toBigDecimal() * getDataValue("mult_twd").toInteger()) / getDataValue("mult_twd").toInteger()) : (Math.round((owm.daily[1].temp.min.toBigDecimal() - 32) / 1.8 * getDataValue("mult_twd").toInteger()) / getDataValue("mult_twd").toInteger())).toString())
            updateDataValue("forecastLow2", (tMetric=="°F" ? (Math.round(owm.daily[2].temp.min.toBigDecimal() * getDataValue("mult_twd").toInteger()) / getDataValue("mult_twd").toInteger()) : (Math.round((owm.daily[2].temp.min.toBigDecimal() - 32) / 1.8 * getDataValue("mult_twd").toInteger()) / getDataValue("mult_twd").toInteger())).toString())

            updateDataValue("imgName0", '<img class=\"centerImage\" src=' + getDataValue('iconLocation') + getImgName(owm.daily[0].weather[0].id, getDataValue('is_day')) + (((getDataValue("iconLocation").toLowerCase().contains('://github.com/')) && (getDataValue("iconLocation").toLowerCase().contains('/blob/master/'))) ? "?raw=true" : "") + '>')
            updateDataValue("imgName1", '<img class=\"centerImage\" src=' + getDataValue('iconLocation') + getImgName(owm.daily[1].weather[0].id, 'true') + (((getDataValue("iconLocation").toLowerCase().contains('://github.com/')) && (getDataValue("iconLocation").toLowerCase().contains('/blob/master/'))) ? "?raw=true" : "") + '>')
            updateDataValue("imgName2", '<img class=\"centerImage\" src=' + getDataValue('iconLocation') + getImgName(owm.daily[2].weather[0].id, 'true') + (((getDataValue("iconLocation").toLowerCase().contains('://github.com/')) && (getDataValue("iconLocation").toLowerCase().contains('/blob/master/'))) ? "?raw=true" : "") + '>')
        }

        updateDataValue("forecastHigh", (tMetric=="°F" ? (Math.round(owm.daily[0].temp.max.toBigDecimal() * getDataValue("mult_twd").toInteger()) / getDataValue("mult_twd").toInteger()) : (Math.round((owm.daily[0].temp.max.toBigDecimal() - 32) / 1.8 * getDataValue("mult_twd").toInteger()) / getDataValue("mult_twd").toInteger())).toString())
        updateDataValue("forecastLow", (tMetric=="°F" ? (Math.round(owm.daily[0].temp.min.toBigDecimal() * getDataValue("mult_twd").toInteger()) / getDataValue("mult_twd").toInteger()) : (Math.round((owm.daily[0].temp.min.toBigDecimal() - 32) / 1.8 * getDataValue("mult_twd").toInteger()) / getDataValue("mult_twd").toInteger())).toString())

        if(precipExtendedPublish){
            updateDataValue("rainTomorrow", getDataValue('Precip1'))
            updateDataValue("rainDayAfterTomorrow", getDataValue('Precip2'))
        }

        updateLux(false)
        updateDataValue("ultravioletIndex", owm.current.uvi.toBigDecimal().toString())

        BigDecimal t_fl
        if(tMetric == "°F") {
            t_fl = Math.round(owm.current.feels_like.toBigDecimal() * getDataValue("mult_twd").toInteger()) / getDataValue("mult_twd").toInteger()
        } else {
            t_fl = Math.round((owm.current.feels_like.toBigDecimal() - 32) / 1.8 * getDataValue("mult_twd").toInteger()) / getDataValue("mult_twd").toInteger()
        }
        updateDataValue("feelsLike", t_fl.toString())

// >>>>>>>>>> End Setup Forecast Variables <<<<<<<<<<

	    // <<<<<<<<<< Begin Icon Processing  >>>>>>>>>>
        String imgName = (getDataValue("iconType")== 'true' ? getImgName(owm.current.weather[0].id, getDataValue('is_day')) : getImgName(owm.daily[0].weather[0].id, getDataValue('is_day')))
        LOGINFO("imgName: " + imgName)
        sendEventPublish(name: "condition_icon", value: '<img src=' + getDataValue("iconLocation") + imgName + (((getDataValue("iconLocation").toLowerCase().contains('://github.com/')) && (getDataValue("iconLocation").toLowerCase().contains('/blob/master/'))) ? "?raw=true" : "") + '>')
        sendEventPublish(name: "condition_iconWithText", value: "<img src=" + getDataValue("iconLocation") + imgName + (((getDataValue("iconLocation").toLowerCase().contains('://github.com/')) && (getDataValue("iconLocation").toLowerCase().contains('/blob/master/'))) ? "?raw=true" : "") + "><br>" + (getDataValue("iconType")== 'true' ? getDataValue("condition_text") : getDataValue("forecast_text")))
        sendEventPublish(name: "condition_icon_url", value: getDataValue("iconLocation") + imgName + (((getDataValue("iconLocation").toLowerCase().contains('://github.com/')) && (getDataValue("iconLocation").toLowerCase().contains('/blob/master/'))) ? "?raw=true" : ""))
        updateDataValue("condition_icon_url", getDataValue("iconLocation") + imgName + (((getDataValue("iconLocation").toLowerCase().contains('://github.com/')) && (getDataValue("iconLocation").toLowerCase().contains('/blob/master/'))) ? "?raw=true" : ""))
        sendEventPublish(name: "condition_icon_only", value: imgName.split("/")[-1].replaceFirst("\\?raw=true",""))
        // >>>>>>>>>> End Icon Processing <<<<<<<<<<
        PostPoll()
    }
    return
}
// >>>>>>>>>> End OpenWeatherMap Poll Routine <<<<<<<<<<

// <<<<<<<<<< Begin NWS Active Alert Poll Routines >>>>>>>>>>
void pollAlerts() {
    def ParamsAlerts
    ParamsAlerts = [ uri: 'https://api.weather.gov/alerts/active?status=actual&message_type=alert,update&point=' + altLat + ',' + altLon + '&urgency=unknown,future,expected,immediate&severity=unknown,moderate,severe,extreme&certainty=unknown,possible,likely,observed',
                    requestContentType: "application/json",
				    contentType: "application/json" ]
    LOGINFO("Poll api.weather.gov/alerts/active: $ParamsAlerts")
	asynchttpGet("pollAlertsHandler", ParamsAlerts)
    return
}

void pollAlertsHandler(resp, data) {
    if(resp.getStatus() != 200 && resp.getStatus() != 207) {
        LOGWARN('Calling https://api.weather.gov/alerts/active?status=actual&message_type=alert,update&point=' + altLat + ',' + altLon + '&urgency=unknown,future,expected,immediate&severity=unknown,moderate,severe,extreme&certainty=unknown,possible,likely,observed')
        LOGWARN(resp.getStatus() + ":" + resp.getErrorMessage())
	} else {
        def NWSAlerts = parseJson(resp.data)
        if(NWSAlerts.features[0] == null) {
            updateDataValue('noAlert','true')
            updateDataValue("alert", 'No current weather alerts for this area')
            updateDataValue("alertTileLink", '<a href="https://forecast.weather.gov/MapClick.php?lat=' + altLat + '&lon=' + altLon + '" target=\"_blank\">No current weather alerts for this area.</a>')
            updateDataValue("alertLink", '<a>' + getDataValue("condition_text") + '</a>')
            updateDataValue("alertLink2", '<a>' + getDataValue("condition_text") + '</a>')
            updateDataValue("alertLink3", '<a>' + getDataValue("condition_text") + '</a>')
            updateDataValue("possAlert", "false")
        } else {
            updateDataValue('noAlert','false')
            updateDataValue("alert", NWSAlerts.features[0].properties.event.toString().replaceAll("[{}\\[\\]]", "").split(/,/)[0])
            updateDataValue("alertTileLink", '<a style="font-style:italic;color:red;" href="https://forecast.weather.gov/MapClick.php?lat=' + altLat + '&lon=' + altLon +'" target=\'_blank\'>'+NWSAlerts.features[0].properties.event.toString().replaceAll("[{}\\[\\]]", "").split(/,/)[0]+'</a>')
            updateDataValue("alertLink", '<a style="font-style:italic;color:red;" href="https://forecast.weather.gov/MapClick.php?lat=' + altLat + '&lon=' + altLon + '" target=\'_blank\'>'+NWSAlerts.features[0].properties.event.toString().replaceAll("[{}\\[\\]]", "").split(/,/)[0]+'</a>')
            def String al2 = '<a style="font-style:italic;color:red;" href="https://forecast.weather.gov/MapClick.php?lat=' + altLat + '&lon=' + altLon + '" target="_blank">'
            updateDataValue("alertLink2", al2+NWSAlerts.features[0].properties.event.toString().replaceAll("[{}\\[\\]]", "").split(/,/)[0]+'</a>')
            updateDataValue("alertLink3", '<a style="font-style:italic;color:red;" target=\'_blank\'>'+NWSAlerts.features[0].properties.event.toString().replaceAll("[{}\\[\\]]", "").split(/,/)[0]+'</a>')
            updateDataValue("possAlert", "true")
        }
        //  <<<<<<<<<< Begin Built alertTile >>>>>>>>>>
        if(alertPublish){ // don't bother setting these values if it's not enabled
            String alertTile = "Weather Alerts for " + '<a href="https://forecast.weather.gov/MapClick.php?lat=' + altLat + '&lon=' + altLon + '" target="_blank">' + getDataValue("city") + '</a><br>updated at ' + getDataValue('Summary_last_poll_time') + ' on ' + getDataValue('Summary_last_poll_date') + '.<br>'
            alertTile+= getDataValue("alertTileLink") + '<br>'
            alertTile+= '<a href=\"https://forecast.weather.gov/MapClick.php?lat=' + altLat + '&lon=' + altLon + '\" target=\'_blank\'><img src=' + getDataValue("iconLocation") + 'NWS_240px.png' + ' style=\"height:2.0em;display:inline;\"></a>'
            updateDataValue("alertTile", alertTile)
            sendEvent(name: "alert", value: getDataValue("alert"))
            sendEvent(name: "alertTile", value: getDataValue("alertTile"))
        }
        //  >>>>>>>>>> End Built alertTile <<<<<<<<<<
    }
    return
}
// >>>>>>>>>> End NWS Active Alert Poll Routines <<<<<<<<<<

// >>>>>>>>>> Begin Lux Processing <<<<<<<<<<
void updateLux(Boolean pollAgain=true) {
	LOGINFO("UpdateLux $pollAgain")
	if(pollAgain) {
		String curTime = new Date().format("HH:mm", TimeZone.getDefault())
		String newLight
		if(curTime < getDataValue("tw_begin") || curTime > getDataValue("tw_end")) {
			newLight =  "false"
		} else {
			newLight =  "true"
		}
		if(newLight != getDataValue("is_lightOld")) {
			pollOWM()
			return
		}
	}
    def (lux, bwn) = estimateLux(getDataValue("condition_id").toInteger(), getDataValue("cloud").toInteger())
    updateDataValue("illuminance", !lux ? "0" : lux.toString())
    updateDataValue("illuminated", String.format("%,4d", !lux ? 0 : lux).toString())
	updateDataValue("bwn", bwn)
	if(pollAgain) PostPoll()
    return
}
// >>>>>>>>>> End Lux Processing <<<<<<<<<<

// <<<<<<<<<< Begin Post-Poll Routines >>>>>>>>>>
void PostPoll() {
    def sunRiseSet = parseJson(getDataValue("sunRiseSet")).results
    setDateTimeFormats(datetimeFormat)
    setMeasurementMetrics(distanceFormat, pressureFormat, rainFormat, tempFormat)
    setDisplayDecimals(TWDDecimals, PDecimals, RDecimals)
/*  SunriseSunset Data Eements */
    if(localSunrisePublish){  // don't bother setting these values if it's not enabled
        sendEvent(name: "tw_begin", value: new Date().parse("yyyy-MM-dd'T'HH:mm:ssXXX", sunRiseSet.civil_twilight_begin).format(timeFormat, TimeZone.getDefault()))
        sendEvent(name: "sunriseTime", value: new Date().parse("yyyy-MM-dd'T'HH:mm:ssXXX", sunRiseSet.sunrise).format(timeFormat, TimeZone.getDefault()))
        sendEvent(name: "noonTime", value: new Date().parse("yyyy-MM-dd'T'HH:mm:ssXXX", sunRiseSet.solar_noon).format(timeFormat, TimeZone.getDefault()))
        sendEvent(name: "sunsetTime", value: new Date().parse("yyyy-MM-dd'T'HH:mm:ssXXX", sunRiseSet.sunset).format(timeFormat, TimeZone.getDefault()))
        sendEvent(name: "tw_end", value: new Date().parse("yyyy-MM-dd'T'HH:mm:ssXXX", sunRiseSet.civil_twilight_end).format(timeFormat, TimeZone.getDefault()))
    }
    if(dashSharpToolsPublish || dashSmartTilesPublish || localSunrisePublish) {
        sendEvent(name: "localSunset", value: new Date().parse("yyyy-MM-dd'T'HH:mm:ssXXX", sunRiseSet.sunset).format(timeFormat, TimeZone.getDefault())) // only needed for certain dashboards
        sendEvent(name: "localSunrise", value: new Date().parse("yyyy-MM-dd'T'HH:mm:ssXXX", sunRiseSet.sunrise).format(timeFormat, TimeZone.getDefault())) // only needed for certain dashboards
    }

/*  Capability Data Elements */
	sendEvent(name: "humidity", value: getDataValue("humidity").toBigDecimal(), unit: '%')
    sendEvent(name: "illuminance", value: getDataValue("illuminance").toInteger(), unit: 'lx')
    sendEvent(name: "pressure", value: getDataValue("pressure").toBigDecimal(), unit: pMetric)
	sendEvent(name: "pressured", value: String.format(ddisp_p, getDataValue("pressure").toBigDecimal()), unit: pMetric)
	sendEvent(name: "temperature", value: getDataValue("temperature").toBigDecimal(), unit: tMetric)
    sendEvent(name: "ultravioletIndex", value: getDataValue("ultravioletIndex").toBigDecimal(), unit: 'uvi')
    sendEvent(name: "feelsLike", value: getDataValue("feelsLike").toBigDecimal(), unit: tMetric)

/*  'Required for Dashboards' Data Elements */
    if(dashHubitatOWMPublish || dashSharpToolsPublish || dashSmartTilesPublish) { sendEvent(name: "city", value: getDataValue("city")) }
    if(dashSharpToolsPublish) { sendEvent(name: "forecastIcon", value: getCondCode(getDataValue("condition_id").toInteger(), getDataValue('is_day'))) }
    if(dashSharpToolsPublish || dashSmartTilesPublish || rainTodayPublish) { sendEvent(name: "rainToday", value: getDataValue("rainToday").toBigDecimal(), unit: rMetric) }
    if(dashSharpToolsPublish || dashSmartTilesPublish) { sendEvent(name: "weather", value: getDataValue("condition_text")) }
    if(dashSharpToolsPublish || dashSmartTilesPublish) { sendEvent(name: "weatherIcon", value: getCondCode(getDataValue("condition_id").toInteger(), getDataValue('is_day'))) }
    if(dashHubitatOWMPublish) { sendEvent(name: "weatherIcons", value: getCondCode(getDataValue("condition_id").toInteger(), getDataValue('is_day'))) }
    if(dashHubitatOWMPublish || dashSharpToolsPublish || windPublish) { sendEvent(name: "wind", value: getDataValue("wind").toBigDecimal(), unit: dMetric) }
    if(dashHubitatOWMPublish) { sendEvent(name: "windSpeed", value: getDataValue("wind").toBigDecimal(), unit: dMetric) }
    if(dashHubitatOWMPublish) { sendEvent(name: "windDirection", value: getDataValue("wind_degree").toInteger(), unit: "DEGREE")   }

/*  Selected optional Data Elements */
    sendEventPublish(name: "betwixt", value: getDataValue("bwn"))
    sendEventPublish(name: "cloud", value: getDataValue("cloud").toInteger(), unit: '%')
    sendEventPublish(name: "condition_code", value: getDataValue("condition_code"))
    sendEventPublish(name: "condition_text", value: getDataValue("condition_text"))
    sendEventPublish(name: "dewpoint", value: getDataValue("dewpoint").toBigDecimal(), unit: tMetric)

    sendEventPublish(name: "forecast_code", value: getDataValue("forecast_code"))
    sendEventPublish(name: "forecast_text", value: getDataValue("forecast_text"))
    if(fcstHighLowPublish){ // don't bother setting these values if it's not enabled
        sendEvent(name: "forecastHigh", value: getDataValue("forecastHigh").toBigDecimal(), unit: tMetric)
    	sendEvent(name: "forecastLow", value: getDataValue("forecastLow").toBigDecimal(), unit: tMetric)
    }
    sendEventPublish(name: "illuminated", value: getDataValue("illuminated") + ' lx')
    sendEventPublish(name: "is_day", value: getDataValue("is_day"))

    if(obspollPublish){  // don't bother setting these values if it's not enabled
    	sendEvent(name: "last_poll_Forecast", value: new Date().parse("EEE MMM dd HH:mm:ss z yyyy", getDataValue("futime")).format(dateFormat, TimeZone.getDefault()) + ", " + new Date().parse("EEE MMM dd HH:mm:ss z yyyy", getDataValue("futime")).format(timeFormat, TimeZone.getDefault()))
        sendEvent(name: "last_observation_Forecast", value: new Date().parse("EEE MMM dd HH:mm:ss z yyyy", getDataValue("fotime")).format(dateFormat, TimeZone.getDefault()) + ", " + new Date().parse("EEE MMM dd HH:mm:ss z yyyy", getDataValue("fotime")).format(timeFormat, TimeZone.getDefault()))
    }

    if(precipExtendedPublish){ // don't bother setting these values if it's not enabled
        sendEvent(name: "rainDayAfterTomorrow", value: getDataValue("rainDayAfterTomorrow").toBigDecimal(), unit: rMetric)	
    	sendEvent(name: "rainTomorrow", value: getDataValue("rainTomorrow").toBigDecimal(), unit: rMetric)
    }
    sendEventPublish(name: "vis", value: Math.round(getDataValue("vis").toBigDecimal() * getDataValue("mult_twd").toBigDecimal()) / getDataValue("mult_twd").toBigDecimal(), unit: (dMetric=="MPH" ? "miles" : "kilometers"))
    sendEventPublish(name: "wind_degree", value: getDataValue("wind_degree").toInteger(), unit: "DEGREE")
    sendEventPublish(name: "wind_direction", value: getDataValue("wind_direction"))
    sendEventPublish(name: "wind_cardinal", value: getDataValue("wind_cardinal"))
    sendEventPublish(name: "wind_gust", value: getDataValue("wind_gust").toBigDecimal(), unit: dMetric)
    sendEventPublish(name: "wind_string", value: getDataValue("wind_string"))

    //  <<<<<<<<<< Begin Built Weather Summary text >>>>>>>>>>
    String mtprecip = 'There has been ' + (getDataValue("rainToday").toBigDecimal() > 0 ? String.format(ddisp_r, getDataValue("rainToday").toBigDecimal()) + (rMetric == 'in' ? ' inches' : ' millimeters') + ' of ' : ' no ') + " precipitation today. "
    if(weatherSummaryPublish){ // don't bother setting these values if it's not enabled
		String Summary_forecastTemp = ' with a high of ' + String.format(ddisp_twd, getDataValue("forecastHigh").toBigDecimal()) + tMetric + " and a low of " + String.format(ddisp_twd, getDataValue("forecastLow").toBigDecimal()) + tMetric + ". "
		String Summary_precip = 'There has been ' + (getDataValue('rainToday').toBigDecimal() > 0 ? String.format(ddisp_r, getDataValue('rainToday').toBigDecimal()) + (rMetric == 'in' ? ' inches' : ' millimeters') + " of " : " no ") + "precipitation today. "
        LOGINFO("Summary_precip: ${Summary_precip}")
		String Summary_vis = "Visibility is around " + String.format(ddisp_twd, getDataValue("vis").toBigDecimal()) + (dMetric=="MPH" ? " miles." : " kilometers.")
        SummaryMessage(summaryType, getDataValue('Summary_last_poll_date'), getDataValue('Summary_last_poll_time'), Summary_forecastTemp, Summary_precip, Summary_vis)
    }
//  >>>>>>>>>> End Built Weather Summary text <<<<<<<<<<
    String OWMIcon = '<a href="https://openweathermap.org/weathermap?basemap=map&cities=true&layer=temperature&lat=' + altLat + '&lon=' + altLon + '&zoom=12" target="_blank"><img src=' + getDataValue("iconLocation") + 'OWM.png style="height:2em;"></a>' // + ' style="display:inline;"' //height:2em;
    String OWMIcon2 = '<a href="https://openweathermap.org" target="_blank"><img src=' + getDataValue("iconLocation") + 'OWM.png style="height:2em;"></a>'
    String OWMText = '<a href="https://openweathermap.org" target="_blank">OpenWeatherMap.org</a>'
//  <<<<<<<<<< Begin Built 3dayfcstTile >>>>>>>>>>
    if(threedayTilePublish) {
        String my3day = '<style type=\"text/css\">'
        my3day += '.centerImage'
        my3day += '{text-align:center;display:inline;height:50%;}'
        my3day += '</style>'
        my3day += '<table align="center" style="width:100%">'
        my3day += '<tr>'
        my3day += '<td></td>'
        my3day += '<td>Today</td>'
	    my3day += '<td>' + getDataValue('day1') + '</td>'
	    my3day += '<td>' + getDataValue('day2') + '</td>'
        my3day += '</tr>'
        my3day += '<tr>'
        my3day += '<td></td>'
        my3day += '<td>' + getDataValue('imgName0') + '</td>'
	    my3day += '<td>' + getDataValue('imgName1') + '</td>'
	    my3day += '<td>' + getDataValue('imgName2') + '</td>'
        my3day += '</tr>'
        my3day += '<tr>'
        my3day += '<td style="text-align:right">Now:</td>'
        my3day += '<td>' + String.format(ddisp_twd, getDataValue('temperature').toBigDecimal()) + tMetric + '</td>'
        my3day += '<td>' + getDataValue('forecast_text1') + '</td>'
	    my3day += '<td>' + getDataValue('forecast_text2') + '</td>'
        my3day += '</tr>'
        my3day += '<tr>'
        my3day += '<td style="text-align:right">Low:</td>'
        my3day += '<td>' + String.format(ddisp_twd, getDataValue('forecastLow').toBigDecimal()) + tMetric + '</td>'
        my3day += '<td>' + String.format(ddisp_twd, getDataValue('forecastLow1').toBigDecimal()) + tMetric + '</td>'
        my3day += '<td>' + String.format(ddisp_twd, getDataValue('forecastLow2').toBigDecimal()) + tMetric + '</td>'
        my3day += '</tr>'
        my3day += '<tr>'
        my3day += '<td style="text-align:right">High:</td>'
        my3day += '<td>' + String.format(ddisp_twd, getDataValue('forecastHigh').toBigDecimal()) + tMetric + '</td>'
        my3day += '<td>' + String.format(ddisp_twd, getDataValue('forecastHigh1').toBigDecimal()) + tMetric + '</td>'
        my3day += '<td>' + String.format(ddisp_twd, getDataValue('forecastHigh2').toBigDecimal()) + tMetric + '</td>'
        my3day += '</tr>'
        my3day += '<tr>'
        my3day += '<td style="text-align:right">Precip:</td>'
        my3day += '<td>' + (getDataValue("Precip0").toBigDecimal() > 0 ? String.format(ddisp_r, getDataValue("Precip0").toBigDecimal()) + " " + rMetric : "None") + '</td>'
        my3day += '<td>' + (getDataValue("Precip1").toBigDecimal() > 0 ? String.format(ddisp_r, getDataValue("Precip1").toBigDecimal()) + " " + rMetric : "None") + '</td>'
        my3day += '<td>' + (getDataValue("Precip2").toBigDecimal() > 0 ? String.format(ddisp_r, getDataValue("Precip2").toBigDecimal()) + " " + rMetric : "None") + '</td>'
        my3day += '</tr>'
        my3day += '</table>'
        LOGINFO("my3day character length: " + my3day.length() + "; OWMIcon length: " + OWMIcon.length() + "; OWMIcon2 length: " + OWMIcon2.length() + "; OWMText length: " + OWMText.length())
        if(my3day.length() + 33 > 1024) {
            my3day = "Too much data to display.</br></br>Exceeds maximum tile length by " + 1024 - my3day.length() - 33 + " characters."
        }else if((my3day.length() + OWMIcon.length() + 11) < 1025) {
            my3day += OWMIcon + ' @ ' + getDataValue('Summary_last_poll_time')
        }else if((my3day.length() + OWMText.length() + 11) < 1025) {
            my3day += OWMIcon2 + ' @ ' + getDataValue('Summary_last_poll_time')
        }else if((my3day.length() + OWMText2.length() + 11) < 1025) {
            my3day += OWMText + ' @ ' + getDataValue('Summary_last_poll_time')
        }else{
            my3day += 'OpenWeatherMap.org @ ' + getDataValue('Summary_last_poll_time')
        }
        sendEvent(name: "threedayfcstTile", value: my3day.take(1024))
    }
//  >>>>>>>>>> End Built 3dayfcstTile <<<<<<<<<<

//  <<<<<<<<<< Begin Built mytext >>>>>>>>>>
    if(myTilePublish){ // don't bother setting these values if it's not enabled
        Boolean gitclose = (getDataValue("iconLocation").toLowerCase().contains('://github.com/')) && (getDataValue("iconLocation").toLowerCase().contains('/blob/master/'))
        String iconClose = (gitclose ? "?raw=true" : "")
        String iconCloseStyled = iconClose + '>'
        Boolean noAlert = (!getDataValue("possAlert") || getDataValue("possAlert")=="" || getDataValue("possAlert")=="false")
        String alertStyleOpen = (noAlert ? '' :  '<span>')
        String alertStyleClose = (noAlert ? '<br>' : '</span><br>')
        BigDecimal wgust
        if(getDataValue("wind_gust").toBigDecimal() < 1.0 ) {
            wgust = 0.0g
        } else {
            wgust = getDataValue("wind_gust").toBigDecimal()
        }

        String mytextb = '<span style="display:inline;"><a href="https://openweathermap.org/weathermap?basemap=map&cities=true&layer=temperature&lat=' + altLat + '&lon=' + altLon + '&zoom=12" target="_blank">' + getDataValue("city") + '</a><br>'
        String mytextm1 = getDataValue("condition_text") + (noAlert ? '' : ' | ') + alertStyleOpen + (noAlert ? '' : getDataValue("alertLink")) + alertStyleClose
        String mytextm2 = getDataValue("condition_text") + (noAlert ? '' : ' | ') + alertStyleOpen + (noAlert ? '' : getDataValue("alertLink2")) + alertStyleClose
        String mytextm3 = getDataValue("condition_text") + (noAlert ? '' : ' | ') + alertStyleOpen + (noAlert ? '' : getDataValue("alertLink3")) + alertStyleClose
        String mytexte = String.format(ddisp_twd, getDataValue("temperature").toBigDecimal()) + tMetric + '<img src=' + getDataValue("condition_icon_url") + iconClose + ' style=\"height:2.2em;display:inline;\">'
        mytexte+= ' Feels like ' + String.format(ddisp_twd, getDataValue("feelsLike").toBigDecimal()) + tMetric + '<br></span>'
        mytexte+= '<span style=\"font-size:.9em;\"><img src=' + getDataValue("iconLocation") + getDataValue("wind_bft_icon") + iconCloseStyled + getDataValue("wind_direction") + " "
        mytexte+= (getDataValue("wind").toBigDecimal() < 1.0 ? 'calm' : "@ " + String.format(ddisp_twd, getDataValue("wind").toBigDecimal()) + " " + dMetric)
        mytexte+= ', gusts ' + ((wgust < 1.0) ? 'calm' :  "@ " + String.format(ddisp_twd, wgust) + " " + dMetric) + '<br>'
        mytexte+= '<img src=' + getDataValue("iconLocation") + 'wb.png' + iconCloseStyled + String.format(ddisp_p, getDataValue("pressure").toBigDecimal()) + " " + pMetric + '     <img src=' + getDataValue("iconLocation") + 'wh.png' + iconCloseStyled
        mytexte+= getDataValue("humidity") + '%     ' + '<img src=' + getDataValue("iconLocation") + 'wu.png' + iconCloseStyled + (getDataValue("rainToday").toBigDecimal() > 0 ? String.format(ddisp_r, getDataValue("rainToday").toBigDecimal()) + " " + rMetric : "None") + '<br>'
        mytexte+= '<img src=' + getDataValue("iconLocation") + 'wsr.png' + iconCloseStyled + getDataValue("localSunrise") + '     <img src=' + getDataValue("iconLocation") + 'wss.png' + iconCloseStyled
        mytexte+= getDataValue("localSunset") + '     Updated: ' + getDataValue('Summary_last_poll_time')

        String mytext = mytextb + mytextm1 + mytexte
        if((mytext.length() + OWMIcon.length() + 10) < 1025) {
            mytext+= '<br>' + OWMIcon + '</span>'
        } else if((mytext.length() + OWMIcon2.length() + 10) < 1025) {
                mytext+= '<br>' + OWMIcon2 + '</span>'
        } else if((mytext.length() + OWMText.length() + 10) < 1025) {
                    mytext+= '<br>' + OWMText + '</span>'
        } else {
            mytext = mytextb + mytextm2 + mytexte
            if((mytext.length() + OWMIcon.length() + 10) < 1025) {
                mytext+= '<br>' + OWMIcon + '</span>'
            }else if((mytext.length() + OWMIcon2.length() + 10) < 1025) {
                mytext+= '<br>' + OWMIcon2 + '</span>'
            }else if((mytext.length() + OWMText.length() + 10) < 1025) {
                mytext+= '<br>' + OWMText + '</span>'
            }else{
                mytext+= '<br>Forecast by OpenWeatherMap.org</span>'
            }
        }
        if(mytext.length() > 1024) {
            Integer iconfilepath = ('<img src=' + getDataValue("iconLocation") + getDataValue("wind_bft_icon") + iconCloseStyled).length()
            Integer excess = (mytext.length() - 1024)
            Integer removeicons = 0
            Integer ics = iconfilepath + iconCloseStyled.length()
            if((excess - ics + 11) < 0) {
                removeicons = 1  //Remove sunset
            }else if((excess - (ics * 2) + 20) < 0) {
                removeicons = 2 //remove sunset and sunrise
            }else if((excess - (ics * 3) + 31) < 0) {
                removeicons = 3 //remove sunset, sunrise, Precip0
            }else if((excess - (ics * 4) + 38) < 0) {
                removeicons = 4 //remove sunset, sunrise, Precip0, Humidity
            }else if((excess - (ics * 5) + 42) < 0) {
                removeicons = 5 //remove sunset, sunrise, Precip0, Humidity, Pressure
            }else if((excess - (ics * 6) + 42) < 0) {
                removeicons = 6 //remove sunset, sunrise, Precip0, Humidity, Pressure, Wind
            }else if((excess - (ics * 7) + 42) < 0) {
                removeicons = 7 //remove sunset, sunrise, Precip0, Humidity, Pressure, Wind, condition
            }else{
                removeicons = 8 // still need to remove html formatting
            }
            if(removeicons < 8) {
                LOGDEBUG("myTile exceeds 1,024 characters (" + mytext.length() + ") ... removing last " + (removeicons + 1).toString() + " icons.")
                mytext = '<span>' + getDataValue("city") + '<br>'
                mytext+= getDataValue("condition_text") + (noAlert ? '' : ' | ') + alertStyleOpen + (noAlert ? '' : getDataValue("alert")) + alertStyleClose + '<br>'
                mytext+= String.format(ddisp_twd, getDataValue("temperature").toBigDecimal()) + tMetric + (removeicons < 7 ? '<img src=' + getDataValue("condition_icon_url") + iconClose + ' style=\"height:2.0em;display:inline;\">' : '')
                mytext+= ' Feels like ' + String.format(ddisp_twd, getDataValue("feelsLike").toBigDecimal()) + tMetric + '<br></span>'
                mytext+= '<span style=\"font-size:.8em;\">' + (removeicons < (raintoday ? 7 : 6) ? '<img src=' + getDataValue("iconLocation") + getDataValue("wind_bft_icon") + iconCloseStyled : '') + getDataValue("wind_direction") + " "
                mytext+= (removeicons < 6 ? '<img src=' + getDataValue("iconLocation") + getDataValue("wind_bft_icon") + iconCloseStyled : '') + getDataValue("wind_direction") + " "
                mytext+= (getDataValue("wind").toBigDecimal() < 1.0 ? 'calm' : "@ " + String.format(ddisp_twd, getDataValue("wind").toBigDecimal()) + " " + dMetric)
                mytext+= ', gusts ' + ((wgust < 1.0) ? 'calm' :  "@ " + String.format(ddisp_twd, wgust) + " " + dMetric) + '<br>'
                mytext+= (removeicons < 5 ? '<img src=' + getDataValue("iconLocation") + 'wb.png' + iconCloseStyled : 'Bar: ') + String.format(ddisp_p, getDataValue("pressure").toBigDecimal()) + " " + pMetric + '  '
                mytext+= (removeicons < 4 ? '<img src=' + getDataValue("iconLocation") + 'wh.png' + iconCloseStyled : ' | Hum: ') + getDataValue("humidity") + '%  '
                mytext+= (removeicons < 3 ? '<img src=' + getDataValue("iconLocation") + 'wu.png' + iconCloseStyled : ' | Precip: ') + (getDataValue("rainToday").toBigDecimal() > 0 ? String.format(ddisp_r, getDataValue("rainToday").toBigDecimal()) + " " + rMetric : "") + '<br>'
                mytext+= (removeicons < 2 ? '<img src=' + getDataValue("iconLocation") + 'wsr.png' + iconCloseStyled : 'Sunrise: ') + getDataValue("localSunrise") + '  '
                mytext+= (removeicons < 1 ? '<img src=' + getDataValue("iconLocation") + 'wss.png' + iconCloseStyled : ' | Sunset: ') + getDataValue("localSunset")
                mytext+= '     Updated ' + getDataValue('Summary_last_poll_time') + '</span>'
            }else{
                LOGINFO("myTile still exceeds 1,024 characters (" + mytext.length() + ") ... removing all formatting.")
                mytext = getDataValue("city") + '<br>'
                mytext+= getDataValue("condition_text") + (noAlert ? '' : ' | ') + (noAlert ? '' : getDataValue("alert")) + '<br>'
                mytext+= String.format(ddisp_twd, getDataValue("temperature").toBigDecimal()) + tMetric + ' Feels like ' + String.format(ddisp_twd, getDataValue("feelsLike").toBigDecimal()) + tMetric + '<br>'
                mytext+= getDataValue("wind_direction") + " "
                mytext+= getDataValue("wind").toBigDecimal() < 1.0 ? 'calm' : "@ " + String.format(ddisp_twd, getDataValue("wind").toBigDecimal()) + " " + dMetric
                mytext+= ', gusts ' + ((wgust < 1.0) ? 'calm' :  "@ " + String.format(ddisp_twd, wgust) + " " + dMetric) + '<br>'
                mytext+= 'Bar: ' + String.format(ddisp_p, getDataValue("pressure").toBigDecimal()) + " " + pMetric
                mytext+= ' | Hum: ' + getDataValue("humidity") + '%  ' + ' | Precip: ' + (getDataValue("rainToday").toBigDecimal() > 0 ? String.format(ddisp_r, getDataValue("rainToday").toBigDecimal()) + " " + rMetric : "") + '<br>'
                mytext+= 'Sunrise: ' + getDataValue("localSunrise") + ' | Sunset:' + getDataValue("localSunset") + ' |  Updated:' + getDataValue('Summary_last_poll_time')
                if(mytext.length() > 1024) {
                    LOGINFO("myTile even still exceeds 1,024 characters (" + mytext.length() + ") ... truncating.")
                }
            }
        }
        LOGINFO("mytext: ${mytext}")
        sendEvent(name: "myTile", value: mytext.take(1024))
    }
//  >>>>>>>>>> End Built mytext <<<<<<<<<<
}
// >>>>>>>>>> End Post-Poll Routines <<<<<<<<<<

public void refresh() {
    updateLux(true)
    return
}

void updated()   {
	unschedule()
	updateCheck()
	initialize()
    runEvery5Minutes(updateLux, [Data: [true]])
	Random rand = new Random(now())
	Integer ssseconds = rand.nextInt(60)
	schedule("${ssseconds} 20 0/8 ? * * *", pollSunRiseSet)
	runIn(5, pollOWM)
	if(settingEnable) runIn(2100,settingsOff)// "roll up" (hide) the condition selectors after 35 min
	if(settings.logSet) runIn(1800,logsOff)// "turns off extended logging after 30 min
	Integer r_minutes = rand.nextInt(60)
	schedule("0 ${r_minutes} 8 ? * FRI *", updateCheck)
}
void initialize() {
    unschedule(pollOWM)
    Boolean logSet = (settings.logSet ?: false)
    String city = (settings.city ?: "")
    updateDataValue("city", city)
    Boolean altCoord = (settings.altCoord ?: false)
    String valtLat = location.latitude.toString().replace(" ", "")
    String valtLon = location.longitude.toString().replace(" ", "")
    String altLat = settings.altLat ?: valtLat
    String altLon = settings.altLon ?: valtLon
    if (altCoord) {
        if (altLat == null) {
            device.updateSetting("altLat", [value:valtLat,type:"text"])
        }
        if (altLon == null) {
            device.updateSetting("altLon", [value:valtLon,type:"text"])
        }
        if (altLat == null || altLon == null) {
            if ((valtLAt == null) || (ValtLat = "")) {
                log.error "OpenWeatherMap.org Weather Driver - ERROR: The Override Coorinates feature is selected but Both Hub & the Override Latitude are null."
            } else {
                device.updateSetting("altLat", [value:valtLat,type:"text"])
            }
            if ((valtLon == null) || (valtLon = "")) {
                log.error "OpenWeatherMap.org Weather Driver - ERROR: The Override Coorinates feature is selected but Both Hub & the Override Longitude are null."
            } else {
                device.updateSetting("altLon", [value:valtLon,type:"text"])
            }
        }
    } else {
        device.removeSetting("altLat")
        device.removeSetting("altLon")
        device.updateSetting("altLat", [value:valtLat,type:"text"])
        device.updateSetting("altLon", [value:valtLon,type:"text"])
        if (altLat == null || altLon == null) {
            if ((valtLat == null) || (valtLat = "")) {
                log.error "OpenWeatherMap.org Weather Driver - ERROR: The Hub's latitude is not set. Please set it, or use the Override Coorinates feature."
            } else {
                device.updateSetting("altLat", [value:valtLat,type:"text"])
            }
            if ((valtLon == null) || (valtLon = "")) {
                log.error "OpenWeatherMap.org Weather Driver - ERROR: The Hub's longitude is not set. Please set it, or use the Override Coorinates feature."
            } else {
                device.updateSetting("altLon", [value:valtLon,type:"text"])
            }
        }
    }
    String pollIntervalForecast = (settings.pollIntervalForecast ?: "3 Hours")
    String pollIntervalForecastnight = (settings.pollIntervalForecastnight ?: "3 Hours")
    String datetimeFormat = (settings.datetimeFormat ?: "1")
    String distanceFormat = (settings.distanceFormat ?: "Miles (mph)")
    String pressureFormat = (settings.pressureFormat ?: "Inches")
    String TWDDecimals = (settings.TWDDecimals ?: "0")
    String PDecimals = (settings.PDecimals ?: "0")
    String RDecimals = (settings.PDecimals ?: "0")
    String rainFormat = (settings.rainFormat ?: "Inches")
    String tempFormat = (settings.tempFormat ?: "Fahrenheit (°F)")
    Boolean luxjitter = (settings.luxjitter ?: false)
    Boolean iconType = (settings.iconType ?: false)
    updateDataValue("iconType", iconType ? 'true' : 'false')
    Boolean summaryType = (settings.summaryType ?: false)
    String iconLocation = (settings.iconLocation ?: "https://tinyurl.com/y6xrbhpf/")
    updateDataValue("iconLocation", iconLocation)
    state.OWM = '<a href="https://openweathermap.org" target="_blank"><img src=' + getDataValue("iconLocation") + 'OWM.png style="height:2em;"></a>'
    setDateTimeFormats(datetimeFormat)
    String dMetric
    String pMetric
    String rMetric
    String tMetric
    setMeasurementMetrics(distanceFormat, pressureFormat, rainFormat, tempFormat)
    String ddisp_twd
    String ddisp_p
    String ddisp_r
    String mult_twd
    String mult_p
    String mult_r
    setDisplayDecimals(TWDDecimals, PDecimals, RDecimals)

    pollSunRiseSet()

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
	}else{
		dsseconds = ssseconds - 60 + 4
	}
	if(getDataValue("is_light")=="true") {
		if(pollIntervalForecast == "Manual Poll Only"){
			LOGINFO("MANUAL FORECAST POLLING ONLY")
		} else {
			pollIntervalForecast = (settings.pollIntervalForecast ?: "3 Hours").replace(" ", "")
            LOGINFO("pollIntervalForecast: $pollIntervalForecast")
			if(pollIntervalForecast=='2Minutes'){
				schedule("${dsseconds} ${minutes2}/2 * * * ? *", pollOWM)
			}else if(pollIntervalForecast=='5Minutes'){
				schedule("${dsseconds} ${minutes5}/5 * * * ? *", pollOWM)
			}else if(pollIntervalForecast=='10Minutes'){
				schedule("${dsseconds} ${minutes10}/10 * * * ? *", pollOWM)
			}else if(pollIntervalForecast=='15Minutes'){
				schedule("${dsseconds} ${minutes15}/15 * * * ? *", pollOWM)
			}else if(pollIntervalForecast=='30Minutes'){
				schedule("${dsseconds} ${minutes30}/30 * * * ? *", pollOWM)
			}else if(pollIntervalForecast=='1Hour'){
				schedule("${dsseconds} ${minutes60} * * * ? *", pollOWM)
			}else if(pollIntervalForecast=='3Hours'){
				schedule("${dsseconds} ${minutes60} ${hours3}/3 * * ? *", pollOWM)
			}
		}
	}else{
		if(pollIntervalForecastnight == "Manual Poll Only"){
			LOGINFO("MANUAL FORECAST POLLING ONLY")
		} else {
			pollIntervalForecastnight = (settings.pollIntervalForecastnight ?: "3 Hours").replace(" ", "")
            LOGINFO("pollIntervalForecastnight: $pollIntervalForecastnight")
			if(pollIntervalForecastnight=='2Minutes'){
				schedule("${dsseconds} ${minutes2}/2 * * * ? *", pollOWM)
			}else if(pollIntervalForecastnight=='5Minutes'){
				schedule("${dsseconds} ${minutes5}/5 * * * ? *", pollOWM)
			}else if(pollIntervalForecastnight=='10Minutes'){
				schedule("${dsseconds} ${minutes10}/10 * * * ? *", pollOWM)
			}else if(pollIntervalForecastnight=='15Minutes'){
				schedule("${dsseconds} ${minutes15}/15 * * * ? *", pollOWM)
			}else if(pollIntervalForecastnight=='30Minutes'){
				schedule("${dsseconds} ${minutes30}/30 * * * ? *", pollOWM)
			}else if(pollIntervalForecastnight=='1Hour'){
				schedule("${dsseconds} ${minutes60} * * * ? *", pollOWM)
			}else if(pollIntervalForecastnight=='3Hours'){
				schedule("${dsseconds} ${minutes60} ${hours3}/3 * * ? *", pollOWM)
			}
		}
	}
	return
}

public void pollData() {
	pollOWM()
    return
}
// ************************************************************************************************

public void setDateTimeFormats(String formatselector){
    switch(formatselector) {
        case "1": DTFormat = "M/d/yyyy h:mm a";   dateFormat = "M/d/yyyy";   timeFormat = "h:mm a"; break;
        case "2": DTFormat = "M/d/yyyy HH:mm";    dateFormat = "M/d/yyyy";   timeFormat = "HH:mm";  break;
    	case "3": DTFormat = "MM/dd/yyyy h:mm a"; dateFormat = "MM/dd/yyyy"; timeFormat = "h:mm a"; break;
    	case "4": DTFormat = "MM/dd/yyyy HH:mm";  dateFormat = "MM/dd/yyyy"; timeFormat = "HH:mm";  break;
		case "5": DTFormat = "d/M/yyyy h:mm a";   dateFormat = "d/M/yyyy";   timeFormat = "h:mm a"; break;
    	case "6": DTFormat = "d/M/yyyy HH:mm";    dateFormat = "d/M/yyyy";   timeFormat = "HH:mm";  break;
    	case "7": DTFormat = "dd/MM/yyyy h:mm a"; dateFormat = "dd/MM/yyyy"; timeFormat = "h:mm a"; break;
        case "8": DTFormat = "dd/MM/yyyy HH:mm";  dateFormat = "dd/MM/yyyy"; timeFormat = "HH:mm";  break;
    	case "9": DTFormat = "yyyy/MM/dd HH:mm";  dateFormat = "yyyy/MM/dd"; timeFormat = "HH:mm";  break;
    	default: DTFormat = "M/d/yyyy h:mm a";  dateFormat = "M/d/yyyy";   timeFormat = "h:mm a"; break;
	}
    return
}

public void setMeasurementMetrics(distFormat, pressFormat, precipFormat, temptFormat){
    if(distFormat == "Miles (mph)") {
        dMetric = "MPH"
    } else if(distFormat == "knots") {
        dMetric = "knots"
    } else if(distFormat == "Kilometers (kph)") {
        dMetric = "KPH"
    } else {
        dMetric = "m/s"
    }
    if(pressFormat == "Millibar") {
        pMetric = "MBAR"
    } else if(pressFormat == "Inches") {
        pMetric = "inHg"
    } else {
        pMetric = "hPa"
    }
    if(precipFormat == "Millimeters") {
        rMetric = "mm"
    } else {
        rMetric = "in"
    }
    if(temptFormat == "Fahrenheit (°F)") {
        tMetric = "°F"
    } else {
        tMetric = "°C"
    }
    return
}

public void setDisplayDecimals(TWDDisp, PressDisp, RainDisp) {
    switch(TWDDisp) {
        case "0": ddisp_twd = "%3.0f"; mult_twd = "1"; break;
        case "1": ddisp_twd = "%3.1f"; mult_twd = "10"; break;
        case "2": ddisp_twd = "%3.2f"; mult_twd = "100"; break;
        case "3": ddisp_twd = "%3.3f"; mult_twd = "1000"; break;
        case "4": ddisp_twd = "%3.4f"; mult_twd = "10000"; break;
       	default: ddisp_twd = "%3.0f"; mult_twd = "1"; break;
	}
    updateDataValue("ddisp_twd", ddisp_twd)
    updateDataValue("mult_twd", mult_twd)
    switch(PressDisp) {
        case "0": ddisp_p = "%,4.0f"; mult_p = "1"; break;
        case "1": ddisp_p = "%,4.1f"; mult_p = "10"; break;
        case "2": ddisp_p = "%,4.2f"; mult_p = "100"; break;
        case "3": ddisp_p = "%,4.3f"; mult_p = "1000"; break;
        case "4": ddisp_p = "%,4.4f"; mult_p = "10000"; break;
       	default: ddisp_p = "%,4.0f"; mult_p = "1"; break;
	}
    updateDataValue("ddisp_p", ddisp_p)
    updateDataValue("mult_p", mult_p)
    switch(RainDisp) {
        case "0": ddisp_r = "%2.0f"; mult_r = "1"; break;
        case "1": ddisp_r = "%2.1f"; mult_r = "10"; break;
        case "2": ddisp_r = "%2.2f"; mult_r = "100"; break;
        case "3": ddisp_r = "%2.3f"; mult_r = "1000"; break;
        case "4": ddisp_r = "%2.4f"; mult_r = "10000"; break;
       	default: ddisp_r = "%2.0f"; mult_r = "1"; break;
	}
    updateDataValue("ddisp_r", ddisp_r)
    updateDataValue("mult_r", mult_r)

    return
}

def estimateLux(Integer condition_id, Integer cloud)     {	
	Long lux = 0L
	Boolean aFCC = true
	Double l
	String bwn
	String tt                 = getDataValue("sunRiseSet")
	if(!tt) return [lux, bwn]
	def sunRiseSet            = parseJson(getDataValue("sunRiseSet")).results
	def tZ                    = TimeZone.getDefault() //TimeZone.getTimeZone(tz_id)
	String lT                 = new Date().format("yyyy-MM-dd'T'HH:mm:ssXXX", tZ)
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
			bwn = "Fully Night Time"
			lux = 5l
            aFCC = false
			break
		case { it < sunriseTimeMillis}:
			bwn = "between twilight and sunrise"
			l = (((localeMillis - twilight_beginMillis) * 50f) / (sunriseTimeMillis - twilight_beginMillis))
			lux = (l < 10f ? 10l : l.trunc(0) as Long)
			break
		case { it < noonTimeMillis}:
			bwn = "between sunrise and noon"
			l = (((localeMillis - sunriseTimeMillis) * 10000f) / (noonTimeMillis - sunriseTimeMillis))
			lux = (l < 50f ? 50l : l.trunc(0) as Long)
			break
		case { it < sunsetTimeMillis}:
			bwn = "between noon and sunset"
			l = (((sunsetTimeMillis - localeMillis) * 10000f) / (sunsetTimeMillis - noonTimeMillis))
			lux = (l < 50f ? 50l : l.trunc(0) as Long)
			break
		case { it < twilight_endMillis}:
			bwn = "between sunset and twilight"
			l = (((twilight_endMillis - localeMillis) * 50f) / (twilight_endMillis - sunsetTimeMillis))
			lux = (l < 10f ? 10l : l.trunc(0) as Long)
			break
		case { it < twiStartNextMillis}:
			bwn = "Fully Night Time"
			lux = 5l
            aFCC = false
			break
		case { it < sunriseNextMillis}:
			bwn = "between twilight and sunrise"
			l = (((localeMillis - twiStartNextMillis) * 50f) / (sunriseNextMillis - twiStartNextMillis))
			lux = (l < 10f ? 10l : l.trunc(0) as Long)
			break
		case { it < noonTimeNextMillis}:
			bwn = "between sunrise and noon"
			l = (((localeMillis - sunriseNextMillis) * 10000f) / (noonTimeNextMillis - sunriseNextMillis))
			lux = (l < 50f ? 50l : l.trunc(0) as Long)
			break
		case { it < sunsetNextMillis}:
			bwn = "between noon and sunset"
			l = (((sunsetNextMillis - localeMillis) * 10000f) / (sunsetNextMillis - noonTimeNextMillis))
			lux = (l < 50f ? 50l : l.trunc(0) as Long)
			break
		case { it < twiEndNextMillis}:
			bwn = "between sunset and twilight"
			l = (((twiEndNextMillis - localeMillis) * 50f) / (twiEndNextMillis - sunsetNextMillis))
			lux = (l < 10f ? 10l : l.trunc(0) as Long)
			break
		default:
			bwn = "Fully Night Time"
			lux = 5l
			aFCC = false
			break
	}
    String cC = condition_id.toString()
	String cCT = " using cloud cover from API"
    Double cCF = (!cloud || cloud=="") ? 0.998d : (1 - (cloud/100 / 3d))
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
    if(luxjitter){
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
	LOGINFO("condition: $cC | condition factor: $cCF | condition text: $cCT| lux: $lux")
	return [lux, bwn]
}

private Long getEpoch (String aTime) {
	def tZ = TimeZone.getDefault() //TimeZone.getTimeZone(tz_id)
	def localeTime = new Date().parse("yyyy-MM-dd'T'HH:mm:ssXXX", aTime, tZ)
	Long localeMillis = localeTime.getTime()
	return (localeMillis)
}

void SummaryMessage(Boolean SType, String Slast_poll_date, String Slast_poll_time, String SforecastTemp, String Sprecip, String Svis){
    BigDecimal windgust
    if(getDataValue("wind_gust") == "" || getDataValue("wind_gust").toBigDecimal() < 1.0 || getDataValue("wind_gust")==null) {
        windgust = 0.00g
    } else {
        windgust = getDataValue("wind_gust").toBigDecimal()
    }
    String wSum = (String)null
    if(SType){
        wSum = "Weather summary for " + getDataValue("city") + " updated at ${Slast_poll_time} on ${Slast_poll_date}. "
        wSum+= getDataValue("condition_text")
        wSum+= (!SforecastTemp || SforecastTemp=="") ? ". " : "${SforecastTemp}"
        wSum+= "Humidity is " + getDataValue("humidity") + "% and the temperature is " + String.format(ddisp_twd, getDataValue("temperature").toBigDecimal()) + tMetric + ". "
        wSum+= "The temperature feels like it is " + String.format(ddisp_twd, getDataValue("feelsLike").toBigDecimal()) + tMetric + ". "
        wSum+= "Wind: " + getDataValue("wind_string") + ", gusts: " + ((windgust < 1.00) ? "calm. " : "up to " + windgust.toString() + " " + dMetric + ". ")
        wSum+= Sprecip
        wSum+= Svis
        wSum+= ((!getDataValue("alert") || getDataValue("alert")==null) ? "" : " " + getDataValue("alert") + '.')
    } else {
        wSum = getDataValue("condition_text") + " "
        wSum+= ((!SforecastTemp || SforecastTemp=="") ? ". " : "${SforecastTemp}")
        wSum+= " Humidity: " + getDataValue("humidity") + "%. Temperature: " + String.format(ddisp_twd, getDataValue("temperature").toBigDecimal()) + tMetric + ". "
        wSum+= getDataValue("wind_string") + ", gusts: " + ((windgust == 0.00) ? "calm. " : "up to " + windgust + dMetric + ".")
	}
    wSum = wSum.take(1024)
    sendEvent(name: "weatherSummary", value: wSum)
	return
}

String getImgName(Integer wCode, String iconTOD){
    LOGINFO("getImgName Input: wCodes: " + wCode.toString() + " is_day: ${iconTOD}")
    LUitem = LUTable.find{ it.id == wCode }
	LOGINFO("getImgName Result: image: " + "${iconTOD}"=='true' ? (LUitem ? LUitem.Icond : 'na.png') : (LUitem ? LUitem.Iconn : 'na.png'))
    return ("${iconTOD}"=='true' ? (LUitem ? LUitem.Icond : 'na.png') : (LUitem ? LUitem.Iconn : 'na.png'))
}

String getCondCode(Integer cid, String iconTOD){
    LOGINFO("getCondCode Input: cid: " + cid.toString() + " is_day: ${iconTOD}")
    LUitem = LUTable.find{ it.id == cid }
	LOGINFO("getCondCode Result: cod: " + "${iconTOD}"=='true' ? (LUitem ? LUitem.stdIcond : 'na.png') : (LUitem ? LUitem.stdIconn : 'na.png'))
    return ("${iconTOD}"=='true' ? (LUitem ? LUitem.stdIcond : 'na.png') : (LUitem ? LUitem.stdIconn : 'na.png'))
}

void logCheck(){
    if(settings.logSet){
        log.info "OpenWeatherMap.org Weather Driver - INFO:  All Logging Enabled"
    } else {
        log.info "OpenWeatherMap.org Weather Driver - INFO:  Further Logging Disabled"
    }
    return
}

void LOGDEBUG(txt){
    if(settings.logSet){ log.debug("OpenWeatherMap.org Weather Driver - DEBUG:  ${txt}") }
    return
}

void LOGINFO(txt){
    if(settings.logSet){log.info("OpenWeatherMap.org Weather Driver - INFO:  ${txt}") }
    return
}

void LOGWARN(txt){
    if(settings.logSet){log.info("OpenWeatherMap.org Weather Driver - WARNING:  ${txt}") }
    return
}

void LOGERR(txt){
    if(settings.logSet){log.info("OpenWeatherMap.org Weather Driver - ERROR:  ${txt}") }
    return
}

void logsOff(){
	log.info "OpenWeatherMap.org Weather Driver - INFO:  extended logging disabled..."
	device.updateSetting("logSet",[value:"false",type:"bool"])
}

void settingsOff(){
	log.info "OpenWeatherMap.org Weather Driver - INFO: Settings disabled..."
	device.updateSetting("settingEnable",[value:"false",type:"bool"])
}

void sendEventPublish(evt)	{
// 	Purpose: Attribute sent to DB if selected	
    if (settings."${evt.name + "Publish"}") {
		sendEvent(name: evt.name, value: evt.value, descriptionText: evt.descriptionText, unit: evt.unit, displayed: evt.displayed);
		LOGINFO("$evt.name") //: $evt.name, $evt.value $evt.unit"
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
[id: 622, OWMd: '13d.png', OWMn: '13n.png', Icond: '54.png', Iconn: '55.png', luxpercent: 0.3, stdIcond: 'snow', stdIconn: 'nt_snow'],
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
[id: 801, OWMd: '02d.png', OWMn: '02n.png', Icond: '34.png', Iconn: '33.png', luxpercent: 0.9, stdIcond: 'partlycloudy', stdIconn: 'nt_partlycloudy'],
[id: 802, OWMd: '03d.png', OWMn: '03n.png', Icond: '53.png', Iconn: '52.png', luxpercent: 0.8, stdIcond: 'partlycloudy', stdIconn: 'nt_partlycloudy'],
[id: 803, OWMd: '04d.png', OWMn: '04n.png', Icond: '51.png', Iconn: '50.png', luxpercent: 0.6, stdIcond: 'mostlycloudy', stdIconn: 'nt_mostlycloudy'],
[id: 804, OWMd: '04d.png', OWMn: '04n.png', Icond: '51.png', Iconn: '50.png', luxpercent: 0.6, stdIcond: 'mostlycloudy', stdIconn: 'nt_mostlycloudy'],

    ]

@Field static attributesMap = [
    "threedayTile":             [title: "Three Day Forecast Tile", descr: "Display Three Day Forecast Tile?", typeof: false, default: "false"],
	"alert":				    [title: "Weather Alert", descr: "Display any weather alert?", typeof: false, default: "false"],
    "betwixt":				    [title: "Slice of Day", descr: "Display the 'slice-of-day'?", typeof: "string", default: "false"],
	"cloud":			    	[title: "Cloud", descr: "Display cloud coverage %?", typeof: "number", default: "false"],
	"condition_code":			[title: "Condition Code", descr: "Display 'condition_code'?", typeof: "string", default: "false"],
	"condition_icon_only":		[title: "Condition Icon Only", descr: "Display 'condition_code_only'?", typeof: "string", default: "false"],
	"condition_icon_url":		[title: "Condition Icon URL", descr: "Display 'condition_code_url'?", typeof: "string", default: "false"],
	"condition_icon":			[title: "Condition Icon", descr: "Display 'condition_icon'?", typeof: "string", default: "false"],
    "condition_iconWithText":   [title: "Condition Icon With Text", descr: "Display 'condition_iconWithText'?", typeof: "string", default: "false"],
	"condition_text":			[title: "Condition Text", descr: "Display 'condition_text'?", typeof: "string", default: "false"],
    "dashHubitatOWM":           [title: "Dash - Hubitat and OpenWeatherMap", descr: "Display attributes required by Hubitat and OpenWeatherMap dashboards?", typeof: false, default: "false"],
    "dashSmartTiles":           [title: "Dash - SmartTiles", descr: "Display attributes required by SmartTiles dashboards?", typeof: false, default: "false"],
    "dashSharpTools":           [title: "Dash - SharpTools.io", descr: "Display attributes required by SharpTools.io?", typeof: false, default: "false"],
    "dewpoint":                 [title: "Dewpoint (in default unit)", descr: "Display the dewpoint?", typeof: "number", default: "false"],
    "fcstHighLow":              [title: "Forecast High/Low Temperatures:", descr: "Display forecast High/Low temperatures?", typeof: false, default: "false"],
	"forecast_code":		    [title: "Forecast Code", descr: "Display 'forecast_code'?", typeof: "string", default: "false"],
	"forecast_text":		    [title: "Forecast Text", descr: "Display 'forecast_text'?", typeof: "string", default: "false"],
	"illuminated":			    [title: "Illuminated", descr: "Display 'illuminated' (with 'lux' added for use on a Dashboard)?", typeof: "string", default: "false"],
	"is_day":				    [title: "Is daytime", descr: "Display 'is_day'?", typeof: "number", default: "false"],
	"localSunrise":			    [title: "Local SunRise and SunSet", descr: "Display the Group of 'Time of Local Sunrise and Sunset,' with and without Dashboard text?", typeof: false, default: "false"],
	"myTile":				    [title: "myTile for dashboard", descr: "Display 'mytile'?", typeof: "string", default: "false"],
    "rainToday":			    [title: "Today's Precipitation", descr: "Display today's precipitation?", typeof: "number", default: "false"],
	"precipExtended":			[title: "Precipitation Forecast", descr: "Display precipitation forecast?", typeof: false, default: "false"],
    "obspoll":			        [title: "Observation time", descr: "Display Observation and Poll times?", typeof: false, default: "false"],
	"vis":				        [title: "Visibility (in default unit)", descr: "Display visibility distance?", typeof: "number", default: "false"],
    "weatherSummary":			[title: "Weather Summary Message", descr: "Display the Weather Summary?", typeof: "string", default: "false"],
	"wind_cardinal":		    [title: "Wind Cardinal", descr: "Display the Wind Direction (text initials)?", typeof: "number", default: "false"],	
	"wind_degree":			    [title: "Wind Degree", descr: "Display the Wind Direction (number)?", typeof: "number", default: "false"],
	"wind_direction":			[title: "Wind direction", descr: "Display the Wind Direction (text words)?", typeof: "string", default: "false"],
	"wind_gust":				[title: "Wind gust (in default unit)", descr: "Display the Wind Gust?", typeof: "number", default: "false"],
	"wind_string":			    [title: "Wind string", descr: "Display the wind string?", typeof: "string", default: "false"],
]

// Check Version   ***** with great thanks and acknowledgment to Cobra (CobraVmax) for his original code ****
def updateCheck()
{
	def paramsUD = [uri: "https://raw.githubusercontent.com/Scottma61/Hubitat/master/docs/version2.json"] //https://hubitatcommunity.github.io/???/version2.json"]
	
 	asynchttpGet("updateCheckHandler", paramsUD)
}

void updateCheckHandler(resp, data) {

	state.InternalName = "OpenWeatherMap-NWS Alerts Weather Driver"
    Boolean descTextEnable = settings.logSet ?: false

	if (resp.getStatus() == 200 || resp.getStatus() == 207) {
		def respUD = parseJson(resp.data)
		// log.warn " Version Checking - Response Data: $respUD"   // Troubleshooting Debug Code - Uncommenting this line should show the JSON response from your webserver
		state.Copyright = respUD.copyright
		// uses reformattted 'version2.json'
		String newVer = padVer(respUD.driver.(state.InternalName).ver)
		String currentVer = padVer(version())
		state.UpdateInfo = (respUD.driver.(state.InternalName).updated)
            // log.debug "updateCheck: ${respUD.driver.(state.InternalName).ver}, $state.UpdateInfo, ${respUD.author}"

		switch(newVer) {
			case { it == "NLS"}:
                state.Status = "<b>** This Driver is no longer supported by ${respUD.author}  **</b>"
                if (descTextEnable) log.warn "** This Driver is no longer supported by ${respUD.author} **"
				break
			case { it > currentVer}:
			      state.Status = "<b>New Version Available (Version: ${respUD.driver.(state.InternalName).ver})</b>"
			      if (descTextEnable) log.warn "** There is a newer version of this Driver available  (Version: ${respUD.driver.(state.InternalName).ver}) **"
			      if (descTextEnable) log.warn "** $state.UpdateInfo **"
				break
			case { it < currentVer}:
			      state.Status = "<b>You are using a Test version of this Driver (Expecting: ${respUD.driver.(state.InternalName).ver})</b>"
			      if (descTextEnable) log.warn "You are using a Test version of this Driver (Expecting: ${respUD.driver.(state.InternalName).ver})"
				break
			default:
				state.Status = "Current Version: ${respUD.driver.(state.InternalName).ver}"
				if (descTextEnable) log.info "You are using the current version of this driver"
				break
        }

    } else {
        log.error "Something went wrong: CHECK THE JSON FILE AND IT'S URI"
    }
}

/*
	padVer

	Version progression of 1.4.9 to 1.4.10 would mis-compare unless each duple is padded first.

*/
String padVer(String ver) {
	String pad = ""
	ver.replaceAll( "[vV]", "" ).split( /\./ ).each { pad += it.padLeft( 2, '0' ) }
	return pad
}

    String getThisCopyright(){"&copy; 2020 Matthew (scottma61) "}
