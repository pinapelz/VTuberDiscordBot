# vTuber Discord Bot 
JDA Discord Bot Written in Java to get the schedules of VTubers. ARM compatible, to an extent.
Currently there is support for all HololiveJP, EN, ID, Holostars, and Nijisanji JP personalities

There is a second bot included (onionBot) based off of [HoloBot](https://github.com/Lukeisun/HoloBot) that 
both refreshes the Nijisanji Schedule and also displays a feed of who is currently streaming. 

It is highly reccomended to run this bot along side the main bot since it is necessary for automated updating of the schedule.
It can be ran with no feed by changing the ChannelIDs under config.py to "0"

Should you wish to not run the bot then the Nijisanji schedule will be updated manually each time the command is ran (please activate this by changing the
value in autoRefreshNiji.txt to false). **You may encounter slow response speeds when running Nijisanji schedule command with this method**

## Requirements 
- Java Maven
- Python 3.6
- Youtube Data API V3 Key
- [Chrome Driver Executable](https://chromedriver.chromium.org/downloads)
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

You may now run through the IDE or package and run as a shaded JAR file.
For now please input your chromedriver path manually under ScreenShotTool.java class
For more infomation view wiki.


