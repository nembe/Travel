var sass = require('gulp-sass'),
    gulp = require('gulp');

var paths = {
    scss: 'src/main/resources/static/css/*.scss',
    destination: './target/classes/static'
};

var displayError = function (err) {
    var errorString = '[' + err.plugin + ']';
    errorString += ' ' + err.message.replace("\n",''); // Removes new line at the end

    if(err.fileName)
        errorString += ' in ' + err.fileName;

    if(err.lineNumber)
        errorString += ' on line ' + err.lineNumber;

    console.error(errorString);
};

gulp.task('sass', function () {
    gulp.src(paths.scss)
        .pipe(sass())
        .on('error', displayError)
        .pipe(gulp.dest(paths.destination + '/css'));
});

gulp.task('watch', function() {
    gulp.watch(paths.scss, ['sass']);
});

gulp.task('build', ['sass'], function(){});
