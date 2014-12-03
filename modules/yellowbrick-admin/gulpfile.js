var sass = require('gulp-sass'),
    gulp = require('gulp');

var paths = {
    scss: 'src/main/resources/static/css/*.scss',
    destination: './target/classes/static'
};

gulp.task('sass', function () {
    gulp.src(paths.scss)
        .pipe(sass())
        .pipe(gulp.dest(paths.destination + '/css'));
});

gulp.task('watch', function() {
    gulp.watch(paths.scss, ['sass']);
});

gulp.task('build', ['sass'], function(){});
