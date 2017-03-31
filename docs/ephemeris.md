---
layout: default
title: Ephemeris Schema and Options
---

# Ephemeris Schema and Options

An [ephemeris] provides data on positions, velocities, orbits, etc., of
astronomical bodies. This is the core functionality of the HORIZONS system,
and this application tries to expose that functionality to the web,
and does so at the endpoint `/bodies/:id/ephemeris`.

[ephemeris]: https://en.wikipedia.org/wiki/Ephemeris

## Defaults

With a plain GET request to `/bodies/:id/ephemeris`, options default to:

 - Cartesian vector components (x, y, z, vx, vy, vz +)
 - Earth at 0,0,0
 - Ecliptic coordinate frame (outputs wrt ecliptic and equinox of reference epoch)
 - Time frame starting at midnight, TDB, at the beginning of the current day
 - Time frame ends 14 days from the start date
 - Coordinates are given over intervals of one hour.

These options cannot yet be changed, but that functionality is on its way.

## Schema

An ephemeris response contains a block about the requested time-frame,
and then an array of timestamped measurements.

### Time-frame

Contains the date-time of the first measurement,
of the last measurement, and of the duration between each measurement.
All temporal values are given in [ISO 8601] format.

[ISO 8601]: https://en.wikipedia.org/wiki/ISO_8601

### Ephemeris line-item

Each line item contains the position of a body at a point in time.

HORIZONS provides this description for coordinate values:

> ####Coordinate system description:
> 
> Ecliptic and Mean Equinox of Reference Epoch
> 
> **Reference epoch**: J2000.0
> 
> **XY-plane:** plane of the Earth's orbit at the reference epoch
> *Note*: obliquity of 84381.448 arcseconds wrt ICRF equator (IAU76)
> 
> **X-axis:** out along ascending node of instantaneous plane of the Earth's
> orbit and the Earth's mean equator at the reference epoch
> 
> **Z-axis:** perpendicular to the xy-plane in the directional (+ or -) sense
> of Earth's north pole at the reference epoch.
> 
> Symbol meaning (1 day=86400.0 s):
> 
>  - **JDTDB**: Julian Day Number, Barycentric Dynamical Time
>  - **X**: X-component of position vector (km)
>  - **Y**: Y-component of position vector (km)
>  - **Z**: Z-component of position vector (km)
>  - **VX**: X-component of velocity vector (km/day)
>  - **VY**: Y-component of velocity vector (km/day)
>  - **VZ**: Z-component of velocity vector (km/day)
>  - **LT**: One-way down-leg Newtonian light-time (day)
>  - **RG**: Range; distance from coordinate center (km)
>  - **RR**: Range-rate; radial velocity wrt coord. center (km/day)
> 
> Geometric states/elements have no aberrations applied.

**Caution**: Reference the units that will be provided in the response,
instead of relying on the specific information provided here.
Unfortunately, that is not implemented yet, but is currently being worked on.
See the progress on issue [#11] on the GitHub repo.

[#11]: https://github.com/Cantido/horizons/issues/11
