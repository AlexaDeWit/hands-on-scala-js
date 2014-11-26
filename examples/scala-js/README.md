# Scala.js, a Scala to JavaScript compiler

Scala.js compiles Scala code to JavaScript, allowing you to write your
Web application entirely in Scala!

Noteworthy features are:

*   Support all of Scala (including macros!),
    modulo [a few semantic differences](http://www.scala-js.org/doc/semantics.html)
*   Very good [interoperability with JavaScript code](http://www.scala-js.org/doc/js-interoperability.html).
    For example, use jQuery and HTML5 from your Scala.js code, either in a
    typed or untyped way. Or create Scala.js objects and call their methods
    from JavaScript.
*   Integrated with [sbt](http://www.scala-sbt.org/)
    (including support for dependency management and incremental compilation)
*   Can be used with your favorite IDE for Scala
*   Generates [Source Maps](http://www.html5rocks.com/en/tutorials/developertools/sourcemaps/)
    for a smooth debugging experience (step through your Scala code from within
    your browser supporting source maps)
*   Integrates [Google Closure Compiler](https://developers.google.com/closure/compiler/)
    for producing minimal code for production.

## Resources

*   [Website](http://www.scala-js.org/)
*   [Mailing list](https://groups.google.com/forum/?fromgroups#!forum/scala-js)

## Get started

We provide a
[bootstrapping application](https://github.com/sjrd/scala-js-example-app)
which you can fork to kick off your own project. Its readme provides further
explanations on how to do so.

## Contribute

### Compile

Scala.js uses [sbt](http://www.scala-sbt.org/) for its build process.
To compile your fork, simply run:

    sbt> package

By default the sbt environment uses Scala 2.11.2. You can switch to any of the
supported versions with, e.g.,

    sbt> ++2.10.4

### Run the test suite

Compile and run the Scala.js-specific test suite with

    sbt> testSuite/test

(you must have run `package` before running the test suite)

To run the Scala test suite (aka partest), you have to use a 2.11 version, e.g.,
2.11.0 or 2.11.1, and run:

    sbt> partestSuite/test

Beware, this takes a very long time. You may use the `--fastOpt` and
`--fullOpt` switches to run Scala.js DCE or the full Google Closure
Compiler:

    sbt> partestSuite/testOnly -- --fastOpt

A complete test session from scratch on 2.11.1 would then be

    sbt> ++2.11.1
    sbt> package
    sbt> testSuite/test
    sbt> partestSuite/test

### Test the examples

After having compiled Scala.js, you can compile the example applications with:

    sbt> examples/fullOptJS

Then, you can "execute" them by opening their respective HTML files in your
favorite browser. Since fully optimizing the JavaScript takes time
(up to ten seconds, depending on your hardware), it is also possible
to only partially optimize JS by doing instead:

    sbt> examples/fastOptJS

In this case, you have to open the `-fastopt` version of the HTML
files.

Currently, two examples are provided:

*   `examples/helloworld/helloworld.html`, saying Hello World in four different
    ways (using DOM or jQuery, and using the untyped or typed interface to
    JavaScript).
*   `examples/reversi/reversi.html`, an implementation of a
    [Reversi](http://en.wikipedia.org/wiki/Reversi) game. Note that it uses the
    HTML5 Canvas element, so it won't work with Internet Explorer 8 or
    below.

If both `fastOptJS` and `fullOptJS` break, you can try and use
`packageJS` which doesn't perform any optimizations (use the `-pack`
version of the HTML files).

### Use your fork with your own projects

Simply publish it locally with:

    sbt> publishLocal
    sbt> tools/publishLocal
    sbt> sbtPlugin/publishLocal

## License

Scala.js is distributed under the
[Scala License](http://www.scala-lang.org/license.html).
