var Backbone = require('backbone');

module.exports = Backbone.Model.extend({
    url : function() {
      var base = 'service';
      if (this.isNew()) return base;
      return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id;
    }
});
