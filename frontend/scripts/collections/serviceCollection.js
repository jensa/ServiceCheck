var $ = require('jquery');
var Backbone = require('backbone');
var _ = require('underscore');
Backbone.$ = $;

var ServiceModel = require('../models/service');

var ServiceList = Backbone.Collection.extend({
   model: ServiceModel,

   initList:function(callback){
     var list = this;
     var toExec = callback;
     $.getJSON('/service', function(data) {
       if(data) {
         _(data.services).each(function(i) {list.add(new ServiceModel(i)); });
       } else {
         new Error({ message: "Error loading services." });
       }
       if(toExec)
        toExec();
     });
   }
 });

module.exports = new ServiceList;
