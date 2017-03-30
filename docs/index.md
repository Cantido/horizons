---
layout: default
title: HORIZONS Web API
---

# HORIZONS API

A RESTful-style API for NASA's [HORIZONS] system,
written in Clojure. [Check it out](https://secure-gorge-17286.herokuapp.com/).

## Introduction

The [HORIZONS] system provides a model for the solar system.
Through it, queries can be made to find the positions of planets, asteroids, comets, and
other astronomical bodies.
This kind of data base is sometimes called an *ephemeredes* (singular *ephemeris*).

> The JPL HORIZONS on-line solar system data and ephemeris computation service
provides access to key solar system data and flexible production of highly
accurate ephemerides for solar system objects ( 728955 asteroids, 3451 comets,
178 planetary satellites, 8 planets, the Sun, L1, L2, select spacecraft, and
system barycenters ). HORIZONS is provided by the Solar System Dynamics Group of
 the Jet Propulsion Laboratory.
>
> &mdash; <cite>NASA's documentation for the HORIZONS system</cite>

The only ways to access HORIZONS are through `telnet`, email, and
a CGI web interface intended for end-user access.
I want to provide a modern REST-style JSON interface to this system.
 
This application is written in [Clojure],
and uses [Liberator] to handle the details of RESTful representations.

[Clojure]: https://clojure.org/
[HORIZONS]: http://ssd.jpl.nasa.gov/?horizons
[Liberator]: http://clojure-liberator.github.io/liberator/

## Usage

This application is built using [Leiningen] and [Ring],
so you can easily start a development server using the [lein-ring] plugin:

```bash
lein ring server
```

The server will start up on port 3000 by default, and will present you with
an index page giving you a list of the currently-supported planetary bodies.

See the documentation on `lein-ring` for details.

[lein-ring]: https://github.com/weavejester/lein-ring
[Leiningen]: https://github.com/technomancy/leiningen
[Ring]: https://github.com/ring-clojure/ring
