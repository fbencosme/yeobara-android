![feature]

# yeobara-android

<a href="https://play.google.com/store/apps/details?id=io.github.yeobara.android&utm_source=global_co&utm_medium=prtnr&utm_content=Mar2515&utm_campaign=PartBadge&pcampaignid=MKT-AC-global-none-all-co-pr-py-PartBadges-Oct1515-1"><img alt="Get it on Google Play" src="https://play.google.com/intl/en_us/badges/images/apps/en-play-badge.png" width="300px"/></a>


## Pre-requirements

- Android Studio Plugins (_Required_)
  - [Kotlin]

- Device
  - &gt;= [LOLLIPOP]
    - [ScanFilter]
  - [BLE(Blutooth Low Energy)][ble]
    - [Eddystone]


## Configuration

See `local.properties.example`.

- Basically you must
    - set `firebaseUrl` key and be same value as you set in [yeobara-desktop].
    - set `gcmSenderId` for GCM. Also you can refer to [yeobara-desktop].

- Optionally you can
    - set `playStoreP12File`, `playStorePublisher` for release to PlayStore using gradle plugin
        - https://github.com/Triple-T/gradle-play-publisher.
    - set `githubToken` for release to Github using gradle plugin
        - https://github.com/riiid/gradle-github-plugin.
    - set keystore's information for signed apk
        - `storeFile`, `storePassword`, `keyAlias` and `keyPassword`


## License

MIT © [Yeobara](https://github.com/yeobara)








[feature]: https://cloud.githubusercontent.com/assets/1744446/11339520/375ea73e-923d-11e5-97d6-e34bb21ffbc8.png
[ble]: http://developer.android.com/guide/topics/connectivity/bluetooth-le.html
[ScanFilter]: http://developer.android.com/reference/android/bluetooth/le/ScanFilter.html
[Eddystone]: https://github.com/google/eddystone
[LOLLIPOP]: http://developer.android.com/intl/ko/reference/android/os/Build.VERSION_CODES.html#LOLLIPOP
[Kotlin]: https://plugins.jetbrains.com/plugin/6954?pr=androidstudio
[KotlinExtForAndroid]: https://plugins.jetbrains.com/plugin/7717?pr=androidstudio
[yeobara-desktop]: https://github.com/yeobara/yeobara-desktop#configuration
