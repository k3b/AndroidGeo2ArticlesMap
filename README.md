# AndroidGeo2ArticlesMap

Plugin for [Location Map Viewer](https://f-droid.org/en/packages/de.k3b.android.locationMapViewer): 
Show articles from Wikipedia™ or Wikivoyage™ near a given geographic location in an interactive map.

Uscase: Suppose you are going on holiday. Your navigation app shows you a geo-map with your hotel
and you want to know: what is near this place that has an article in [wikipedia](https://en.wikipedia.org) 
( or [wikivoyage](https://en.wikivoyage.org) ). 

In your navigation app select "view in external app" (or "send location to" or "share location with" or ...) 
choose "Show Articles in Map" and you will get a map with marks for articles. If you click on a mark you get
a small popup with the article summary and a link to the wikipedia article.  

![](https://raw.githubusercontent.com/k3b/AndroidGeo2ArticlesMap/main/fastlane/metadata/android/en-US/images/phoneScreenshots/0-ageo2ArticleMap-map-popup.png)

Some en.wikipedia.org articles from Berlin, Germany 

--- 

You can get geo-infos from these opensource apps

* navigation: [OsmAnd](https://f-droid.org/packages/net.osmand.plus/),
* public transportation: [Transportr](https://f-droid.org/packages/de.grobox.liberario) or [oeffi](https://f-droid.org/packages/de.schildbach.oeffi)
* geo-caching [c:geo](https://apt.izzysoft.de/fdroid/index/apk/cgeo.geocaching)
* translate postal address to geo [Acastus Photon ](https://f-droid.org/packages/name.gdr.acastus_photon)
* Share your current position [LocationShare](https://f-droid.org/packages/ca.cmetcalfe.locationshare) or [My Position](https://f-droid.org/packages/net.mypapit.mobile.myposition)
* and probably many others.......

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
     alt="Get it on F-Droid"
     height="80">](https://f-droid.org/packages/de.k3b.android.geo2articlesmap/)

## Usage.

If you select view + "Show Articles in Map" from a geo app you will see the settings page:

![](https://raw.githubusercontent.com/k3b/AndroidGeo2ArticlesMap/main/fastlane/more_images/91-ageo2ArticleMap-settings.png)

* (6) In the **message area** you see the current geo-location "Berlin Germany (52.51,13.35)" where the articles are near by.
* (1) shows the **current service** "en.wikipedia.org" where the geo-data is taken from. You can 
  * (1) edit the service or 
  * use (2) to pick one from the **history** of the last used services or 
  * (3) pick one of the 93 bigger **known services**
* (4) If you enable **"Load symbols/images"** you get an image belonging for every article (if available in wikipedia) that will be shown in the map and the popup.
  * Loading images will slow downloading the articles and increase the used internet bandwidth.
* (5) If you enable **"Do not show this dialog again"** then getting articles will be done without this settings dialog.
* (7) If you press the **"View"** button the app will start loading data from current service (1) "en.wikipedia.org" that is near the current location (6) (Berlin Germany (52.51,13.35).
  * In the (6) **message area** you can see what the app does
  * Downloading...
  * Analysing...
  * Saving to file ...
* Finally you get the result.

--- 

![](https://raw.githubusercontent.com/k3b/AndroidGeo2ArticlesMap/main/fastlane/metadata/android/en-US/images/phoneScreenshots/2-ageo2ArticleMap-pick-service.png)

(3) pick one of the 93 bigger **known services**

--- 

![](https://raw.githubusercontent.com/k3b/AndroidGeo2ArticlesMap/main/fastlane/metadata/android/en-US/images/phoneScreenshots/3-ageo-service-history.png)

(2) to pick one from the **history** of the last used services or

--- 


## Technical Description 

Technically speaking the AndroidGeo2ArticlesMap app 
* hooks into the android system for view/send/share events for geographic coordinates (also known as [geo: - uri](https://en.wikipedia.org/wiki/Geo_URI_scheme)), 
* [asks wikipedia for article infos near this location](https://en.wikipedia.org/w/api.php),
* translates the articles found into a standardformat ([kmz](https://en.wikipedia.org/wiki/Keyhole_Markup_Language) )
* and asks android to show the generated kmz file
* The result is shown in [Location Map Viewer](https://f-droid.org/en/packages/de.k3b.android.locationMapViewer) (or any other installed app that understands kmz) 

## Legal stuff

[AndroidGeo2ArticlesMap](https://github.com/k3b/AndroidGeo2ArticlesMap) and 
[Location Map Viewer](https://github.com/k3b/LocationMapViewer) 
Copyright (c) by k3b, Licensed under GPL, Version 3.0 or later.

Wikipedia and Wikivoyage are  [trademarks of the Wikimedia Foundation](https://foundation.wikimedia.org/wiki/Wikimedia_trademarks)

-----

## Donations: 

If you like this app please consider to donating to http://donate.openstreetmap.org/ .

Since android-developping is a hobby (and an education tool) i donot want any 
money for my apps so donation should go to projects i benefit from.

