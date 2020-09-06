FROM openjdk:16-jdk-alpine

WORKDIR /usr/src/app

# Run maven package first! so that the JMusicBot-Snapshot-All.jar is available in target directory
COPY ./target/JMusicBot-Snapshot-All.jar .
COPY Playlists Playlists
COPY serversettings.json serversettings.json

CMD java -jar -Dnogui=true JMusicBot-Snapshot-All.jar

# to run the container, do
# docker run -e <env> --name <name> -d container:tag
# environment variables are as followings:
# music_bot_token: required, the bot token obtained from Discord
# music_bot_owner_id: required, the numeric ID of the owner obtained from Discord
# music_bot_start_time: optional for scheduled play, time in HH:mi:ss format in America/Los_Angeles timezone, eg. 18:02:09
# music_bot_end_time: optional for scheduled stop, time in HH:mi:ss format in America/Los_Angeles timezone, eg. 18:45:59
# music_bot_frequency: optional for scheduled reschedule, in minutes. eg. 600 -> every 10 hours since music_bot_start_time, it will start playing; every 10 hours since music_bot_end_time it will stop playing
# music_bot_days: optional for scheduled days (of week), 1-Monday, 2-Tuesday, ..., 7-Sunday. Eg. 12345, Monday through Friday
# music_bot_playlist: optional for scheduled play of music, indicate which playlist to use, if not specified, bot uses default playlist