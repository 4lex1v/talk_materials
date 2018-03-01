/**
 *
 * Akka HTTP modules (core, high-level api) implement a full server/client HTTP
 * stack on top of two essential parts of Akka toolkit - Actor and Streams.
 *
 * Can be perceived as Spray 2.0.
 *
 * While high-level api (Routing DSL) remains unchanged, the underlying model
 * and IO layer were changed significantly
 *
 * Plan:
 *   Server [[universe.server.Server]]
 *   Routing DSL [[universe.routing.Routing]]
 *   Directives [[universe.directives.Directives]]
 *   Errors [[universe.errors.Errors]]
 *   Marshalling [[universe.marshalling.Marshalling]]
 *   Magnet Pattern [[universe.magnets.Magnets]]
 *
 */