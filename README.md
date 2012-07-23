Tact
====

Tact is a Java 6 program and package that inserts runtime
checks into Java bytecode. The goal of these checks is to aid
the development of multi-threaded programs by ensuring
that only the intended threads access any object, either by
sole ownership or holding appropriate locks.

Example
-------

The most convenient way to run tact is on a jar:

	java -jar path/to/tact.jar files.jar

This will inject runtime checks into the bytecode of all classes
in files.jar.

It's also possible to inject individual class files:

	java -jar path/to/tact.jar Hello.class

Like with jars, this will inject the class's bytecode.

What's Going On?
----------------

The above examples will guard access to all non-final objects
used by files.jar and the Hello class with the following strategy:

> An object is owned by the Thread that creates it. No other
> Thread can access it, and any that try will throw an
> IllegalAccessError.

This is strict, but the goal is to catch unintended object accesses.
This is also impractical for every multi-threaded program, because they all
have to create Thread objects before starting them. So,
other strategies can be specified in the program's source with
the unh.edu.cs.tact pacakge, after calling Checker.init().
The first aid is the Checker.releaseAndStart()
method, which atomically relinquishes the current Thread's ownership of
a Thread or Runnable object, then starts the new Thread.

	Runnable r = new MyTask();
	Checker.releaseAndStart(r);

The next aid is the GuardedBy annotation. This can be applied to individual
fields of a class to specify that they can only be accessed when the current
Thread holds a certain lock.

	@GuardedBy("this") public int sharedData;

	…
	synchronized(this){ sharedData = 7; } // OK
	…
	sharedData = 13; // throws IllegalAccessError

Only "this", static and non-staic members of the form 
"full.package.and.Class.field", and class objects of the form 
"full.package.and.Class.class" are accepted as guard locks. Oftentimes, these
compile-time guards are insufficient. A runtime method exists to fill
this role:

	Checker.guardBy(obj, guard);
	synchronized(guard){ obj.field = 7; } // OK
	obj.field = 13; // throws IllegalAccessError

*Remember*, Checker.init() must be called before any of these methods.
See the JavaDoc for more methods and details.

TODO
----

Tact is still incomplete, and the correctness of the current
implementation is still not fully vetted. Use at your own risk!
