var $ = require('jquery');
var _ = require('underscore');
var Backbone = require('backbone');

var ServiceModel = require('../models/service');

module.exports = Backbone.View.extend({

    initialize: function(options) {
        this.services = options.services;
        this.render();
    },

    events: {
        "click .delete" : "delete",
        "click .add" : "add",
    },

    render: function() {
      var out = "<h3>Service status:</h3><ul>";
      _(this.services).each(function(item) {
        out += "<li><a href='#services/" + item.id + "'>" + item.name + "</a></li>";
      });
      out += "</ul>";
      out += "<div class='add'>Test add one</div>";
      $(this.el).html(out);
      $('#app').html(this.el);
    },

    delete: function(event){

    },

    add: function(event){
      var name = "addTest";
      var url = "addUrl";
      var service = new ServiceModel({name:name, url:url});
      service.save({
          success: function(model, resp) {
            this.services.push(model);
          },
          error: function() {
              new Error({ message: 'Could not add service'});
              window.location.hash = '#';
          }
      });
    }
  });
