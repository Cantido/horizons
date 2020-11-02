# Horizons 

[![Heroku](https://img.shields.io/badge/heroku-deployed-blue.svg)](https://cantido-horizons.herokuapp.com/index.html)
[![Build Status](https://travis-ci.com/Cantido/horizons.svg?branch=master)](https://travis-ci.com/Cantido/horizons)
[![standard-readme compliant](https://img.shields.io/badge/readme%20style-standard-brightgreen.svg)](https://github.com/RichardLitt/standard-readme)
[![Contributor Covenant](https://img.shields.io/badge/Contributor%20Covenant-v2.0%20adopted-ff69b4.svg)](docs/CODE_OF_CONDUCT.md)

A RESTful-style API for NASA's [HORIZONS] system,
written in Clojure.
See more at https://git.io/horizons-docs.

[HORIZONS]: https://ssd.jpl.nasa.gov/?horizons

## Background

The HORIZONS system provides a model for the solar system.
Through it, queries can be made to find the positions of planets, asteroids, comets,
and other astronomical bodies.
This kind of data base is sometimes called an *ephemeredes* (singular *ephemeris*).

> The JPL HORIZONS on-line solar system data and ephemeris computation service
provides access to key solar system data and flexible production of highly
accurate ephemerides for solar system objects ( 728955 asteroids, 3451 comets,
178 planetary satellites, 8 planets, the Sun, L1, L2, select spacecraft, and
system barycenters ). HORIZONS is provided by the Solar System Dynamics Group of
 the Jet Propulsion Laboratory.
>
> &mdash; <cite>[NASA's documentation for the HORIZONS system][HORIZONS]</cite>

The only ways to access HORIZONS are through `telnet`, email, and
a CGI web interface intended for end-user access.
I want to provide a modern REST-style JSON interface to this system.

## Install

Clone this repository via Git

```sh
git clone https://github.com/Cantido/horizons.git
```

## Usage

This application contains its own Jetty web server.
To start in Production mode, use the provided `start-server.sh` script.

```sh
./start-server.sh
```

You can also run this app in development mode with `lein run`

```sh
lein run -m horizons.main
```

## Maintainers

This project is maintained by [Rosa Richter](https://github.com/Cantido).
You can get in touch with her on [Keybase.io](https://keybase.io/cantido).

## Contributing

Questions and pull requests are more than welcome.
I believe bad documentation is a bug,
so if anything is unclear, please [file an issue](https://github.com/Cantido/horizons/issues/new)!
Ideally, my answer to your question will be in an update to the docs.

Please see [CONTRIBUTING.md](docs/CONTRIBUTING.md) for all the details you could ever want about helping me with this project.

Note that this project is released with a Contributor [Code of Conduct](docs/CODE_OF_CONDUCT.md).
By participating in this project you agree to abide by its terms.

## License

MIT License

Copyright 2020 Rosa Richter.

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
of the Software, and to permit persons to whom the Software is furnished to do
so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
