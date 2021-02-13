<html>
<head>
<meta name="google-site-verification" content="_8j-Yknfj6qiVQ8uGR6xYRhoUkkwFat7eBIVWQ5Dy0M" />
</head>
</html>

# VTuber Discord Bot (Hololive and Nijisanji)
JDA Discord Bot Written in Java to get the schedules of VTubers. ARM compatible, to an extent.
Currently there is support for all HololiveJP, EN, ID, Holostars, and Nijisanji personalities

There is a second bot included (onionBot) based off of [HoloBot](https://github.com/Lukeisun/HoloBot) that 
both refreshes the Nijisanji Schedule and also displays a feed of who is currently streaming. 

The two bots have useful features such as telling you when someone has gone live, listing the scheduled streams of talents, and a music bot feature featuring custom made and editable Vtuber music playlist (not restricted to just VTubers).

For more information please visit the Github Page using the button above

## Requirements 
- Java Maven
- Python 3.6
- Youtube Data API V3 Key
- [Chrome Driver Executable](https://chromedriver.chromium.org/downloads)
- Chrome Web Browser
All dependencies should already be included in the pom.xml file 

## Building from source
This project is formatted for Intellij IDEA 
1. Download Source
2. Place files in a folder and open using Intellij
3. Fill in Youtube Data API Key in apikey.txt
4. Open Discord Bot Token in discordToken.txt
5. Download chromedriver executable and save it anywhere you'd like   
6. Configure onionBot by editing config.py with the channel ids you want the feeds to be sent to and the Discord bot token

## Features
- Music Bot (Large Hololive Playlist), vTuber Schedule

![Schedule](https://i.imgur.com/OpbhYNR.png)
![Schedule2](https://i.imgur.com/pMCfmSm.png)
![bot3](https://i.imgur.com/d5Jd6Hq.png)


