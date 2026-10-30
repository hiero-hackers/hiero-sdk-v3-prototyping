/// Query the schedules of the network from the Mirror Node. A schedule holds a scheduled transaction until it has
/// collected the signatures it needs, or until it expires. Use the `ScheduleRepository` (available as
/// `MirrorNodeClient.schedules`) to look up a schedule by its id (`findById`) and to list the schedules of the network
/// (`findAll`), executed and not yet executed ones, optionally filtered by account and schedule id.
///
/// A `Schedule` carries the scheduled transaction body, who created it and who pays for it, the signatures collected so
/// far, and when it was created, executed or expires.
package org.hiero.mirrornode.schedule;
