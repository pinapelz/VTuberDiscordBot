import discord
import datetime
import pytz
from yt import findVideoID
from config import WELCOMEMSG


def clearMessageEmbed():
    embed = discord.Embed(
        title='**Clearing messages**',
        color=discord.Colour.red()
    )
    return embed




def displayEmbed(j, i, ID):
    vidID = findVideoID(j, ID)
    embed = discord.Embed(
        title=i[1]+' is live!',
        description='https://www.youtube.com/watch?v=' + vidID,
        color=discord.Colour.blue()
    )
    dt_JST = datetime.datetime.now(tz=pytz.timezone('Asia/Tokyo'))
    dt_PST = dt_JST.astimezone(pytz.timezone('America/Vancouver'))
    embed.set_footer(text=dt_JST.strftime('%X %Z')
                     + '\n' + dt_PST.strftime('%X %Z'))
    embed.add_field(name="**Link**",
                    value="https://www.youtube.com/watch?v=" +
                    vidID)
    embed.set_image(
     url="https://i.ytimg.com/vi/"+vidID+"/hqdefault_live.jpg")
    return embed
