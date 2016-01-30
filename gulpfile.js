var gulp = require('gulp');
var source = require('vinyl-source-stream');
var browserify = require('browserify');
var watchify = require('watchify');
var concat = require('gulp-concat');

gulp.task('browserify', function() {
    var bundler = browserify({
        entries: ['./frontend/scripts/app.js'],
        cache: {}, packageCache: {}, fullPaths: true
    })
    .bundle()
    .on('error', function(err){
      console.log(err.message);
      this.emit('end');
    })
    .pipe(source('bundle.js'))
    .pipe(gulp.dest('./webroot/'));
});

gulp.task('watch', function() {
    var bundler = browserify({
        entries: ['./frontend/scripts/app.js'],
        cache: {}, packageCache: {}, fullPaths: true
    });
    var watcher  = watchify(bundler);

    return watcher
    .on('update', function () {
        var updateStart = Date.now();
        console.log('Updating!');
        watcher.bundle()
        .on('error', function(err){
          console.log(err.message);
          this.emit('end');
        })
        .pipe(source('bundle.js'))
        .pipe(gulp.dest('./webroot/'));
        console.log('Updated!', (Date.now() - updateStart) + 'ms');
    })
    .bundle()
    .on('error', function(err){
      console.log(err.message);
      this.emit('end');
    })
    .pipe(source('bundle.js'))
    .pipe(gulp.dest('./webroot/'));
});

gulp.task('default', ['browserify']);
