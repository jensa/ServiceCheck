var $ = require('jquery');
var Backbone = require('backbone');
var _ = require('underscore');
Backbone.$ = $;

var servicesView = require('../views/serviceList');
var ServiceModel = require('../models/service');

module.exports = Backbone.Router.extend({
    routes: {
        "services/:id": "delete",
        "": "index"
    },

    delete: function(id) {
        var service = new ServiceModel({ id: id });
        service.destroy({
            success: function(model, resp) {
            },
            error: function() {
                new Error({ message: 'Could delete service with id: ' + id + '.' });
                window.location.hash = '#';
            }
        });
    },

    index: function() {
        $.getJSON('/service', function(data) {
            if(data) {
                var services = _(data.services).map(function(i) { return new ServiceModel(i); });
                new servicesView({ services: services });
            } else {
              new Error({ message: "Error loading services." });
            }
        });
    },


});
