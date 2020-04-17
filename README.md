# OpenWeatherMap-NWS-Alerts-Weather-Driver
OpenWeatherMap-NWS Alerts Weather Driver

INITIAL ALPHA RELEASE - USE WITH CAUTION

This driver is a morph of the DarkSky.net Weather Driver to use OpenWeatherMap.org and the National Weather Service (only for weather alerts).

This inital alpha release just attempts to recreate the same data as the previous driver it was created from.  There are differences.  Some of those are listed here:
<Table><TR><TD>DarkSky</TD><TD>OpenWeatherMap</TD><TD>Comments</TD></TR>
<TR><TD>Probability of Precipitation</TD><TD>Preciptation volume</TD><TD>OWM does not provide PoP, only volume.</TD></TR>
<TR><TD>Ozone</TD><TD>------</TD><TD>OWM does not provide Ozone</TD></TR>
<TR><TD>Moon Phase</TD><TD>------</TD><TD>OWM does not provide the Moon Phase</TD></TR>
<TR><TD>Nearest Storm metrics</TD><TD>------</TD><TD>OWM does not provide nearest storm metrics</TD></TR>
<TR><TD>Weather Alerts</TD><TD>------</TD><TD>OWM does not provide Weather Alerts</TD></TR>
</Table>



Even though OWM does provide Weather Alerts, this driver pulls that in from the National Weather Service (weather.gov).
CAUTION - This has NOT been tested.  There have been no alerts for my area and I cannot verify the actual structure of the JSON return data when there is an alert.

Please report any bugs/issue in the Hubitat Community here:
https://community.hubitat.com/t/openweathermap-nws-alerts-weather-driver/38249
