#Fetch the schedule of hololive live stream

import sys
import unicodedata

from src.fetch_html import *
from src.scraping import *
from src.util import *


def main(options):
    timezone = check_timezone()
    f = open("hololive.txt", "w")

    if options is None:

        eng_flag = True
        tomorrow_flag = False
        all_flag = True

    else:
        eng_flag, tomorrow_flag, all_flag = option_check(options)
    source_html = fetch_source_html(tomorrow_flag)
    time_list, stream_members_list, stream_url_list = scraping(source_html, all_flag)

    
    if timezone != "Asia/Tokyo":
       time_list = timezone_convert(time_list, timezone)

    if eng_flag:
        show_in_english(time_list, stream_members_list, stream_url_list, timezone)
        sys.exit()


    lists_length = len(time_list)

    stream_members_list = replace_name(stream_members_list, lists_length)
    hour_list = list(map(lambda x: int(x.split(':')[0]), time_list))

    #Check if date is shifted
    if hour_list != sorted(hour_list):
        date_shift = True
        shift_index = check_shift(hour_list)

    else:
        date_shift = False
        shift_index = (256, 256)

    # Show in Japanese
    print('Index   Time       Member              Streaming URL  ({})'.format(timezone))
    #f.write('Index   Time       Member              Streaming URL  ({})'.format(timezone))

    for i in range(lists_length):

        if date_shift:

            if shift_index[0] == i - 1:

                if tomorrow_flag:
                    print('\nTomorrow\n')

                else:
                    print('\nToday\n')

            if shift_index[1] == i - 1:

                if tomorrow_flag:
                    print('\The day after tomorrow\n')

                else:
                    print('\nTomorrow\n')

        if i < 9:
            space = ' '

        else:
            space = ''
        if unicodedata.east_asian_width(stream_members_list[i][0]) == 'W':
            m_space = ' ' * ( (-2 * len(stream_members_list[i]) + 18))

        else:
            m_space = ' ' * ( (-1 * len(stream_members_list[i]) ) + 18)
        f.write('\n{}      {}~     {}{}  {}'.format(space, time_list[i], stream_members_list[i], m_space, stream_url_list[i]))
        print('{}{}~      {}     {}{}  {}'.format(i+1, space, time_list[i], stream_members_list[i], m_space, stream_url_list[i]))


if __name__ == '__main__':

    argv = sys.argv

    move_current_directory()

    if len(argv) > 1:
        argv.pop(0)

        try:
            argv = set(eval_argv(argv))

        except TypeError:
            print("Invalid argument")
            sys.exit()

        # If inputed option is invalid eval_argv returns None
        if argv is None:
            print('Error: invalid options. Execute with --help to check about options')
            sys.exit()

        else:
           # f.close()
            main(argv)
          

    #No option
    else:
        main(None)
