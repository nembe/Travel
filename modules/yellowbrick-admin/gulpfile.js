var sass = require('gulp-sass'),
    coffee = require('gulp-coffee'),
    gulp = require('gulp'),
    argv = require('yargs').argv;

var paths = {
    scss: 'src/main/resources/static/css/**/*.scss',
    coffee: 'src/main/resources/static/javascript/**/*.coffee',
    target: argv.target || './target/classes/static'
};

var displayError = function(err) {
    var errorString = '[' + err.plugin + ']';
    errorString += ' ' + err.message.replace("\n",''); // Removes new line at the end

    if(err.fileName)
        errorString += ' in ' + err.fileName;

    if(err.lineNumber)
        errorString += ' on line ' + err.lineNumber;

    console.error(errorString);
};

gulp.task('sass', function() {
    gulp.src(paths.scss)
        .pipe(sass({
            includePaths: require('node-neat').includePaths
        }))
        .on('error', displayError)
        .pipe(gulp.dest(paths.target + '/css'));
});

gulp.task('coffee', function() {
    gulp.src(paths.coffee)
        .pipe(coffee({ bare: true })
        .on('error', displayError))
        .pipe(gulp.dest(paths.target + '/javascript'));
});

gulp.task('watch', function() {
    gulp.watch(paths.scss, ['sass']);
    gulp.watch(paths.coffee, ['coffee']);
});

gulp.task('build', ['sass', 'coffee'], function(){});
