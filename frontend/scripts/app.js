var $ = require('jquery');
var _ = require('underscore')
var Backbone = require('backbone');
Backbone.$ = $;

var Router = require('./routers/router');
new Router();
Backbone.history.start();
