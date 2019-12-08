package fpdesign

/**
 * Functor is "a mapping between categories in the context of category theory".  In practice, it's a type class that
 * defines how map applies to it, e.g. Option, Either, List, Future, Function too!, etc.  Try; however, may not be
 * a Functor since the identity law will break.
 *
 * Functor.map must satisfy these 2 laws:
 *
 * 1. Identity law:
 *    Functor[X].map(x => identity(x)) == Functor[X]
 *    This is to ensure map only applies the function passed to it on the contained value and does not
 *    perform any other operation of its own.
 * 2. Associative law:
 *    Functor[X].map(f).map(g) == Functor[X].map(x => g(f(x))
 *
 * All Monads are Functors but not the reverse. Functions are Functors as well!
 */
object Functor
