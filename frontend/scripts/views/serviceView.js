var $ = require('jquery');
var _ = require('underscore');
var Backbone = require('backbone');

var ServiceModel = require('../models/service');
var ServiceList = require('../collections/serviceCollection');

var ServiceItemView = Backbone.View.extend({
  tagName: 'li',

  initialize: function(){
		this.model.on('change', this.render, this);
		this.model.on('destroy', this.remove, this);
  },

  remove: function(){
    this.$el.remove();
  },

  delete:function(){
    this.model.destroy(null, {
        success: function(model, resp) {
        },
        error: function() {
            new Error({ message: 'Could delete service with id: ' + id + '.' });
            window.location.hash = '#';
          }
    });
  },

  render: function(){
    var url = this.model.escape("url");
    if(url.lastIndexOf("http", 0) !== 0)
      url = "http://"+url;
    var statusClass = this.model.escape("status") === "OK" ? "ok-status" : "notok-status";
    var html = "<div class='listItemContainer " + statusClass;
    html += "'><span class='listItemContent'>";
    html += "<a href='"+url+"'>" + this.model.escape("name") + "</a>";
    html += " was last checked on " +this.model.escape("lastCheck")+" and is "+this.model.escape("status")+"</span>";
    html += "<span class='fa fa-times delete'>";
    html += "</span></div>";
    this.$el.html(html);
    return this;
  },

  events: {
      "click .delete" : "delete",
  }
});

var InputView = Backbone.View.extend({
  tagName: 'span',

  render: function(){
    this.$el.html("<input type='text' id='name'/><input type='text' id='url'/><span id='addButton' class='add fa fa-plus'></span>");
    return this;
  },

  add: function(event){
    var name = $('#name').val();
    var url = $('#url').val();
    var service = new ServiceModel({name:name, url:url});
    service.save(null, {
        success: function(model, resp) {
          ServiceList.add(model);
        },
        error: function() {
          new Error({ message: 'Could not add service'});
          window.location.hash = '#';
        }
      });
    },

    keyPressEventHandler : function(event){
      if(event.keyCode == 13){
        this.$("#addButton").click();
      }
    },

    events: {
      "click .add" : "add",
      "keyup #url" : "keyPressEventHandler"
    }
});

module.exports = Backbone.View.extend({
  tagName: 'ul',

    initialize: function(options) {
      this.listenTo(ServiceList, 'add', this.added);
      this.listenTo(ServiceList, 'remove', this.render);
    },

    added:function(){
      this.render().$('#name').focus();;
    },

    render: function(){
      this.$el.empty();
      var inputView = new InputView({});
      this.$el.append(inputView.render().el);
      ServiceList.each(this.addOne, this);
      return this;
    },

    addOne: function(service){
      var serviceItemView = new ServiceItemView({ model: service });
      this.$el.append(serviceItemView.render().el);
    }
  });
