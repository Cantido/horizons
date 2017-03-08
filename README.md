# HORIZONS API

A RESTful-style API  for [NASA's HORIZONS system](http://ssd.jpl.nasa.gov/?horizons),
written in Clojure.

## Introduction

The [HORIZONS](http://ssd.jpl.nasa.gov/?horizons) system provides a model for the solar system. Through it, queries
can be made to find the positions of planets, asteroids, comets, and other
solar system objects.

> The JPL HORIZONS on-line solar system data and ephemeris computation service
provides access to key solar system data and flexible production of highly
accurate ephemerides for solar system objects ( 728955 asteroids, 3451 comets,
178 planetary satellites, 8 planets, the Sun, L1, L2, select spacecraft, and
system barycenters ). HORIZONS is provided by the Solar System Dynamics Group of
 the Jet Propulsion Laboratory.
>
> &mdash; <cite>NASA's documentation for the HORIZONS system</cite>

The only ways to access HORIZONS are through `telnet`, email, and a CGI web
interface intended for end-user access. I want to provide a modern REST-style
JSON interface to this system. It's written in [Clojure](https://clojure.org/),
using [Liberator](http://clojure-liberator.github.io/liberator/) to handle the
details of RESTful representations.

## Usage

This application is built using [Leiningen](https://github.com/technomancy/leiningen)
and [Ring](https://github.com/ring-clojure/ring), so you can easily start a
development server using the [lein-ring](https://github.com/weavejester/lein-ring) plugin:

```bash
lein ring server
```

See the documentation on `lein-ring` for details.

Beyond that, I'm still working on `telnet` access to the HORIZONS API.
There are no endpoints implemented yet.

## Roadmap

I plan to implement functionality in this order:

1. ~~Access the HORIZONS `telnet` API successfully~~
1. *Get current orbital coordinates for Earth*
1. Get past or future orbital coordinates for Earth
1. Get past or future orbital coordinates for any planet in the Solar System

## License

Copyright © 2017  Robert Richter

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see http://www.gnu.org/licenses/.