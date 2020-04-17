# OpenWeatherMap-NWS-Alerts-Weather-Driver
OpenWeatherMap-NWS Alerts Weather Driver

INITIAL ALPHA RELEASE - USE WITH CAUTION

This driver is a morph of the DarkSky.net Weather Driver to use OpenWeatherMap.org and the National Weather Service (only for weather alerts).

This inital alpha release just attempts to recreate the same data as the previous driver it was created from.  There are differences.  Some of those are listed here:

DarkSky                            OpenWeatherMap                 Comments
-----------------------            ----------------------------   -------------------------------------
Probability of Precipitation       Preciptation volume            OWM does not provide PoP, only volume.
Ozone                              ------                         OWM does not provide Ozone
Moon Phase                         ------                         OWM does not provide the Moon Phase
Nearest Storm metrics              ------                         OWM does not provide nearest storm metrics
Weather Alerts                     ------                         OWM does not provide Weather Alerts

Even though OWM does provide Weather Alerts, this driver pulls that in from the National Weather Service (weather.gov).
CAUTION - This has NOT been tested.  There have been no alerts for my area and I cannot verify the actual structure of the JSON return data when there is an alert.

PLease report any bug/issue in the Hubitat Community here:
