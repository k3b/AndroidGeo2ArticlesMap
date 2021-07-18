# AndroidGeo2ArticlesMap
Plugin for [Location Map Viewer](https://f-droid.org/en/packages/de.k3b.android.locationMapViewer): 
Show articles from Wikipedia™ or Wikivoyage™ near a given geographic location in an interactive map.

Uscase: Suppose you are going on holiday. Your navigation app shows you a geo-map with your hotel
and you want to know: what is near this place that has an article in [wikipedia](en.wikipedia.org/) 
( or [wikivoyage](https://en.wikivoyage.org/) ). 

In your navigation app select "view in external app" (or "send location to" or "share location with" or ...) 
choose "Show Articles in Map" and you will get a map with marks for articles. If you click on a mark you get
a small popup with the article summary and a link to the wikipedia article.  

You can get geo-infos from these opensource apps

* navigation: [OsmAnd](https://f-droid.org/packages/net.osmand.plus/),
* public transportation: [Transportr](https://f-droid.org/packages/de.grobox.liberario) or [oeffi](https://f-droid.org/packages/de.schildbach.oeffi)
* geo-caching [c:geo](https://apt.izzysoft.de/fdroid/index/apk/cgeo.geocaching)
* translate postal adress to geo [Acastus Photon ](https://f-droid.org/packages/name.gdr.acastus_photon)
* Share your current positon [LocationShare](https://f-droid.org/packages/ca.cmetcalfe.locationshare) or [My Position](https://f-droid.org/packages/net.mypapit.mobile.myposition)
* and probably many others.......

## Technical Description 

Technically speaking the AndroidGeo2ArticlesMap app 
* hooks into the android system for view/send/share events for geographic coordinates (also known as [geo: - uri](https://en.wikipedia.org/wiki/Geo_URI_scheme)), 
* [asks wikipedia for article infos near this location](https://en.wikipedia.org/w/api.php),
* translates the articles found into a standardformat ([kmz](https://en.wikipedia.org/wiki/Keyhole_Markup_Language) 
* and asks android to show the generated kmz file
* The result is shown in [Location Map Viewer](https://f-droid.org/en/packages/de.k3b.android.locationMapViewer) (or any other installed app that understands kmz) 

## Legal stuff

[AndroidGeo2ArticlesMap](github.com/k3b/AndroidGeo2ArticlesMap) and 
[Location Map Viewer](https://github.com/k3b/LocationMapViewer) 
Copyright (c) by k3b, Licensed under GPL, Version 3.0 or later.

Wikipedia and Wikivoyage are  [trademarks of the Wikimedia Foundation](https://foundation.wikimedia.org/wiki/Wikimedia_trademarks)
