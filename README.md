My App List
========================

This GitHub repository hosts the code for the Android app My App List

Download from: https://play.google.com/store/apps/details?id=com.projectsexception.myapplist

Building
------------

1.Add the next sign properties to your gradle distribution (for example in ~/.gradle/gradle.properties)

    storeFile=/home/user/keystore
    storePassword=keystorepassword
    keyAlias=keyalias
    keyPassword=keypassword

2.Build the desired distribution

2.1. Play Store: `gradle clean assemblePlayRelease`

2.2. Open source: `gradle clean assembleOpenRelease`

Contributing
------------

If you want to contribute fork the repository, code and tell me about it

Help me to translate: http://crowdin.net/project/my-app-list/invite

License
-------

    Copyright 2012 Projects Exception

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
