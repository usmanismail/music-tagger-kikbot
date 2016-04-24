# music-tagger-kikbot

* You need to Register a kik bot [here](https://dev.kik.com/#/docs/getting-started) and get its: Name, and API_KEY and add it [here](https://github.com/usmanismail/music-tagger-kikbot/blob/master/src/main/java/com/kik/musictag/MessageResource.java#L64-L65)
* Create new Project with [ACRCloud](https://www.acrcloud.com/) and specify its API Key and Secret [here](https://github.com/usmanismail/music-tagger-kikbot/blob/master/src/main/java/com/kik/musictag/MessageResource.java#L62-L63). 
* You need to find the relvent native library for your platform ([Linux](https://github.com/acrcloud/acrcloud_sdk_java/tree/master/linux/x86-64/libs-so) or [OSX](https://github.com/acrcloud/acrcloud_sdk_java/tree/master/mac/x86-64/libs-so)) and add it to your Java Library path.

## Building

    mvn clean install

## Testing 

    mvn jetty:run

