Uses permissions, and dependencies on other projects.

Much better managed with `Android Studio`. See [here](https://developer.android.com/studio/install).

### Permissions
Requires `ACCESS_FINE_LOCATION` and `ACCESS_COARSE_LOCATION`.

On the phone, go to `Settings` > `Apps` > `Astro Computer` > `Permissions`, and turn `Location` on.

### Dependencies
Will use the Celestial resources from other projects (in [`raspberry.coffee`](https://github.com/OlivierLD/raspberry-coffee)), for the calculations.

> Note: those resources are **copied** for now, as they include packages not supported on Android (like `awt`).

#### To refer to a maven-installed package
Install it on your local maven repo:
```
 $ cd [some.where]/raspberry-coffee/common-utils
 $ ../gradlew clean install
```
Then its content can be referred to as in:
```groovy
 dependencies {
    . . .
    implementation 'oliv.raspi.coffee:common-utils:1.0'
    . . .
 }
```

> Note: Android may require Java 8, not 9.