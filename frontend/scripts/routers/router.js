var $ = require('jquery');
var Backbone = require('backbone');
var _ = require('underscore');
Backbone.$ = $;

var servicesView = require('../views/serviceView');
var ServiceModel = require('../models/service');
var ServiceList = require('../collections/serviceCollection');

module.exports = Backbone.Router.extend({
    routes: {
        "services/:id": "delete",
        "": "index"
    },

    delete: function(id) {

    },

    index: function() {
      ServiceList.initList(function(){
        var view = new servicesView({});
        $('#serviceList').append(view.render().el);
      })

    },


});
