Cordova HTTP-MBtiles plugin
===========================

A Cordova plugin for serving MBtiles via an embedded HTTP server

### Motivation
What in principle seems a very simple task, rendering sqlite stored tiles
in a javascript map library i.e. Leaflet, Openlayers, becomes a non trivial task, partially for the async nature of the components (sqlite-java-javascript-dom) but mainly because the javascript libraries are optimized for handling http resources.

Using an embedded http server let the javascript libraries to operate in the way they were designed and in theory the rendering should be more fluid with the use of the native caching of the webView.


### Installation
```
cordova plugin add https://github.com/rgamez/cordova-plugin-http-mbtiles
```


### Usage

```
var HTTPMBTiles = cordova.plugins.HTTPMBTiles;
var port;
var layerName = 'mylayer';
var mbtilesFile = '/storage/emulated0/tiles/test.mbtiles'
```

**Starting the server and retrieve the listening port**

```
HTTPMBTiles
	.startServer()
	.then(function(serverConfig) {
		port = serverConfig.port;
		console.debug(serverConfig.port)
	});
```

**Adding a TMS layer**


```
HTTPMBTiles
	.addTiles(layerName, mbtilesFile)
	.then(function(layerOptions) {
		console.debug('Layer bounds: ' + layerOptions.bounds)
	});
```


**Adding the layer to the map**


Leaflet

```
L.tileLayer(http://localhost:' + port + '/' + layerName + '/{z}/{x}/{y}.png', { tms: true })
	.addTo(map);
```



Openlayers 3

```
map.addLayer(
	new ol.layer.Tile({
		source: new ol.source.OSM({
        	url: 'http://localhost:' + port + '/' + layerName + '/{z}/{x}/{-y}.png'
        })
    })
);


```


### Limitations
- The plugin is in early stages and non proper benchmark has been done to compare it to the existing alternatives.
- Only working in android, a first prototype fro the idea implemented using the NanoHTTPD library.

### Alternatives

- [cordova-plugin-mbtiles](https://github.com/ffournier/cordova-plugin-mbtiles) is the most mature MBtiles plugin out there.
- FieldTrip-Open uses a custom implementation in its [overlays](https://github.com/edina/fieldtrip-overlays) plugin, an early explanation from the idea behind [here](https://mobilegeo.wordpress.com/2013/06/07/mbtiles-and-openlayers/)