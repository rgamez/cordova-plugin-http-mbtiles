var exec = require('cordova/exec');

var execAsPromise = function(success, error, service, action, params) {
    return new Promise(function(resolve, reject) {
        exec(
            function(data) {
                resolve(data);
                if (typeof success === 'function') {
                    success(data);
                }
            },
            function(err) {
                reject(err);
                if (typeof error === 'function') {
                    error(err);
                }
            },
            service,
            action,
            params);
    });
};

exports.startServer = function(success, error) {
    return execAsPromise(success, error, 'HTTPMBTilesServer', 'startServer', []);
};

exports.addTiles = function(layerName, fileName, success, error) {
    return execAsPromise(success, error, 'HTTPMBTilesServer', 'addTiles', [layerName, fileName]);
};
